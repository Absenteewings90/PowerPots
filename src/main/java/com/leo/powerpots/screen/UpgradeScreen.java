package com.leo.powerpots.screen;

import com.leo.powerpots.PowerPots;
import com.leo.powerpots.upgrade.UpgradeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;


public class UpgradeScreen extends AbstractContainerScreen<UpgradeMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(PowerPots.MODID, "textures/gui/upgrade_gui.png");

    public UpgradeScreen(UpgradeMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        // center the screen normally
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        // draw the texture at exact screen position
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, 176, 166);
        drawStats(graphics);
    }

    private void drawStats(GuiGraphics graphics) {
        int textX = leftPos + 36; // right of the slots

        // Speed — beside slot 1
        float speed = menu.blockEntity.getUpgradeModifier(UpgradeType.SPEED);
        int speedPct = (int)((speed - 1f) * 100);
        graphics.drawString(this.font,
                Component.literal("Speed: +" + speedPct + "%"),
                textX, topPos + 18,
                speedPct > 0 ? 0x14A614 : 0x555555, false);

        // Output — beside slot 2
        float output = menu.blockEntity.getUpgradeModifier(UpgradeType.OUTPUT);
        int outputPct = (int)((output - 1f) * 100);
        graphics.drawString(this.font,
                Component.literal("Output: +" + outputPct + "%"),
                textX, topPos + 40,
                outputPct > 0 ? 0x14A614 : 0x555555, false);

        // Energy — beside slot 3
        float energy = menu.blockEntity.getUpgradeModifier(UpgradeType.ENERGY);
        int energyPct = (int)((1f - energy) * 100);
        graphics.drawString(this.font,
                Component.literal("Energy: -" + energyPct + "%"),
                textX, topPos + 62,
                energyPct > 0 ? 0x14A614 : 0x555555, false);

        // Fortune — below the slots
        int fortune = menu.blockEntity.getFortuneLevel();
        graphics.drawString(this.font,
                Component.literal("Fortune: +" + fortune),
                textX, topPos + 74,
                fortune > 0 ? 0x14A614 : 0x555555, false);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // title — top left, offset from leftPos/topPos automatically by parent
        graphics.drawString(this.font,
                Component.translatable("screen.powerpots.upgrades"),
                7, 4, 0x404040, false);
    }
}