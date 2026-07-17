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
import betterquesting.api2.client.gui.controls.PanelTextField;
import betterquesting.api2.client.gui.controls.filters.FieldFilterNumber;
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
import koolkrafter5.questrep.reputation.FactionData;
import koolkrafter5.questrep.reputation.ReputationTier;
import koolkrafter5.questrep.tasks.TaskReputation;

public class GuiEditTaskReputation extends GuiScreenCanvas implements IFactionSelectionReceiver {

    private final Map.Entry<UUID, IQuest> quest;
    private final TaskReputation task;

    private int lowerTierIndex, upperTierIndex;
    private boolean lowerTextUpdate, upperTextUpdate;
    private List<ReputationTier> currentTiers = new ArrayList<>();

    private PanelTextField<Integer> txtLowerBound, txtUpperBound;
    private PanelTextBox currentFaction, lowerText, upperText, previewText;
    private PanelButton lowerMin, lowerDec, lowerInc, lowerMax;
    private PanelButton upperMin, upperDec, upperInc, upperMax;

    public GuiEditTaskReputation(GuiScreen parent, Map.Entry<UUID, IQuest> quest, TaskReputation task) {
        super(parent);
        this.quest = quest;
        this.task = task;
        lowerTierIndex = -1;
        upperTierIndex = FactionData.getTiers(task.faction)
            .size();
        this.setVolatile(true);
    }

    @Override
    public void initPanel() {
        super.initPanel();
        Keyboard.enableRepeatEvents(true);

        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(), PresetTexture.PANEL_MAIN.getTexture());
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

        // Lower Bound Controls
        lowerText = new PanelTextBox(new GuiTransform(GuiAlign.TOP_CENTER, -100, 64, 200, 12, 0), "").setAlignment(1)
            .setColor(PresetColor.TEXT_MAIN.getColor());
        cvBackground.addPanel(lowerText);
        txtLowerBound = new PanelTextField<>(
            new GuiTransform(GuiAlign.TOP_CENTER, -60, 74, 120, 16, 0),
            Integer.toString(task.lowerBound),
            FieldFilterNumber.INT).setCallback(val -> {
                task.lowerBound = val;
                lowerTextUpdate = true;
                setLowerIndex();
                rebuildText();
            });
        cvBackground.addPanel(txtLowerBound);

