package koolkrafter5.questrep.client.gui.editors;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Keyboard;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.QuestingPacket;
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
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.QuestTranslation;
import koolkrafter5.questrep.reputation.FactionData;
import koolkrafter5.questrep.reputation.ReputationTier;
import koolkrafter5.questrep.tasks.TaskReputation;

public class GuiEditTaskReputation extends GuiScreenCanvas {

    private static final ResourceLocation QUEST_EDIT = new ResourceLocation("betterquesting:quest_edit");
    private final DBEntry<IQuest> quest;
    private final TaskReputation task;

    private int lowerTierIndex = -1;
    private int upperTierIndex;
    private List<ReputationTier> currentTiers = new ArrayList<>();
    private PanelTextField<Integer> txtLowerBound, txtUpperBound;
    private boolean lowerTextUpdate, upperTextUpdate;

    private PanelTextBox currentFaction;
    private PanelTextBox lowerText, upperText;
    private PanelButton lowerMin, lowerDec, lowerInc, lowerMax;
    private PanelButton upperMin, upperDec, upperInc, upperMax;

    public GuiEditTaskReputation(GuiScreen parent, DBEntry<IQuest> quest, TaskReputation task) {
        super(parent);
        this.quest = quest;
        this.task = task;
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
            new GuiTransform(GuiAlign.TOP_CENTER, -80, 74, 160, 16, 0),
            task.lowerBound != Integer.MIN_VALUE ? Integer.toString(task.lowerBound) : "",
            FieldFilterNumber.INT);
        txtLowerBound.setCallback(val -> {
            task.lowerBound = val;
            lowerTextUpdate = true;
            setLowerButtons();
            rebuildText();
        });
        cvBackground.addPanel(txtLowerBound);

        lowerMin = new PanelButton(new GuiTransform(GuiAlign.TOP_CENTER, -120, 74, 20, 16, 0), 1, "<<") {

            // Remove Lower Bound
            @Override
            public void onButtonClick() {
                if (currentTiers.isEmpty()) {
                    return;
                }
                txtLowerBound.setText("");
                lowerTierIndex = -1;
                updateLowerBound();
            }
        };
        cvBackground.addPanel(lowerMin);
        lowerDec = new PanelButton(new GuiTransform(GuiAlign.TOP_CENTER, -100, 74, 20, 16, 0), 0, "<") {

            // Decrease Lower Bound
            @Override
            public void onButtonClick() {
                if (currentTiers.isEmpty()) {
                    return;
                }
                if (lowerTextUpdate) {
                    for (int i = 0; i < currentTiers.size(); i++) {
                        if (currentTiers.get(i).value >= task.lowerBound) {
                            lowerTextUpdate = false;
                            lowerTierIndex = i;
                            break;
                        }
                    }
                }
                lowerTierIndex = lowerTierIndex - 1;
                if (lowerTierIndex <= -1) {
                    txtLowerBound.setText("");
                }
                updateLowerBound();
            }
        };
        cvBackground.addPanel(lowerDec);
        lowerInc = new PanelButton(new GuiTransform(GuiAlign.TOP_CENTER, 80, 74, 20, 16, 0), 1, ">") {

            // Increase Lower Bound
            @Override
            public void onButtonClick() {
                if (currentTiers.isEmpty()) {
                    return;
                }
                if (lowerTextUpdate) {
                    for (int i = currentTiers.size() - 1; i >= 0; i--) {
                        if (currentTiers.get(i).value <= task.lowerBound) {
                            lowerTextUpdate = false;
                            lowerTierIndex = i;
                            break;
                        }
                    }
                }
                lowerTierIndex = lowerTierIndex + 1;
                updateLowerBound();
            }
        };
        cvBackground.addPanel(lowerInc);
        lowerMax = new PanelButton(new GuiTransform(GuiAlign.TOP_CENTER, 100, 74, 20, 16, 0), 1, ">>") {

            // Set Lower Bound to the highest tier
            @Override
            public void onButtonClick() {
                if (currentTiers.isEmpty()) {
                    return;
                }
                lowerTierIndex = currentTiers.size() - 1;
                updateLowerBound();
            }
        };
        cvBackground.addPanel(lowerMax);

