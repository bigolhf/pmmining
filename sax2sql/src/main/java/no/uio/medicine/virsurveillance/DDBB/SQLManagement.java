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
import no.uio.medicine.virsurveillance.datamodels.PubmedArticle;
import no.uio.medicine.virsurveillance.datamodels.PubmedAuthor;
import no.uio.medicine.virsurveillance.datamodels.PubmedJournal;

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

        stmt.executeUpdate("insert into test.pubmed_article (title,  publication,pubmedjournal_idpubmedjournal,abstract)" + " values('" + pa.getTitle() + "','" + date + "'," + journalId + ",'" + AbstractText + "')");

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
        stmt.executeUpdate("insert into test.pubmed_article (title,  publication,pubmedjournal_idpubmedjournal,abstract)" + " values('" + pa.getTitle() + "','" + date + "'," + journalId + ",'" + AbstractText + "')");
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
            stmt.executeUpdate("insert into test.pubmed_author (forename, surname, initials)" + " values('" + auth.getForeName() + "','" + auth.getLastName() + "','" + auth.getInitials() + "')");
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
                stmt.executeUpdate("insert into test.pubmed_author (forename, surname, initials)" + " values('" + auth.getForeName() + "','" + auth.getLastName() + "','" + auth.getInitials() + "')");
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
        ResultSet result = stmt.executeQuery("select idpubmed_author from test.pubmed_author where forename='" + auth.getForeName() + "' and surname='" + auth.getLastName() + "';");
        if (result.next()) {
            ret = result.getInt("idpubmed_author");
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
            System.out.println("Duplicat = "+ret + "-" + result.getInt("idpubmedjournal"));            
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

}
