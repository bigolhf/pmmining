/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uio.medicine.virsurveillance.DDBB;

import java.sql.*;
import com.ibatis.common.jdbc.ScriptRunner;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uio.medicine.virsurveillance.datamodels.CountryData;
import no.uio.medicine.virsurveillance.datamodels.PubmedArticle;
import no.uio.medicine.virsurveillance.datamodels.PubmedAuthor;
import no.uio.medicine.virsurveillance.datamodels.PubmedJournal;
import no.uio.medicine.virsurveillance.datamodels.Virus;

/**
 *
 * @author Albert
 */
public class SQLManagement {

    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/test";

    private static final String USER = "user1";
    private static final String PASS = "myPass";

    private static final String SCRIPT_PATH = "/Users/apla/Documents/Virus Suirvellance/SQL stuff/createDatabase.sql";

    protected Connection sqlConnection;

    protected Map<String, Integer> calendar;

    public SQLManagement() {
        calendar = new HashMap<>();
        calendar.put("jan", 1);
        calendar.put("feb", 2);
        calendar.put("mar", 3);
        calendar.put("apr", 4);
        calendar.put("may", 5);
        calendar.put("jun", 6);
        calendar.put("jul", 7);
        calendar.put("aug", 8);
        calendar.put("sep", 9);
        calendar.put("oct", 10);
        calendar.put("nov", 11);
        calendar.put("dec", 12);
        calendar.put("null", 1);
    }

    public void connect2DB() throws SQLException, ClassNotFoundException {
        Class.forName(JDBC_DRIVER);
        sqlConnection = DriverManager.getConnection(
                DB_URL, USER, PASS);
    }

    public void closeDB() throws SQLException {
        this.sqlConnection.close();
    }

    public void createSchema() throws FileNotFoundException, IOException, SQLException {

        ScriptRunner sr = new ScriptRunner(sqlConnection, false, false);

        // Give the input file to Reader
        Reader reader = new BufferedReader(
                new FileReader(SCRIPT_PATH));

        // Exctute script
        sr.runScript(reader);

    }

    public int addArticle2DB(PubmedArticle pa) throws SQLException {
        int index = -1;
        Statement stmt = sqlConnection.createStatement();
        String AbstractText = "";
        for (String s : pa.getAbstracts()) {
            AbstractText = AbstractText + s;
        }
        int month;
        if (pa.getPublicationMonth() != null) {
            month = calendar.get(pa.getPublicationMonth().toLowerCase());
        } else {
            month = 1;
        }
        String date = pa.getPublicationYear() + "-" + month + "-01";

        int journalId = addJournal2DB(pa.getJournal());

        stmt.executeUpdate("insert into test.pubmedarticle (title,  publication,pubmedjournal_idpubmedjournal,abstract)" + " values('" + pa.getTitle() + "','" + date + "'," + journalId + ",'" + AbstractText + "')");

        stmt.close();

        //TODO decide if authors must be considered
        //for (PubmedAuthor auth : pa.getAuthorList()) {authors not considered
        //   addAuthor2DB(auth); authors not considered
        //}//**/
        /*pa.getAuthorList().parallelStream().forEach((auth) -> {
            try {
                this.addAuthor2DB(auth);
            } catch (SQLException ex) {
                Logger.getLogger(SQLManagement.class.getName()).log(Level.SEVERE, null, ex);
            }
        });//**/ //end of parallel version
        return index;
    }

