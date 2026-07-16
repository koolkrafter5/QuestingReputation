package koolkrafter5.questrep.client.gui.tasks;

import net.minecraft.client.Minecraft;
import net.minecraft.util.StatCollector;

import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import bq_standard.client.gui.tasks.PanelTaskItemBase;
import koolkrafter5.questrep.reputation.FactionData;
import koolkrafter5.questrep.reputation.ReputationData;
import koolkrafter5.questrep.tasks.TaskReputation;

public class PanelTaskReputation extends PanelTaskItemBase<TaskReputation> {

    public PanelTaskReputation(IGuiRect rect, TaskReputation task) {
        super(rect, task);
    }

    @Override
    protected int getItemCount() {
        return 1;
    }

    @Override
    protected BigItemStack getItemStack(int i) {
        return FactionData.getRepresentativeStack(task.faction);
    }

    @Override
    protected GuiRectangle createItemSlotRect(int i) {
        return new GuiRectangle(0, i * 36 + (hasOneLine() ? 0 : 4), 36, 36, 0);
    }

    @Override
    protected GuiRectangle createTextBoxRect(int i, int width) {
        return new GuiRectangle(40, i * 36, width - 40, hasOneLine() ? 36 : 45, 0);
    }

    private boolean hasOneLine() {
        return task.lowerBound == Integer.MIN_VALUE || task.upperBound == Integer.MAX_VALUE
            || task.lowerBound == task.upperBound;
    }

    @Override
    protected void initPanelExtras(int listW) {}

    @Override
    protected void addItemName(StringBuilder sb, BigItemStack stack, int index) {
        sb.append(
            StatCollector.translateToLocalFormatted(
                "questrep.gui.reputation.faction",
                StatCollector.translateToLocal(FactionData.getDisplayName(task.faction))));
    }

    @Override
    protected void addOreDict(StringBuilder sb, BigItemStack stack, int index) {
        int reputation = ReputationData.get()
            .getReputation(Minecraft.getMinecraft().thePlayer, task.faction);
        sb.append("\n")
            .append(
                StatCollector.translateToLocalFormatted(
                    "questrep.gui.reputation.current",
                    reputation,
                    StatCollector.translateToLocal(
                        FactionData.getTier(task.faction, reputation)
                            .name())));
        sb.append("\n");
    }

    @Override
    protected void addProgress(StringBuilder sb, BigItemStack stack, int index) {
        task.targetText(sb);
    }

}
