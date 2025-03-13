package org.nbc.csvtospreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

public class SpreadSheetTest {

    @Test
    public void SetGetCellValue() {
        SpreadSheet sheet = new SpreadSheet();

        sheet.setCellValue(new Pair(0, 0), "Hello");
        String actual = sheet.getCellValue(new Pair(0, 0));

        assertEquals("Hello", actual, "Expected 'Hell' but got '" + actual + "'");

        String outOfBounds = sheet.getCellValue(new Pair(10, 10));
        assertEquals("", outOfBounds, "Expected '' for out-of-bounds but got '" + outOfBounds + "'");
    }

    @Test
    public void ConvertCellReference() {
        Pair p1 = SpreadSheet.convertCellReference("A1");
        assertEquals(0, p1.getX(), "A1 should map to column 0");
        assertEquals(0, p1.getY(), "A1 should map to row 0");

        Pair p2 = SpreadSheet.convertCellReference("B3");
        assertEquals(1, p2.getX(), "B3 should map to column 1");
        assertEquals(2, p2.getY(), "B3 should map to row 2");

        Pair p3 = SpreadSheet.convertCellReference("Z1");
        assertEquals(25, p3.getX(), "Z1 should map to column 25");
        assertEquals(0, p3.getY(), "Z1 should map to row 0");
    }

    @Test
    public void LoadCsvSimple() throws FileNotFoundException {
        File csvFile = new File("test.csv");
        assertTrue(csvFile.exists(), "Test CSV file not found at " + csvFile.getAbsolutePath());

        SpreadSheet sheet = new SpreadSheet();
        sheet.loadCsv(csvFile.getPath());

        String cellA1 = sheet.getCellValue(new Pair(0, 0));
        assertEquals("Values", cellA1, "Expected 'Values' but got '" + cellA1 + "'");

        String cellB1 = sheet.getCellValue(new Pair(1, 0));
        assertEquals("Factor", cellB1, "Expected 'Factor' but got '" + cellB1 + "'");

        String cellA2 = sheet.getCellValue(new Pair(0, 1));
        assertEquals("#hl", cellA2, "Expected '#hl' but got '" + cellA2 + "'");

        String cellB2 = sheet.getCellValue(new Pair(1, 1));
        assertEquals("#hl", cellB2, "Expected '#hl' but got '" + cellB2 + "'");
    }

    @Test
    public void evaluateExpressionsWithNoCircularReferences() {
        SpreadSheet sheet = new SpreadSheet();

        sheet.setCellValue(new Pair(0, 0), "2"); // A1
        sheet.setCellValue(new Pair(0, 1), "3"); // A2

        sheet.setCellValue(new Pair(1, 0), "#(sum A1 A2)"); // B1 references A1 & A2
        sheet.getCachedExpressions().put(new Pair(1, 0), "#(sum A1 A2)");

        sheet.evaluateAllExpressions();

        String result = sheet.getCellValue(new Pair(1, 0));
        assertEquals("5.0", result, "Expected '5.0' but got '" + result + "'");
    }

    @Test
    public void evaluateExpressionsWithCircularReferences() {
        SpreadSheet sheet = new SpreadSheet();

        sheet.setCellValue(new Pair(0, 0), "#(sum A2)");
        sheet.getCachedExpressions().put(new Pair(0, 0), "#(sum A2)");

        sheet.setCellValue(new Pair(0, 1), "#(sum A1)");
        sheet.getCachedExpressions().put(new Pair(0, 1), "#(sum A1)");

        sheet.evaluateAllExpressions();

        boolean isA1Blacklisted = sheet.getBlacklistedPairs().contains(new Pair(0, 0));
        boolean isA2Blacklisted = sheet.getBlacklistedPairs().contains(new Pair(0, 1));
        assertTrue(isA1Blacklisted && isA2Blacklisted, "Expected A1 & A2 to be blacklisted due to circular ref.");
    }

    /**
     * Tests that adding a column via setCellValue (which uses ensureColumnExists)
     * works correctly. Here, we set a cell in a column/row that doesn't exist yet,
     * and verify that the cell is set while an unset cell returns an empty string.
     */
    @Test
    public void AddColumnFunctionality() {
        SpreadSheet sheet = new SpreadSheet();
        // Setting a cell at column index 2, row index 3 should force the grid to create columns and rows.
        sheet.setCellValue(new Pair(2, 3), "Test");
        // Verify that the cell is set correctly.
        assertEquals("Test", sheet.getCellValue(new Pair(2, 3)), "Cell (2,3) should be 'Test'");
        // A cell that has not been set should return an empty string.
        assertEquals("", sheet.getCellValue(new Pair(0, 0)), "Cell (0,0) should be empty");
    }

    /**
     * Tests the printGrid() function by constructing a simple grid,
     * capturing the printed output, and comparing it to the expected formatted output.
     */
    @Test
    public void PrintGrid() {
        SpreadSheet sheet = new SpreadSheet();
        sheet.addColumn();
        sheet.addColumn();

        sheet.setCellValue(new Pair(0, 0), "Hello");
        sheet.setCellValue(new Pair(1, 0), "World");
        sheet.setCellValue(new Pair(0, 1), "Test");
        sheet.setCellValue(new Pair(1, 1), "123");

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(outContent));
            sheet.printGrid();
        } finally {
            System.setOut(originalOut);
        }

        String output = outContent.toString();
        String expectedOutput = "|Hello|World|" + System.lineSeparator()
                + "|Test |  123|" + System.lineSeparator();
        assertEquals(expectedOutput, output, "Printed grid does not match expected output");
    }

    @Test
    public void printGridToFile() throws Exception {
        SpreadSheet sheet = new SpreadSheet();

       sheet.setCellValue(new Pair(0, 0), "Alpha");
        sheet.setCellValue(new Pair(1, 0), "Beta");
        sheet.setCellValue(new Pair(0, 1), "#hl");
        sheet.setCellValue(new Pair(1, 1), "999");

        sheet.finalizeHorizontalLines();

        File tempFile = File.createTempFile("spreadsheet_test", ".txt");
        tempFile.deleteOnExit();

        sheet.printGridToFile(tempFile.getAbsolutePath());

        String expected =
                "|Alpha|Beta |\n" +
                        "|-----|  999|\n";

        StringBuilder sb = new StringBuilder();
        try (Scanner sc = new Scanner(new FileReader(tempFile, StandardCharsets.UTF_8))) {
            while (sc.hasNextLine()) {
                sb.append(sc.nextLine()).append("\n");
            }
        }
        String fileContents = sb.toString();

        assertEquals(expected, fileContents, "File output does not match expected content");
    }

}
