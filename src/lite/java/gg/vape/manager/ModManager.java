package gg.vape.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gg.vape.Vape;
import gg.vape.config.Profile;
import gg.vape.event.EventHandler;
import gg.vape.event.EventListener;
import gg.vape.event.impl.EventModStateChange;
import gg.vape.module.Category;
import gg.vape.module.MinecraftVersionConstraint;
import gg.vape.module.Mod;
import gg.vape.module.SubModule;
import gg.vape.module.blatant.AutoAnchor;
import gg.vape.module.blatant.AntiBot;
import gg.vape.module.blatant.AutoHeal;
import gg.vape.module.blatant.Backtrack;
import gg.vape.module.blatant.blink.BlinkPacketRenderModule;
import gg.vape.module.blatant.BlockIn;
import gg.vape.module.blatant.Fly;
import gg.vape.module.blatant.HitBoxes;
import gg.vape.module.blatant.InvWalk;
import gg.vape.module.blatant.KeepSprint;
import gg.vape.module.blatant.KillAura;
import gg.vape.module.blatant.NoFall;
import gg.vape.module.blatant.NoSlowdown;
import gg.vape.module.blatant.SafeWalk;
import gg.vape.module.blatant.Scaffold;
import gg.vape.module.blatant.Speed;
import gg.vape.module.blatant.Timer;
import gg.vape.module.combat.AimAssist;
import gg.vape.module.combat.Triggerbot;
import gg.vape.module.combat.autoclicker.AutoClickerInputModule;
import gg.vape.module.combat.HitSwap;
import gg.vape.module.combat.BlockHit;
import gg.vape.module.combat.BowAimbot;
import gg.vape.module.combat.CrystalAura;
import gg.vape.module.combat.LeftClicker;
import gg.vape.module.combat.Reach;
import gg.vape.module.combat.RightClicker;
import gg.vape.module.combat.SilentAura;
import gg.vape.module.combat.silentaura.SilentAuraTargetingModule;
import gg.vape.module.combat.Sprint;
import gg.vape.module.combat.WTap;
import gg.vape.module.combat.Velocity;
import gg.vape.module.combat.velocity.VelocityPacketMode;
import gg.vape.module.combat.velocity.VelocityPacketReceiveMode;
import gg.vape.module.combat.HitSelect;
import gg.vape.module.combat.silentaura.SilentAuraClicker;
import gg.vape.module.render.BedPlates;
import gg.vape.module.world.MurderFinder;
import gg.vape.module.none.ClientSettings;
import gg.vape.module.none.MouseDelayFix;
import gg.vape.module.render.Search;
import gg.vape.module.none.TextGuiSettings;
import gg.vape.module.world.XRay;
import gg.vape.module.world.AntiAFK;
import gg.vape.module.render.Animations;
import gg.vape.module.render.AntiDebuff;
import gg.vape.module.render.Arrows;
import gg.vape.module.render.Chams;
import gg.vape.module.render.ESP;
import gg.vape.module.render.PropHunt;
import gg.vape.module.render.Freecam;
import gg.vape.module.render.Fullbright;
import gg.vape.module.render.Health;
import gg.vape.module.render.Indicators;
import gg.vape.module.render.ItemESP;
import gg.vape.module.render.NameTags;
import gg.vape.module.render.Explosions;
import gg.vape.module.render.SpawnerFinder;
import gg.vape.module.render.StorageESP;
import gg.vape.module.render.Tracers;
import gg.vape.module.render.Trajectories;
import gg.vape.module.render.hud.ArmorStatusHudModule;
import gg.vape.module.render.hud.BlockOverlayHudModule;
import gg.vape.module.render.hud.BlockRenderColorOverrideHudModule;
import gg.vape.module.render.hud.BlockhitAnimationHudModule;
import gg.vape.module.render.hud.ClockHudModule;
import gg.vape.module.render.hud.CompassHudModule;
import gg.vape.module.render.hud.CoordinatesHudModule;
import gg.vape.module.render.hud.FpsDisplayHudModule;
import gg.vape.module.render.hud.FreeLookHudModule;
import gg.vape.module.render.hud.HudModule;
import gg.vape.module.render.hud.InventoryBlurHudModule;
import gg.vape.module.render.hud.KeystrokesHudModule;
import gg.vape.module.render.hud.NoClickDelayHudModule;
import gg.vape.module.render.hud.NoFogHudModule;
import gg.vape.module.render.hud.NoHurtCameraHudModule;
import gg.vape.module.render.hud.NoHurtDelayHudModule;
import gg.vape.module.render.hud.PotionEffectsHudModule;
import gg.vape.module.render.hud.ReachDisplayHudModule;
import gg.vape.module.render.hud.ScoreboardHudModule;
import gg.vape.module.render.hud.TimeChangerHudModule;
import gg.vape.module.render.hud.WeatherChangerHudModule;
import gg.vape.module.render.proj.Projectiles;
import gg.vape.module.utility.ArmorSwitch;
import gg.vape.module.utility.AutoArmor;
import gg.vape.module.utility.InvCleaner;
import gg.vape.module.utility.MLG;
import gg.vape.module.utility.AutoPearl;
import gg.vape.module.utility.AutoTool;
import gg.vape.module.utility.AutoTotem;
import gg.vape.module.utility.Clutch;
import gg.vape.module.utility.InventoryManager;
import gg.vape.module.utility.AutoHotbar;
import gg.vape.module.utility.AutoFish;
import gg.vape.module.utility.Panic;
import gg.vape.module.utility.Parkour;
import gg.vape.module.utility.Refill;
import gg.vape.module.utility.ThrowDebuff;
import gg.vape.module.utility.Throwpot;
import gg.vape.module.utility.WindCharge;
import gg.vape.module.utility.inventory.InventoryActionModule;
import gg.vape.module.world.BedBreaker;
import gg.vape.module.world.ChestSteal;
import gg.vape.module.world.FastPlace;
import gg.vape.module.world.FakeLag;
import gg.vape.notification.NotificationType;
import gg.vape.notification.ReusableTextNotification;
import gg.vape.ui.click.component.GuiComponent;
import gg.vape.unmap.ModeSelection;
import gg.vape.value.ModeValue;
import gg.vape.value.SubModuleValue;
import gg.vape.value.Value;
import gg.vape.wrapper.impl.ForgeVersion;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;

