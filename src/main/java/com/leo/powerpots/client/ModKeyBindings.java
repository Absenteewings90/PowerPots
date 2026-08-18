package com.leo.powerpots.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {

    public static final KeyMapping OPEN_UPGRADES = new KeyMapping(
            "key.powerpots.open_upgrades",        // lang key
            InputConstants.Type.KEYSYM,            // keyboard key
            GLFW.GLFW_KEY_GRAVE_ACCENT,                       // default key — `
            "key.categories.powerpots"             // category in controls menu
    );

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_UPGRADES);
    }
}
