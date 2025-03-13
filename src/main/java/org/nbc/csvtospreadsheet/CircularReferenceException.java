package org.nbc.csvtospreadsheet;

/** Custom exception for handling circular references in spreadsheet expressions. */
public class CircularReferenceException extends Exception {
    private final Pair initialCell;

    public CircularReferenceException(String message, Pair initialCell) {
        super(message);
        this.initialCell = initialCell;
    }

    public Pair getInitialCell() {
        return initialCell;
    }
}
