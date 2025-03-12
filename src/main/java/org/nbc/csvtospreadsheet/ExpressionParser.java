package org.nbc.csvtospreadsheet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.nbc.csvtospreadsheet.SpreadSheetUtils.columnLetterToGridIndex;

// serves as a parser for expressions
public class ExpressionParser {

    private static final String REGEX = "^#\\(\\s*([a-zA-Z]+)\\s+([A-Za-z]\\d+(?:\\s+[A-Za-z]\\d+)*)\\s*\\)$";
    private static final Pattern PATTERN = Pattern.compile(REGEX);
    private static final Logger LOGGER = Logger.getLogger(ExpressionParser.class.getName());

    public static boolean isExpression(String text) {
        return PATTERN.matcher(text).matches();
    }

    /**
     * Parses an expression and resolves cell references into (col, row) Pairs.
     * Returns an ExpressionInfo object containing the operation and resolved cell references.
     */
    public static ExpressionInfo parseExpression(String expression) {
        Matcher matcher = PATTERN.matcher(expression);
        if (!matcher.matches()) {
            LOGGER.warning("Invalid expression format: " + expression);
            return null;
        }

        // Extract operation type e.g. SUM or PROD
        String opStr = matcher.group(1).toUpperCase();
        SupportedOperations operation = parseOperation(opStr);
        if (operation == null) return null;

        // Extract cell references and resolve them to (col, row) Pairs
        String refsStr = matcher.group(2);
        List<Pair> resolvedPairs = resolveCellReferences(refsStr.split("\\s+"));

        LOGGER.fine(() -> String.format("Parsed operation=%s, references=%s", operation, resolvedPairs));

        return new ExpressionInfo(operation, resolvedPairs);
    }

    /**
     * Converts the operations "sum" or "prod" to the matching SupportedOperations enum.
     */
    private static SupportedOperations parseOperation(String opStr) {
        try {
            return SupportedOperations.valueOf(opStr);
        } catch (IllegalArgumentException e) {
            LOGGER.warning("Unsupported operation: " + opStr);
            return null;
        }
    }

    /**
     * Converts a list of cell references (e.g. "A3", "B6") into a list of Pair objects.
     * Each Pair represents the (colIndex, rowIndex) in the grid.
     */
    private static List<Pair>
    resolveCellReferences(String[] cellRefs) {
        List<Pair> cellPairs = new ArrayList<>();

        for (String ref : cellRefs) {
            if (!ref.matches("[A-Za-z]\\d+")) {
                LOGGER.warning("Invalid cell reference: " + ref);
                continue;
            }
            int colIndex = columnLetterToGridIndex(ref.charAt(0));
            int rowIndex = Integer.parseInt(ref.substring(1)) - 1;
            cellPairs.add(new Pair(colIndex, rowIndex));
        }
        return cellPairs;
    }
}
