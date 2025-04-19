//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package koolkrafter5.questrep.client.gui.panels.lists;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.minecraft.util.StringUtils;

import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.lists.CanvasSearch;
import koolkrafter5.questrep.reputation.FactionData;

public class CanvasFactionDatabase extends CanvasSearch<String, String> {

    private final int btnId;

    public CanvasFactionDatabase(IGuiRect rect, int buttonId) {
        super(rect);
        this.btnId = buttonId;
    }

    protected Iterator<String> getIterator() {
        List<String> list = new ArrayList<>(FactionData.getAllFactions());
        Collections.sort(list);
        return list.iterator();
    }

    protected void queryMatches(String entry, String query, ArrayDeque<String> results) {
        if (!StringUtils.isNullOrEmpty(entry)) {
            if (entry.toLowerCase()
                .contains(query)
                || FactionData.getDisplayName(entry)
                    .toLowerCase()
                    .contains(query)) {
                results.add(entry);
            }
        }
    }

    protected boolean addResult(String entry, int index, int cachedWidth) {
        if (entry == null) {
            return false;
        } else {
            this.addPanel(
                new PanelButtonStorage<>(
                    new GuiRectangle(0, index * 16, cachedWidth, 16, 0),
                    this.btnId,
                    FactionData.getDisplayName(entry),
                    entry));
            return true;
        }
    }
}