    public int addArticle2DB(PubmedArticle pa, boolean duplicatesAllowed) throws SQLException {
        if (!duplicatesAllowed) {
            return addArticle2DB(pa);
        }

        int index = -1;
        Statement stmt = sqlConnection.createStatement();
        String AbstractText = "";
        for (String s : pa.getAbstracts()) {
            AbstractText = AbstractText + s;
        }
        int month;
        if (pa.getPublicationMonth() != null) {
            month = calendar.get(pa.getPublicationMonth().toLowerCase());
        } else {
            month = 1;
        }
        String date = pa.getPublicationYear() + "-" + month + "-01";

        int journalId = addJournal2DB(pa.getJournal());

        //int journalIndex = getIndexOfJournal(pa.getJournalName());
        //if (journalIndex < 0) {
        //    stmt.executeUpdate("insert into test.pubmedjournal (title,impactfactor_idimpactfactor) values('test',1)");
        //}
        stmt.executeUpdate("insert into test.pubmedarticle (title,  publication,pubmedjournal_idpubmedjournal,abstract)" + " values('" + pa.getTitle() + "','" + date + "'," + journalId + ",'" + AbstractText + "')");
        ResultSet result = stmt.executeQuery("SELECT LAST_INSERT_ID();");
        if (result.next()) {
            index = result.getInt(1);
        }
        stmt.close();

        //parallel version of the following loop
        //TODO decide if authors must be considered
        //for (PubmedAuthor auth : pa.getAuthorList()) {authors not considered
        //   addAuthor2DB(auth); authors not considered
        //}//**/
        /*pa.getAuthorList().parallelStream().forEach((auth) -> {
            try {
                this.addAuthor2DB(auth);
            } catch (SQLException ex) {
                Logger.getLogger(SQLManagement.class.getName()).log(Level.SEVERE, null, ex);
            }
        });//**/ //end of parallel version
        return index;
    }

    public int addAuthor2DB(PubmedAuthor auth) throws SQLException {
        int index = getIndexOfAuthor(auth);
        if (index < 0) {
            Statement stmt = sqlConnection.createStatement();
            stmt.executeUpdate("insert into test.pubmedauthor (forename, surname, initials)" + " values('" + auth.getForeName() + "','" + auth.getLastName() + "','" + auth.getInitials() + "')");
            ResultSet result = stmt.executeQuery("SELECT LAST_INSERT_ID();");
            if (result.next()) {
                index = result.getInt(1);
            }
            stmt.close();

        }
        return index;
    }

    public int addAuthor2DB(PubmedAuthor auth, boolean duplicatesAllowed) throws SQLException {
        if (duplicatesAllowed) {
            return addAuthor2DB(auth);
        } else {
            int index = getIndexOfAuthor(auth);
            if (index < 0) {
                Statement stmt = sqlConnection.createStatement();
                stmt.executeUpdate("insert into test.pubmedauthor (forename, surname, initials)" + " values('" + auth.getForeName() + "','" + auth.getLastName() + "','" + auth.getInitials() + "')");
                ResultSet result = stmt.executeQuery("SELECT LAST_INSERT_ID();");
                if (result.next()) {
                    index = result.getInt(1);
                }
                stmt.close();

            }
            return index;
        }
    }

    public int addJournal2DB(String journalTitle) throws SQLException {
        int index = getIdOfJournal(journalTitle);
        if (index < 0) {
            Statement stmt = sqlConnection.createStatement();
            stmt.executeUpdate("insert into test.pubmedjournal (title) values('" + journalTitle + "')");
            ResultSet result = stmt.executeQuery("select idpubmedjournal from test.pubmedjournal where title = '" + journalTitle + "'");

            //ResultSet result = stmt.executeQuery("SELECT LAST_INSERT_ID();");
            if (result.next()) {
                index = result.getInt(1);
            }
            stmt.close();
        }

        return index;
    }

    public int addJournal2DB(String journalTitle, boolean duplicatesAllowed) throws SQLException {
        if (!duplicatesAllowed) {
            return addJournal2DB(journalTitle);
        }
        int index = getIdOfJournal(journalTitle);
        if (index < 0) {
            Statement stmt = sqlConnection.createStatement();
            stmt.executeUpdate("insert into test.pubmedjournal (title) values('" + journalTitle + "')");
            ResultSet result = stmt.executeQuery("SELECT LAST_INSERT_ID();");
            if (result.next()) {
                index = result.getInt(1);
            }
            stmt.close();
        }

        return index;
    }

