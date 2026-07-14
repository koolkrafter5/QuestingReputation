package koolkrafter5.questrep.client.gui.tasks;

import java.awt.*;

import net.minecraft.util.StatCollector;

import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import koolkrafter5.questrep.network.ClientReputationCache;
import koolkrafter5.questrep.reputation.FactionData;
import koolkrafter5.questrep.tasks.TaskReputation;

public class PanelTaskReputation extends CanvasEmpty {

    private final TaskReputation task;

    public PanelTaskReputation(IGuiRect rect, TaskReputation task) {
        super(rect);
        this.task = task;
    }

    public void initPanel() {
        super.initPanel();
        String displayName = FactionData.getDisplayName(task.faction);
        PanelTextBox target = new PanelTextBox(
            new GuiRectangle(0, 0, 200, 20, 0),
            StatCollector.translateToLocalFormatted("questrep.gui.reputation.required", displayName, task.targetText()),
            true);
        target.setColor(PresetColor.TEXT_MAIN.getColor())
            .setFontSize(12);
        this.addPanel(target);

        int rep = ClientReputationCache.getReputation(task.faction);
        PanelTextBox current = new PanelTextBox(
            new GuiRectangle(0, 50, 200, 20, 0),
            StatCollector.translateToLocalFormatted(
                "questrep.gui.reputation.current",
                rep,
                FactionData.getTierName(task.faction, rep)),
            true);
        if (task.checkReputation(rep)) {
            current.setColor(new GuiColorStatic(new Color(0, 180, 0)));
        } else {
            current.setColor(new GuiColorStatic(new Color(180, 0, 0)));
        }
        current.setFontSize(12);
        this.addPanel(current);
    }
}
