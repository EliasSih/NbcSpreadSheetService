package org.nbc.csvtospreadsheet;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PairTest {

    @Test
    public void testPairWithoutExcelReference() {
        Pair p = new Pair(0, 0);
        assertEquals(0, p.getX(), "X coordinate should be 0");
        assertEquals(0, p.getY(), "Y coordinate should be 0");
        assertEquals("A1", p.toExcelReference(), "Expected computed Excel reference to be 'A1'");
    }

    @Test
    public void testPairWithValidExcelReference() {
        Pair p = new Pair(2, 3, "C4");
        assertEquals(2, p.getX(), "X coordinate should be 2");
        assertEquals(3, p.getY(), "Y coordinate should be 3");
        assertEquals("C4", p.toExcelReference(), "Expected Excel reference to be 'C4'");
    }

    @Test
    public void testPairEqualsAndHashCode() {
        Pair p1 = new Pair(1, 1);
        Pair p2 = new Pair(1, 1, "B2");
        assertEquals(p1, p2, "Pairs with the same coordinates should be equal");
        assertEquals(p1.hashCode(), p2.hashCode(), "Pairs with the same coordinates should have the same hashCode");

        Pair p3 = new Pair(2, 2, "C3");
        assertNotEquals(p1, p3, "Pairs with different coordinates should not be equal");
    }

    @Test
    public void testInvalidExcelReferenceThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Pair(0, 0, "invalid");
        });
        String expectedMessage = "Invalid Excel reference format";
        assertTrue(exception.getMessage().contains(expectedMessage),
                "Exception message should contain '" + expectedMessage + "'");
    }

    @Test
    public void testToStringUsesExcelReference() {
        Pair p1 = new Pair(3, 4);
        assertEquals("D5", p1.toString(), "toString() should return computed Excel reference 'D5'");
        Pair p2 = new Pair(3, 4, "X9");
        assertEquals("X9", p2.toString(), "toString() should return the provided Excel reference 'X9'");
    }
}
