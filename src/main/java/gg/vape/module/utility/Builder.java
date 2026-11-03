package gg.vape.module.utility;

import gg.vape.config.ClientSettings;
import gg.vape.event.EventHandler;
import gg.vape.event.impl.EventPostTick;
import gg.vape.event.impl.EventPreEntityUpdate;
import gg.vape.input.KeyBindingHelper;
import gg.vape.mapping.MappedClasses;
import gg.vape.module.Category;
import gg.vape.module.Mod;
import gg.vape.utils.TimerUtil;
import gg.vape.value.RandomValue;
import gg.vape.wrapper.impl.AxisAlignedBB;
import gg.vape.wrapper.impl.EntityPlayerSP;
import gg.vape.wrapper.impl.ForgeVersion;
import gg.vape.wrapper.impl.GameSettings;
import gg.vape.wrapper.impl.ItemStack;
import gg.vape.wrapper.impl.KeyBinding;
import gg.vape.wrapper.impl.Minecraft;
import gg.vape.wrapper.impl.RayTraceResult;
import gg.vape.wrapper.impl.RayTraceResult_type;

public class Builder extends Mod {
    private final RandomValue actionDelay = RandomValue.createWithDescription(
            this, "Action delay", "#", "ms", 80.0, 120.0, 180.0, 240.0, 10.0,
            "Random delay between simulated block-placement inputs");
    private final RandomValue sneakReleaseDelay = RandomValue.createWithDescription(
            this, "Sneak release delay", "#", "ms", 60.0, 100.0, 160.0, 220.0, 10.0,
            "Random delay before releasing sneak after leaving an edge");
    private final TimerUtil actionTimer = new TimerUtil();
    private KeyBinding sneakKey;
    private KeyBinding useKey;
    private boolean physicalSneakDown;
    private boolean physicalUseDown;
    private long sneakReleaseAt;
    private long actionDelayMs;

    public Builder() {
        super("Builder", -256, Category.UTILITY, "Uses normal movement, sneak, and use inputs to assist with bridging.");
        this.addValue(this.actionDelay, this.sneakReleaseDelay);
    }

    @Override
    public void onEnable() {
        this.actionDelayMs = (long)this.actionDelay.getRandomValue();
        this.actionTimer.reset();
    }

    @EventHandler
    public void onPlayerUpdate(EventPreEntityUpdate event) {
        EntityPlayerSP player = Minecraft.thePlayer();
        if (player.isNull() || Minecraft.theWorld().isNull()
                || !event.getEntity().equals(player)
                || Minecraft.currentScreen().isNotNull()
                || player.S$src$Z$151gttj()) {
            return;
        }
        String expectedThread = ForgeVersion.MC_1_16_5.d() ? "Render thread" : "Client thread";
        if (!Thread.currentThread().getName().equals(expectedThread)) {
            return;
        }

        GameSettings settings = Minecraft.gameSettings();
        this.sneakKey = settings.d$src$Lgg_vape_wrapper_impl_KeyBinding_$adn2z0();
        this.useKey = settings.b$src$Lgg_vape_wrapper_impl_KeyBinding_$1yi3362();
        this.physicalSneakDown = ClientSettings.isPhysicalKeyDown(this.sneakKey);
        this.physicalUseDown = ClientSettings.isPhysicalKeyDown(this.useKey);

        boolean moving = Math.abs(player.movementInput().D()) > 0.05f
                || Math.abs(player.movementInput().T()) > 0.05f;
        boolean atEdge = moving && player.b$src$Z$fqlxe4() && this.hasNoSupportAhead(player);
        long now = System.currentTimeMillis();
        if (atEdge) {
            this.sneakReleaseAt = now + (long)this.sneakReleaseDelay.getRandomValue();
        }
        boolean shouldSneak = atEdge || now < this.sneakReleaseAt;
        KeyBindingHelper.setPressedAndTick(this.sneakKey, shouldSneak || this.physicalSneakDown);

        if (atEdge && !this.physicalUseDown && this.isAimingAtPlaceableBlock(player)
                && this.actionTimer.hasTimeElapsed(this.actionDelayMs)) {
            KeyBindingHelper.setPressedAndTick(this.useKey, true);
            this.actionTimer.reset();
            this.actionDelayMs = (long)this.actionDelay.getRandomValue();
        }
    }

    @EventHandler
    public void onPostTick(EventPostTick event) {
        this.restoreInputStates();
    }

    @Override
    public void onDisable() {
        this.restoreInputStates();
        this.sneakReleaseAt = 0L;
    }

    private boolean hasNoSupportAhead(EntityPlayerSP player) {
        AxisAlignedBB bounds = player.R$src$Lgg_vape_wrapper_impl_AxisAlignedBB_$r19dfl();
        if (!ForgeVersion.MC_1_8_9.d()) {
            bounds = bounds.copy();
        }
        AxisAlignedBB supportCheck = bounds.expand(-0.2, 0.0, -0.2)
                .k(player.t(), ForgeVersion.MC_1_20_6.d() ? 1.0 : -1.0, player.T());
        return Minecraft.theWorld().i(player, supportCheck).isEmpty();
    }

    private boolean isAimingAtPlaceableBlock(EntityPlayerSP player) {
        ItemStack heldItem = player.getHeldItemHand();
        if (heldItem.isNull() || heldItem.getItem().isNull()
                || !heldItem.getItem().isInstance(MappedClasses.Vw)) {
            return false;
        }
        RayTraceResult hit = Minecraft.p$src$Lgg_vape_wrapper_impl_RayTraceResult_$5rw6n0();
        return hit.isNotNull() && hit.getTypeOfHit().equals(RayTraceResult_type.block());
    }

    private void restoreInputStates() {
        if (this.sneakKey != null && this.sneakKey.isNotNull()) {
            KeyBindingHelper.setPressedAndTick(this.sneakKey, this.physicalSneakDown);
        }
        if (this.useKey != null && this.useKey.isNotNull()) {
            KeyBindingHelper.setPressedAndTick(this.useKey, this.physicalUseDown);
        }
    }
}
