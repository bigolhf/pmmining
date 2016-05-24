/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools Templates
 * and open the template in the editor.
 */
package no.uio.medicine.virsurveillance.parserTests;

import no.uio.medicine.virsurveillance.parsers.SaxXMLProcess;
import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import no.uio.medicine.virsurveillance.DDBB.SQLManagement;
import no.uio.medicine.virsurveillance.DDBB.SQLQueries;
import no.uio.medicine.virsurveillance.datamodels.PubmedArticle;
import no.uio.medicine.virsurveillance.datamodels.PubmedAuthor;
import no.uio.medicine.virsurveillance.datamodels.QueryResult;
import no.uio.medicine.virsurveillance.datamodels.Virus;
import no.uio.medicine.virsurveillance.parsers.CSVVirusNames;
import no.uio.medicine.virsurveillance.parsers.CSVsGBDdata;
import no.uio.medicine.virsurveillance.parsers.CsvWosParser;
import no.uio.medicine.virsurveillance.parsers.XlsPopulationParser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author Albert
 */
public class SaxTest {

    private static String JCS_CSV_FOLDER = "/Users/apla/Documents/Virus Suirvellance/Data/ThomsonReuters/";

    private static String JCS_CSV_FILE = "/Users/apla/Documents/Virus Suirvellance/Data/ThomsonReuters/SCI+SCIE2014.csv";
    private static String JCS_CSV_FILE2 = "/Users/apla/Documents/Virus Suirvellance/Data/ThomsonReuters/SCI+SCIE2013.csv";
    private static String PUBMED_SAMPLE_XML_FILE_NAME = "/Users/apla/Documents/Virus Suirvellance/Data/bigpubmed.xml";
    private static int batchSize = 10000;

    //This is intended to test different xml parsing tools to deal with the PubMed database
    //http://stackoverflow.com/questions/2134507/fast-lightweight-xml-parser
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, SQLException, ClassNotFoundException {

        SQLManagement sqlM = new SQLManagement();
        sqlM.connect2DB();

        //createAndLoadData();
        //parseXLS();
        /*

            System.out.println("##################################");
            System.out.println("##################################");
            System.out.println("#######PARSING PUBMED DATA ##########");
            System.out.println("##################################");
            System.out.println("##################################");
            loadXMLDatabase(sqlM);

            System.out.println("##################################");
            System.out.println("##################################");
            System.out.println("#######PARSING JOURNAL VIRUS DATA ##########");
            System.out.println("##################################");
            System.out.println("##################################");
            for (int i = 1997; i < 2015; i++) {
                System.out.println(JCS_CSV_FOLDER + "SCI+SCIE" + i + ".csv");
                loadCSVJournalFiles(JCS_CSV_FOLDER + "SCI+SCIE" + i + ".csv",sqlM);
            }
            *//*
            System.out.println("##################################");
            System.out.println("##################################");
            System.out.println("#######PARSING  POPULATION DATA ##########");
            System.out.println("##################################");
            System.out.println("##################################");
            parseXLS(sqlM);

            System.out.println("##################################");
            System.out.println("##################################");
            System.out.println("#######PARSING HUMAN VIRUS DATA ##########");
            System.out.println("##################################");
            System.out.println("##################################");
         
        getDataFromVirusCSV("/Users/apla/Documents/Virus Suirvellance/Data/Table_human_viruses.csv", sqlM);
        
            System.out.println("##################################");
            System.out.println("##################################");
            System.out.println("#######PARSING GDB DATA ##########");
            System.out.println("##################################");
            System.out.println("##################################");
         
            CSVsGBDdata gbd = new CSVsGBDdata(sqlM);
            gbd.parse("/Users/apla/Documents/Virus Suirvellance/Data/ GBD 2013 Mortality and Causes of Death Results/IHME GBD 2013 deaths by Location 1990-2013");
                
            gbd.parse("/Users/apla/Documents/Virus Suirvellance/Data/ GBD 2013 Mortality and Causes of Death Results/IHME GBD 2013 YLLs by Location 1990-2013");
            
            sqlM.closeDB();
            */
           performVirusQueries();
            

    }