        lowerMin = new PanelButton(new GuiTransform(GuiAlign.TOP_CENTER, -100, 74, 20, 16, 0), 1, "x") {

            // Remove Lower Bound
            @Override
            public void onButtonClick() {
                if (currentTiers.isEmpty()) {
                    return;
                }
                lowerTextUpdate = false;
                lowerTierIndex = -1;
                updateLowerBound();
            }
        };
        lowerMin.setTooltip(
            Collections.singletonList(StatCollector.translateToLocal("questrep.button.reputation.remove.lower")));
        cvBackground.addPanel(lowerMin);
        lowerDec = new PanelButton(new GuiTransform(GuiAlign.TOP_CENTER, -80, 74, 20, 16, 0), 0, "<") {

            // Decrease Lower Bound
            @Override
            public void onButtonClick() {
                if (currentTiers.isEmpty()) {
                    return;
                }
                if (lowerTextUpdate) {
                    if (currentTiers.getFirst()
                        .value() > task.lowerBound) {
                        lowerTierIndex = 0;
                    } else if (currentTiers.getLast()
                        .value() < task.lowerBound) {
                            lowerTierIndex = currentTiers.size();
                        } else {
                            for (int i = 0; i < currentTiers.size(); i++) {
                                if (currentTiers.get(i)
                                    .value() >= task.lowerBound) {
                                    lowerTierIndex = i;
                                    break;
                                }
                            }
                        }
                    lowerTextUpdate = false;
                }
                lowerTierIndex = lowerTierIndex - 1;
                updateLowerBound();
            }
        };
        lowerDec.setTooltip(
            Collections.singletonList(StatCollector.translateToLocal("questrep.button.reputation.previous")));
        cvBackground.addPanel(lowerDec);
        lowerInc = new PanelButton(new GuiTransform(GuiAlign.TOP_CENTER, 60, 74, 20, 16, 0), 1, ">") {

            // Increase Lower Bound
            @Override
            public void onButtonClick() {
                if (currentTiers.isEmpty()) {
                    return;
                }
                if (lowerTextUpdate) {
                    if (currentTiers.getFirst()
                        .value() > task.lowerBound) {
                        lowerTierIndex = -1;
                    } else if (currentTiers.getLast()
                        .value() < task.lowerBound) {
                            lowerTierIndex = currentTiers.size();
                        } else {
                            for (int i = 0; i < currentTiers.size(); i++) {
                                if (currentTiers.get(i)
                                    .value() > task.lowerBound) {
                                    lowerTierIndex = i - 1;
                                    break;
                                }
                            }
                        }
                    lowerTextUpdate = false;
                }
                lowerTierIndex = lowerTierIndex + 1;
                updateLowerBound();
            }
        };
        lowerInc
            .setTooltip(Collections.singletonList(StatCollector.translateToLocal("questrep.button.reputation.next")));
        cvBackground.addPanel(lowerInc);
        lowerMax = new PanelButton(new GuiTransform(GuiAlign.TOP_CENTER, 80, 74, 20, 16, 0), 1, ">>") {

            // Set Lower Bound to the highest tier
            @Override
            public void onButtonClick() {
                if (currentTiers.isEmpty()) {
                    return;
                }
                lowerTierIndex = currentTiers.size() - 1;
                lowerTextUpdate = false;
                updateLowerBound();
            }
        };
        lowerMax.setTooltip(
            Collections.singletonList(StatCollector.translateToLocal("questrep.button.reputation.highest")));
        cvBackground.addPanel(lowerMax);

