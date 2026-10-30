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

    private static final int PENDING_TICKS = 5;

    private static KeyBinding toggleKey;

    private boolean jumpWasDown;
    private int pendingTicks;

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

    private void onTick(MinecraftClient client) {
        while (toggleKey.wasPressed()) {
            boolean state = Notlar22.toggle();
            ToggleSound.play(state);
            pendingTicks = 0;
        }

        ClientPlayerEntity player = client.player;
        boolean jumpDown = client.options.jumpKey.isPressed();
        boolean jumpPressed = jumpDown && !jumpWasDown;
        jumpWasDown = jumpDown;

        if (player == null || !Notlar22.isEnabled()) {
            pendingTicks = 0;
            return;
        }

        if (jumpPressed) {
            pendingTicks = PENDING_TICKS;
        }

        if (pendingTicks > 0) {
            pendingTicks--;
            if (player.isGliding()) {
                pendingTicks = 0;
            } else if (tryGlide(player)) {
                pendingTicks = 0;
            }
        }
    }

    private static boolean tryGlide(ClientPlayerEntity player) {
        if (player.isOnGround()
                || player.hasVehicle()
                || player.isTouchingWater()
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