        // Upper Bound Controls
        upperText = new PanelTextBox(new GuiTransform(GuiAlign.TOP_CENTER, -100, 98, 200, 12, 0), "").setAlignment(1)
            .setColor(PresetColor.TEXT_MAIN.getColor());
        cvBackground.addPanel(upperText);
        txtUpperBound = new PanelTextField<>(
            new GuiTransform(GuiAlign.TOP_CENTER, -80, 108, 160, 16, 0),
            Integer.toString(task.upperBound),
            FieldFilterNumber.INT);
        txtUpperBound.setCallback(val -> {
            task.upperBound = val;
            upperTextUpdate = true;
            setTierIndices();
            rebuildText();
        });
        upperMin = new PanelButton(new GuiTransform(GuiAlign.TOP_CENTER, -120, 108, 20, 16, 0), 1, "<<") {

            // Set Upper Bound to the lowest tier
            @Override
            public void onButtonClick() {
                if (currentTiers.isEmpty()) {
                    return;
                }
                upperTierIndex = 0;
                updateUpperBound();
            }
        };
        cvBackground.addPanel(upperMin);
        upperDec = new PanelButton(new GuiTransform(GuiAlign.TOP_CENTER, -100, 108, 20, 16, 0), 2, "<") {

            // Decrease Upper Bound
            @Override
            public void onButtonClick() {
                if (currentTiers.isEmpty()) {
                    return;
                }
                if (upperTextUpdate) {
                    for (int i = 0; i < currentTiers.size(); i++) {
                        if (currentTiers.get(i).value >= task.upperBound) {
                            upperTextUpdate = false;
                            upperTierIndex = i;
                            break;
                        }
                    }
                }
                upperTierIndex = upperTierIndex - 1;
                updateUpperBound();
            }
        };
        cvBackground.addPanel(upperDec);
        upperInc = new PanelButton(new GuiTransform(GuiAlign.TOP_CENTER, 80, 108, 20, 16, 0), 3, ">") {

            // Increase Upper Bound
            @Override
            public void onButtonClick() {
                if (currentTiers.isEmpty()) {
                    return;
                }
                if (upperTextUpdate) {
                    for (int i = currentTiers.size() - 1; i >= 0; i--) {
                        if (currentTiers.get(i).value <= task.upperBound) {
                            upperTextUpdate = false;
                            upperTierIndex = i;
                            break;
                        }
                    }
                }
                if (upperTierIndex >= currentTiers.size() - 1) {
                    txtUpperBound.setText("");
                }
                upperTierIndex = upperTierIndex + 1;
                updateUpperBound();
            }
        };
        cvBackground.addPanel(upperInc);
        upperMax = new PanelButton(new GuiTransform(GuiAlign.TOP_CENTER, 100, 108, 20, 16, 0), 3, ">>") {

            // Remove Upper Bound
            @Override
            public void onButtonClick() {
                if (currentTiers.isEmpty()) {
                    return;
                }
                txtUpperBound.setText("");
                upperTierIndex = currentTiers.size() - 1;
                updateUpperBound();
            }
        };
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

