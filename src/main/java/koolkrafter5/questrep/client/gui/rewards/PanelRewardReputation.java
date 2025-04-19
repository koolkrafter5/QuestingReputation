package koolkrafter5.questrep.client.gui.rewards;

import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.util.vector.Vector4f;

import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import koolkrafter5.questrep.rewards.RewardReputation;

public class PanelRewardReputation extends CanvasEmpty {

    RewardReputation reward;

    public PanelRewardReputation(IGuiRect rect, RewardReputation reward) {
        super(rect);
        this.reward = reward;
    }

    public void initPanel() {
        super.initPanel();
        // this.addPanel((new PanelTextBox(new GuiTransform(new Vector4f(0.0F, 0.5F, 1.0F, 0.5F), new GuiPadding(0, -16,
        // 0, 0), 0), this.reward.score)).setAlignment(1).setColor(PresetColor.TEXT_MAIN.getColor()));
        String txt2 = EnumChatFormatting.BOLD.toString();
        if (!this.reward.relative) {
            txt2 = txt2 + "= " + this.reward.value;
        } else if (this.reward.value >= 0) {
            txt2 = txt2 + EnumChatFormatting.GREEN + "+ " + Math.abs(this.reward.value);
        } else {
            txt2 = txt2 + EnumChatFormatting.RED + "- " + Math.abs(this.reward.value);
        }

        this.addPanel(
            (new PanelTextBox(
                new GuiTransform(new Vector4f(0.0F, 0.5F, 1.0F, 0.5F), new GuiPadding(0, 0, 0, -16), 0),
                txt2)).setAlignment(1)
                    .setColor(PresetColor.TEXT_MAIN.getColor()));
    }
}
