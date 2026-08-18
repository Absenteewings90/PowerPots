package com.leo.powerpots.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {

    public static final KeyMapping OPEN_UPGRADE_GUI = new KeyMapping(
            "key.powerpots.open_upgrade_gui",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_GRAVE_ACCENT, // default key is ` (backtick)
            "key.categories.powerpots"
    );
}
