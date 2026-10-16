package com.mahghuuuls.jassintegrations.client.quark;

import com.mahghuuuls.jass.api.client.ItemStaminaInfo;
import com.mahghuuuls.jass.api.client.StaminaTooltipEvent;
import com.mahghuuuls.jassintegrations.Tags;
import com.mahghuuuls.jassintegrations.diagnostic.DebugLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Shows an item's JASS values as one row of icons and numbers in Quark's style, while Quark's Visual
 * Stat Display is on and Shift is up. JASS's value lines are replaced by one marker line: it starts
 * with format codes the font draws as nothing and that Quark does not strip, so Quark (which places
 * its own row on the first blank line) never takes it; the spaces after them reserve the row's
 * width. The line then moves up, right below Quark's own row, and the row is drawn on it after the
 * tooltip text. Modifier lines stay where JASS wrote them.
 */
public final class QuarkStatRow implements Runnable {

    /** Two format codes the font skips and Quark keeps: the line is never blank, and it is ours. */
    static final String MARKER = "§z§j";

    static final int BOLT = 0;
    static final int SHIELD = 1;
    static final int SWORD = 2;
    static final int ARROW = 3;
    static final int WEIGHT = 4;

    private static final ResourceLocation ICONS = new ResourceLocation(Tags.MOD_ID, "textures/gui/stat_icons.png");
    private static final int ICON_SIZE = 9;
    private static final int ATLAS_WIDTH = 45;
    /** As Quark: the number starts 12 pixels after its icon, and the next icon 20 pixels after the number. */
    private static final int TEXT_OFFSET = 12;
    private static final int ENTRY_GAP = 20;
    private static final int WHITE = 0xFFFFFF;
    /** JASS's hint line in its Shift-only tooltip mode; a translation key, not JASS API, so a rename would let the row replace the hint. */
    private static final String JASS_HOLD_SHIFT_KEY = "tooltip.jass.hold_shift";

    /** One icon and its number. */
    static final class Entry {
        final int icon;
        final String text;

        Entry(int icon, String text) {
            this.icon = icon;
            this.text = text;
        }
    }

    private final QuarkFeatureProbe probe = new QuarkFeatureProbe();
    private volatile boolean failed;
    /** The row of the tooltip being shown; client thread only. */
    private ItemStack rowStack;
    private List<Entry> rowEntries = Collections.emptyList();

    @Override
    public void run() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onStaminaTooltip(StaminaTooltipEvent event) {
        rowStack = null;
        if (failed || GuiScreen.isShiftKeyDown()) {
            return;
        }
        try {
            ItemStaminaInfo info = event.getInfo();
            List<String> lines = event.getLines();
            int valueLines = valueLineCount(info);
            String hint = TextFormatting.GRAY + I18n.format(JASS_HOLD_SHIFT_KEY);
            if (!replacesValueLines(valueLines, info.getModifiers().size(), lines, hint) || !probe.visualStatDisplayOn()) {
                return;
            }
            List<Entry> entries = entries(info);
            FontRenderer font = fontFor(event.getStack());
            lines.subList(0, valueLines).clear();
            lines.add(0, markerLine(rowWidth(entries, font::getStringWidth), font.getCharWidth(' ')));
            rowStack = event.getStack();
            rowEntries = entries;
        } catch (RuntimeException | LinkageError e) {
            switchOff(e);
        }
    }

    /** After every other handler: moves the row up, right below Quark's own row (or the item name). */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onItemTooltip(ItemTooltipEvent event) {
        if (rowStack == null || event.getItemStack() != rowStack || failed) {
            return;
        }
        try {
            moveBelowTopRows(event.getToolTip());
        } catch (RuntimeException | LinkageError e) {
            switchOff(e);
        }
    }

    @SubscribeEvent
    public void onPostText(RenderTooltipEvent.PostText event) {
        if (rowStack == null || event.getStack() != rowStack || failed) {
            return;
        }
        try {
            int line = markerLineIndex(event.getLines());
            if (line > 0) {
                draw(event.getFontRenderer(), event.getX(), event.getY() + 10 * line + 1);
            }
        } catch (RuntimeException | LinkageError e) {
            switchOff(e);
        }
    }

    private void draw(FontRenderer font, int x, int y) {
        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, 0.0F, mc.getRenderItem().zLevel);
        for (Entry entry : rowEntries) {
            GlStateManager.color(1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(ICONS);
            Gui.drawModalRectWithCustomSizedTexture(x, y, entry.icon * ICON_SIZE, 0, ICON_SIZE, ICON_SIZE, ATLAS_WIDTH, ICON_SIZE);
            font.drawStringWithShadow(entry.text, x + TEXT_OFFSET, y + 1, WHITE);
            x += font.getStringWidth(entry.text) + ENTRY_GAP;
        }
        GlStateManager.popMatrix();
    }

    private void switchOff(Throwable e) {
        failed = true;
        rowStack = null;
        DebugLog.warn("Quark row: switched off after an error: " + e);
    }

