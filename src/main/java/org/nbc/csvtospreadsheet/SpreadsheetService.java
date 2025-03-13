package org.nbc.csvtospreadsheet;

import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Facilitates an interactive console-based routine.
 * the routine runs until the user decides to stop. Upon receiving a CSV file
 * the csv data is read into a SpreadSheet object and the expressions in the csv are evaluated.
 * The output is then written to the specified output file.
 * Entering "exit" at either prompt ends the routine.
 */
public class SpreadsheetService {
    private static final Logger logger = Logger.getLogger("org.nbc.csvtospreadsheet");

    /**
     * Acts as the main entry point for the console-driven application.
     * It continually prompts for a CSV file path and a destination file
     * until the user types "exit." The specified CSV is then loaded,
     * evaluated, and the results are written to the indicated output file.
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Service started. Type 'exit' to quit at any time.");

        while (true) {
            System.out.println("\nEnter CSV path (or 'exit'):");
            String inputCsv = sc.nextLine().trim();
            if ("exit".equalsIgnoreCase(inputCsv)) {
                break;
            }

            System.out.println("Enter output file path:");
            String outputFile = sc.nextLine().trim();
            if ("exit".equalsIgnoreCase(outputFile)) {
                break;
            }

            SpreadSheet spreadSheet = new SpreadSheet();

            try {
                spreadSheet.loadCsv(inputCsv);
            } catch (FileNotFoundException e) {
                logger.severe("CSV file not found: " + inputCsv);
                continue;
            }

            spreadSheet.evaluateAllExpressions();

            try {
                spreadSheet.printGridToFile(outputFile);
                logger.info("Output written to " + outputFile);
            } catch (Exception e) {
                logger.severe("Error writing output: " + e.getMessage());
            }
        }

        sc.close();
        logger.info("Service stopped.");
    }
}
