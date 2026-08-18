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
            ResourceLocation.fromNamespaceAndPath(PowerPots.MODID, "textures/gui/upgrade_gui.png");

    public UpgradeScreen(UpgradeMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        drawStats(graphics);
    }

    private void drawStats(GuiGraphics graphics) {
        int textX = leftPos + 36;

        float speed = menu.getSpeedModifier();
        int speedPct = (int)((speed - 1f) * 100);
        graphics.drawString(this.font,
                Component.literal("Speed: +" + speedPct + "%"),
                textX, topPos + 18,
                speedPct > 0 ? 0x14A614 : 0x555555, false);

        float output = menu.getOutputModifier();
        int outputPct = (int)((output - 1f) * 100);
        graphics.drawString(this.font,
                Component.literal("Output: +" + outputPct + "%"),
                textX, topPos + 40,
                outputPct > 0 ? 0x14A614 : 0x555555, false);

        float energy = menu.getEnergyModifier();
        int energyPct = (int)((1f - energy) * 100);
        graphics.drawString(this.font,
                Component.literal("Energy: -" + energyPct + "%"),
                textX, topPos + 62,
                energyPct > 0 ? 0x14A614 : 0x555555, false);

        int fortune = menu.getFortuneLevel();
        graphics.drawString(this.font,
                Component.literal("Fortune: +" + fortune),
                textX, topPos + 74,
                fortune > 0 ? 0x14A614 : 0x555555, false);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font,
                Component.translatable("screen.powerpots.upgrades"),
                7, 4, 0x404040, false);
    }
}