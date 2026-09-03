package koolkrafter5.questrep.client.gui.widgets;

import java.util.Collections;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import net.minecraft.util.StatCollector;

import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.PanelTextField;
import betterquesting.api2.client.gui.controls.filters.FieldFilterNumber;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import koolkrafter5.questrep.reputation.ReputationTier;

public final class BoundSelector extends CanvasEmpty {

    private final Supplier<List<ReputationTier>> tiers;
    public int tierIndex;
    public boolean textUpdate;
    public PanelTextField<Integer> field;
    public PanelTextBox label;
    public PanelButton min;
    public PanelButton dec;
    public PanelButton inc;
    public PanelButton max;
    private final IntSupplier getter;
    private final IntConsumer setter;
    private final Runnable onChange;
    private final int minIndex, maxIndex;
    private final boolean isLower;

    public BoundSelector(IGuiRect rect, Supplier<List<ReputationTier>> tiers, IntSupplier getter, IntConsumer setter,
        Runnable onChange, int minIndex, int maxIndex, boolean isLower) {
        super(rect);
        this.getter = getter;
        this.setter = setter;
        this.tiers = tiers;
        this.onChange = onChange;
        this.minIndex = minIndex;
        this.maxIndex = maxIndex;
        this.isLower = isLower;
    }

    @Override
    public void initPanel() {
        super.initPanel();
        label = new PanelTextBox(new GuiTransform(GuiAlign.FULL_BOX, -100, 0, 200, 12, 0), "").setAlignment(1)
            .setColor(PresetColor.TEXT_MAIN.getColor());
        field = new PanelTextField<>(
            new GuiTransform(GuiAlign.FULL_BOX, -60, 10, 120, 16, 0),
            getFieldVal(getter.getAsInt()),
            FieldFilterNumber.INT).setCallback(val -> {
                setter.accept(val);
                textUpdate = true;
                setIndex();
                onChange.run();
            });
        min = new PanelButton(new GuiTransform(GuiAlign.FULL_BOX, -100, 10, 20, 16, 0), 1, isLower ? "x" : "<<") {

            @Override
            public void onButtonClick() {
                if (tiers.get()
                    .isEmpty()) {
                    return;
                }
                textUpdate = false;
                tierIndex = minIndex;
                updateBound();
            }
        };
        min.setTooltip(
            Collections.singletonList(
                StatCollector.translateToLocal(
                    isLower ? "questrep.button.reputation.remove" : "questrep.button.reputation.lowest")));
        dec = new PanelButton(new GuiTransform(GuiAlign.FULL_BOX, -80, 10, 20, 16, 0), 0, "<") {

            @Override
            public void onButtonClick() {
                if (tiers.get()
                    .isEmpty()) {
                    return;
                }
                if (textUpdate) {
                    if (tiers.get()
                        .getFirst()
                        .value() > getter.getAsInt()) {
                        tierIndex = 0;
                    } else if (tiers.get()
                        .getLast()
                        .value() < getter.getAsInt()) {
                            tierIndex = tiers.get()
                                .size();
                        } else {
                            for (int i = 0; i < tiers.get()
                                .size(); i++) {
                                if (tiers.get()
                                    .get(i)
                                    .value() >= getter.getAsInt()) {
                                    tierIndex = i;
                                    break;
                                }
                            }
                        }
                    textUpdate = false;
                }
                tierIndex = tierIndex - 1;
                updateBound();
            }
        };
        dec.setTooltip(
            Collections.singletonList(StatCollector.translateToLocal("questrep.button.reputation.previous")));
        inc = new PanelButton(new GuiTransform(GuiAlign.FULL_BOX, 60, 10, 20, 16, 0), 1, ">") {

            @Override
            public void onButtonClick() {
                if (tiers.get()
                    .isEmpty()) {
                    return;
                }
                if (textUpdate) {
                    if (tiers.get()
                        .getFirst()
                        .value() > getter.getAsInt()) {
                        tierIndex = -1;
                    } else if (tiers.get()
                        .getLast()
                        .value() < getter.getAsInt()) {
                            tierIndex = tiers.get()
                                .size();
                        } else {
                            for (int i = 0; i < tiers.get()
                                .size(); i++) {
                                if (tiers.get()
                                    .get(i)
                                    .value() > getter.getAsInt()) {
                                    tierIndex = i - 1;
                                    break;
                                }
                            }
                        }
                    textUpdate = false;
                }
                tierIndex = tierIndex + 1;
                updateBound();
            }
        };
        inc.setTooltip(Collections.singletonList(StatCollector.translateToLocal("questrep.button.reputation.next")));
        max = new PanelButton(new GuiTransform(GuiAlign.FULL_BOX, 80, 10, 20, 16, 0), 1, isLower ? ">>" : "x") {

            @Override
            public void onButtonClick() {
                if (tiers.get()
                    .isEmpty()) {
                    return;
                }
                tierIndex = maxIndex;
                textUpdate = false;
                updateBound();
            }
        };
        max.setTooltip(
            Collections.singletonList(
                StatCollector.translateToLocal(
                    isLower ? "questrep.button.reputation.highest" : "questrep.button.reputation.remove")));
        this.addPanel(field);
        this.addPanel(label);
        this.addPanel(min);
        this.addPanel(dec);
        this.addPanel(inc);
        this.addPanel(max);
    }

    private String getFieldVal(int fieldVal) {
        if (isLower && fieldVal == Integer.MIN_VALUE || !isLower && fieldVal == Integer.MAX_VALUE) return "";
        return Integer.toString(fieldVal);
    }

    public void setIndex() {
        int val = getter.getAsInt();
        List<ReputationTier> tiers = this.tiers.get();
        if (tiers.getFirst()
            .value() > val) {
            tierIndex = minIndex;
        } else if (tiers.getLast()
            .value() < val) {
                tierIndex = maxIndex;
            } else {
                for (int i = 0; i < tiers.size(); i++) {
                    if (tiers.get(i)
                        .value() >= val) {
                        tierIndex = i;
                        break;
                    }
                }
            }
        updateBound();
    }

    private void updateBound() {
        if (tiers.get()
            .isEmpty()) return;
        setButtons();
        if (textUpdate) {
            setter.accept(field.getValue());
            onChange.run();
            return;
        }
        if (isLower && tierIndex <= minIndex) {
            setter.accept(Integer.MIN_VALUE);
            field.setText("");
            onChange.run();
            return;
        } else if (!isLower && tierIndex >= maxIndex) {
            setter.accept(Integer.MAX_VALUE);
            field.setText("");
            onChange.run();
            return;
        }
        setter.accept(
            tiers.get()
                .get(tierIndex)
                .value());
        field.setText(Integer.toString(getter.getAsInt()));
        onChange.run();
    }

    public void setButtons() {
        min.setEnabled(tierIndex > minIndex);
        max.setEnabled(tierIndex < maxIndex);

        dec.setEnabled(min.isEnabled());
        inc.setEnabled(max.isEnabled());
        onChange.run();
    }

    @Override
    public void drawPanel(int mx, int my, float partialTick) {
        super.drawPanel(mx, my, partialTick);
    }

}
