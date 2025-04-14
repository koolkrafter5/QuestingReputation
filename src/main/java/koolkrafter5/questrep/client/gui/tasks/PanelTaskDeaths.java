package koolkrafter5.questrep.client.gui.tasks;

import java.awt.*;

import net.minecraft.init.Items;
import net.minecraft.util.StatCollector;

import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.misc.*;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.content.PanelItemSlot;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import koolkrafter5.questrep.tasks.TaskDeaths;

public class PanelTaskDeaths extends CanvasEmpty {

    private final TaskDeaths task;

    public PanelTaskDeaths(IGuiRect rect, TaskDeaths task) {
        super(rect);
        this.task = task;
    }

    public void initPanel() {
        super.initPanel();
        PanelTextBox text;
        if (task.progress < task.target) {
            text = new PanelTextBox(
                new GuiRectangle(
                    0,
                    0,
                    this.getTransform()
                        .getWidth(),
                    20,
                    0),
                StatCollector.translateToLocalFormatted("questrep.gui.deaths.incomplete", task.progress, task.target));
            text.setColor(PresetColor.TEXT_MAIN.getColor());
        } else {
            text = new PanelTextBox(
                new GuiRectangle(
                    0,
                    0,
                    this.getTransform()
                        .getWidth(),
                    20,
                    0),
                StatCollector.translateToLocalFormatted("questrep.gui.deaths.complete", task.progress));
            text.setColor(new GuiColorStatic(0, 176, 0, 255));
        }
        text.setFontSize(12);
        PanelItemSlot skull = new PanelItemSlot(new GuiRectangle(0, 20, 24, 24, 0), -1, new BigItemStack(Items.skull));
        this.addPanel(text);
        this.addPanel(skull);
    }
}