        // Upper Bound Controls
        upperText = new PanelTextBox(new GuiTransform(GuiAlign.TOP_CENTER, -100, 98, 200, 12, 0), "").setAlignment(1)
            .setColor(PresetColor.TEXT_MAIN.getColor());
        cvBackground.addPanel(upperText);
        txtUpperBound = new PanelTextField<>(
            new GuiTransform(GuiAlign.TOP_CENTER, -60, 108, 120, 16, 0),
            Integer.toString(task.upperBound),
            FieldFilterNumber.INT).setCallback(val -> {
                task.upperBound = val;
                upperTextUpdate = true;
                setUpperIndex();
                updateUpperBound();
                rebuildText();
            });
        upperMin = new PanelButton(new GuiTransform(GuiAlign.TOP_CENTER, -100, 108, 20, 16, 0), 1, "<<") {

            // Set Upper Bound to the lowest tier
            @Override
            public void onButtonClick() {
                if (currentTiers.isEmpty()) {
                    return;
                }
                upperTierIndex = 0;
                upperTextUpdate = false;
                updateUpperBound();
            }
        };
        upperMin
            .setTooltip(Collections.singletonList(StatCollector.translateToLocal("questrep.button.reputation.lowest")));
        cvBackground.addPanel(upperMin);
        upperDec = new PanelButton(new GuiTransform(GuiAlign.TOP_CENTER, -80, 108, 20, 16, 0), 2, "<") {

            // Decrease Upper Bound
            @Override
            public void onButtonClick() {
                if (currentTiers.isEmpty()) {
                    return;
                }
                if (upperTextUpdate) {
                    if (currentTiers.getFirst()
                        .value() > task.upperBound) {
                        upperTierIndex = 0;
                    } else if (currentTiers.getLast()
                        .value() < task.upperBound) {
                            upperTierIndex = currentTiers.size();
                        } else {
                            for (int i = 0; i < currentTiers.size(); i++) {
                                if (currentTiers.get(i)
                                    .value() >= task.upperBound) {
                                    upperTierIndex = i;
                                    break;
                                }
                            }
                        }
                    upperTextUpdate = false;
                }
                upperTierIndex = upperTierIndex - 1;
                updateUpperBound();
            }
        };
        upperDec.setTooltip(
            Collections.singletonList(StatCollector.translateToLocal("questrep.button.reputation.previous")));
        cvBackground.addPanel(upperDec);
        upperInc = new PanelButton(new GuiTransform(GuiAlign.TOP_CENTER, 60, 108, 20, 16, 0), 3, ">") {

            // Increase Upper Bound
            @Override
            public void onButtonClick() {
                if (currentTiers.isEmpty()) {
                    return;
                }
                if (upperTextUpdate) {
                    if (currentTiers.getFirst()
                        .value() > task.upperBound) {
                        upperTierIndex = -1;
                    } else if (currentTiers.getLast()
                        .value() < task.upperBound) {
                            upperTierIndex = currentTiers.size() - 1;
                        } else {
                            for (int i = currentTiers.size() - 1; i >= 0; i--) {
                                if (currentTiers.get(i)
                                    .value() <= task.upperBound) {
                                    upperTierIndex = i;
                                    break;
                                }
                            }
                        }
                    upperTextUpdate = false;
                }
                if (upperTierIndex >= currentTiers.size() - 1) {
                    txtUpperBound.setText("");
                }
                upperTierIndex = upperTierIndex + 1;
                updateUpperBound();
            }
        };
        upperInc
            .setTooltip(Collections.singletonList(StatCollector.translateToLocal("questrep.button.reputation.next")));
        cvBackground.addPanel(upperInc);
        upperMax = new PanelButton(new GuiTransform(GuiAlign.TOP_CENTER, 80, 108, 20, 16, 0), 3, "x") {

            // Remove Upper Bound
            @Override
            public void onButtonClick() {
                if (currentTiers.isEmpty()) {
                    return;
                }
                task.upperBound = Integer.MAX_VALUE;
                txtUpperBound.setText("");
                upperTierIndex = currentTiers.size();
                upperTextUpdate = false;
                updateUpperBound();
            }
        };
        upperMax.setTooltip(
            Collections.singletonList(StatCollector.translateToLocal("questrep.button.reputation.remove.upper")));
        cvBackground.addPanel(upperMax);
        cvBackground.addPanel(txtUpperBound);

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

