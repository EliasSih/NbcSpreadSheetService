# README

## What This Service Does
This command-line program reads a CSV file, maps its contents to a grid, and writes the final, formatted data to a file you specify. Certain cells may be standard text, others might be numeric, and some can contain special expressions like `#(sum A1 B2)` or `#(prod A1 B2)`.

## How the CSV Is Processed
- Each line in the CSV file is read, and each comma-delimited element becomes a cell.
- If a cell looks like an expression (for instance, `#(sum A1 B2)`), it is stored separately for later evaluation.
- Once all lines are read, these expressions are evaluated so each reference is replaced by a computed value.
- Each row of the CSV is handled once. As lines are read, cells go straight into the grid. If any cell appears to be an expression (for instance, #(sum A1 B2)), it is added right away to cachedExpressions, keyed by its location. This saves a full scan later on. Once loading finishes, the system can iterate over the stored expressions without searching every cell. This single-pass strategy keeps both memory and CPU use low.

## Recursively Evaluating Expressions
Whenever an expression cell references other cells, the system inspects each reference:
1. If a referenced cell holds a number, that number is returned immediately.
2. If it holds another expression, the code calls itself to evaluate that expression first.
3. This process continues until a numeric result can be determined or a loop is found.

Because of this recursive structure, each expression is resolved only after its dependencies are computed. The approach is flexible and allows multiple layers of references.

## Handling of Circular References
If a cell indirectly points back to itself through other cells, the code recognizes that it has revisited the same location during evaluation. This indicates a loop, so a `CircularReferenceException` is thrown. Any cell found in such a loop is “blacklisted” so it will not be evaluated again. This prevents endless recursion and ensures other cells in the spreadsheet can still be processed.

## How Expressions Are Evaluated
- **Sum**: Adds together the numeric values from each referenced cell.
- **Product**: Multiplies the numeric values from each referenced cell.
- Additional operations can be added in the future by extending the evaluation logic.

## Tests (JUnit) and How to Run Them
All tests rely on JUnit. To compile and run them:
```
mvn clean test
```
If any test fails, you’ll see a build failure. The tests cover reading CSVs, expression parsing, loop detection, and more.

## Using Maven to Run the Service
1. **Compile** the code:
   ```
   mvn clean compile
   ```
2. **Launch** the service (on demand):
   ```
   mvn exec:java -Dexec.mainClass="org.nbc.csvtospreadsheet.SpreadsheetService"
   ```
   Once started, the application prompts for a CSV file path and an output file path. It then reads and processes the CSV data, writing the final layout to your chosen output.

If you prefer the service to run automatically during the packaging phase, add an `<execution>` for the `exec-maven-plugin` in your `pom.xml` bound to `package`. Then simply run:
```
mvn clean package
```
which will build, test, and then execute the service.
