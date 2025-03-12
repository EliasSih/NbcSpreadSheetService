package org.nbc.csvtospreadsheet;

import static org.junit.jupiter.api.Assertions.*;
import static org.nbc.csvtospreadsheet.SpreadSheetUtils.columnLetterToGridIndex;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class ExpressionParserTest {

    @Test
    public void isExpressionWithValidExpression() {
        assertTrue(ExpressionParser.isExpression("#(sum A1 B2)"), "Expression '#(sum A1 B2)' should be valid");
        assertTrue(ExpressionParser.isExpression("#(prod A3 B10)"), "Expression '#(prod A3 B10)' should be valid");
        assertTrue(ExpressionParser.isExpression("#( sum   A9 B4 C12 )"), "Expression with extra spaces should be valid");
    }

    @Test
    public void isExpressionWithInvalidExpression() {
        assertFalse(ExpressionParser.isExpression("#(sum )"), "Expression with missing cell refs should be invalid");
        assertFalse(ExpressionParser.isExpression("#(sumA1)"), "Expression with incorrect format should be invalid");
    }

    @Test
    public void parseExpressionWithValidSum() {
        ExpressionInfo info = ExpressionParser.parseExpression("#(sum A1 B2)");
        assertNotNull(info, "Parsing should succeed for a valid expression");
        assertEquals(SupportedOperations.SUM, info.getOperation(), "Operation should be SUM");

        List<Pair> expectedPairs = new ArrayList<>();

        expectedPairs.add(new Pair(columnLetterToGridIndex('A'), 0));

        expectedPairs.add(new Pair(columnLetterToGridIndex('B'), 1));
        assertEquals(expectedPairs, info.getCellRefs(), "Parsed cell references do not match expected");
    }

    @Test
    public void parseExpressionWithValidProd() {
        ExpressionInfo info = ExpressionParser.parseExpression("#(prod A3 B10)");
        assertNotNull(info, "Parsing should succeed for a valid product expression");
        assertEquals(SupportedOperations.PROD, info.getOperation(), "Operation should be PROD");

        List<Pair> expectedPairs = new ArrayList<>();
        expectedPairs.add(new Pair(columnLetterToGridIndex('A'), 2));
        expectedPairs.add(new Pair(columnLetterToGridIndex('B'), 9));
        assertEquals(expectedPairs, info.getCellRefs(), "Parsed cell references do not match expected");
    }

    @Test
    public void parseExpressionWithValidExtraSpaces() {
        ExpressionInfo info = ExpressionParser.parseExpression("#( sum   A9 B4 C12 )");
        assertNotNull(info, "Parsing should succeed for expression with extra spaces");
        assertEquals(SupportedOperations.SUM, info.getOperation(), "Operation should be SUM");

        List<Pair> expectedPairs = new ArrayList<>();
        expectedPairs.add(new Pair(columnLetterToGridIndex('A'), 8));
        expectedPairs.add(new Pair(columnLetterToGridIndex('B'), 3));
        expectedPairs.add(new Pair(columnLetterToGridIndex('C'), 11));
        assertEquals(expectedPairs, info.getCellRefs(), "Parsed cell references with extra spaces do not match expected");
    }

    @Test
    public void parseExpressionWithUnsupportedOperation() {
        ExpressionInfo info = ExpressionParser.parseExpression("#(multiply A1 A2)");
        assertNull(info, "Parsing should fail for an unsupported operation");
    }

    @Test
    public void parseExpressionWithInvalidNoRefs() {
        ExpressionInfo info = ExpressionParser.parseExpression("#(sum )");
        assertNull(info, "Parsing should fail if cell references are missing");
    }

    @Test
    public void parseExpressionWithInvalidFormat() {
        ExpressionInfo info = ExpressionParser.parseExpression("#(sumA1)");
        assertNull(info, "Parsing should fail for an expression with incorrect format");
    }
}
