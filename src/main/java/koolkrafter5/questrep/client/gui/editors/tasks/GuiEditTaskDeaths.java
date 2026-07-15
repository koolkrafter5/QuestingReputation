package koolkrafter5.questrep.client.gui.editors.tasks;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;

import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.PanelTextField;
import betterquesting.api2.client.gui.controls.filters.FieldFilterNumber;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.content.PanelLine;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetLine;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.network.handlers.NetQuestEdit;
import koolkrafter5.questrep.tasks.TaskDeaths;

public class GuiEditTaskDeaths extends GuiScreenCanvas {

    private final Map.Entry<UUID, IQuest> quest;
    private final TaskDeaths task;
    private static final ResourceLocation QUEST_EDIT = new ResourceLocation("betterquesting:quest_edit");

    public GuiEditTaskDeaths(GuiScreen parent, Map.Entry<UUID, IQuest> quest, TaskDeaths task) {
        super(parent);
        this.quest = quest;
        this.task = task;
        this.setVolatile(true);
    }

    public GuiEditTaskDeaths getScreenRef() {
        return this;
    }

    @Override
    public void initPanel() {
        super.initPanel();
        Keyboard.enableRepeatEvents(true);

        // Background
        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);

        // TitleText
        cvBackground.addPanel(
            new PanelTextBox(
                new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(16, 16, 16, -32), 0),
                QuestTranslation.translate("questrep.title.edit_deaths_task")).setAlignment(1)
                    .setColor(PresetColor.TEXT_HEADER.getColor()));

        // Done Button
        cvBackground.addPanel(
            new PanelButton(
                new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0),
                -1,
                QuestTranslation.translate("gui.done")) {

                @Override
                public void onButtonClick() {
                    sendChanges();
                    mc.displayGuiScreen(parent);
                }
            });

        int width = cvBackground.getTransform()
            .getWidth() / 2;

        cvBackground.addPanel(
            new PanelTextBox(
                new GuiTransform(GuiAlign.TOP_LEFT, width - 100, 38, 96, 12, 0),
                QuestTranslation.translate("questrep.label.deaths")).setAlignment(2)
                    .setColor(PresetColor.TEXT_MAIN.getColor()));
        cvBackground.addPanel(
            new PanelTextField<>(
                new GuiTransform(GuiAlign.TOP_LEFT, width, 32, width - 16, 16, 0),
                Integer.toString(task.target),
                FieldFilterNumber.INT).setCallback(value -> task.target = value));

        // region Decorative Elements
        // Top Decorative Line
        IGuiRect ls0 = new GuiTransform(GuiAlign.TOP_LEFT, 16, 32, 0, 0, 0);
        ls0.setParent(cvBackground.getTransform());
        IGuiRect rs0 = new GuiTransform(GuiAlign.TOP_RIGHT, -16, 32, 0, 0, 0);
        rs0.setParent(cvBackground.getTransform());
        PanelLine plTop = new PanelLine(
            ls0,
            rs0,
            PresetLine.GUI_DIVIDER.getLine(),
            1,
            PresetColor.GUI_DIVIDER.getColor(),
            -1);
        cvBackground.addPanel(plTop);

        // Bottom Decorative Line
        IGuiRect ls1 = new GuiTransform(GuiAlign.BOTTOM_LEFT, 16, -32, 0, 0, 0);
        ls1.setParent(cvBackground.getTransform());
        IGuiRect rs1 = new GuiTransform(GuiAlign.BOTTOM_RIGHT, -16, -32, 0, 0, 0);
        rs1.setParent(cvBackground.getTransform());
        PanelLine plBottom = new PanelLine(
            ls1,
            rs1,
            PresetLine.GUI_DIVIDER.getLine(),
            1,
            PresetColor.GUI_DIVIDER.getColor(),
            -1);
        cvBackground.addPanel(plBottom);
        // endregion
    }

    // private void sendChanges() {
    // NBTTagCompound base = new NBTTagCompound();
    // base.setTag("config", quest.writeToNBT(new NBTTagCompound()));
    // base.setTag("progress", quest.writeProgressToNBT(new NBTTagCompound(), null));
    // NBTTagCompound tags = new NBTTagCompound();
    // tags.setInteger("action", EnumPacketAction.EDIT.ordinal());
    // tags.setInteger("questID", QuestingAPI.getAPI(ApiReference.QUEST_DB).getID(quest));
    // tags.setTag("data", base);
    // QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToServer(new QuestingPacket(QUEST_EDIT, tags));
    // }
    //
    // private void sendChanges() {
    // NBTTagCompound entry = new NBTTagCompound();
    // NBTTagCompound payload = new NBTTagCompound();
    //
    // entry.setTag("progress", quest.writeProgressToNBT(new NBTTagCompound(), null));
    //
    // payload.setInteger("questID", QuestingAPI.getAPI(ApiReference.QUEST_DB).getID(quest));
    // entry.setTag("config", quest.writeToNBT(new NBTTagCompound()));
    //
    // payload.setTag("data", entry);
    // payload.setInteger("action", EnumPacketAction.EDIT.ordinal());
    //
    // QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToServer(new QuestingPacket(QUEST_EDIT, payload));
    // }

    private void sendChanges() {
        NetQuestEdit.requestEdit(Collections.singletonMap(quest.getKey(), quest.getValue()));
    }
}
