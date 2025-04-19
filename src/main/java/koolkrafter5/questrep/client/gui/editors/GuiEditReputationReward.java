package koolkrafter5.questrep.client.gui.editors;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.network.handlers.NetQuestEdit;
import koolkrafter5.questrep.reputation.FactionData;
import koolkrafter5.questrep.rewards.RewardReputation;

public class GuiEditReputationReward extends GuiScreenCanvas implements IFactionSelectionReceiver {

    private final DBEntry<IQuest> quest;
    private final RewardReputation reward;

    private PanelTextBox currentFaction;

    public GuiEditReputationReward(GuiScreen parent, DBEntry<IQuest> quest, RewardReputation reward) {
        super(parent);
        this.quest = quest;
        this.reward = reward;
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
                StatCollector.translateToLocal("questrep.gui.edit_reputation_reward")).setAlignment(1)
                    .setColor(PresetColor.TEXT_HEADER.getColor()));

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
                    mc.displayGuiScreen(new GuiFactionSelect(GuiEditReputationReward.this, reward.faction));
                }
            });

        PanelTextBox changeText = new PanelTextBox(
            new GuiTransform(GuiAlign.TOP_CENTER, -100, 64, 200, 12, 0),
            QuestTranslation.translate("questrep.gui.change")).setAlignment(1)
                .setColor(PresetColor.TEXT_MAIN.getColor());
        cvBackground.addPanel(changeText);
        PanelTextField<Integer> reputation = new PanelTextField<>(
            new GuiTransform(GuiAlign.TOP_CENTER, -60, 74, 120, 16, 0),
            (reward.amount != 0 ? Integer.toString(reward.amount) : ""),
            FieldFilterNumber.INT);
        reputation.setCallback(val -> { reward.amount = val; });
        cvBackground.addPanel(reputation);

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

        rebuildText();
    }

    @Override
    public void setFaction(String faction) {
        reward.faction = faction;
    }

    @Override
    public void rebuildText() {
        currentFaction.setText(
            QuestTranslation
                .translate("questrep.label.reputation.faction", FactionData.getDisplayName(reward.faction)));
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
        payload.setInteger("action", 0);
        NetQuestEdit.sendEdit(payload);
    }
}