    public int addJournal2DB(PubmedJournal journal) throws SQLException {
        int index = getIdOfJournal(journal.getJournalName());
        if (index < 0) {
            Statement stmt = sqlConnection.createStatement();
            stmt.executeUpdate("insert into test.pubmedjournal (title) values('" + journal.getJournalName() + "')");
            ResultSet result = stmt.executeQuery("select idpubmedjournal from test.pubmedjournal where title = '" + journal.getJournalName() + "'");
            if (result.next()) {
                index = result.getInt(1);
            }

            stmt.close();
        }
        return index;
    }

    public int addJournal2DB(PubmedJournal journal, boolean duplicatesAllowed) throws SQLException {
        int journalId = getIdOfJournal(journal.getJournalName());
        if (journalId < 0) {
            Statement stmt = sqlConnection.createStatement();
            stmt.executeUpdate("insert into test.pubmedjournal (title) values('" + journal.getJournalName() + "')");
            ResultSet result = stmt.executeQuery("select idpubmedjournal from test.pubmedjournal where title = '" + journal.getJournalName() + "'");
            if (result.next()) {
                journalId = result.getInt(1);
            }
            stmt.close();
            for (Integer year : journal.getYearImpactFactor().keySet()) {
                addImpactFactorToJournal(year, journal.getYearImpactFactor().get(year), journalId);
            }

        }
        return journalId;
    }

    private int getIndexOfAuthor(PubmedAuthor auth) throws SQLException {
        int ret = -1;
        Statement stmt = sqlConnection.createStatement();
        ResultSet result = stmt.executeQuery("select idpubmedauthor from test.pubmedauthor where forename='" + auth.getForeName() + "' and surname='" + auth.getLastName() + "';");
        if (result.next()) {
            ret = result.getInt("idpubmedauthor");
        }
        if (ret < 0) {
            ret = -1;
        }
        stmt.close();
        return ret;
    }

    private int getIndexOfJournal(PubmedJournal journal) throws SQLException {
        int ret = -1;
        Statement stmt = sqlConnection.createStatement();
        ResultSet result = stmt.executeQuery("select idpubmedjournal from test.pubmedjournal where title='" + journal.getJournalName() + "';");
        if (result.next()) {
            ret = result.getInt("idpubmedjournal");
        }
        if (ret < 0) {
            ret = -1;
            result = stmt.executeQuery("select idpubmedjournal from test.pubmedjournal where abbreviated_title='" + journal.getJournalShortName() + "';");
            if (result.next()) {
                ret = result.getInt("idpubmedjournal");
            }
        }
        stmt.close();
        return ret;

    }

    private int getIdOfJournal(String journalTitle) throws SQLException {
        int ret = -1;
        Statement stmt = sqlConnection.createStatement();
        ResultSet result = stmt.executeQuery("select idpubmedjournal from test.pubmedjournal where title ='" + journalTitle + "';");
        if (result.next()) {
            ret = result.getInt("idpubmedjournal");
        }
        if (result.next()) {
            System.out.println("Duplicat = " + ret + "-" + result.getInt("idpubmedjournal"));
        }
        if (ret < 0) {
            ret = -1;
        }
        stmt.close();
        return ret;

    }

    public int updateJournal(PubmedJournal journal, boolean restrictive) throws SQLException {
        if (!restrictive) {
            return addJournal2DB(journal);
        }
        int journalId = getIdOfJournal(journal.getJournalName());
        if (journalId >= 0) {
            Statement stmt = sqlConnection.createStatement();

            stmt.executeUpdate("update test.pubmedjournal set title='" + journal.getJournalName() + "', abbreviated_title='" + journal.getJournalShortName() + "' where idpubmedjournal=" + journalId);
            stmt.close();
            for (Integer year : journal.getYearImpactFactor().keySet()) {
                addImpactFactorToJournal(year, journal.getYearImpactFactor().get(year), journalId);
            }
        }
        //TODO: IMPLEMENTE IMPACT FACTOR UPDATE

        return journalId;

    }

