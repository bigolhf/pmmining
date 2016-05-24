/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uio.medicine.virsurveillance.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import no.uio.medicine.virsurveillance.DDBB.SQLManagement;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author apla
 */
public class CSVsGBDdata {

    private SQLManagement sqlM;

    public CSVsGBDdata(SQLManagement sqlM) {
        this.sqlM = sqlM;
    }

    public SQLManagement getSqlM() {
        return sqlM;
    }

    public void setSqlM(SQLManagement sqlM) {
        this.sqlM = sqlM;
    }

    public void parse(String deathFolder) throws IOException {
        File f = new File(deathFolder);
        Runtime runtime=Runtime.getRuntime();
        if (f.isDirectory()) {
            String[] filesInDir = f.list();

            for (String fil : filesInDir) {
                if (fil.endsWith(".zip")) {
                    ZipFile zipFile = new ZipFile(deathFolder + "/" + fil);

                    Enumeration<? extends ZipEntry> entries = zipFile.entries();

                    while (entries.hasMoreElements()) {
                        System.out.println("Used memory: "+ (runtime.totalMemory()-runtime.freeMemory())/(1024*1024)+" Free memory: "+ (runtime.freeMemory())/(1024*1024));
                        
                        ZipEntry entry = entries.nextElement();
                        InputStream stream = zipFile.getInputStream(entry);
                        BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                        CSVParser parser = CSVFormat.RFC4180.withDelimiter(',').withIgnoreEmptyLines().withHeader().parse(br);
                        
                        List<CSVRecord> records = parser.getRecords();
                        System.out.println("Reading records: "+zipFile.getName()+"/"+entry);
                        /*for (int i=0;i<records.size();i++) {
                            CSVRecord csvRecord = records.get(i);*/
                        for (CSVRecord csvRecord: records){
                            if (csvRecord.isMapped("age_group_id")) { //age group 22 corresponds to all ages
                                if (csvRecord.get("age_group_id").equalsIgnoreCase("22")) {
                                    String location = null;
                                    String year = null;
                                    String sex = null; 
                                    String cause = null;
                                    String number = null;
                                    String metric = null;

                                    if (csvRecord.isMapped("location_code")) {
                                        location = csvRecord.get("location_code");
                                    }
                                    if (csvRecord.isMapped("year")) {
                                        year = csvRecord.get("year");
                                    }
                                    if (csvRecord.isMapped("sex_id")) { //1=male, 2 = female
                                        if (csvRecord.get("sex_id").equalsIgnoreCase(("1"))) {
                                            sex = "m";
                                        } else if (csvRecord.get("sex_id").equalsIgnoreCase("2")) {
                                            sex = "f";
                                        }
                                    }
                                    if (csvRecord.isMapped("cause_name")) {
                                        cause = csvRecord.get("cause_name");
                                    }
                                    if (csvRecord.isMapped("mean")) {
                                        number = csvRecord.get("mean");
                                    }
                                    if (csvRecord.isMapped("metric") && csvRecord.isMapped("unit")) {
                                        metric = csvRecord.get("metric") + "-" + csvRecord.get("unit");
                                    }

                                    if (location != null && year != null
                                            && sex != null && cause != null
                                            && number != null && metric != null) {
                                        try {
                                            sqlM.addSanitaryIssueToCountry(location, year, sex, cause, metric, number);
                                        } catch (SQLException ex) {
                                            
                                            Logger.getLogger(CSVsGBDdata.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }

                                }

                            }

                        }
                        
                        parser.close();

                        stream.close();
                        br.close();
                    }
                    zipFile.close();
                }
            }
        } else {
            System.out.println("Not a directory");
        }
    }

    public void zip() throws IOException {
        ZipFile zipFile = new ZipFile("C:/test.zip");

        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            InputStream stream = zipFile.getInputStream(entry);
        }

    }

}
