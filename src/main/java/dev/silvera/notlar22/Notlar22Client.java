package dev.silvera.notlar22;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import org.lwjgl.glfw.GLFW;

public final class Notlar22Client implements ClientModInitializer {

    private static final int PENDING_TICKS = 12;
    private static final int MIN_AIR_TICKS = 2;
    private static final int VERIFY_TICKS = 10;
    private static final int MAX_ATTEMPTS = 3;

    private static KeyBinding toggleKey;

    private boolean jumpWasDown;
    private int pendingTicks;
    private int airTicks;
    private int attempts;
    private int verifyTicks;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.notlar22.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.notlar22"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void resetGlideState() {
        pendingTicks = 0;
        attempts = 0;
        verifyTicks = 0;
    }

    private void onTick(MinecraftClient client) {
        while (toggleKey.wasPressed()) {
            boolean state = Notlar22.toggle();
            ToggleSound.play(state);
            resetGlideState();
        }

        ClientPlayerEntity player = client.player;

        boolean pressed = false;
        while (client.options.jumpKey.wasPressed()) {
            pressed = true;
        }
        boolean jumpDown = client.options.jumpKey.isPressed();
        if (jumpDown && !jumpWasDown) {
            pressed = true;
        }
        jumpWasDown = jumpDown;

        if (player == null) {
            airTicks = 0;
            resetGlideState();
            return;
        }

        if (player.isOnGround()) {
            airTicks = 0;
        } else {
            airTicks++;
        }

        if (!Notlar22.isEnabled()) {
            resetGlideState();
            return;
        }

        if (pressed) {
            pendingTicks = PENDING_TICKS;
            attempts = 0;
            verifyTicks = 0;
        }

        if (player.isGliding()) {
            if (verifyTicks > 0) {
                verifyTicks--;
            }
            pendingTicks = 0;
            return;
        }

        if (verifyTicks > 0) {
            verifyTicks = 0;
            if (attempts < MAX_ATTEMPTS) {
                pendingTicks = PENDING_TICKS;
            }
        }

        if (pendingTicks > 0) {
            pendingTicks--;
            if (airTicks >= MIN_AIR_TICKS && attempts < MAX_ATTEMPTS && tryGlide(player)) {
                attempts++;
                verifyTicks = VERIFY_TICKS;
            }
        }
    }

    private static boolean tryGlide(ClientPlayerEntity player) {
        if (player.isOnGround()
                || player.hasVehicle()
                || player.isTouchingWater()
                || player.isInLava()
                || player.isClimbing()
                || player.isSpectator()
                || player.getAbilities().flying
                || player.hasStatusEffect(StatusEffects.LEVITATION)) {
            return false;
        }
        if (player.checkGliding()) {
            player.networkHandler.sendPacket(
                    new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            return true;
        }
        return false;
    }
}
