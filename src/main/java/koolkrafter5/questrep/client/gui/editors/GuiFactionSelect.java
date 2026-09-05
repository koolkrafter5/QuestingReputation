package koolkrafter5.questrep.client.gui.editors;

import java.util.function.Consumer;

import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.IPanelButton;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.controls.PanelTextField;
import betterquesting.api2.client.gui.controls.filters.FieldFilterString;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.PanelEvent;
import betterquesting.api2.client.gui.events.types.PEventButton;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelLine;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetLine;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.utils.QuestTranslation;
import koolkrafter5.questrep.QuestingReputation;
import koolkrafter5.questrep.client.gui.panels.lists.CanvasFactionDatabase;
import koolkrafter5.questrep.reputation.FactionData;

public class GuiFactionSelect extends GuiScreenCanvas {

    private final IFactionSelectionReceiver parent;
    private String faction;
    CanvasEmpty factionInfo;

    public GuiFactionSelect(IFactionSelectionReceiver parent, String faction) {
        super((GuiScreen) parent);
        this.parent = parent;
        this.faction = faction;
        this.setVolatile(true);
    }

    public void initPanel() {
        super.initPanel();
        Keyboard.enableRepeatEvents(true);
        ButtonConsumer buttonConsumer = new ButtonConsumer();
        PEventBroadcaster.INSTANCE.register(buttonConsumer, PEventButton.class);

        CanvasTextured cvBackground = new CanvasTextured(
            new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0),
            PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);

        // Done button
        cvBackground
            .addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0), -1, "Done"));
        PanelTextBox txTitle = (new PanelTextBox(
            new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 16, 0, -32), 0),
            QuestTranslation.translate("Select Faction"))).setAlignment(1);
        txTitle.setColor(PresetColor.TEXT_HEADER.getColor());
        cvBackground.addPanel(txTitle);

        // Right Panel (Faction Search)
        CanvasEmpty factionSearch = new CanvasEmpty(
            new GuiTransform(GuiAlign.HALF_RIGHT, new GuiPadding(8, 32, 16, 32), 0));
        cvBackground.addPanel(factionSearch);
        CanvasFactionDatabase factionDatabase = new CanvasFactionDatabase(
            new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 16, 8, 0), 0),
            1);
        factionSearch.addPanel(factionDatabase);
        PanelTextField<String> searchBox = new PanelTextField<>(
            new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 0, 8, -16), 0),
            "",
            FieldFilterString.INSTANCE);
        searchBox.setCallback(factionDatabase::setSearchFilter)
            .setWatermark("Search...");
        factionSearch.addPanel(searchBox);
        PanelVScrollBar scEdit = new PanelVScrollBar(
            new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-8, 16, 0, 0), 0));
        factionDatabase.setScrollDriverY(scEdit);
        factionSearch.addPanel(scEdit);

        // Left Panel (Faction Info)
        factionInfo = new CanvasEmpty(new GuiTransform(GuiAlign.HALF_LEFT, new GuiPadding(8, 32, 16, 32), 0));
        cvBackground.addPanel(factionInfo);
        PanelVScrollBar scFaction = new PanelVScrollBar(
            new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-8, 16, 0, 0), 0));
        factionInfo.addPanel(scFaction);

        PanelTextBox soon = new PanelTextBox(
            new GuiTransform(GuiAlign.MID_CENTER, -100, 0, 200, 12, 0),
            "Visual editor coming soon! Edit factions in config/betterquesting/factions.json for now.",
            true).setAlignment(1)
                .setColor(PresetColor.TEXT_MAIN.getColor());
        factionInfo.addPanel(soon);

        factionInfo.setEnabled(
            FactionData.getAllFactions()
                .contains(faction));

        // Decorative Lines
        IGuiRect ls0 = new GuiTransform(GuiAlign.TOP_CENTER, 0, 32, 0, 0, 0);
        ls0.setParent(cvBackground.getTransform());
        IGuiRect le0 = new GuiTransform(GuiAlign.BOTTOM_CENTER, 0, -32, 0, 0, 0);
        le0.setParent(cvBackground.getTransform());
        PanelLine paLine0 = new PanelLine(
            ls0,
            le0,
            PresetLine.GUI_DIVIDER.getLine(),
            1,
            PresetColor.GUI_DIVIDER.getColor(),
            1);
        cvBackground.addPanel(paLine0);
    }

    private class ButtonConsumer implements Consumer<PanelEvent> {

        @Override
        public void accept(PanelEvent event) {
            if (event instanceof PEventButton buttonEvt) {
                IPanelButton btn = buttonEvt.getButton();
                if (btn.getButtonID() == -1) {
                    try {
                        parent.setFaction(faction);
                        parent.rebuildText();
                    } catch (Exception e) {
                        QuestingReputation.LOG.error("Unable to return faction selection!", e);
                    }
                    mc.displayGuiScreen((GuiScreen) parent);
                } else if (btn.getButtonID() == 1 && btn instanceof PanelButtonStorage pbs) {
                    Object f = pbs.getStoredValue();
                    if (f instanceof String s) {
                        faction = s;
                        factionInfo.setEnabled(true);
                        // this.factionInfo = this.selFaction; Set faction info panel here
                    }
                }
            }
        }
    }
}