    private static void createDatabaseSchema(SQLManagement sqlM) {
        try {
            
            sqlM.createSchema();

            
        } catch (SQLException ex) {
            Logger.getLogger(SaxTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SaxTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void loadXMLDatabase(SQLManagement sqlM) {
        try {
            

            //Load the file
            File inputFile = new File(PUBMED_SAMPLE_XML_FILE_NAME);
            //Create the parser
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser myXMLParser = spf.newSAXParser();
            SaxXMLProcess myHandler = new SaxXMLProcess(batchSize, sqlM);
            myXMLParser.parse(inputFile, myHandler);
            //myHandler.printTitles();
            //xr.
            //cosa=xr.parse(PUBMED_SAMPLE_XML_FILE_NAME);*/


        //System.out.println(sqlM.addAuthor2DB(new PubmedAuthor("Coyote","John")));
    }
    catch (Exception ex

    
        ) {
            Logger.getLogger(SaxTest.class.getName()).log(Level.SEVERE, null, ex);
    }
}

private static void loadCSVJournalFiles(String file, SQLManagement sqlM) {
        
            

            CsvWosParser myParser = new CsvWosParser(sqlM, batchSize);
            myParser.parse(file);
        

    }

    private static void createAndLoadData() {
        /*
        createDatabaseSchema();
        long tStart = System.currentTimeMillis();
        loadXMLDatabase();
        long tEnd = System.currentTimeMillis();
        System.out.println("Time use to parse and load the xml: "+((tEnd - tStart)/1000.0));
        
        //System.out.println("Hit a key to update the CSV journal list");
        //System.in.read();
        //loadCSVJournalFiles(JCS_CSV_FILE);
        //loadCSVJournalFiles(JCS_CSV_FILE2);
        
        for (int i=1997;i<2015;i++){
            System.out.println(JCS_CSV_FOLDER+"SCI+SCIE"+i+".csv");
            loadCSVJournalFiles(JCS_CSV_FOLDER+"SCI+SCIE"+i+".csv");
        }*/
        //loadCSVJournalFiles(JCS_CSV_FOLDER+"SCI+SCIE2015temp.csv");
        //loadCSVJournalFiles(JCS_CSV_FOLDER+"SCI+SCIE2016temp.csv");
    }

    private static void parseXLS(SQLManagement sqlM) throws SQLException, ClassNotFoundException {
        String xlsFile = "/Users/apla/Documents/Virus Suirvellance/Data/API_SP.POP.TOTL_DS2_en_excel_v2.xls";

        

        XlsPopulationParser xlspp = new XlsPopulationParser(sqlM);
        xlspp.parse(xlsFile);
    }

    private static void performImpactFactorQueries() throws SQLException, ClassNotFoundException, IOException {
        String output = "/Users/apla/Documents/Virus Suirvellance/outputs/journalIfacs";
        try {
            new File(output).mkdir();
        } catch (Exception e) {

        }

        SQLQueries sqlQuery = new SQLQueries();
        sqlQuery.connect2DB();

        QueryResult result0 = sqlQuery.getJournalImpactFactors();
        result0.getBoxplotChart().updateChartData();
        result0.save2File(output + "/generalIfac");
        System.out.println("General Informationi obtained");

        for (int i = -1; i < 2; i++) {
            String auxStr = "variableIfac";
            if (i == -1) {
                auxStr = "DecreasingIfac";
            } else if (i == 1) {
                auxStr = "IncreasingIFac";
            }

            QueryResult result1 = sqlQuery.getJournalWithVariableImpactFactors(5, i);
            result1.getBoxplotChart().updateChartData();
            ArrayList<String> journals1 = new ArrayList<>();
            for (String journalName : result1.getValues().get("abbreviated_title")) {
                if (!journalName.equalsIgnoreCase("null") && !journals1.contains(journalName)) {
                    journals1.add(journalName);

                }
            }
            QueryResult resultaux1 = sqlQuery.getJournalEvolution(journals1, auxStr + " evolution of journals with IF var of 5");
            resultaux1.getXyLineChart().updateChartData();

            QueryResult result2 = sqlQuery.getJournalWithVariableImpactFactors(10, i);
            result2.getBoxplotChart().updateChartData();
            ArrayList<String> journals2 = new ArrayList<>();
            for (String journalName : result2.getValues().get("abbreviated_title")) {
                if (!journalName.equalsIgnoreCase("null") && !journals2.contains(journalName)) {
                    journals2.add(journalName);
                }
            }
            QueryResult resultaux2 = sqlQuery.getJournalEvolution(journals2, auxStr + " evolution of journals with IF var of 10");
            resultaux2.getXyLineChart().updateChartData();

            QueryResult result3 = sqlQuery.getJournalWithVariableImpactFactors(20, i);
            result3.getBoxplotChart().updateChartData();
            ArrayList<String> journals3 = new ArrayList<>();
            for (String journalName : result3.getValues().get("abbreviated_title")) {
                if (!journalName.equalsIgnoreCase("null") && !journals3.contains(journalName)) {
                    journals3.add(journalName);
                }
            }
            QueryResult resultaux3 = sqlQuery.getJournalEvolution(journals3, auxStr + " evolution of journals with IF var of 20");
            resultaux3.getXyLineChart().updateChartData();

            result1.save2File(output + "/" + auxStr + "IfacVar5");
            result2.save2File(output + "/" + auxStr + "IfacVar10");
            result3.save2File(output + "/" + auxStr + "IfacVar20");

            resultaux1.save2File(output + "/" + auxStr + "Evolution_IfacVar5");
            resultaux2.save2File(output + "/" + auxStr + "Evolution_IfacVar10");
            resultaux3.save2File(output + "/" + auxStr + "Evolution_IfacVar20");

            System.out.println(auxStr + " PerfomImpactFactorQueries Finished");
        }

    }

    private static void performJournalEvolutionQueries() throws SQLException, ClassNotFoundException, IOException {
        SQLQueries sqlQuery = new SQLQueries();
        sqlQuery.connect2DB();

        ArrayList<String> journals = new ArrayList<>();

        QueryResult resultAux = sqlQuery.getJournalWithVariableImpactFactors(15, -1);
        int max = 100;
        int count = 0;
        for (String journalName : resultAux.getValues().get("abbreviated_title")) {
            if (!journalName.equalsIgnoreCase("null") && !journals.contains(journalName) && count < max) {
                journals.add(journalName);
                count++;
            }

        }

        //journals.add("lancet");
        QueryResult result0 = sqlQuery.getJournalEvolution(journals);
        result0.getXyLineChart().updateChartData();

        //result0.save2File(output+"/generalIfac");
        //System.out.println("PerfomImpactFactorQueries Finished");
    }

    private static void performVirusQueries() throws SQLException, ClassNotFoundException, IOException {
        SQLQueries sqlQuery = new SQLQueries();
        sqlQuery.connect2DB();
        String inputVirusFile = "/Users/apla/Documents/Virus Suirvellance/Data/Table_human_viruses.csv";
       SQLManagement sqlM=new SQLManagement(); sqlM.connect2DB();
        ArrayList<String> viruses = getDataFromVirusCSV(inputVirusFile,sqlM);

        for (String topic : viruses) {

            QueryResult result0 = sqlQuery.getStatsPerTopicIgnoringIFUnder(topic, 0);
            result0.printResult();
            result0.getXyLineChart().updateChartData();
            result0.save2File("/Users/apla/Documents/Virus Suirvellance/outputs/ifPerYear5copy/" + topic);

            //QueryResult result = sqlQuery.getStatsPerTopicIgnoringIFUnder(topic,0);
            QueryResult result = sqlQuery.getPublicationCount(topic);
            result.printResult();
            if (result.getXyLineChart() != null) {
                result.getXyLineChart().updateChartData();
            }
            if (result.getStackedChart() != null) {
                result.getStackedChart().updateChartData();
            }
            result.save2File("/Users/apla/Documents/Virus Suirvellance/outputs/pubsPerYear5copy/" + topic);

            QueryResult result2 = sqlQuery.getPublicationCountSelfNormalized(topic);
            result2.printResult();
            if (result2.getXyLineChart() != null) {
                result2.getXyLineChart().updateChartData();
            }
            if (result2.getStackedChart() != null) {
                result2.getStackedChart().updateChartData();
            }
            result2.save2File("/Users/apla/Documents/Virus Suirvellance/outputs/pubsPerYearSN5copy/" + topic);

            QueryResult result3 = sqlQuery.getRelativePublicationCount(topic);
            result3.printResult();
            if (result3.getXyLineChart() != null) {
                result3.getXyLineChart().updateChartData();
            }
            if (result3.getStackedChart() != null) {
                result3.getStackedChart().updateChartData();
            }
            result3.save2File("/Users/apla/Documents/Virus Suirvellance/outputs/pubsPerYearRel5copy/" + topic);
        }

    }

    private static ArrayList<String> getDataFromVirusCSV(String inputVirusFile,SQLManagement sqlM) throws FileNotFoundException, IOException, SQLException, ClassNotFoundException {
        

        CSVVirusNames csvVir = new CSVVirusNames(sqlM);
        ArrayList<String> virList = csvVir.getVirusNamesAndParse(inputVirusFile);

        

        return virList;

    }

}
