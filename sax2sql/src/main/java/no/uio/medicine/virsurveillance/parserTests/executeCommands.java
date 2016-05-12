/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uio.medicine.virsurveillance.parserTests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.System.exit;
import java.sql.SQLException;
import java.util.ArrayList;
import no.uio.medicine.virsurveillance.DDBB.SQLQueries;
import no.uio.medicine.virsurveillance.datamodels.QueryResult;

/**
 *
 * @author apla
 */
public class executeCommands {

    protected static ArrayList<QueryResult> queryList;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        // TODO code application logic here     
        String action;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        printOperations();

        queryList = new ArrayList<>();

        do {
            System.out.print(">>");
            action = reader.readLine();

            String[] actionPar = action.split(" ");
            if (actionPar.length > 0) {
                switch (actionPar[0].toLowerCase()) {
                    case "exit":
                        break;
                    case "help":
                        printOperations();
                        break;
                    case "clear":
                        cleanScreen();
                        break;
                    case "journalevolution":
                        journalEvolution(action);
                        break;
                    case "virusinformation":
                        virusInformation(action);
                        break;
                    case "ifv":
                        impactFactorVariabilityInfo(action);
                        break;
                    default:
                        System.out.println(actionPar[0] + " not recognized as a valid action");
                        printOperations();
                }
            }

        } while (!action.equalsIgnoreCase("exit"));
        exit(0);
    }

    private static void printOperations() {
        System.out.println("Available Operations (not case sensitive)");
        System.out.println("=========================================");
        System.out.println("Exit\t\t exit program");
        System.out.println("Help\t\t show program options");
        System.out.println("Clear\t\t closes the opened charts");
        System.out.println("virusInformation 'virus1,virus2,virusN'\t\t Shows the impact factor that publications regarding the different virus have achieved along time");
        System.out.println("ifv 'variability1,variability2,variabilityN'\t\t Shows data regarding the variability of the journals impactfactor amongst years. Information can be filtered over a minimum variability requirement");
        System.out.println("journalEvolution 'journalName1,journalName2,journalNameN'\t\t Shows the evolution of a set of journals. Journals separated by commas");

    }

    private static void journalEvolution(String action) throws SQLException, ClassNotFoundException {

        String auxAction = action.toLowerCase();
        auxAction = auxAction.replace("journalevolution", "");
        auxAction = auxAction.replace("'", "");
        String[] journals = auxAction.split(",");

        SQLQueries sqlQuery = new SQLQueries();
        sqlQuery.connect2DB();
        ArrayList<String> journalList = new ArrayList<>();
        for (String j : journals) {
            journalList.add(j);
        }
        QueryResult result = sqlQuery.getJournalEvolution(journalList, "Evolution of journals");
        result.updateCharts();
        queryList.add(result);
    }

    private static void virusInformation(String action) throws SQLException, ClassNotFoundException {

        String auxAction = action.toLowerCase();
        auxAction = auxAction.replace("virusinformation", "");
        auxAction = auxAction.replace("'", "");
        String[] vriuses = auxAction.split(",");

        SQLQueries sqlQuery = new SQLQueries();
        sqlQuery.connect2DB();
        ArrayList<String> virusList = new ArrayList<>();
        for (String topic : vriuses) {
            System.out.println("Results for virus: " + topic);

            QueryResult result0 = sqlQuery.getStatsPerTopicIgnoringIFUnder(topic, 0);
            result0.printResult();
            result0.getXyLineChart().updateChartData();

            queryList.add(result0);
            //QueryResult result = sqlQuery.getStatsPerTopicIgnoringIFUnder(topic,0);
            QueryResult result = sqlQuery.getPublicationCount(topic);
            result.printResult();
            if (result.getXyLineChart() != null) {
                result.getXyLineChart().updateChartData();
            }
            if (result.getStackedChart() != null) {
                result.getStackedChart().updateChartData();
            }
            queryList.add(result);

            QueryResult result2 = sqlQuery.getPublicationCountSelfNormalized(topic);
            result2.printResult();
            if (result2.getXyLineChart() != null) {
                result2.getXyLineChart().updateChartData();
            }
            if (result2.getStackedChart() != null) {
                result2.getStackedChart().updateChartData();
            }
            queryList.add(result2);

            QueryResult result3 = sqlQuery.getRelativePublicationCount(topic);
            result3.printResult();
            if (result3.getXyLineChart() != null) {
                result3.getXyLineChart().updateChartData();
            }
            if (result3.getStackedChart() != null) {
                result3.getStackedChart().updateChartData();
            }
            queryList.add(result3);
        }

    }

    private static void cleanScreen() {
        for (QueryResult qr : queryList) {
            qr.closeCharts();
        }
        queryList = new ArrayList<>();
    }

    private static void impactFactorVariabilityInfo(String action) throws SQLException, ClassNotFoundException {
        SQLQueries sqlQuery = new SQLQueries();
        sqlQuery.connect2DB();

        QueryResult result0 = sqlQuery.getJournalImpactFactors();
        result0.getBoxplotChart().updateChartData();
        queryList.add(result0);
        System.out.println("General Informationi obtained");

        String auxAction = action.toLowerCase();
        auxAction = auxAction.replace("ifv", "");
        auxAction = auxAction.replace("'", "");
        String[] variabilities = auxAction.replace(" ", "").split(",");

        for (String vari : variabilities) {
            boolean isFloat = false;
            float variability=0;
            try {
                variability=Float.parseFloat(vari);
                isFloat = true;
            } catch (Exception e) {
                isFloat = false;
                System.out.println(vari + " is not a float");

            };
            
            

            if (isFloat) {
                for (int i = -1; i < 2; i++) {
                    String auxStr = "variableIfac";
                    if (i == -1) {
                        auxStr = "DecreasingIfac";
                    } else if (i == 1) {
                        auxStr = "IncreasingIFac";
                    }

                    QueryResult result1 = sqlQuery.getJournalWithVariableImpactFactors(variability, i);
                    result1.getBoxplotChart().updateChartData();
                    queryList.add(result1);
                    
                    ArrayList<String> journals1 = new ArrayList<>();
                    for (String journalName : result1.getValues().get("abbreviated_title")) {
                        if (!journalName.equalsIgnoreCase("null") && !journals1.contains(journalName)) {
                            journals1.add(journalName);

                        }
                    }
                    QueryResult resultaux1 = sqlQuery.getJournalEvolution(journals1, auxStr + " evolution of journals with IF var of "+variability);
                    resultaux1.getXyLineChart().updateChartData();
                    queryList.add(resultaux1);

                }
            }

        }

    }

}