    private void addImpactFactorToJournal(int year, double ifac, int journalId) throws SQLException {
        Statement stmt = sqlConnection.createStatement();
        stmt.executeUpdate("insert into test.impactfactor (date,ifac,pubmedjournal_idpubmedjournal) values(" + year + "," + ifac + "," + journalId + ")");
        stmt.close();
    }

    public int getCountryId(String countryName) throws SQLException {
        int ret = -1;
        Statement stmt = sqlConnection.createStatement();
        ResultSet result = stmt.executeQuery("select idCountry from test.Country where name = '" + countryName + "' or countrycode='" + countryName + "'");
        if (result.next()) {
            ret = result.getInt("idCountry");
        }
        if (result.next()) {
            System.out.println("Duplicat = " + ret + "-" + result.getInt("idCountry"));
        }
        if (ret < 0) {
            ret = -1;
        }
        stmt.close();
        return ret;

    }

    public int addCountry2DDBB(CountryData cd) throws SQLException {
        int countryId = getCountryId(cd.getName());
        int isACountry = (cd.isIsAnAgregator()) ? 0 : 1;;
        if (countryId < 0) { // the country is not inside the database
            if (cd.isIsAnAgregator()) {
                Statement stmt = sqlConnection.createStatement();
                String query = "insert into test.Country(name,countrycode,isacountry) values('" + cd.getName() + "','" + cd.getCode() + "','" + isACountry + "')";
                System.out.println(query);
                stmt.executeUpdate(query);
                countryId = getCountryId(cd.getName());
            } else {

                int idRegion = addRegion2DDBB(cd.getRegion());
                int idIncome = addIncomeGroup2DDBB(cd.getIncomeGroup());

                Statement stmt = sqlConnection.createStatement();
                String sqlInsert = "insert into test.Country(name,countrycode,isacountry,region_idregion,incomeGroup_idincomeGroup) "
                        + "values('" + cd.getName() + "','" + cd.getCode() + "','" + isACountry + "'," + idRegion + "," + idIncome + ")";
                System.out.println(sqlInsert);
                stmt.executeUpdate(sqlInsert);
                countryId = getCountryId(cd.getName());

            }

        }
        return countryId;
    }

    private int addRegion2DDBB(String region) throws SQLException {
        int regionId = getRegionId(region);
        if (regionId < 0) {
            Statement stmt = sqlConnection.createStatement();
            stmt.executeUpdate("insert into test.region(name) values('" + region + "')");
            regionId = getRegionId(region);
        }
        return regionId;
    }

    private int addIncomeGroup2DDBB(String group) throws SQLException {
        int groupId = getIncomeGroupId(group);
        if (groupId < 0) {
            Statement stmt = sqlConnection.createStatement();
            stmt.executeUpdate("insert into test.incomeGroup(name) values('" + group + "')");
            groupId = getIncomeGroupId(group);
        }
        return groupId;
    }

    private int getRegionId(String region) throws SQLException {
        int ret = -1;
        Statement stmt = sqlConnection.createStatement();
        ResultSet result = stmt.executeQuery("select idRegion from test.region where name = '" + region + "'");
        if (result.next()) {
            ret = result.getInt("idRegion");
        }
        if (result.next()) {
            System.out.println("Duplicat = " + ret + "-" + result.getInt("idRegion"));
        }
        if (ret < 0) {
            ret = -1;
        }
        stmt.close();
        return ret;
    }

    private int getIncomeGroupId(String group) throws SQLException {
        int ret = -1;
        Statement stmt = sqlConnection.createStatement();
        ResultSet result = stmt.executeQuery("select idincomeGroup from test.incomeGroup where name = '" + group + "'");
        if (result.next()) {
            ret = result.getInt("idIncomeGroup");
        }
        if (result.next()) {
            System.out.println("Duplicat = " + ret + "-" + result.getInt("idincomeGroup"));
        }
        if (ret < 0) {
            ret = -1;
        }
        stmt.close();
        return ret;
    }

