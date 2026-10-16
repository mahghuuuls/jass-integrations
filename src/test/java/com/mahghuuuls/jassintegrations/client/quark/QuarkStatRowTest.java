package com.mahghuuuls.jassintegrations.client.quark;

import com.mahghuuuls.jass.api.client.ItemStaminaInfo;
import net.minecraft.util.text.TextFormatting;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuarkStatRowTest {

    private static String row(ItemStaminaInfo info) {
        return QuarkStatRow.entries(info).stream().map(e -> e.icon + ":" + e.text).collect(Collectors.joining(" "));
    }

    @Test
    void aGreatswordShowsSwingCostAndHeldWeight() {
        ItemStaminaInfo info = new ItemStaminaInfo.Builder().swingCost(18).heldWeight(6).build();
        assertEquals("0:18 4:6", row(info));
        assertEquals(2, QuarkStatRow.valueLineCount(info));
    }

    @Test
    void aTowerShieldShowsStabilitySharesBlockCostAndWeight() {
        ItemStaminaInfo info = new ItemStaminaInfo.Builder().blocking(80, 1.0, 1.0, 0.5625).heldWeight(8).build();
        assertEquals("1:80 2:100% 3:100% 0:0.56 4:8", row(info));
        assertEquals(4, QuarkStatRow.valueLineCount(info));
    }

    @Test
    void aBowShowsItsDrainPerSecond() {
        assertEquals("0:10/s", row(new ItemStaminaInfo.Builder().drawPerSecond(10).build()));
        assertEquals("0:0/s", row(new ItemStaminaInfo.Builder().drawPerSecond(0).build()));
    }

    @Test
    void armorShowsWornWeightAndMarksAnEstimate() {
        assertEquals("4:~4.5", row(new ItemStaminaInfo.Builder().weight(4.5, true).heldWeight(2).build()));
        assertEquals("4:3", row(new ItemStaminaInfo.Builder().weight(3, false).build()));
        assertEquals(1, QuarkStatRow.valueLineCount(new ItemStaminaInfo.Builder().weight(3, false).heldWeight(1).build()));
    }

    @Test
    void modifiersAreNotValueLines() {
        ItemStaminaInfo info = new ItemStaminaInfo.Builder().swingCost(6).modifier("flat_maximum", 15, true).build();
        assertEquals(1, QuarkStatRow.valueLineCount(info));
        assertEquals("0:6", row(info));
        assertEquals(0, QuarkStatRow.valueLineCount(new ItemStaminaInfo.Builder().modifier("flat_maximum", 15, true).build()));
    }

    @Test
    void numbersMatchJassFormatting() {
        assertEquals("18", QuarkStatRow.number(18.0));
        assertEquals("0.56", QuarkStatRow.number(0.5625));
        assertEquals("22.4", QuarkStatRow.number(22.40));
        assertEquals("0", QuarkStatRow.number(0.001));
        assertEquals("?", QuarkStatRow.number(Double.NaN));
    }

    @Test
    void theRowWidthFollowsQuarkSpacing() {
        List<QuarkStatRow.Entry> entries = QuarkStatRow.entries(new ItemStaminaInfo.Builder().swingCost(18).heldWeight(6).build());
        // Each character 6 pixels wide: (12 + 12) + 8 + (12 + 6).
        assertEquals(50, QuarkStatRow.rowWidth(entries, s -> 6 * s.length()));
        assertEquals(0, QuarkStatRow.rowWidth(Collections.emptyList(), s -> 6 * s.length()));
    }

    @Test
    void theMarkerLineIsNeverBlankToQuarkAndCoversTheRow() {
        String line = QuarkStatRow.markerLine(50, 4);
        assertTrue(line.startsWith(QuarkStatRow.MARKER));
        assertEquals(13, line.length() - QuarkStatRow.MARKER.length());
        // Quark places its own row on the first line that is blank once formatting codes are removed.
        assertFalse(TextFormatting.getTextWithoutFormattingCodes(line).trim().isEmpty());
        assertEquals(QuarkStatRow.MARKER.length() + 13, QuarkStatRow.markerLine(50, 0).length());
    }

    @Test
    void theMarkerCodesAreOnesTheFontSkips() {
        // FontRenderer reads a format code from this list; any other character after the section sign draws nothing.
        String fontCodes = "0123456789abcdefklmnor";
        assertEquals(-1, fontCodes.indexOf(QuarkStatRow.MARKER.charAt(1)));
        assertEquals(-1, fontCodes.indexOf(QuarkStatRow.MARKER.charAt(3)));
    }

    private static final String HINT = "§7Hold Shift for Stamina";

    @Test
    void valueLinesBecomeTheRowWhenTheShapeIsJasss() {
        assertTrue(QuarkStatRow.replacesValueLines(2, 0, Arrays.asList("§7Stamina per swing: 18", "§7Weight: 6"), HINT));
        assertTrue(QuarkStatRow.replacesValueLines(1, 1, Arrays.asList("§7Weight: 3", "§9+15 Maximum Stamina"), HINT));
        assertTrue(QuarkStatRow.replacesValueLines(1, 0, Collections.singletonList("§7Weight: 3"), HINT));
    }

    @Test
    void jasssShiftHintAndOtherShapesAreLeftAlone() {
        // JASS's Shift-only mode sends just the hint; an item with one value line has the same count.
        assertFalse(QuarkStatRow.replacesValueLines(1, 0, Collections.singletonList(HINT), HINT));
        assertFalse(QuarkStatRow.replacesValueLines(2, 0, Collections.singletonList(HINT), HINT));
        // Another addon added a line.
        assertFalse(QuarkStatRow.replacesValueLines(1, 0, Arrays.asList("§7Weight: 3", "extra"), HINT));
        // Only modifier lines: nothing to draw.
        assertFalse(QuarkStatRow.replacesValueLines(0, 1, Collections.singletonList("§9+15 Maximum Stamina"), HINT));
    }

    @Test
    void aShieldWithWeightAndModifiersCountsOnlyItsValueLines() {
        ItemStaminaInfo info = new ItemStaminaInfo.Builder().blocking(50, 0.8, 1.0, 1.0).heldWeight(4)
                .modifier("flat_maximum", 10, true).modifier("regeneration_reduction", 0.2, false).build();
        assertEquals(4, QuarkStatRow.valueLineCount(info));
        assertEquals("1:50 2:80% 3:100% 0:1 4:4", row(info));
    }
}