    /**
     * Whether JASS's lines are its full value lines followed by its modifier lines, so the value lines
     * can become the row. Any other shape is left as it is: JASS's "Hold Shift" hint (its Shift-only
     * mode), or lines another addon changed.
     */
    static boolean replacesValueLines(int valueLines, int modifierLines, List<String> lines, String jassHint) {
        if (valueLines == 0 || lines.size() != valueLines + modifierLines) {
            return false;
        }
        return !(lines.size() == 1 && lines.get(0).equals(jassHint));
    }

    /**
     * Moves the marker line to just after the space-filled lines that follow the title. Those are the
     * lines Quark reserves for its own rows, so this row sits right below them, or right below the item
     * name when there are none. The marker line itself is never blank, so Quark does not count it.
     */
    static void moveBelowTopRows(List<String> tooltip) {
        int marker = markerLineIndex(tooltip);
        if (marker < 0) {
            return;
        }
        String line = tooltip.remove(marker);
        int position = 1;
        while (position < tooltip.size() && isQuarkRowLine(tooltip.get(position))) {
            position++;
        }
        tooltip.add(Math.min(position, tooltip.size()), line);
    }

    /**
     * A line Quark reserves for one of its rows: spaces only, never empty. An empty line is another
     * mod's separator (Quality Tools adds one before its quality), and the row goes above it.
     */
    private static boolean isQuarkRowLine(String line) {
        String text = TextFormatting.getTextWithoutFormattingCodes(line);
        return text != null && !text.isEmpty() && text.trim().isEmpty();
    }

    /**
     * The index of the marker line after the title, or -1. The marker may follow other format codes:
     * the creative inventory, for one, puts gray in front of every line after the title.
     */
    static int markerLineIndex(List<String> lines) {
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).contains(MARKER)) {
                return i;
            }
        }
        return -1;
    }

    /** The font the tooltip is drawn with, so the reserved width matches the drawn row. */
    private static FontRenderer fontFor(ItemStack stack) {
        FontRenderer font = stack.getItem().getFontRenderer(stack);
        return font != null ? font : Minecraft.getMinecraft().fontRenderer;
    }

    /** How many of JASS's lines are value lines (the rest are modifier lines), as JASS writes them. */
    static int valueLineCount(ItemStaminaInfo info) {
        int count = 0;
        if (!Double.isNaN(info.getSwingCost())) {
            count++;
        }
        if (!Double.isNaN(info.getDrawPerSecond())) {
            count++;
        }
        if (info.isBlocking()) {
            count += 3;
        }
        if (!Double.isNaN(info.getWeight()) || !Double.isNaN(info.getHeldWeight())) {
            count++;
        }
        return count;
    }

    /** The row's icons and numbers, in JASS's line order. */
    static List<Entry> entries(ItemStaminaInfo info) {
        List<Entry> entries = new ArrayList<>();
        if (!Double.isNaN(info.getSwingCost())) {
            entries.add(new Entry(BOLT, number(info.getSwingCost())));
        }
        if (!Double.isNaN(info.getDrawPerSecond())) {
            entries.add(new Entry(BOLT, number(info.getDrawPerSecond()) + "/s"));
        }
        if (info.isBlocking()) {
            entries.add(new Entry(SHIELD, number(info.getStability())));
            entries.add(new Entry(SWORD, number(info.getMeleeShare() * 100.0) + "%"));
            entries.add(new Entry(ARROW, number(info.getProjectileShare() * 100.0) + "%"));
            entries.add(new Entry(BOLT, number(info.getBlockCostPerDamage())));
        }
        if (!Double.isNaN(info.getWeight())) {
            entries.add(new Entry(WEIGHT, (info.isWeightApproximate() ? "~" : "") + number(info.getWeight())));
        } else if (!Double.isNaN(info.getHeldWeight())) {
            entries.add(new Entry(WEIGHT, number(info.getHeldWeight())));
        }
        return entries;
    }

    /** The drawn width of the row: each entry is its icon gap plus number, with a gap between entries. */
    static int rowWidth(List<Entry> entries, ToIntFunction<String> textWidth) {
        int width = 0;
        for (Entry entry : entries) {
            width += TEXT_OFFSET + textWidth.applyAsInt(entry.text);
        }
        return width + (ENTRY_GAP - TEXT_OFFSET) * Math.max(0, entries.size() - 1);
    }

    /** The marker followed by enough spaces to cover {@code width} pixels. */
    static String markerLine(int width, int spaceWidth) {
        StringBuilder line = new StringBuilder(MARKER);
        int step = spaceWidth > 0 ? spaceWidth : 4;
        for (int covered = 0; covered < width; covered += step) {
            line.append(' ');
        }
        return line.toString();
    }

    /** At most two decimals, no trailing zeros: 18, 0.56, 22.4. Same rule as JASS's tooltip numbers (its {@code TooltipLines.number}). */
    static String number(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "?";
        }
        BigDecimal rounded = BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
        return rounded.compareTo(BigDecimal.ZERO) == 0 ? "0" : rounded.toPlainString();
    }
}
