package org.nbc.csvtospreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class ExpressionInfoTest {

    @Test
    public void GettersReturnValidAttributeValues() {
        List<Pair> pairs = Arrays.asList(new Pair(0, 0, "A1"), new Pair(1, 1, "B2"));
        ExpressionInfo info = new ExpressionInfo(SupportedOperations.SUM, pairs);

        assertEquals(SupportedOperations.SUM, info.getOperation(), "Operation should be SUM");
        assertEquals(pairs, info.getCellRefs(), "Cell references list should match expected");
    }

    @Test
    public void testEqualsAndHashCode() {
        List<Pair> pairs1 = Arrays.asList(new Pair(0, 0, "A1"), new Pair(1, 1, "B2"));
        ExpressionInfo info1 = new ExpressionInfo(SupportedOperations.PROD, pairs1);

        List<Pair> pairs2 = Arrays.asList(new Pair(0, 0, "A1"), new Pair(1, 1, "B2"));
        ExpressionInfo info2 = new ExpressionInfo(SupportedOperations.PROD, pairs2);

        assertEquals(info1, info2, "ExpressionInfo objects with same operation and cell refs should be equal");
        assertEquals(info1.hashCode(), info2.hashCode(), "Hash codes should be equal for equal objects");

        ExpressionInfo info3 = new ExpressionInfo(SupportedOperations.SUM, pairs2);
        assertNotEquals(info1, info3, "ExpressionInfo objects with different operations should not be equal");

        List<Pair> pairs3 = Arrays.asList(new Pair(0, 0, "A1"));
        ExpressionInfo info4 = new ExpressionInfo(SupportedOperations.PROD, pairs3);
        assertNotEquals(info1, info4, "ExpressionInfo objects with different cell references should not be equal");
    }

    @Test
    public void testToString() {
        List<Pair> pairs = Arrays.asList(new Pair(0, 0, "A1"), new Pair(1, 1, "B2"));
        ExpressionInfo info = new ExpressionInfo(SupportedOperations.SUM, pairs);
        String expected = "ExpressionInfo{operation=SUM, cellRefs=[A1, B2]}";
        assertEquals(expected, info.toString(), "toString() output should match expected format");
    }
}
