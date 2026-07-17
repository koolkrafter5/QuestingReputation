package koolkrafter5.questrep.client.gui.rewards;

import net.minecraft.util.EnumChatFormatting;

import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasMinimum;
import betterquesting.api2.client.gui.panels.content.PanelItemSlot;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import bq_standard.client.gui.panels.content.PanelItemSlotBuilder;
import koolkrafter5.questrep.reputation.FactionData;
import koolkrafter5.questrep.rewards.RewardReputation;

public class PanelRewardReputation extends CanvasMinimum {

    private final RewardReputation reward;
    private final IGuiRect initialRect;

    public PanelRewardReputation(IGuiRect rect, RewardReputation reward) {
        super(rect);
        this.reward = reward;
        this.initialRect = rect;
    }

    public void initPanel() {
        super.initPanel();
        int listWidth = initialRect.getWidth();

        BigItemStack stack = FactionData.getRepresentativeStack(reward.faction);
        GuiRectangle rectangle = new GuiRectangle(0, 0, 18, 18, 0);
        PanelItemSlot is = PanelItemSlotBuilder.forValue(stack, rectangle)
            .build();
        addPanel(is);
        addPanel(
            new PanelTextBox(new GuiRectangle(22, 6, listWidth - 22, 14, 0), getText())
                .setColor(PresetColor.TEXT_MAIN.getColor()));
        recalcSizes();
    }

    private String getText() {
        if (reward.amount > 0) {
            return EnumChatFormatting.GREEN + "+"
                + reward.amount
                + EnumChatFormatting.RESET
                + " "
                + EnumChatFormatting.getTextWithoutFormattingCodes(FactionData.getDisplayName(reward.faction));
        }
        if (reward.amount == 0) {
            return EnumChatFormatting.YELLOW + "+"
                + reward.amount
                + EnumChatFormatting.RESET
                + " "
                + EnumChatFormatting.getTextWithoutFormattingCodes(FactionData.getDisplayName(reward.faction));
        }
        return "" + EnumChatFormatting.RED
            + reward.amount
            + EnumChatFormatting.RESET
            + " "
            + EnumChatFormatting.getTextWithoutFormattingCodes(FactionData.getDisplayName(reward.faction));
    }
}
