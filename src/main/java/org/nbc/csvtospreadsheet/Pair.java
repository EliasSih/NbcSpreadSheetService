package org.nbc.csvtospreadsheet;

import java.util.Objects;
import java.util.regex.Pattern;

import static org.nbc.csvtospreadsheet.SpreadSheetUtils.columnIndexToLetter;

public class Pair {
    private final int x;  // Column index (0-based)
    private final int y;  // Row index (0-based)
    private final String excelReference; // Stores Excel format (A1, B2), validated if provided

    /** Regex for validating Excel-style references (e.g., A1, AB56, AAA12) */
    private static final Pattern EXCEL_REF_PATTERN = Pattern.compile("^[A-Z]+[1-9][0-9]*$");

    /** Constructor for creating a Pair with an Excel reference */
    public Pair(int x, int y, String excelReference) {
        this.x = x;
        this.y = y;

        if (excelReference != null && !EXCEL_REF_PATTERN.matcher(excelReference).matches()) {
            throw new IllegalArgumentException("Invalid Excel reference format: " + excelReference);
        }

        this.excelReference = excelReference; // Store validated Excel reference
    }

    /** Constructor when no Excel reference is available */
    public Pair(int x, int y) {
        this.x = x;
        this.y = y;
        this.excelReference = null; // Excel reference not provided
    }

    public int getX() { return x; }
    public int getY() { return y; }

    /** Returns the stored Excel reference if available, otherwise converts it */
    public String toExcelReference() {
        return (excelReference != null) ? excelReference : columnIndexToLetter(x) + (y + 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;
        Pair pair = (Pair) o;
        return x == pair.x && y == pair.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return toExcelReference(); // Default to Excel format for logging
    }
}
