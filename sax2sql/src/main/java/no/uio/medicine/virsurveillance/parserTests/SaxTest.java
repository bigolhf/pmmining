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
import no.uio.medicine.virsurveillance.parsers.CsvWosParser;
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
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {

        try {
            //createAndLoadData();
            performJournalEvolutionQueries();
        } catch (Exception ex) {
            Logger.getLogger(SaxTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static void createDatabaseSchema() {
        try {
            SQLManagement sqlM = new SQLManagement();
            sqlM.connect2DB();
            sqlM.createSchema();

            sqlM.closeDB();
        } catch (SQLException ex) {
            Logger.getLogger(SaxTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SaxTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SaxTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void loadXMLDatabase() {
        try {
            SQLManagement sqlM = new SQLManagement();
            sqlM.connect2DB();

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

            sqlM.closeDB();
            //System.out.println(sqlM.addAuthor2DB(new PubmedAuthor("Coyote","John")));
        } catch (SQLException ex) {
            Logger.getLogger(SaxTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(SaxTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void loadCSVJournalFiles(String file) {
        try {
            SQLManagement sqlM = new SQLManagement();
            sqlM.connect2DB();

            CsvWosParser myParser = new CsvWosParser(sqlM, batchSize);
            myParser.parse(file);
        } catch (SQLException ex) {
            Logger.getLogger(SaxTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SaxTest.class.getName()).log(Level.SEVERE, null, ex);
        }

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

    private static void performImpactFactorQueries() throws SQLException, ClassNotFoundException, IOException {
        SQLQueries sqlQuery = new SQLQueries();
        sqlQuery.connect2DB();
        
        QueryResult result0 = sqlQuery.getJournalImpactFactors();
        result0.getBoxplotChart().updateChartData();

        QueryResult result1 = sqlQuery.getJournalWithVariableImpactFactors(5);
        result1.getBoxplotChart().updateChartData();
        
        QueryResult result2 = sqlQuery.getJournalWithVariableImpactFactors(10);
        result2.getBoxplotChart().updateChartData();
        
        QueryResult result3 = sqlQuery.getJournalWithVariableImpactFactors(20);
        result3.getBoxplotChart().updateChartData();
        String output="/Users/apla/Documents/Virus Suirvellance/outputs/journalIfacs";
        new File(output).mkdir();
        result0.save2File(output+"/generalIfac");
        result1.save2File(output+"/IfacVar5");
        result2.save2File(output+"/IfacVar10");
        result3.save2File(output+"/IfacVar20");
        
        System.out.println("PerfomImpactFactorQueries Finished");

        
    }
    
    private static void performJournalEvolutionQueries() throws SQLException, ClassNotFoundException, IOException {
        SQLQueries sqlQuery = new SQLQueries();
        sqlQuery.connect2DB();
        
        ArrayList<String> journals = new ArrayList<>(); 
        
        QueryResult resultAux = sqlQuery.getJournalWithVariableImpactFactors(15,-1);
        int max=100;
        int count=0;
        for (String journalName: resultAux.getValues().get("abbreviated_title")){
            if (!journalName.equalsIgnoreCase("null") && !journals.contains(journalName) && count < max){
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
        
        String inputVirusFile = "/Users/apla/Documents/Virus Suirvellance/Data/Table_human_viruses.csv";
        ArrayList<String> viruses = getDataFromVirusCSV(inputVirusFile);

        for (String topic : viruses) {

            QueryResult result0 = sqlQuery.getStatsPerTopicIgnoringIFUnder(topic, 0);
            result0.printResult();
            result0.getXyLineChart().updateChartData();
            result0.save2File("/Users/apla/Documents/Virus Suirvellance/outputs/ifPerYear4copy/" + topic);

            //QueryResult result = sqlQuery.getStatsPerTopicIgnoringIFUnder(topic,0);
            QueryResult result = sqlQuery.getPublicationCount(topic);
            result.printResult();
            if (result.getXyLineChart() != null) {
                result.getXyLineChart().updateChartData();
            }
            if (result.getStackedChart() != null) {
                result.getStackedChart().updateChartData();
            }
            result.save2File("/Users/apla/Documents/Virus Suirvellance/outputs/pubsPerYear4copy/" + topic);

            QueryResult result2 = sqlQuery.getPublicationCountSelfNormalized(topic);
            result2.printResult();
            if (result2.getXyLineChart() != null) {
                result2.getXyLineChart().updateChartData();
            }
            if (result2.getStackedChart() != null) {
                result2.getStackedChart().updateChartData();
            }
            result2.save2File("/Users/apla/Documents/Virus Suirvellance/outputs/pubsPerYearSN4copy/" + topic);

            QueryResult result3 = sqlQuery.getRelativePublicationCount(topic);
            result3.printResult();
            if (result3.getXyLineChart() != null) {
                result3.getXyLineChart().updateChartData();
            }
            if (result3.getStackedChart() != null) {
                result3.getStackedChart().updateChartData();
            }
            result3.save2File("/Users/apla/Documents/Virus Suirvellance/outputs/pubsPerYearRel4copy/" + topic);
        }
         
    }

    private static ArrayList<String> getDataFromVirusCSV(String inputVirusFile) throws FileNotFoundException, IOException {
        ArrayList<String> viruses = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new FileReader(inputVirusFile));
        CSVParser parser = CSVFormat.RFC4180.withDelimiter(';').withIgnoreEmptyLines().withHeader().parse(reader);
        for (CSVRecord csvRecord : parser) {
            if (csvRecord.isMapped("Virus Name")) {
                String currentVirus = csvRecord.get("Virus Name").replace(" virus", "").replace("’", "%%");
                viruses.add(currentVirus);
            }
        }

        return viruses;

    }
    
    

}
