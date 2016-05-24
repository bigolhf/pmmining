/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uio.medicine.virsurveillance.parsers;

import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uio.medicine.virsurveillance.DDBB.SQLManagement;
import no.uio.medicine.virsurveillance.datamodels.CountryData;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import static sun.net.www.http.HttpClient.New;

/**
 *
 * @author apla
 */
public class XlsPopulationParser {

    private SQLManagement sqlM;
    private Collection<CountryData> countries;

    public XlsPopulationParser(SQLManagement sqlM) {
        this.sqlM = sqlM;
        this.countries = new ArrayList<>();
    }

    public void parse(String inputFile) {
        System.out.println("Reading countries from csv");
        this.readCountries(inputFile);
        System.out.println("Storing countries to DDBB");
        this.storeCountriesInDDBB();
        System.out.println("Adding Populations to Countries");
        this.readAndStorePopulations(inputFile);

    }

    private void readCountries(String inputFile) {
        try {
            POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(inputFile));
            HSSFWorkbook wb = new HSSFWorkbook(fs);

            //            
            HSSFSheet sheet = wb.getSheetAt(1); //page with the information of the countries
            HSSFRow row;
            HSSFCell cell;

            int rows; // No of rows
            rows = sheet.getPhysicalNumberOfRows();

            int cols = 0; // No of columns
            int tmp = 0;

            // This trick ensures that we get the data properly even if it doesn't start from first few rows.
            // taken from stack overflow
            for (int i = 0; i < 10 || i < rows; i++) {
                row = sheet.getRow(i);
                if (row != null) {
                    tmp = sheet.getRow(i).getPhysicalNumberOfCells();
                    if (tmp > cols) {
                        cols = tmp;
                    }
                }
            }

            HSSFRow header = sheet.getRow(0);

            int ccInd = 0;
            int regInd = 0;
            int igInd = 0;
            int namInd = 0;
            for (int i = 0; i < header.getLastCellNum(); i++) {
                if (header.getCell(i).toString().equalsIgnoreCase("Country Code")) {
                    ccInd = i;
                }
                if (header.getCell(i).toString().equalsIgnoreCase("Region")) {
                    regInd = i;
                }
                if (header.getCell(i).toString().equalsIgnoreCase("IncomeGroup")) {
                    igInd = i;
                }
                if (header.getCell(i).toString().equalsIgnoreCase("TableName")) {
                    namInd = i;
                }
            }

            for (int r = 1; r < rows; r++) {
                row = sheet.getRow(r);
                if (row != null) {
                    CountryData cd;
                    if (row.getCell(regInd) != null && row.getCell(igInd) != null) {
                        cd = new CountryData(row.getCell(namInd).toString().replace("'", "`"),
                                row.getCell(ccInd).toString().replace("'", "`"),
                                row.getCell(regInd).toString().replace("'", "`"),
                                row.getCell(igInd).toString().replace("'", "`"));

                    } else {
                        cd = new CountryData(row.getCell(namInd).toString(),
                                row.getCell(ccInd).toString(),
                                "",
                                "",
                                true);

                    }
                    countries.add(cd);

                }
            }
        } catch (Exception ioe) {
            ioe.printStackTrace();
            System.out.println("##### ERROR: It looks like " + inputFile + " is not the appropriate type of file or it is not propperly structured");
        }
    }

    private void storeCountriesInDDBB() {
        for (CountryData cd : countries) {
            try {
                sqlM.addCountry2DDBB(cd);
            } catch (SQLException ex) {
                Logger.getLogger(XlsPopulationParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void readAndStorePopulations(String inputFile) {
        try {
            POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(inputFile));
            HSSFWorkbook wb = new HSSFWorkbook(fs);

            //            
            HSSFSheet sheet = wb.getSheetAt(0); //page with the population of the countries
            HSSFRow row;
            HSSFCell cell;

            int rows; // No of rows
            rows = sheet.getPhysicalNumberOfRows();

            int cols = 0; // No of columns
            int tmp = 0;

            // This trick ensures that we get the data properly even if it doesn't start from first few rows.
            // taken from stack overflow
            for (int i = 0; i < 10 || i < rows; i++) {
                row = sheet.getRow(i);
                if (row != null) {
                    tmp = sheet.getRow(i).getPhysicalNumberOfCells();
                    if (tmp > cols) {
                        cols = tmp;
                    }
                }
            }

            //Start reading countries;
            HSSFRow header = sheet.getRow(3);
            ArrayList<Integer> years = new ArrayList<>();
            for (int i = 4; i < header.getLastCellNum(); i++) {
                years.add(Integer.parseInt(header.getCell(i).toString()));

            }

            for (int r = 4; r < rows; r++) {
                row = sheet.getRow(r);
                if (row != null) {
                    String countryName = row.getCell(1).toString();

                    int count = 0;
                    for (int i = 4; i < row.getLastCellNum(); i++) {
                        if (row.getCell(i) != null) {
                            //System.out.print(years.get(count)+": "+Float.parseFloat(row.getCell(i).toString())+ " - ");
                            try {
                                sqlM.addPopulation(countryName, years.get(count), row.getCell(i).toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        count++;
                    }

                }
            }
        } catch (Exception ioe) {
            ioe.printStackTrace();
            System.out.println("##### ERROR: It looks like " + inputFile + " is not the appropriate type of file or it is not propperly structured");
        }
    }

}
