package org.nbc.csvtospreadsheet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Logger;

import static org.nbc.csvtospreadsheet.SpreadSheetUtils.*;

public class SpreadSheet {
    private static final Logger logger = Logger.getLogger(SpreadSheet.class.getName());

    private final List<List<String>> grid;

    private final Map<Pair, String> cachedExpressions = new HashMap<>();
    private final Set<Pair> blacklisted = new HashSet<>();
    private int cachedWidth = -1;

    public SpreadSheet() {
        this.grid = new ArrayList<>();
    }

    public Map<Pair, String> getCachedExpressions() {
        return cachedExpressions;
    }

    public Set<Pair> getBlacklistedPairs() {
        return blacklisted;
    }


    /** Adds a new column to the grid */
    public void addColumn() {
        grid.add(new ArrayList<>());
    }

    /** Ensures that a column exists at index `colIndex` */
    private void ensureColumnExists(int colIndex) {
        while (grid.size() <= colIndex) {
            addColumn();
        }
    }

    /** Ensures that a row exists within a specific column */
    private void ensureRowExists(int colIndex, int rowIndex) {
        ensureColumnExists(colIndex);
        List<String> column = grid.get(colIndex);
        while (column.size() <= rowIndex) {
            column.add("");
        }
    }

    /**
     * Loads a CSV file into this spreadsheet, populating the grid and caching expressions.
     */
    public void loadCsv(String filePath) throws FileNotFoundException {
        try (Scanner scanner = new Scanner(new File(filePath))) {
            int rowIndex = 0;

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] cells = line.split(",");

                for (int col = 0; col < cells.length; col++) {
                    String cellVal = cells[col].trim();
                    Pair position = new Pair(col, rowIndex);

                    if (ExpressionParser.isExpression(cellVal)) {
                        cachedExpressions.put(position, cellVal);
                    }

                    setCellValue(position, cellVal);
                }
                rowIndex++;
            }
        }
    }

    /**
     * Evaluates all expressions, storing numeric results in the grid.
     * then replaces any "#hl" cells with dash strings.
     */
    public void evaluateAllExpressions() {
        for (Map.Entry<Pair, String> entry : cachedExpressions.entrySet()) {
            Pair location = entry.getKey();

            if (blacklisted.contains(location)) {
                logger.warning("Skipping evaluation: " + location + " is part of a circular dependency.");
                continue;
            }

            try {
                double result = resolveCellValue(location, new HashSet<>());
                setCellValue(location, Double.toString(result));
            } catch (CircularReferenceException e) {
                logger.severe("Circular dependency detected at cell: " + e.getInitialCell());
                blacklisted.add(e.getInitialCell());
            }
        }

        // Compute width once and store it
        this.cachedWidth = computeGlobalWidth();

        // Then replace any "#hl" cells with dashes
        finalizeHorizontalLines();
    }

    /**
     * Scans the grid for "#hl" cells and replaces them with dash strings
     * matching the cachedWidth.
     */
    public void finalizeHorizontalLines() {
        // If cachedWidth is not set, compute it
        if (cachedWidth < 0) {
            cachedWidth = computeGlobalWidth();
        }

        for (int col = 0; col < grid.size(); col++) {
            List<String> columnData = grid.get(col);
            for (int row = 0; row < columnData.size(); row++) {
                String cellVal = columnData.get(row);
                if ("#hl".equals(cellVal)) {
                    columnData.set(row, "-".repeat(cachedWidth));
                }
            }
        }
    }

    /**
     * Recursively evaluates a cell, detecting circular dependencies.
     * Moved from SpreadsheetService (resolveCellValue).
     */
    private double resolveCellValue(Pair location, Set<Pair> referenceChain) throws CircularReferenceException {
        // If we've visited this cell in the current recursion stack → cycle
        if (!referenceChain.add(location)) {
            blacklisted.add(location);
            throw new CircularReferenceException("Circular reference detected at " + location, location);
        }

        // If already a numeric cell, just return its double value
        String cellValue = getCellValue(location);
        if (SpreadSheetUtils.isNumeric(cellValue)) {
            return Double.parseDouble(cellValue);
        }

        // If no expression is cached, treat as 0
        if (!cachedExpressions.containsKey(location)) {
            return 0.0;
        }

        // Parse expression (e.g.  #(sum A1 B2))
        ExpressionInfo exprInfo = ExpressionParser.parseExpression(cachedExpressions.get(location));
        double[] values = new double[exprInfo.getCellRefs().size()];
        boolean canEvaluate = true;

        // Recursively evaluate expressions
        for (int i = 0; i < exprInfo.getCellRefs().size(); i++) {
            Pair ref = exprInfo.getCellRefs().get(i);

            // If blacklisted, do no evaluate.
            if (blacklisted.contains(ref)) {
                logger.warning("Skipping evaluation: " + location + " references blacklisted " + ref);
                canEvaluate = false;
                break;
            }

            values[i] = resolveCellValue(ref, referenceChain);
        }

        referenceChain.remove(location);

        // If references evaluated, compute sum/product
        if (canEvaluate) {
            double result = SpreadSheetUtils.evaluateExpression(exprInfo.getOperation(), values);
            setCellValue(location, Double.toString(result));
            return result;
        } else {
            blacklisted.add(location);
            throw new CircularReferenceException("Evaluation failed for " + location, location);
        }
    }

    /** Sets a cell value (unchanged, but needed for expression results) */
    public void setCellValue(Pair position, String value) {
        ensureRowExists(position.getX(), position.getY());
        grid.get(position.getX()).set(position.getY(), value);
    }

    /** Retrieves the value of a cell  */
    public String getCellValue(Pair position) {
        if (position.getX() >= grid.size() || position.getY() >= grid.get(position.getX()).size()) {
            return "";
        }
        return grid.get(position.getX()).get(position.getY());
    }

    /** Converts (A1, B3) to a Pair  */
    public static Pair convertCellReference(String ref) {
        char colLetter = ref.charAt(0);
        int rowNumber = Integer.parseInt(ref.substring(1)) - 1;
        return new Pair(columnLetterToGridIndex(colLetter), rowNumber);
    }



    public void printGrid() {
        // fallback if expressions were never evaluated
        int width = (cachedWidth >= 0) ? cachedWidth : computeGlobalWidth();

        int maxRows = 0;
        for (List<String> col : grid) {
            maxRows = Math.max(maxRows, col.size());
        }

        for (int row = 0; row < maxRows; row++) {
            StringBuilder sb = new StringBuilder();
            for (int col = 0; col < grid.size(); col++) {
                List<String> columnData = grid.get(col);
                String cellVal = (row < columnData.size()) ? columnData.get(row) : "";
                sb.append("|").append(isNumeric(cellVal)
                        ? rightAlign(cellVal, width)
                        : leftAlign(cellVal, width));
            }
            sb.append("|");
            System.out.println(sb);
        }
    }

    /** Prints the grid in SpreadSheet format to an output file*/
    public void printGridToFile(String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            int width = (cachedWidth >= 0) ? cachedWidth : computeGlobalWidth();

            int maxRows = 0;
            for (List<String> col : grid) {
                maxRows = Math.max(maxRows, col.size());
            }

            for (int row = 0; row < maxRows; row++) {
                StringBuilder sb = new StringBuilder();
                for (int col = 0; col < grid.size(); col++) {
                    List<String> columnData = grid.get(col);
                    String cellVal = (row < columnData.size()) ? columnData.get(row) : "";
                    if (isNumeric(cellVal)) {
                        sb.append("|").append(rightAlign(cellVal, width));
                    } else {
                        sb.append("|").append(leftAlign(cellVal, width));
                    }
                }
                sb.append("|");
                writer.write(sb.toString());
                writer.write(System.lineSeparator());
            }
        }
    }

    /** Helper function for computing the longest text length in the grid*/
    private int computeGlobalWidth() {
        int maxLen = 0;
        for (List<String> column : grid) {
            for (String cellVal : column) {
                maxLen = Math.max(maxLen, cellVal.length());
            }
        }
        return maxLen;
    }


}
