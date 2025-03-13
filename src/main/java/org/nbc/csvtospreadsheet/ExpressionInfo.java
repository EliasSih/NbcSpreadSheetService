package org.nbc.csvtospreadsheet;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This class encapsulates information about an expression in the spreadsheet.
 * It stores which operation is used (sum or product)
 * and which cells the expression refers to e.g #(sum A3 B4) refers to cell A3 and B4.
 */
public class ExpressionInfo {
    /**
     * The operation that will be applied to the cells
     * example, sum or product.
     */
    private final SupportedOperations operation;

    /**
     * The list of cell locations involved in the expression.
     * Each cell is represented by a Pair indicating its position.
     */
    private final List<Pair> cellRefs;

    /**
     * Creates an ExpressionInfo with a specific operation
     * and the cells on which that operation will act.
     * @param operation The operation type (e.g., sum, product).
     * @param cellRefs  The cells to be included in this operation.
     */
    public ExpressionInfo(SupportedOperations operation, List<Pair> cellRefs) {
        this.operation = operation;
        this.cellRefs = cellRefs;
    }

    /**
     * Gets the operation that will be performed.
     * @return The type of operation (sum or product).
     */
    public SupportedOperations getOperation() {
        return operation;
    }

    /**
     * Gets the list of cell positions involved in this expression.
     *
     * @return The list of cell references as Pair objects.
     */
    public List<Pair> getCellRefs() {
        return cellRefs;
    }

    /**
     * Checks if this object is the same as another one, based on
     * the operation and the list of cell references.
     *
     * @param o Another object to compare.
     * @return True if both objects have the same operation and cell references.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpressionInfo)) return false;
        ExpressionInfo other = (ExpressionInfo) o;
        return operation == other.operation && Objects.equals(cellRefs, other.cellRefs);
    }

    /**
     * Generates a numeric code that represents this object's state.
     * @return A hash code based on the operation and cell references.
     */
    @Override
    public int hashCode() {
        return Objects.hash(operation, cellRefs);
    }

    /**
     * Creates a readable string that describes this expression.
     * It includes the operation type and the cells it affects.
     * @return A string describing the operation and its cell references.
     */
    @Override
    public String toString() {
        String cellRefsStr = cellRefs.stream()
                .map(Pair::toString)
                .collect(Collectors.joining(", "));
        return String.format("ExpressionInfo{operation=%s, cellRefs=[%s]}", operation, cellRefsStr);
    }
}
