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
import no.uio.medicine.virsurveillance.DDBB.SQLManagement;
import no.uio.medicine.virsurveillance.datamodels.Virus;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author apla
 */
public class CSVVirusNames {

    private SQLManagement sqlM;

    public CSVVirusNames(SQLManagement sqlM) {
        this.sqlM = sqlM;
    }

    public ArrayList<String> getVirusNames(String inputVirusFile) throws FileNotFoundException, IOException {
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

    public void parse(String inputVirusFile) throws FileNotFoundException, IOException, SQLException {
        for (String virus : getVirusNames(inputVirusFile)) {
            sqlM.addVirus(new Virus(virus));
        }

    }
    
    public ArrayList<String> getVirusNamesAndParse(String inputVirusFile) throws IOException, FileNotFoundException, SQLException{
        ArrayList<String> vir=getVirusNames(inputVirusFile);
        parse(inputVirusFile);
        return vir;
        
    }

}
