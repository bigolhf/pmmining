/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uio.medicine.virsurveillance.parsers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uio.medicine.virsurveillance.DDBB.SQLManagement;
import no.uio.medicine.virsurveillance.datamodels.PubmedJournal;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author Albert
 */
public class CsvWosParser {

    private SQLManagement sqlM;
    private int batchSize = 1;

    private ArrayList<PubmedJournal> journals;

    private int currentBatchSize = 0;

    public CsvWosParser() {
        journals = new ArrayList<>();
        currentBatchSize = 0;
    }

    public CsvWosParser(SQLManagement sqlM, int batchSize) {
        this.sqlM = sqlM;
        this.batchSize = batchSize;
        journals = new ArrayList<>();
        currentBatchSize = 0;
    }

    public void parse(String inputPath) {

        try {
            int year = 2014;//default value
            BufferedReader reader = new BufferedReader(new FileReader(inputPath));
            String line = reader.readLine(); // Read the first/current line.
            try {
                if (line.contains("Selected JCR Year:")) {
                    String[] splitedLine = line.split("Selected JCR Year:");
                    year = Integer.parseInt(splitedLine[1].split(" ")[1]);
                }
            } catch (Exception e) {
                System.out.println("Journal Citation year selected by default: " + year);
                Logger.getLogger(CsvWosParser.class.getName()).log(Level.WARNING, "Journal Citation year selected by default: " + year, e);

            }

            CSVParser parser = CSVFormat.RFC4180.withDelimiter(',').withIgnoreEmptyLines().withHeader().parse(reader);

            for (CSVRecord csvRecord : parser) {
                if (!csvRecord.get("Rank").contains("Copyright")) {
                    PubmedJournal journal = new PubmedJournal(csvRecord.get("Full Journal Title").replaceAll("\\(.*?\\) ?", ""));
                    if (csvRecord.isMapped("JCR Abbreviated Title")) {
                        journal.setJournalShortName(csvRecord.get("JCR Abbreviated Title"));
                    }
                    try {
                        journal.setYearImpactFactor(year, Float.parseFloat(csvRecord.get("Journal Impact Factor")));
                    } catch (Exception e) {
                        System.out.println("Impact factor " + csvRecord.get("Journal Impact Factor") + " for " + csvRecord.get("Full Journal Title"));
                        journal.setYearImpactFactor(year, -1);
                    }
                    this.journals.add(journal);
                    this.currentBatchSize++;
                    currentBatchSize++;
                    if (this.currentBatchSize >= this.batchSize) {
                        currentBatchSize = 0;
                        for (PubmedJournal pm : journals) {
                            this.sqlM.updateJournal(pm, true);
                        }
                        this.journals = new ArrayList<>();
                    }
                }

            }

            for (PubmedJournal pm : journals) {
                this.sqlM.updateJournal(pm, true);
            }
            this.journals = new ArrayList<>();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(CsvWosParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CsvWosParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(CsvWosParser.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
