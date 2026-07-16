package koolkrafter5.questrep.client.gui.tasks;

import net.minecraft.init.Items;
import net.minecraft.util.StatCollector;

import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import bq_standard.client.gui.tasks.PanelTaskItemBase;
import koolkrafter5.questrep.tasks.TaskDeaths;

public class PanelTaskDeaths extends PanelTaskItemBase<TaskDeaths> {

    public static final BigItemStack SKULL_STACK = new BigItemStack(Items.skull);

    public PanelTaskDeaths(IGuiRect rect, TaskDeaths task) {
        super(rect, task);
    }

    @Override
    protected int getItemCount() {
        return 1;
    }

    @Override
    protected BigItemStack getItemStack(int i) {
        return SKULL_STACK;
    }

    @Override
    protected GuiRectangle createItemSlotRect(int i) {
        return new GuiRectangle(0, i * 36, 36, 36, 0);
    }

    @Override
    protected GuiRectangle createTextBoxRect(int i, int width) {
        return new GuiRectangle(40, i * 36, width - 40, 36, 0);
    }

    @Override
    protected void initPanelExtras(int listW) {}

    @Override
    protected void addItemName(StringBuilder sb, BigItemStack stack, int index) {
        sb.append(StatCollector.translateToLocal("questrep.label.deaths"));
    }
}