        lowerTextUpdate = true;
        upperTextUpdate = true;
        setLowerIndex();
        setUpperIndex();
        rebuildText();
    }

    private void loadTiers() {
        currentTiers = FactionData.getTiers(task.faction);
    }

    private void setLowerIndex() {
        if (currentTiers.getFirst()
            .value() > task.lowerBound) {
            lowerTierIndex = -1;
        } else if (currentTiers.getLast()
            .value() < task.lowerBound) {
                lowerTierIndex = currentTiers.size() - 1;
            } else {
                for (int i = 0; i < currentTiers.size(); i++) {
                    int val = currentTiers.get(i)
                        .value();
                    if (FactionData.getTierValue(task.faction, task.lowerBound) == val) {
                        lowerTierIndex = i;
                        break;
                    }
                }
            }
        updateLowerBound();
    }

    private void setUpperIndex() {
        if (currentTiers.getFirst()
            .value() > task.upperBound) {
            upperTierIndex = 0;
        } else if (currentTiers.getLast()
            .value() < task.upperBound) {
                upperTierIndex = currentTiers.size();
            } else {
                for (int i = 0; i < currentTiers.size(); i++) {
                    int val = currentTiers.get(i)
                        .value();
                    if (FactionData.getTierValue(task.faction, task.upperBound) == val) {
                        upperTierIndex = i;
                    }
                }
            }
        updateUpperBound();
    }

    private void updateLowerBound() {
        if (currentTiers.isEmpty()) return;
        setLowerButtons();
        if (lowerTextUpdate) {
            task.lowerBound = txtLowerBound.getValue();
            rebuildText();
            return;
        }
        if (lowerTierIndex < 0 || lowerTierIndex >= currentTiers.size()) {
            task.lowerBound = Integer.MIN_VALUE;
            txtLowerBound.setText("");
            rebuildText();
            return;
        }
        task.lowerBound = currentTiers.get(lowerTierIndex)
            .value();
        txtLowerBound.setText(Integer.toString(task.lowerBound));
        rebuildText();
    }

    private void updateUpperBound() {
        if (currentTiers.isEmpty()) return;
        setUpperButtons();
        if (upperTextUpdate) {
            task.upperBound = txtUpperBound.getValue();
            rebuildText();
            return;
        }
        if (upperTierIndex < 0 || upperTierIndex >= currentTiers.size()) {
            task.upperBound = Integer.MAX_VALUE;
            txtUpperBound.setText("");
            rebuildText();
            return;
        }
        task.upperBound = currentTiers.get(upperTierIndex)
            .value();
        txtUpperBound.setText(Integer.toString(task.upperBound));
        rebuildText();
    }

    private void setLowerButtons() {
        if (lowerTierIndex <= -1) {
            lowerTierIndex = -1;
            task.lowerBound = Integer.MIN_VALUE;
            lowerMin.setEnabled(false);
            lowerDec.setEnabled(false);
            lowerInc.setEnabled(true);
            lowerMax.setEnabled(true);
            lowerTextUpdate = false;
        } else if (lowerTierIndex >= currentTiers.size() - 1) {
            lowerTierIndex = currentTiers.size() - 1;
            lowerMin.setEnabled(true);
            lowerDec.setEnabled(true);
            lowerInc.setEnabled(false);
            lowerMax.setEnabled(false);
        } else {
            lowerMin.setEnabled(true);
            lowerDec.setEnabled(true);
            lowerInc.setEnabled(true);
            lowerMax.setEnabled(true);
        }
        rebuildText();
    }

    private void setUpperButtons() {
        if (upperTierIndex <= 0) {
            upperTierIndex = 0;
            upperMin.setEnabled(false);
            upperDec.setEnabled(false);
            upperInc.setEnabled(true);
            upperMax.setEnabled(true);
        } else if (upperTierIndex >= currentTiers.size()) {
            task.upperBound = Integer.MAX_VALUE;
            upperTierIndex = currentTiers.size();
            upperMin.setEnabled(true);
            upperDec.setEnabled(true);
            upperInc.setEnabled(false);
            upperMax.setEnabled(false);
        } else {
            upperMin.setEnabled(true);
            upperDec.setEnabled(true);
            upperInc.setEnabled(true);
            upperMax.setEnabled(true);
            upperTextUpdate = false;
        }
        rebuildText();
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
        if (FactionData.getAllFactions()
            .contains(faction)) {
            upperTierIndex = FactionData.getTiers(faction)
                .size();
        } else {
            upperTierIndex = 0;
        }
        task.faction = faction;
        lowerTierIndex = -1;
        lowerText.setText("");
        upperText.setText("");
        loadTiers();
        initPanel();
    }

    @Override
    public void rebuildText() {
        currentFaction.setText(
            QuestTranslation.translate("questrep.label.reputation.faction", FactionData.getDisplayName(task.faction)));
        lowerText.setText(
            QuestTranslation.translate(
                "questrep.label.reputation.lower",
                (task.lowerBound != Integer.MIN_VALUE ? FactionData.getTierName(task.faction, task.lowerBound)
                    : StatCollector.translateToLocal("questrep.label.reputation.none"))));
        upperText.setText(
            QuestTranslation.translate(
                "questrep.label.reputation.upper",
                (task.upperBound != Integer.MAX_VALUE ? FactionData.getTierName(task.faction, task.upperBound)
                    : StatCollector.translateToLocal("questrep.label.reputation.none"))));
        String targetText = task.targetText();
        previewText.setText(targetText.replace("\n", " "));
    }
}
