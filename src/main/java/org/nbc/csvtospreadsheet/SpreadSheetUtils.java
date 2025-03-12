package org.nbc.csvtospreadsheet;

import java.util.Arrays;

public class SpreadSheetUtils {

    public static boolean isNumeric(String cellVal) {
        if (cellVal.isEmpty()) return false;
        try {
            Double.parseDouble(cellVal);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Processes the provided terms based on the chosen operation.
     * sum and product are supported currently, but can be extended for other operations.
     * @param operation The action to perform e.g. SUM, PRODUCT.
     * @param terms     One or more numeric values that will be used by the operation.
     * @return The numeric result after applying the operation to all terms.
     */
    public static double evaluateExpression(SupportedOperations operation, double... terms) {
        return switch (operation) {
            case SUM -> Arrays.stream(terms).sum();
            case PROD -> Arrays.stream(terms).reduce(1, (a, b) -> a * b);
            // We will add more cases here our support for other operations increase
            default -> throw new UnsupportedOperationException("Operation not supported: " + operation);
        };
    }

    public static int columnLetterToGridIndex(char columnLetter) {
        return Character.toUpperCase(columnLetter) - 'A';
    }

    public static String columnIndexToLetter(int col) {
        if (col < 0) {
            throw new IllegalArgumentException("Column index cannot be negative");
        }

        StringBuilder columnName = new StringBuilder();
        while (col >= 0) {
            int remainder = col % 26;
            columnName.append((char) ('A' + remainder));  // Direct ASCII conversion
            col = (col / 26) - 1;
        }

        return columnName.reverse().toString(); // Reverse once at the end (more efficient)
    }

    /** Right-aligns text in a text field of width n */
    public static String rightAlign(String text, int width) {
        if (text.length() >= width) return text;
        return " ".repeat(width - text.length()) + text;
    }

    /** Left-aligns text in a text field of width n  */
    public static String leftAlign(String text, int width) {
        if (text.length() >= width) return text;
        return text + " ".repeat(width - text.length());
    }
}
