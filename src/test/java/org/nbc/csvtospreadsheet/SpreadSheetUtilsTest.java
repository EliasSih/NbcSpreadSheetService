package org.nbc.csvtospreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


public class SpreadSheetUtilsTest {

    @Test
    public void isNumeric() {
        assertFalse(SpreadSheetUtils.isNumeric(""), "Empty string should not be numeric");

        assertTrue(SpreadSheetUtils.isNumeric("123"), "'123' should be numeric");
        assertTrue(SpreadSheetUtils.isNumeric("3.14"), "'3.14' should be numeric");
        assertTrue(SpreadSheetUtils.isNumeric("-42.5"), "'-42.5' should be numeric");

        assertFalse(SpreadSheetUtils.isNumeric("ABC"), "'ABC' should not be numeric");
        assertFalse(SpreadSheetUtils.isNumeric("3.14.15"), "'3.14.15' is not numeric");

        assertTrue(SpreadSheetUtils.isNumeric("  12  "), "String with leading/trailing spaces is still numeric");
    }

    @Test
    public void evaluateExpression() {
        double sumResult = SpreadSheetUtils.evaluateExpression(SupportedOperations.SUM, 2.0, 3.0, 4.0);
        assertEquals(9.0, sumResult, 0.0001, "Sum of 2,3,4 should be 9");

        double prodResult = SpreadSheetUtils.evaluateExpression(SupportedOperations.PROD, 2.0, 3.0, 4.0);
        assertEquals(24.0, prodResult, 0.0001, "Product of 2,3,4 should be 24");

        double sumSingle = SpreadSheetUtils.evaluateExpression(SupportedOperations.SUM, 5.0);
        assertEquals(5.0, sumSingle, 0.0001, "Sum of single term 5.0 should be 5.0");

        double prodEmpty = SpreadSheetUtils.evaluateExpression(SupportedOperations.PROD);
        assertEquals(1.0, prodEmpty, 0.0001, "Product of zero terms should be 1.0 by definition");
    }

    @Test
    public void columnLetterToGridIndex() {
        assertEquals(0, SpreadSheetUtils.columnLetterToGridIndex('A'), "'A' => 0");
        assertEquals(25, SpreadSheetUtils.columnLetterToGridIndex('Z'), "'Z' => 25");
        assertEquals(0, SpreadSheetUtils.columnLetterToGridIndex('a'), "'a' => 0");
        assertEquals(25, SpreadSheetUtils.columnLetterToGridIndex('z'), "'z' => 25");
    }

    @Test
    public void columnIndexToLetter() {
        assertEquals("A", SpreadSheetUtils.columnIndexToLetter(0), "0 => A");
        assertEquals("Z", SpreadSheetUtils.columnIndexToLetter(25), "25 => Z");
        assertEquals("AA", SpreadSheetUtils.columnIndexToLetter(26), "26 => AA");
        assertEquals("AB", SpreadSheetUtils.columnIndexToLetter(27), "27 => AB");
        assertEquals("AZ", SpreadSheetUtils.columnIndexToLetter(51), "51 => AZ");
        assertEquals("BA", SpreadSheetUtils.columnIndexToLetter(52), "52 => BA");

        assertThrows(IllegalArgumentException.class,
                () -> SpreadSheetUtils.columnIndexToLetter(-1),
                "Should throw for negative column index");
    }

    @Test
    public void rightAlign() {
        String result = SpreadSheetUtils.rightAlign("ABC", 5);
        assertEquals("  ABC", result, "Should have 2 leading spaces for a total of 5 chars");

        result = SpreadSheetUtils.rightAlign("Hello", 5);
        assertEquals("Hello", result, "No padding if text length equals width");

        result = SpreadSheetUtils.rightAlign("LongText", 3);
        assertEquals("LongText", result, "Should not truncate; returns text");
    }

    @Test
    public void leftAlign() {
        String result = SpreadSheetUtils.leftAlign("ABC", 5);
        assertEquals("ABC  ", result, "Should have 2 trailing spaces for a total of 5 chars");

        result = SpreadSheetUtils.leftAlign("Hello", 5);
        assertEquals("Hello", result, "No padding if text length equals width");

        result = SpreadSheetUtils.leftAlign("LongText", 4);
        assertEquals("LongText", result, "Should not truncate; returns text");
    }
}