public class ModManager
implements EventListener {
    private static GuiComponent[] legacyCategoryComponents;
    private Map<Class<? extends Mod>, Mod> allModulesByType;
    private ArrayList<Mod> activeModuleList;
    private HashSet<Mod> enabledModules;
    private XRay xrayModule;
    private ReusableTextNotification profileSwitchNotification;
    private Map<Class<? extends Mod>, Mod> activeModulesByType;
    private boolean suppressStateNotifications;

    public Collection<Mod> getTopLevelModules() {
        ArrayList<Mod> topLevelModules = new ArrayList<Mod>();
        for (Mod mod : this.getAllModules()) {
            if (mod instanceof SubModule) continue;
            topLevelModules.add(mod);
        }
        return topLevelModules;
    }

    private static Mod safeModule(String name, java.util.function.Supplier<Mod> supplier) {
      try {
         return supplier.get();
      } catch (Throwable var3) {
         Vape.logError("Failed to create module " + name);
         Vape.logThrowable(var3);
         return null;
      }
   }
   public void init() {
        GuiComponent[] legacyComponentsSnapshot = Category.getLegacyComponents();
        Mod[] coreModules = {
                new ClientSettings(),
                // new LeftClicker(),
                // new RightClicker(),
                // new Velocity(),
                // new VelocityPacketReceiveMode(),
                // new VelocityPacketMode(),
                // new Reach(),
                new Throwpot(),
                new Refill(),
                new Tracers(),
                new NameTags(),
                new Search(),
                new ESP(),
                new ChestSteal(),
                new KeepSprint(),
                new FastPlace(),
                // new HitBoxes(),
                new SpawnerFinder(),
                // new StorageESP(),
                // new Scaffold(),
                new Fullbright(),
                // new WTap(),
                new AutoArmor(),
                new InvCleaner(),
                // new ThrowDebuff(),
                new AutoTool(),
                new Trajectories(),
                new AntiDebuff(),
                // new SafeWalk(),
                new Projectiles(),
                // new Fly(),
                // new KillAura(),
                new Arrows(),
                new BlinkPacketRenderModule(),
                // new AutoPearl(),
                new Panic(),
                // new AntiAFK(),
                new ArmorSwitch(),
                // new ItemESP(),
                // new MLG(),
                new AutoHotbar(),
                // new AutoHeal(),
                // new PropHunt(),
                new Parkour(),
                new MurderFinder(),
                // new BowAimbot(),
                new Indicators(),
                new Sprint(),
                // new Health(),
                // new HitSelect(),
                new Animations(),
                // new SilentAura(),
                // new SilentAuraClicker(),
                // new SilentAuraTargetingModule(),
                // new Clutch(),
                new InventoryManager(),
                // new BlockHit(),
                // new Timer(),
                // new AutoClickerInputModule(),
                // new BedPlates(),
                new AutoTotem(),
                new AutoFish(),
                new AimAssist(),
        };
        this.registerModules(Stream.of(coreModules).filter(Objects::nonNull));
        ModRegistrationBuilder.create().setModule(safeModule("Explosions", Explosions::new)).addVersionConstraint(ForgeVersion.MC_1_16_5.b()).registerWith(this);
        Mod[] versionConstrainedModules = {new Chams(), new XRay()};
        this.registerModules(Stream.of(versionConstrainedModules).filter(Objects::nonNull), ModManager::addMinecraft1165Constraint);
        ModRegistrationBuilder.create().setModule(safeModule("Freecam", Freecam::new)).addVersionConstraint(ForgeVersion.MC_1_16_5.b()).addVersionConstraint(ForgeVersion.MC_1_21_11.n()).registerWith(this);
        this.registerModules(Stream.of(safeModule("InvWalk", InvWalk::new)).filter(Objects::nonNull), ModManager::addMinecraft189Constraint);
        // this.registerModules(Stream.of(safeModule("Backtrack", Backtrack::new)).filter(Objects::nonNull), ModManager::addBacktrackVersionConstraints);
        // this.registerModules(Stream.of(safeModule("AutoFish", AutoFish::new), safeModule("BedBreaker", BedBreaker::new), safeModule("BlockIn", BlockIn::new), safeModule("FakeLag", FakeLag::new)).filter(Objects::nonNull), ModManager::addMinecraft1710Constraint);
        // this.registerModules(Stream.of(safeModule("AntiBot", AntiBot::new)).filter(Objects::nonNull));
        this.registerModules(Stream.of(safeModule("Triggerbot", Triggerbot::new), safeModule("HitSwap", HitSwap::new), safeModule("AutoAnchor", AutoAnchor::new), safeModule("WindCharge", WindCharge::new), safeModule("CrystalAura", CrystalAura::new), safeModule("AutoTotem", AutoTotem::new)).filter(Objects::nonNull), ModManager::addMinecraft1214Constraint);
        // this.registerModules(Stream.of(safeModule("NoFall", NoFall::new), safeModule("NoSlowdown", NoSlowdown::new), safeModule("Speed", Speed::new), safeModule("BlockHit", BlockHit::new), safeModule("Timer", Timer::new)).filter(Objects::nonNull), ModManager::addModernMinecraftConstraint);
        GuiComponent[] preservedLegacyComponents = legacyComponentsSnapshot;
        this.registerTextGuiSettings();
        this.registerHudModules();
        if (preservedLegacyComponents == null) {
            GuiComponent.setLegacyComponentState(new GuiComponent[2]);
        }
    }

    public boolean getState(Class<? extends Mod> clazz) {
        for (Mod mod : this.collectMods()) {
            if (!mod.getClass().equals(clazz)) continue;
            return mod.isEnabled();
        }
        return false;
    }

    private static void addMinecraft1206Constraint(ModRegistrationBuilder modRegistrationBuilder) {
        modRegistrationBuilder.addVersionConstraint(ForgeVersion.MC_1_20_6.b());
    }

    public JsonObject getEnabledNonHudModuleStates() {
        JsonObject enabledStates = new JsonObject();
        for (Mod mod : this.getAllModules()) {
            if (mod instanceof HudModule || !mod.isEnabled()) continue;
            enabledStates.addProperty(mod.getName(), Boolean.valueOf(mod.isEnabled()));
        }
        return enabledStates;
    }

    public void applyProfileModuleStates(Profile profile) {
        this.suppressStateNotifications = true;
        JsonObject enabledStates = profile.getEnabledModuleStates();
        int enabledCount = 0;
        for (Mod mod : this.collectMods()) {
            if (mod instanceof HudModule || mod.getCategory().equals(Category.NONE)) continue;
            try {
                if (enabledStates.has(mod.getName())) {
                    if (!mod.isVisible()) continue;
                    try {
                        if (mod.isEnabled()) continue;
                        mod.setEnabled(enabledStates.get(mod.getName()).getAsBoolean());
                        ++enabledCount;
                    }
                    catch (Exception exception) {
                        Vape.logThrowable(exception);
                    }
                    continue;
                }
                if (mod instanceof ClientSettings || mod.getGuiColor() == 0 || !mod.isEnabled()) continue;
                mod.toggle();
            }
            catch (Exception exception) {
                Vape.logThrowable(exception);
            }
        }
        this.suppressStateNotifications = false;
        if (Vape.INSTANCE.getPublicProfileSettings().profileSwitchNotifications.getEffectiveValue().booleanValue()) {
            this.profileSwitchNotification.withTitle("Profile swap to " + gg.vape.config.ClientSettings.FORMAT_CODE + "6" + profile.getName())
                    .withMessage(enabledCount + " modules enabled").reset();
            Vape.INSTANCE.getNotificationManager().show(this.profileSwitchNotification);
        }
    }

    private static void addModernMinecraftConstraint(ModRegistrationBuilder modRegistrationBuilder) {
        modRegistrationBuilder.addVersionConstraint(ForgeVersion.MC_1_21_4.b());
    }

    public void initializeAllModules() {
        for (Mod mod : this.allModulesByType.values()) {
            mod.I();
        }
    }

    private void registerModules(Stream<Mod> modules, Consumer<ModRegistrationBuilder<?>> customizer) {
        modules.forEach(module -> this.registerModuleWithCustomizer(customizer, module));
    }

    public HashSet<Mod> getMods() {
        return this.enabledModules;
    }

    public static void setLegacyCategoryComponents(GuiComponent[] components) {
        legacyCategoryComponents = components;
    }

    private void registerModules(Stream<Mod> modules) {
        this.registerModules(modules, ModManager::noAdditionalConstraints);
    }

    void registerModule(Mod module, List<List<MinecraftVersionConstraint>> constraintGroups, boolean enableImmediately) {
        this.allModulesByType.put(module.getClass(), module);
        if (!constraintGroups.isEmpty()) {
            boolean supported = false;
            for (List<MinecraftVersionConstraint> constraints : constraintGroups) {
                List<MinecraftVersionConstraint> activeConstraints = MinecraftVersionConstraint.o(constraints);
                if (!activeConstraints.isEmpty()) continue;
                supported = true;
            }
            if (!supported) {
                return;
            }
        }
        this.activeModulesByType.put(module.getClass(), module);
        this.activeModuleList.add(module);
        for (Value<?, ?> value : module.getValues()) {
            if (!(value instanceof ModeValue)) continue;
            ModeValue modeValue = (ModeValue)value;
            for (ModeSelection modeSelection : modeValue.getModes()) {
                SubModuleValue subModuleValue;
                if (!(modeSelection instanceof SubModuleValue) || !((SubModule)(subModuleValue = (SubModuleValue)modeSelection).getInstance()).isSubModuleEnabled()) continue;
                this.registerModule((Mod)subModuleValue.getInstance(), constraintGroups, false);
                module.registerSubModule(new SubModule[]{subModuleValue.getInstance()});
            }
        }
        if (enableImmediately) {
            module.setEnabled(true);
        }
    }

    private void registerTextGuiSettings() {
        ModRegistrationBuilder.create().setModule(new TextGuiSettings()).registerWith(this);
    }

    private void registerModuleWithCustomizer(Consumer consumer, Mod module) {
        ModRegistrationBuilder<Mod> builder = ModRegistrationBuilder.create().setModule(module);
        consumer.accept(builder);
        builder.registerWith(this);
    }

    public void finishModuleInitialization() {
        for (Mod mod : this.activeModulesByType.values()) {
            mod.onFinishModuleInitialization();
        }
    }

    public String buildTranslationTemplate() {
        StringBuilder translations = new StringBuilder();
        for (Mod mod : this.collectMods()) {
            String moduleName = mod.getName();
            String moduleKey = moduleName.replace(" ", "_").toLowerCase();
            translations.append(moduleKey + "=" + moduleName);
            translations.append("\n");
            if (mod.getToolTip() != null && !mod.getToolTip().equals("")) {
                translations.append(moduleKey + ".tooltip=" + mod.getToolTip().replace("\n", " "));
                translations.append("\n");
            }
            for (Value<?, ?> value : mod.getValues()) {
                String valueName = value.getName();
                String valueKey = moduleKey + "." + value.getName().replace(" ", "_").toLowerCase();
                translations.append(valueKey + "=" + valueName);
                translations.append("\n");
                if (value.getDescription() == null || value.getDescription().isEmpty()) continue;
                String description = value.getDescription().replace("\n", " ");
                String tooltipKey = valueKey + ".tooltip";
                translations.append(tooltipKey + "=" + description);
                translations.append("\n");
            }
        }
        return translations.toString();
    }

    private static Exception propagateException(Exception exception) {
        return exception;
    }

    @EventHandler
    public void onModuleStateChanged(EventModStateChange eventModStateChange) {
        Mod mod = eventModStateChange.getModule();
        if (mod.isEnabled()) {
            this.enabledModules.add(mod);
        } else {
            this.enabledModules.remove(mod);
        }
        for (Mod mod2 : this.getMods()) {
            mod2.U(mod);
        }
        if (ClientSettings.INSTANCE.isInputEnabled() && mod.q$src$Z$12h8h4c() && Vape.INSTANCE.getPublicProfileSettings().toggleAlerts.getEffectiveValue().booleanValue() && !this.suppressStateNotifications) {
            mod.showToggleNotification();
        }
    }

    @EventHandler
    public void onModuleEnabled(EventModStateChange eventModStateChange) {
        Mod mod = eventModStateChange.getModule();
        if (eventModStateChange.isEnabled()) {
            mod.j();
        }
    }

    private static void addMinecraft1214Constraint(ModRegistrationBuilder modRegistrationBuilder) {
        modRegistrationBuilder.addVersionConstraint(ForgeVersion.MC_1_21_4.n());
    }

    static {
        ModManager.setLegacyCategoryComponents(new GuiComponent[4]);
    }

    private void registerHudModules() {
        this.registerModules(Stream.of(
                new KeystrokesHudModule(),
                new ClockHudModule(),
                new PotionEffectsHudModule(),
                new ArmorStatusHudModule(),
                new ReachDisplayHudModule(),
                new CompassHudModule(),
                new WeatherChangerHudModule(),
                new TimeChangerHudModule(),
                new CoordinatesHudModule(),
                new FpsDisplayHudModule(),
                new NoFogHudModule(),
                new BlockOverlayHudModule(),
                new BlockRenderColorOverrideHudModule()));
        this.registerModules(Stream.of(new ScoreboardHudModule(), new InventoryBlurHudModule()), ModManager::addMinecraft1206Constraint);
    }

    public int countEnabledModules(Category category) {
        int count = 0;
        for (Mod mod : this.activeModulesByType.values()) {
            if (mod.getCategory() != category || !mod.isEnabled()) continue;
            ++count;
        }
        return count;
    }

    @Nullable
    public XRay getXRayModule() {
        return this.xrayModule;
    }

    public JsonArray toJson(boolean includeDefaults) {
        JsonArray serializedModules = new JsonArray();
        for (Mod mod : this.getTopLevelModules()) {
            JsonObject serializedModule = mod.toJson(includeDefaults);
            if (serializedModule == null) continue;
            serializedModules.add((JsonElement)serializedModule);
        }
        return serializedModules;
    }

    public JsonObject getEnabledHudModuleStates() {
        JsonObject enabledStates = new JsonObject();
        for (Mod mod : this.getAllModules()) {
            if (!(mod instanceof HudModule) || !mod.isEnabled()) continue;
            enabledStates.addProperty(mod.getName(), Boolean.valueOf(mod.isEnabled()));
        }
        return enabledStates;
    }

    public static GuiComponent[] getLegacyCategoryComponents() {
        return legacyCategoryComponents;
    }

    private static void addBacktrackVersionConstraints(ModRegistrationBuilder modRegistrationBuilder) {
        modRegistrationBuilder.addVersionConstraint(ForgeVersion.MC_1_8_9.H()).addVersionConstraint(ForgeVersion.MC_1_21_4.n());
    }

    public void applyHudModuleStates(JsonObject enabledStates) {
        for (Mod mod : this.collectMods()) {
            if (!(mod instanceof HudModule) || !enabledStates.has(mod.getName())) continue;
            boolean enabled = enabledStates.get(mod.getName()).getAsBoolean();
            if (mod.isEnabled() == enabled) continue;
            mod.setEnabled(enabled);
        }
    }

    private static void addMinecraft189Constraint(ModRegistrationBuilder modRegistrationBuilder) {
        modRegistrationBuilder.addVersionConstraint(ForgeVersion.MC_1_8_9.H());
    }

    public <T extends Mod> T getMod(Class<T> clazz) {
        return (T)((Mod)this.activeModulesByType.get(clazz));
    }

    public boolean isOtherInventoryActionActive(Class<? extends InventoryActionModule> moduleType) {
        for (Mod mod : this.activeModulesByType.values()) {
            if (mod.getClass() == moduleType || !(mod instanceof InventoryActionModule)) continue;
            InventoryActionModule inventoryActionModule = (InventoryActionModule)((Object)mod);
            if (!mod.isEnabled() || !inventoryActionModule.isPerformingInventoryAction()) continue;
            return true;
        }
        return false;
    }

    public void disableNonHudModules() {
        this.suppressStateNotifications = true;
        for (Mod mod : this.collectMods()) {
            if (mod.getCategory() == Category.NONE || !mod.isEnabled() || mod instanceof HudModule) continue;
            mod.setEnabled(false);
        }
        this.suppressStateNotifications = false;
    }

    public Mod getMod(String name) {
        for (Map.Entry<Class<? extends Mod>, Mod> entry : this.activeModulesByType.entrySet()) {
            if (!((Mod)entry.getValue()).getName().equals(name)) continue;
            return (Mod)entry.getValue();
        }
        return null;
    }

    private static void addMinecraft1710Constraint(ModRegistrationBuilder modRegistrationBuilder) {
        modRegistrationBuilder.addVersionConstraint(ForgeVersion.MC_1_7_10.N());
    }

    public List<Mod> getProfileModules(JsonObject enabledModuleStates) {
        ArrayList<Mod> modules = new ArrayList<Mod>();
        for (Mod mod : this.collectMods()) {
            if (!enabledModuleStates.has(mod.getName()) || !mod.isVisible() || mod.getCategory() == Category.NONE) continue;
            modules.add(mod);
        }
        return modules;
    }

    public Collection<Mod> getAllModules() {
        return this.allModulesByType.values();
    }

    private static void addMinecraft1165Constraint(ModRegistrationBuilder modRegistrationBuilder) {
        modRegistrationBuilder.addVersionConstraint(ForgeVersion.MC_1_16_5.b());
    }

    private static void noAdditionalConstraints(ModRegistrationBuilder modRegistrationBuilder) {
    }

    public Collection<Mod> collectMods() {
        return this.activeModulesByType.values();
    }

    public void loadJson(JsonArray serializedModules) {
        for (int index = 0; index < serializedModules.size(); ++index) {
            JsonObject serializedModule;
            JsonElement element = serializedModules.get(index);
            if (!element.isJsonObject() || element.isJsonNull() || (serializedModule = element.getAsJsonObject()).get("name") == null || serializedModule.get("name").isJsonNull()) continue;
            String moduleName = serializedModule.get("name").getAsString();
            for (Mod mod : this.getTopLevelModules()) {
                try {
                    if (!mod.getName().equalsIgnoreCase(moduleName)) continue;
                    mod.loadJson(serializedModule);
                }
                catch (Exception exception) {
                    Vape.debugLog(mod.getName());
                    Vape.logThrowable(exception);
                }
            }
        }
    }

    public void disableHiddenModules() {
        int disabledCount = 0;
        for (Mod mod : this.collectMods()) {
            if (mod.isVisible() || mod instanceof ClientSettings || mod.getGuiColor() == 0 || !mod.isEnabled()) continue;
            ++disabledCount;
            mod.toggle();
        }
        if (disabledCount > 0) {
            Vape.INSTANCE.getNotificationManager().show("Hidden Disabled", disabledCount + " module(s) have been disabled!", NotificationType.WARNING, 2500L);
        }
    }

    public ModManager() {
        GuiComponent[] legacyComponentsSnapshot = Category.getLegacyComponents();
        Object[] discardedSlotStorage = new Object[877];
        Array.newInstance(Long.TYPE, 837);
        Array.newInstance(Byte.TYPE, 904);
        Array.newInstance(Float.TYPE, 627);
        Array.newInstance(Short.TYPE, 845);
        Array.newInstance(Object.class, 823);
        Array.newInstance(Character.TYPE, 598);
        Array.newInstance(Double.TYPE, 654);
        GuiComponent[] preservedLegacyComponents = legacyComponentsSnapshot;
        Array.newInstance(Integer.TYPE, 556);
        Array.newInstance(Boolean.TYPE, 506);
        this.allModulesByType = new LinkedHashMap();
        this.activeModulesByType = new LinkedHashMap();
        this.enabledModules = new HashSet();
        this.activeModuleList = new ArrayList();
        this.profileSwitchNotification = new ReusableTextNotification(NotificationType.INFO, "", "", 2000L);
        if (GuiComponent.getLegacyComponentState() == null) {
            Category.setLegacyComponents(new GuiComponent[1]);
        }
    }

    public ArrayList<Mod> getActiveModuleList() {
        return this.activeModuleList;
    }

    private static void addBedPlatesVersionConstraints(ModRegistrationBuilder modRegistrationBuilder) {
        modRegistrationBuilder.addVersionConstraints(ForgeVersion.MC_1_7_10.N(), ForgeVersion.MC_1_20_6.b());
    }
}
