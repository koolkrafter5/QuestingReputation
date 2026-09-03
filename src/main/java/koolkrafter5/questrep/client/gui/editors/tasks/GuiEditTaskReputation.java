package koolkrafter5.questrep.client.gui.editors.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Keyboard;

import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.network.handlers.NetQuestEdit;
import koolkrafter5.questrep.client.gui.editors.GuiFactionSelect;
import koolkrafter5.questrep.client.gui.editors.IFactionSelectionReceiver;
import koolkrafter5.questrep.client.gui.widgets.BoundSelector;
import koolkrafter5.questrep.reputation.FactionData;
import koolkrafter5.questrep.reputation.ReputationTier;
import koolkrafter5.questrep.tasks.TaskReputation;

public class GuiEditTaskReputation extends GuiScreenCanvas implements IFactionSelectionReceiver {

    private final Map.Entry<UUID, IQuest> quest;
    private final TaskReputation task;

    private List<ReputationTier> currentTiers = new ArrayList<>();

    private PanelTextBox currentFaction, previewText;

    private BoundSelector lowerBound, upperBound;
    private CanvasTextured cvBackground;

    public GuiEditTaskReputation(GuiScreen parent, Map.Entry<UUID, IQuest> quest, TaskReputation task) {
        super(parent);
        this.quest = quest;
        this.task = task;
        this.setVolatile(true);
    }

    @Override
    public void initPanel() {
        super.initPanel();
        Keyboard.enableRepeatEvents(true);

        cvBackground = new CanvasTextured(new GuiTransform(), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);

        cvBackground.addPanel(
            new PanelTextBox(
                new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(16, 16, 16, -32), 0),
                StatCollector.translateToLocal("questrep.gui.edit_reputation_task")).setAlignment(1)
                    .setColor(PresetColor.TEXT_HEADER.getColor()));

        // Done button
        cvBackground.addPanel(
            new PanelButton(
                new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0),
                -1,
                StatCollector.translateToLocal("gui.done")) {

                @Override
                public void onButtonClick() {
                    sendChanges();
                    mc.displayGuiScreen(parent);
                }
            });

        // Faction selection button
        currentFaction = new PanelTextBox(new GuiTransform(GuiAlign.TOP_CENTER, -200, 32, 400, 12, 0), "")
            .setAlignment(1)
            .setColor(PresetColor.TEXT_MAIN.getColor());
        cvBackground.addPanel(currentFaction);
        cvBackground.addPanel(
            new PanelButton(
                new GuiTransform(GuiAlign.TOP_CENTER, -100, 42, 200, 16, 0),
                -2,
                StatCollector.translateToLocal("questrep.label.reputation.faction.button")) {

                @Override
                public void onButtonClick() {
                    mc.displayGuiScreen(new GuiFactionSelect(GuiEditTaskReputation.this, task.faction));
                }
            });

        loadTiers();

        createLowerBound();
        cvBackground.addPanel(lowerBound);

        createUpperBound();
        cvBackground.addPanel(upperBound);

        // Invert Toggle
        cvBackground.addPanel(
            new PanelButton(new GuiTransform(GuiAlign.TOP_CENTER, -100, 134, 200, 16, 0), 4, getInvertText()) {

                @Override
                public void onButtonClick() {
                    task.invert = !task.invert;
                    this.setText(getInvertText());
                    rebuildText();
                }
            });

        // Preview Text
        previewText = new PanelTextBox(new GuiTransform(GuiAlign.TOP_CENTER, -100, 158, 200, 12, 0), "", true)
            .setAlignment(1)
            .setColor(PresetColor.TEXT_MAIN.getColor());
        cvBackground.addPanel(previewText);

        lowerBound.setButtons();
        upperBound.setButtons();
        lowerBound.setIndex();
        upperBound.setIndex();
    }

    private void createLowerBound() {
        lowerBound = new BoundSelector(
            new GuiTransform(GuiAlign.TOP_CENTER, 0, 64, 200, 26, 0),
            () -> currentTiers,
            () -> task.lowerBound,
            value -> task.lowerBound = value,
            this::rebuildText,
            -1,
            currentTiers.size() - 1,
            true);
    }

    private void createUpperBound() {
        upperBound = new BoundSelector(
            new GuiTransform(GuiAlign.TOP_CENTER, 0, 100, 200, 26, 0),
            () -> currentTiers,
            () -> task.upperBound,
            value -> task.upperBound = value,
            this::rebuildText,
            0,
            currentTiers.size(),
            false);
    }

    private void loadTiers() {
        currentTiers = FactionData.getTiers(task.faction);
    }

    private String getInvertText() {
        return QuestTranslation.translate(
            "questrep.label.reputation.invert",
            QuestTranslation.translate(task.invert ? "gui.yes" : "gui.no"));
    }

    private void sendChanges() {
        NetQuestEdit.requestEdit(Collections.singletonMap(quest.getKey(), quest.getValue()));
    }

    @Override
    public void setFaction(String faction) {
        task.faction = faction;
        loadTiers();
        cvBackground.removePanel(lowerBound);
        cvBackground.removePanel(upperBound);
        createLowerBound();
        createUpperBound();
        initPanel();
    }

    @Override
    public void rebuildText() {
        currentFaction.setText(
            QuestTranslation.translate("questrep.label.reputation.faction", FactionData.getDisplayName(task.faction)));
        lowerBound.label.setText(
            QuestTranslation.translate(
                "questrep.label.reputation.lower",
                (task.lowerBound != Integer.MIN_VALUE ? FactionData.getTierName(task.faction, task.lowerBound)
                    : StatCollector.translateToLocal("questrep.label.reputation.none"))));
        upperBound.label.setText(
            QuestTranslation.translate(
                "questrep.label.reputation.upper",
                (task.upperBound != Integer.MAX_VALUE ? FactionData.getTierName(task.faction, task.upperBound)
                    : StatCollector.translateToLocal("questrep.label.reputation.none"))));
        String targetText = task.targetText();
        previewText.setText(targetText.replace("\n", " "));
    }
}