    public void addPopulation(String countryName, int year, String population) throws SQLException {
        int countryId = getCountryId(countryName);

        Statement stmt = sqlConnection.createStatement();
        String query = "insert into test.population(country_idCountry,year,population) values('" + countryId + "','" + year + "','" + population + "') "
                + "ON DUPLICATE KEY UPDATE "
                + "population='" + population + "';";

        stmt.executeUpdate(query);

    }

    public int getVirusId(String name) throws SQLException {
        int id = -1;
        String query = "Select idVirus from test.Virus where principalname=?;";
        PreparedStatement ps = sqlConnection.prepareStatement(query);
        ps.setString(1, name);
        ResultSet result = ps.executeQuery();

        if (result.next()) {
            id = result.getInt("idVirus");
        } else {
            query = "select VirusSinonim.virus_idVirus from test.VirusSinonim where VirusSinonim.name = ?;";
            ps = sqlConnection.prepareStatement(query);
            ps.setString(1, name);
            result = ps.executeQuery();
            if (result.next()) {
                id = result.getInt("virus_idVirus");
            }
        }
        ps.close();
        return id;
    }

    public void addVirus(Virus vir) throws SQLException {
        int id = getVirusId(vir.getVirusName());
        if (id < 0) {
            String query = "insert into virus(principalname) values(?);";
            PreparedStatement ps;
            ps = sqlConnection.prepareStatement(query);
            ps.setString(1, vir.getVirusName());
            ps.execute();
            id = getVirusId(vir.getVirusName());
            addVirusSinonim(id, vir.getVirusName());
            for (String sin : vir.getVirusSinonims()) {
                addVirusSinonim(id, sin);
            }
        } else {
            for (String sin : vir.getVirusSinonims()) {
                addVirusSinonim(id, sin);
            }
        }

    }

    public int getSinonimId(int id, String name) throws SQLException {
        int sId = -1;
        String query = "select idVirusSinonim from test.VirusSinonim where virus_idVirus=? and name =?;";
        PreparedStatement ps;
        ps = sqlConnection.prepareStatement(query);
        ps.setInt(1, id);
        ps.setString(2, name);
        ResultSet result = ps.executeQuery();
        if (result.next()) {
            sId = result.getInt("idVirusSinonim");
        }

        ps.close();
        return sId;
    }

    public void addVirusSinonim(int id, String name) throws SQLException {
        int sId = getSinonimId(id, name);
        if (sId < 0) {
            String query = "insert into VirusSinonim(virus_idVirus,name) values(?,?);";
            PreparedStatement ps;
            ps = sqlConnection.prepareStatement(query);
            ps.setInt(1, id);
            ps.setString(2, name);
            ps.execute();
            ps.close();
        }

    }

    public int getSanitaryIssueId(String cause) throws SQLException {
        int sId = -1;
        String query = "select idsanitaryissue from test.sanitaryissue where name =?;";
        PreparedStatement ps;
        ps = sqlConnection.prepareStatement(query);

        ps.setString(1, cause);
        ResultSet result = ps.executeQuery();
        if (result.next()) {
            sId = result.getInt("idsanitaryissue");
        }

        ps.close();
        return sId;
    }

    public void addSanitaryIssue(String cause) throws SQLException {
        int sId = getSanitaryIssueId(cause);
        if (sId < 0) {
            String query = "insert into sanitaryissue(name) values(?);";
            PreparedStatement ps;
            ps = sqlConnection.prepareStatement(query);
            ps.setString(1, cause);
            ps.execute();
            ps.close();
        }
    }