        setTierIndices();
        rebuildText();
    }

    private void loadTiers() {
        currentTiers = FactionData.getTiers(task.faction);
        if (currentTiers == null) currentTiers = new ArrayList<>();
    }

    private void setTierIndices() {
        for (int i = 0; i < currentTiers.size(); i++) {
            int val = currentTiers.get(i).value;
            if (FactionData.getTierValue(task.faction, task.upperBound) == val) {
                upperTierIndex = i;
            }
            if (FactionData.getTierValue(task.faction, task.upperBound) == val) {
                lowerTierIndex = i;
            }
        }
        updateLowerBound();
        updateUpperBound();
    }

    private void updateLowerBound() {
        if (currentTiers.isEmpty()) return;
        if (lowerTextUpdate) {
            task.lowerBound = txtLowerBound.getValue();
            lowerTextUpdate = false;
            rebuildText();
            return;
        }
        if (setLowerButtons()) {
            return;
        }
        task.lowerBound = currentTiers.get(lowerTierIndex).value;
        txtLowerBound.setText(Integer.toString(task.lowerBound));
        rebuildText();
    }

    private boolean setLowerButtons() {
        rebuildText();
        if (lowerTierIndex == -1) {
            task.lowerBound = Integer.MIN_VALUE;
            lowerMin.setEnabled(false);
            lowerDec.setEnabled(false);
            lowerInc.setEnabled(true);
            lowerMax.setEnabled(true);
            rebuildText();
            return true;
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
        return false;
    }

    private void updateUpperBound() {
        if (currentTiers.isEmpty()) return;
        if (upperTextUpdate) {
            task.upperBound = txtUpperBound.getValue();
            upperTextUpdate = false;
            rebuildText();
            return;
        }
        if (setUpperButtons()) {
            return;
        }
        task.upperBound = currentTiers.get(upperTierIndex).value;
        txtUpperBound.setText(Integer.toString(task.upperBound));
        rebuildText();
    }

    private boolean setUpperButtons() {
        rebuildText();
        if (upperTierIndex == 0) {
            upperMin.setEnabled(false);
            upperDec.setEnabled(false);
            upperInc.setEnabled(true);
            upperMax.setEnabled(true);
            return true;
        } else if (upperTierIndex >= currentTiers.size() - 1) {
            task.upperBound = Integer.MAX_VALUE;
            upperTierIndex = currentTiers.size() - 1;
            upperMin.setEnabled(true);
            upperDec.setEnabled(true);
            upperInc.setEnabled(false);
            upperMax.setEnabled(false);
        } else {
            upperMin.setEnabled(true);
            upperDec.setEnabled(true);
            upperInc.setEnabled(true);
            upperMax.setEnabled(true);
        }
        return false;
    }

    private String getInvertText() {
        return QuestTranslation.translate(
            "questrep.label.reputation.invert",
            QuestTranslation.translate(task.invert ? "gui.yes" : "gui.no"));
    }

    private void sendChanges() {
        NBTTagCompound payload = new NBTTagCompound();
        NBTTagList dataList = new NBTTagList();
        NBTTagCompound entry = new NBTTagCompound();
        entry.setInteger("questID", quest.getID());
        entry.setTag(
            "config",
            quest.getValue()
                .writeToNBT(new NBTTagCompound()));
        dataList.appendTag(entry);
        payload.setTag("data", dataList);
        payload.setInteger("action", 0); // Action: Update data
        QuestingAPI.getAPI(ApiReference.PACKET_SENDER)
            .sendToServer(new QuestingPacket(QUEST_EDIT, payload));
    }

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
        loadTiers();
        initPanel();
    }

    public void rebuildText() {
        currentFaction.setText(
            QuestTranslation.translate("questrep.label.reputation.faction", FactionData.getDisplayName(task.faction)));
        lowerText.setText(
            QuestTranslation.translate(
                "questrep.label.reputation.lower",
                (task.lowerBound != Integer.MIN_VALUE ? FactionData.getTierName(task.faction, task.lowerBound)
                    : StatCollector.translateToLocal("questrep.label.reputation.bound.none"))));
        upperText.setText(
            QuestTranslation.translate(
                "questrep.label.reputation.upper",
                (task.upperBound != Integer.MAX_VALUE ? FactionData.getTierName(task.faction, task.upperBound)
                    : StatCollector.translateToLocal("questrep.label.reputation.bound.none"))));
    }
}
