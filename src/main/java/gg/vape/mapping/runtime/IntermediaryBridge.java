package gg.vape.mapping.runtime;

import gg.vape.Vape;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Translates the Mojang (official) class and member names produced by the version
 * mapping tables into the intermediary names a Fabric runtime exposes.
 *
 * <p>Two data sets are combined:
 * <ul>
 *     <li>{@code mappings/mappings.tiny} (tiny v1, {@code official -> intermediary}),
 *         shipped on the classpath by the Fabric Loader.</li>
 *     <li>Mojang's ProGuard format {@code client.txt} (deobfuscated -> obfuscated),
 *         downloaded on demand and cached inside the game directory.</li>
 * </ul>
 */
public final class IntermediaryBridge {
    private static final String TINY_RESOURCE = "mappings/mappings.tiny";
    private static final String VERSION_MANIFEST_URL =
            "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final Pattern MANIFEST_ENTRY = Pattern.compile(
            "\\{[^{}]*?\"id\"\\s*:\\s*\"" + Pattern.quote("{0}") + "\"[^{}]*?\"url\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern CLIENT_MAPPINGS_URL = Pattern.compile(
            "\"client_mappings\"\\s*:\\s*\\{[^{}]*?\"url\"\\s*:\\s*\"([^\"]+)\"");

    private static int state;
    private static Map<String, String> classByMojang = new HashMap<String, String>();
    private static Map<String, Map<String, List<MemberEntry>>> membersByOwner =
            new HashMap<String, Map<String, List<MemberEntry>>>();

    private IntermediaryBridge() {
    }

    private static final class MemberEntry {
        private final String intermediaryName;
        private final int parameterCount;

        private MemberEntry(String intermediaryName, int parameterCount) {
            this.intermediaryName = intermediaryName;
            this.parameterCount = parameterCount;
        }
    }

    private static final class MojangMember {
        private final String obfuscatedName;
        private final int parameterCount;
        private final boolean method;

        private MojangMember(String obfuscatedName, int parameterCount, boolean method) {
            this.obfuscatedName = obfuscatedName;
            this.parameterCount = parameterCount;
            this.method = method;
        }
    }

    /**
     * @return {@code true} when the running game uses intermediary names and the
     *         bridge was able to load the required mapping data.
     */
    public static boolean isActive() {
        if (state == 0) {
            state = initialize() ? 2 : 1;
        }
        return state == 2;
    }

    /**
     * @param mojangName a Mojang (official) class name, for example
     *                   {@code net/minecraft/core/Direction}
     * @return the intermediary class name, or the input when it is not mapped
     */
    public static String remapClassName(String mojangName) {
        if (!isActive() || mojangName == null) {
            return mojangName;
        }
        String intermediary = classByMojang.get(mojangName);
        boolean dotted = intermediary == null && mojangName.indexOf('/') < 0;
        if (intermediary == null) {
            String slashed = mojangName.replace('.', '/');
            if (!slashed.equals(mojangName)) {
                intermediary = classByMojang.get(slashed);
            }
        }
        if (intermediary == null) {
            return mojangName;
        }
        return dotted ? intermediary.replace('/', '.') : intermediary;
    }

    /**
     * @param ownerRuntimeName the runtime name of the declaring class
     * @param mojangName        a Mojang (official) member name
     * @param parameterCount    the number of parameters of the member (0 for fields)
     * @return the intermediary member name, or the input when it is not mapped
     */
    public static String remapMemberName(String ownerRuntimeName, String mojangName, int parameterCount) {
        if (!isActive() || ownerRuntimeName == null || mojangName == null) {
            return mojangName;
        }
        Map<String, List<MemberEntry>> members = membersByOwner.get(ownerRuntimeName.replace('.', '/'));
        if (members == null) {
            return mojangName;
        }
        List<MemberEntry> entries = members.get(mojangName);
        if (entries == null || entries.isEmpty()) {
            return mojangName;
        }
        if (entries.size() == 1) {
            return entries.get(0).intermediaryName;
        }
        for (MemberEntry entry : entries) {
            if (entry.parameterCount == parameterCount) {
                return entry.intermediaryName;
            }
        }
        return entries.get(0).intermediaryName;
    }

    private static boolean initialize() {
        try {
            if (!detectIntermediaryRuntime()) {
                return false;
            }
            String version = detectMinecraftVersion();
            File cache = resolveMojangMappings(version);
            if (cache == null) {
                Vape.debugLog("Mojang mappings for " + version + " are unavailable; "
                        + "keeping Mojang class names");
                return false;
            }
            Map<String, String> mojangToOfficial = new HashMap<String, String>();
            Map<String, List<MojangMember>> mojangMembers = new HashMap<String, List<MojangMember>>();
            if (!loadMojangMappings(cache, mojangToOfficial, mojangMembers)) {
                Vape.debugLog("Mojang mappings for " + version + " could not be parsed");
                return false;
            }
            Map<String, String> officialToIntermediary = new HashMap<String, String>();
            Map<String, String> officialMemberNames = new HashMap<String, String>();
            if (!loadIntermediaryMappings(officialToIntermediary, officialMemberNames)) {
                Vape.debugLog("Intermediary mappings are unavailable; keeping Mojang class names");
                return false;
            }
            compose(mojangToOfficial, mojangMembers, officialToIntermediary, officialMemberNames);
            Vape.debugLog("Intermediary bridge active: " + classByMojang.size() + " classes mapped");
            return true;
        }
        catch (Throwable throwable) {
            Vape.logThrowable(throwable);
            return false;
        }
    }

    private static boolean detectIntermediaryRuntime() {
        try {
            Class.forName("net.minecraft.client.Minecraft", false, IntermediaryBridge.class.getClassLoader());
            return false;
        }
        catch (Throwable ignored) {
        }
        try {
            Class.forName("net.minecraft.class_310", false, IntermediaryBridge.class.getClassLoader());
            return true;
        }
        catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean loadIntermediaryMappings(Map<String, String> classMappings,
            Map<String, String> memberMappings) {
        InputStream input = openTinyResource();
        if (input == null) {
            return false;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(input, UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() == 0 || line.charAt(0) == '#' || line.startsWith("v1")) {
                    continue;
                }
                String[] parts = line.split("\t");
                if (parts.length == 3 && "CLASS".equals(parts[0])) {
                    classMappings.put(parts[1], parts[2]);
                }
                else if (parts.length == 5 && ("FIELD".equals(parts[0]) || "METHOD".equals(parts[0]))) {
                    int parameters = "METHOD".equals(parts[0]) ? countDescriptorParameters(parts[2]) : 0;
                    memberMappings.put(parts[1] + "#" + parts[0] + "#" + parts[3] + "#" + parameters, parts[4]);
                }
            }
            return !classMappings.isEmpty();
        }
        catch (IOException exception) {
            return false;
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException ignored) {
                }
            }
        }
    }

    private static InputStream openTinyResource() {
        try {
            Enumeration<URL> resources = ClassLoader.getSystemResources(TINY_RESOURCE);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                InputStream input = url.openStream();
                if (input != null) {
                    return input;
                }
            }
        }
        catch (Throwable ignored) {
        }
        try {
            Enumeration<URL> resources = IntermediaryBridge.class.getClassLoader()
                    .getResources(TINY_RESOURCE);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                InputStream input = url.openStream();
                if (input != null) {
                    return input;
                }
            }
        }
        catch (Throwable ignored) {
        }
        InputStream fromClassPath = openTinyFromProcessClassPath();
        if (fromClassPath != null) {
            return fromClassPath;
        }
        return null;
    }

    private static InputStream openTinyFromProcessClassPath() {
        String classPath;
        try {
            classPath = System.getProperty("java.class.path", "");
        }
        catch (Throwable ignored) {
            return null;
        }
        for (String entry : classPath.split(File.pathSeparator)) {
            if (entry.length() == 0) {
                continue;
            }
            File file = new File(entry);
            if (!file.isFile()) {
                continue;
            }
            try {
                java.util.zip.ZipFile archive = new java.util.zip.ZipFile(file);
                java.util.zip.ZipEntry zipEntry = archive.getEntry(TINY_RESOURCE);
                if (zipEntry == null) {
                    archive.close();
                    continue;
                }
                InputStream input = archive.getInputStream(zipEntry);
                return input;
            }
            catch (Throwable ignored) {
            }
        }
        return null;
    }

    private static void compose(Map<String, String> mojangToOfficial,
            Map<String, List<MojangMember>> mojangMembers, Map<String, String> officialToIntermediary,
            Map<String, String> officialMemberNames) {
        Map<String, String> intermediaryByOfficial = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : mojangToOfficial.entrySet()) {
            String intermediary = officialToIntermediary.get(entry.getValue());
            if (intermediary == null) {
                continue;
            }
            classByMojang.put(entry.getKey(), intermediary);
            intermediaryByOfficial.put(entry.getValue(), intermediary);
        }
        for (Map.Entry<String, List<MojangMember>> entry : mojangMembers.entrySet()) {
            String key = entry.getKey();
            int separator = key.lastIndexOf('#');
            if (separator < 0) {
                continue;
            }
            String owner = key.substring(0, separator);
            String mojangName = key.substring(separator + 1);
            String obfOwner = mojangToOfficial.get(owner);
            if (obfOwner == null) {
                continue;
            }
            String intermediaryOwner = intermediaryByOfficial.get(obfOwner);
            if (intermediaryOwner == null) {
                continue;
            }
            Map<String, List<MemberEntry>> ownerMembers = membersByOwner.get(intermediaryOwner);
            if (ownerMembers == null) {
                ownerMembers = new HashMap<String, List<MemberEntry>>();
                membersByOwner.put(intermediaryOwner, ownerMembers);
            }
            List<MemberEntry> entries = ownerMembers.get(mojangName);
            if (entries == null) {
                entries = new ArrayList<MemberEntry>();
                ownerMembers.put(mojangName, entries);
            }
            for (MojangMember member : entry.getValue()) {
                String intermediaryName = officialMemberNames.get(obfOwner + "#"
                        + (member.method ? "METHOD" : "FIELD") + "#" + member.obfuscatedName + "#"
                        + member.parameterCount);
                if (intermediaryName == null) {
                    continue;
                }
                entries.add(new MemberEntry(intermediaryName, member.parameterCount));
            }
        }
    }

    private static boolean loadMojangMappings(File file, Map<String, String> classMappings,
            Map<String, List<MojangMember>> memberMappings) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), UTF_8));
            String owner = null;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() == 0) {
                    continue;
                }
                if (line.charAt(0) != ' ') {
                    int arrow = line.indexOf(" -> ");
                    if (arrow < 0) {
                        continue;
                    }
                    String mojang = line.substring(0, arrow).trim();
                    String obf = line.substring(arrow + 4).trim();
                    if (obf.endsWith(":")) {
                        obf = obf.substring(0, obf.length() - 1);
                    }
                    owner = mojang.replace('.', '/');
                    classMappings.put(owner, obf);
                    continue;
                }
                if (owner == null) {
                    continue;
                }
                int arrow = line.indexOf(" -> ");
                if (arrow < 0) {
                    continue;
                }
                String declaration = line.substring(0, arrow).trim();
                String obfName = line.substring(arrow + 4).trim();
                declaration = stripLineNumbers(declaration);
                int parameters = 0;
                boolean method = false;
                int parenthesis = declaration.indexOf('(');
                if (parenthesis >= 0) {
                    method = true;
                    int closing = declaration.indexOf(')', parenthesis);
                    String argumentList = closing > parenthesis
                            ? declaration.substring(parenthesis + 1, closing)
                            : "";
                    parameters = countArguments(argumentList);
                    declaration = declaration.substring(0, parenthesis).trim();
                }
                String memberOwner = owner;
                String mojangName = lastToken(declaration);
                if (mojangName == null) {
                    continue;
                }
                if (mojangName.length() == 0) {
                    continue;
                }
                String key = memberOwner + "#" + mojangName;
                List<MojangMember> members = memberMappings.get(key);
                if (members == null) {
                    members = new ArrayList<MojangMember>();
                    memberMappings.put(key, members);
                }
                members.add(new MojangMember(obfName, parameters, method));
            }
            return !classMappings.isEmpty();
        }
        catch (IOException exception) {
            return false;
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException ignored) {
                }
            }
        }
    }

    private static String stripLineNumbers(String declaration) {
        int firstColon = declaration.indexOf(':');
        if (firstColon <= 0) {
            return declaration;
        }
        String prefix = declaration.substring(0, firstColon).trim();
        for (int index = 0; index < prefix.length(); ++index) {
            if (!Character.isDigit(prefix.charAt(index))) {
                return declaration;
            }
        }
        int secondColon = declaration.indexOf(':', firstColon + 1);
        if (secondColon < 0) {
            return declaration;
        }
        return declaration.substring(secondColon + 1).trim();
    }

    private static String lastToken(String declaration) {
        String value = declaration.trim();
        if (value.length() == 0) {
            return null;
        }
        int space = value.lastIndexOf(' ');
        return space < 0 ? value : value.substring(space + 1).trim();
    }

    private static int countDescriptorParameters(String descriptor) {
        if (descriptor == null) {
            return 0;
        }
        int open = descriptor.indexOf('(');
        int close = descriptor.indexOf(')', open + 1);
        if (open < 0 || close < 0) {
            return 0;
        }
        return countArguments(descriptor.substring(open + 1, close));
    }

    private static int countArguments(String argumentList) {
        int count = 1;
        boolean seen = false;
        for (int index = 0; index < argumentList.length(); ++index) {
            char character = argumentList.charAt(index);
            if (character == ',') {
                ++count;
            }
            else if (!Character.isWhitespace(character)) {
                seen = true;
            }
        }
        return seen ? count : 0;
    }

    private static String detectMinecraftVersion() {
        try {
            Class<?> fabricLoader = Class.forName("net.fabricmc.loader.api.FabricLoader");
            Object instance = fabricLoader.getMethod("getInstance").invoke(null);
            Object container = fabricLoader.getMethod("getModContainer", String.class)
                    .invoke(instance, "minecraft");
            if (container instanceof Optional) {
                Optional<?> optional = (Optional<?>)container;
                container = optional.isPresent() ? optional.get() : null;
            }
            if (container != null) {
                Class<?> modContainer = Class.forName("net.fabricmc.loader.api.ModContainer");
                Object metadata = modContainer.getMethod("getMetadata").invoke(container);
                Class<?> modMetadata = Class.forName("net.fabricmc.loader.api.metadata.ModMetadata");
                Object version = modMetadata.getMethod("getVersion").invoke(metadata);
                Class<?> versionApi = Class.forName("net.fabricmc.loader.api.Version");
                Object friendly = versionApi.getMethod("getFriendlyString").invoke(version);
                return String.valueOf(friendly);
            }
        }
        catch (Throwable ignored) {
        }
        return "unknown";
    }

    private static File resolveMojangMappings(String version) {
        try {
            File directory = new File(resolveGameDirectory(), ".vape421" + File.separator + "mappings");
            File cache = new File(directory, "client-" + version + ".txt");
            if (cache.isFile() && cache.length() > 0L) {
                return cache;
            }
            String url = downloadMojangMappingsUrl(version);
            if (url == null) {
                return null;
            }
            if (!directory.isDirectory() && !directory.mkdirs()) {
                return null;
            }
            File temporary = new File(directory, "client-" + version + ".txt.part");
            if (!download(url, temporary)) {
                return null;
            }
            if (!temporary.renameTo(cache)) {
                return null;
            }
            return cache;
        }
        catch (Throwable throwable) {
            Vape.logThrowable(throwable);
            return null;
        }
    }

    private static String resolveGameDirectory() {
        try {
            Class<?> fabricLoader = Class.forName("net.fabricmc.loader.api.FabricLoader");
            Object instance = fabricLoader.getMethod("getInstance").invoke(null);
            Object gameDirectory = fabricLoader.getMethod("getGameDir").invoke(instance);
            if (gameDirectory != null) {
                return String.valueOf(gameDirectory);
            }
        }
        catch (Throwable ignored) {
        }
        return System.getProperty("user.dir", ".");
    }

    private static String downloadMojangMappingsUrl(String version) {
        String manifest = downloadString(VERSION_MANIFEST_URL);
        if (manifest == null) {
            return null;
        }
        Matcher matcher = MANIFEST_ENTRY.matcher(manifest);
        if (!matcher.find()) {
            return null;
        }
        String versionJson = downloadString(matcher.group(1));
        if (versionJson == null) {
            return null;
        }
        Matcher mappingsMatcher = CLIENT_MAPPINGS_URL.matcher(versionJson);
        if (!mappingsMatcher.find()) {
            return null;
        }
        return mappingsMatcher.group(1);
    }

    private static String downloadString(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(60000);
            connection.setInstanceFollowRedirects(true);
            InputStream input = connection.getInputStream();
            try {
                java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
                byte[] chunk = new byte[8192];
                int read;
                while ((read = input.read(chunk)) > 0) {
                    buffer.write(chunk, 0, read);
                }
                return new String(buffer.toByteArray(), UTF_8);
            }
            finally {
                input.close();
            }
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private static boolean download(String url, File target) {
        try {
            HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(120000);
            connection.setInstanceFollowRedirects(true);
            InputStream input = connection.getInputStream();
            try {
                OutputStream output = new FileOutputStream(target);
                try {
                    byte[] chunk = new byte[8192];
                    int read;
                    while ((read = input.read(chunk)) > 0) {
                        output.write(chunk, 0, read);
                    }
                }
                finally {
                    output.close();
                }
            }
            finally {
                input.close();
            }
            return target.isFile() && target.length() > 0L;
        }
        catch (Throwable ignored) {
            return false;
        }
    }
}