    public void addSanitaryIssueToCountry(String location, String year, String sex, String cause, String metric, String number) throws SQLException {
        /*
        System.out.print("Metric:" + metric);
        System.out.print(" locaitonc+:" + location);
        System.out.print(" sex:" + sex);
        System.out.print(" cause:" + cause);
        System.out.print(" year:" + year);
        System.out.print(" number:" + number);
        System.out.println();//To change body of generated methods, choose Tools | Templates.
         */
        int countryId = getCountryId(location);
        int causeId = getSanitaryIssueId(cause);
        if (causeId < 0) {
            addSanitaryIssue(cause);
        }
        if (countryId >= 0) {

            PreparedStatement ps = null;
            String query = null;
            try {
                switch (metric.toLowerCase()) {
                    case "ylls (years of life lost)-number":
                        query = "insert into countryhasvirus(country_idcountry,sanitaryissue_idsanitaryissue,year,sex,yll) values (?,?,?,?,?)"
                                + "ON DUPLICATE KEY UPDATE yll=?;";
                        ps = sqlConnection.prepareStatement(query);
                        ps.setInt(1, countryId);
                        ps.setInt(2, causeId);
                        ps.setString(3, year);
                        ps.setString(4, sex);
                        ps.setString(5, number);
                        ps.setString(6, number);
                        ps.execute();
                        ps.close();
                        break;
                    case "ylls (years of life lost)-rate per 100,000":
                        query = "insert into countryhasvirus(country_idcountry,sanitaryissue_idsanitaryissue,year,sex,yllrate) values (?,?,?,?,?)"
                                + " ON DUPLICATE KEY UPDATE yllrate=?;";
                        ps = sqlConnection.prepareStatement(query);
                        ps.setInt(1, countryId);
                        ps.setInt(2, causeId);
                        ps.setString(3, year);
                        ps.setString(4, sex);
                        ps.setString(5, number);
                        ps.setString(6, number);
                        ps.execute();
                        ps.close();
                        break;
                    case "ylls (years of life lost)-percent":
                        query = "insert into countryhasvirus(country_idcountry,sanitaryissue_idsanitaryissue,year,sex,yllpercent) values (?,?,?,?,?)"
                                + " ON DUPLICATE KEY UPDATE yllpercent=?;";

                        ps = sqlConnection.prepareStatement(query);
                        ps.setInt(1, countryId);
                        ps.setInt(2, causeId);
                        ps.setString(3, year);
                        ps.setString(4, sex);
                        ps.setString(5, number);
                        ps.setString(6, number);
                        ps.execute();
                        ps.close();
                        break;
                    case "deaths-number":
                        query = "insert into countryhasvirus(country_idcountry,sanitaryissue_idsanitaryissue,year,sex,death) values (?,?,?,?,?)"
                                + " ON DUPLICATE KEY UPDATE death=?;";
                        ps = sqlConnection.prepareStatement(query);
                        ps.setInt(1, countryId);
                        ps.setInt(2, causeId);
                        ps.setString(3, year);
                        ps.setString(4, sex);
                        ps.setString(5, number);
                        ps.setString(6, number);
                        ps.execute();
                        ps.close();
                        break;
                    case "deaths-rate per 100,000":
                        query = "insert into countryhasvirus(country_idcountry,sanitaryissue_idsanitaryissue,year,sex,deathrate) values (?,?,?,?,?)"
                                + " ON DUPLICATE KEY UPDATE deathrate=?;";
                        ps = sqlConnection.prepareStatement(query);
                        ps.setInt(1, countryId);
                        ps.setInt(2, causeId);
                        ps.setString(3, year);
                        ps.setString(4, sex);
                        ps.setString(5, number);
                        ps.setString(6, number);
                        ps.execute();
                        ps.close();
                        break;
                    case "deaths-percent":
                        query = "insert into countryhasvirus(country_idcountry,sanitaryissue_idsanitaryissue,year,sex,deathpercent) values (?,?,?,?,?)"
                                + " ON DUPLICATE KEY UPDATE deathpercent=?;";
                        ps = sqlConnection.prepareStatement(query);
                        ps.setInt(1, countryId);
                        ps.setInt(2, causeId);
                        ps.setString(3, year);
                        ps.setString(4, sex);
                        ps.setString(5, number);
                        ps.setString(6, number);
                        ps.execute();
                        ps.close();
                        break;
                }

            } catch (SQLException se) {
                System.out.println("Failed to insert "+countryId+","+causeId+","+year+","+sex+","+number+","+metric+",");
                throw(se);
            }

        }
    }

}
