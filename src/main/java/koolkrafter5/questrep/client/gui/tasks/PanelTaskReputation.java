package koolkrafter5.questrep.client.gui.tasks;

import net.minecraft.init.Items;
import net.minecraft.util.StatCollector;

import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.content.PanelItemSlot;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import koolkrafter5.questrep.tasks.TaskReputation;

public class PanelTaskReputation extends CanvasEmpty {

    private final TaskReputation task;

    public PanelTaskReputation(IGuiRect rect, TaskReputation task) {
        super(rect);
        this.task = task;
    }

    public void initPanel() {
        super.initPanel();

        PanelTextBox text1 = new PanelTextBox(
            new GuiRectangle(0, 0, 100, 20, 0),
            StatCollector.translateToLocalFormatted("questrep.gui.reputation.text", task.faction, 0));
        text1.setColor(PresetColor.TEXT_MAIN.getColor())
            .setFontSize(12);
        PanelItemSlot skull = new PanelItemSlot(new GuiRectangle(0, 20, 24, 24, 0), -1, new BigItemStack(Items.skull));
        this.addPanel(text1);
        this.addPanel(skull);
    }
}
