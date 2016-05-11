/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uio.medicine.virsurveillance.DDBB;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import no.uio.medicine.virsurveillance.charts.BoxAndWhiskerChart_AWT;
import no.uio.medicine.virsurveillance.charts.StackedChart_AWT;
import no.uio.medicine.virsurveillance.charts.XYLineChart_AWT;
import no.uio.medicine.virsurveillance.datamodels.PubmedArticle;
import no.uio.medicine.virsurveillance.datamodels.QueryResult;

/**
 *
 * @author Albert
 */
public class SQLQueries {

    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/test";

    private static final String USER = "user1";
    private static final String PASS = "myPass";

    protected Connection sqlConnection;

    protected Map<String, Integer> calendar;

    protected int lowerYearLimit = 1997;
    protected int defaultImpactFactorYear = 2014;

    public final int IF_INCREASE = 1;
    public final int IF_DECREASE = -1;
    public final int IF_INDIFERENT = 0;

    public SQLQueries() {
        defineCalendar();
    }

    public SQLQueries(int lowerYearLimit, int defaultImpactFactorYear) {
        this.lowerYearLimit = lowerYearLimit;
        this.defaultImpactFactorYear = defaultImpactFactorYear;
        defineCalendar();
    }

    public void connect2DB() throws SQLException, ClassNotFoundException {
        Class.forName(JDBC_DRIVER);
        sqlConnection = DriverManager.getConnection(
                DB_URL, USER, PASS);
    }

    public void closeDB() throws SQLException {
        this.sqlConnection.close();
    }

    //Returns an array of pubmedarticles that deal with the topic in the title or in the abstract
    public ArrayList<PubmedArticle> selectArticlesAboutATopic(String topic) throws SQLException {
        ArrayList<PubmedArticle> articleList = new ArrayList<>();
        Statement stmt = sqlConnection.createStatement();
        String query = "select idpubmed_article,pubmed_article.title as title,abstract,year(publication) as pubYear,month(publication) as pubMonth, pubmedjournal.title from test.pubmed_article,test.pubmedjournal where (pubmed_article.title like '%" + topic + "%' or abstract like '%" + topic + "%') and pubmed_article.pubmedjournal_idpubmedjournal=pubmedjournal.idpubmedjournal;";
        System.out.println(query);
        ResultSet queryResult = stmt.executeQuery(query);
        while (queryResult.next()) {
            PubmedArticle art = new PubmedArticle();
            art.setId(queryResult.getString("idpubmed_article"));
            art.setTitle(queryResult.getString("title"));
            art.setPublicationYear(queryResult.getInt("pubYear"));
            art.addAbstract(queryResult.getString("abstract"));
            art.setJournalName(queryResult.getString("pubmedjournal.title"));
            articleList.add(art);
        }
        stmt.close();
        return articleList;
    }

    //returns the number of publications that have been done regarding a topic filtered by year
    public QueryResult countPublicationsAboutATopic(String topic) throws SQLException {
        QueryResult qResult = new QueryResult();

        Statement stmt = sqlConnection.createStatement();
        String query = "select  year(pubmed_article.publication) as year, count(*) as articles from test.pubmed_article where title like '%" + topic + "%' or abstract like '%" + topic + "%' group by year(test.pubmed_article.publication) order by year asc;";
        System.out.println(query);
        ResultSet queryResult = stmt.executeQuery(query);

        boolean first = true;
        int columns = 0;
        while (queryResult.next()) {
            if (first) {
                first = false;
                columns = queryResult.getMetaData().getColumnCount();
                for (int i = 1; i <= columns; i++) {
                    qResult.addHeaderValue(queryResult.getMetaData().getColumnName(i));
                }

            }

            for (int i = 0; i < columns; i++) {
                qResult.addValue(queryResult.getMetaData().getColumnName(i), queryResult.getObject(i).toString());
            }
        }
        stmt.close();
        return qResult;
    }

    //Computes the average impact-factor of a topic along time: Maximum impact factor, average impact factor and minimum impact factor.
    public QueryResult getStatsPerTopic(String topic) throws SQLException {
        return getStatsPerTopic(topic, false);
    }

    //Computes the average impact-factor of a topic along time. Calculates maximum impact factor, average impact factor and minimum impact factor. Creates a XY chart to illustrate the results.
    public QueryResult getStatsPerTopic(String topic, boolean rounded) throws SQLException {
        return getStatsPerTopicIgnoringIFUnder(topic, -99, rounded);
    }

    //Computes the average impact-factor of a topic along time. Calculates maximum impact factor, average impact factor and minimum impact factor. Creates a XY chart to illustrate the results.
    public QueryResult getStatsPerTopicIgnoringIFUnder(String topic, float minIfac) throws SQLException {
        return getStatsPerTopicIgnoringIFUnder(topic, minIfac, false);
    }

    //Computes the average impact-factor of a topic along years; ignores all the publications with an impact factor lower than minIfac. Calculates maximum impact factor, average impact factor and minimum impact factor. Creates a XY chart to illustrate the results.
    public QueryResult getStatsPerTopicIgnoringIFUnder(String topic, float minIfac, boolean rounded) throws SQLException {

        QueryResult qResult = new QueryResult();

        Statement stmt = sqlConnection.createStatement();
        String query = "select\n"
                + "		count(*),\n"
                + "        avg(ifac),max(ifac),min(ifac),std(ifac),\n"
                + "        year(pubmed_article.publication) as pubYear\n"
                + "	from test.pubmedjournal,test.pubmed_article,test.impactfactor\n"
                + "    where (pubmed_article.title like '%" + topic + "%' or pubmed_article.abstract like '%" + topic + "%') \n"
                + "		and pubmed_article.pubmedjournal_idpubmedjournal = pubmedjournal.idpubmedjournal \n"
                + "        and impactfactor.pubmedjournal_idpubmedjournal=pubmedjournal.idpubmedjournal\n"
                + "        and impactfactor.date = year(pubmed_article.publication)\n"
                + "        and impactfactor.ifac >" + minIfac + "\n"
                + "	group by pubYear"
                + "     order by pubYear asc;";
        System.out.println(query);
        ResultSet sqlQueryResult = stmt.executeQuery(query);
        boolean first = true;
        int columns = 0;
        ArrayList<ArrayList<Float>> dataPoints = new ArrayList<>();
        ArrayList<ArrayList<Float>> years = new ArrayList<>();
        ArrayList<String> titles = new ArrayList<>();

        final int AVG = 0;
        final int MAX = 1;
        final int MIN = 2;
        final int upSDV = 3;
        final int downSDV = 4;

        titles.add("Avg. impact factor");
        dataPoints.add(new ArrayList<Float>());
        years.add(new ArrayList<Float>());
        titles.add("Max. impact factor");
        dataPoints.add(new ArrayList<Float>());
        years.add(new ArrayList<Float>());
        titles.add("Min. impact factor");
        dataPoints.add(new ArrayList<Float>());
        years.add(new ArrayList<Float>());
        titles.add("+1 stdev");
        dataPoints.add(new ArrayList<Float>());
        years.add(new ArrayList<Float>());
        titles.add("-1 stdev");
        dataPoints.add(new ArrayList<Float>());
        years.add(new ArrayList<Float>());

        while (sqlQueryResult.next()) {
            if (first) {
                first = false;
                columns = sqlQueryResult.getMetaData().getColumnCount();
                for (int i = 1; i <= columns; i++) {
                    qResult.addHeaderValue(sqlQueryResult.getMetaData().getColumnName(i));
                }

            }
            float avg = 0;
            for (int i = 1; i <= columns; i++) {

                if (sqlQueryResult.getMetaData().getColumnName(i).equalsIgnoreCase("avg(ifac)")) {
                    dataPoints.get(AVG).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                    avg = Float.parseFloat(sqlQueryResult.getObject(i).toString());
                }
                if (sqlQueryResult.getMetaData().getColumnName(i).equalsIgnoreCase("min(ifac)")) {
                    dataPoints.get(MIN).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                }
                if (sqlQueryResult.getMetaData().getColumnName(i).equalsIgnoreCase("max(ifac)")) {
                    dataPoints.get(MAX).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                }
                if (sqlQueryResult.getMetaData().getColumnName(i).equalsIgnoreCase("std(ifac)")) {
                    float sdev = Float.parseFloat(sqlQueryResult.getObject(i).toString());
                    dataPoints.get(upSDV).add(avg + sdev);
                    dataPoints.get(downSDV).add(avg - sdev);
                }
                if (sqlQueryResult.getMetaData().getColumnName(i).equalsIgnoreCase("pubYear")) {
                    years.get(AVG).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                    years.get(MAX).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                    years.get(MIN).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                    years.get(upSDV).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                    years.get(downSDV).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                }

                qResult.addValue(sqlQueryResult.getMetaData().getColumnName(i), sqlQueryResult.getObject(i).toString());
                //System.out.print(sqlQueryResult.getObject(i).toString() + "\t\t");
            }

        }
        stmt.close();

        qResult.setXyLineChart(new XYLineChart_AWT("Impact Factor Information: " + topic,
                topic + ": Impact Factor per Year", "Impact Factor", "Year", dataPoints, years, titles));

        //System.out.println(qResult.toString());
        return qResult;
    }

    /* 
    //Computes the average impact-factor of a topic along time: Maximum impact factor, average impact factor and minimum impact factor.
    public QueryResult getRelativeStatsPerTopic(String topic) throws SQLException {
        return getRelativeStatsPerTopic(topic, false);
    }

    //Computes the average impact-factor of a topic along time. Calculates maximum impact factor, average impact factor and minimum impact factor. Creates a XY chart to illustrate the results.
    public QueryResult getRelativeStatsPerTopic(String topic, boolean rounded) throws SQLException {
        return getRelativeStatsPerTopicIgnoringIFUnder(topic, -99, rounded);
    }

    //Computes the average impact-factor of a topic along time. Calculates maximum impact factor, average impact factor and minimum impact factor. Creates a XY chart to illustrate the results.
    public QueryResult getRelativeStatsPerTopicIgnoringIFUnder(String topic, float minIfac) throws SQLException {
        return getRelativeStatsPerTopicIgnoringIFUnder(topic, minIfac, false);
    }

    //Computes the average impact-factor of a topic along years; ignores all the publications with an impact factor lower than minIfac. Calculates maximum impact factor, average impact factor and minimum impact factor. Creates a XY chart to illustrate the results.
    public QueryResult getRelativeStatsPerTopicIgnoringIFUnder(String topic, float minIfac, boolean rounded) throws SQLException {

        QueryResult qResult = new QueryResult();

        Statement stmt = sqlConnection.createStatement();

        String query = "select\n"
                + "		count(*),\n"
                + "        avg(ifac),max(ifac),min(ifac),std(ifac),\n"
                + "        year(pubmed_article.publication) as pubYear\n"
                + "	from test.pubmedjournal,test.pubmed_article,test.impactfactor\n"
                + "    where (pubmed_article.title like '%" + topic + "%' or pubmed_article.abstract like '%" + topic + "%') \n"
                + "		and pubmed_article.pubmedjournal_idpubmedjournal = pubmedjournal.idpubmedjournal \n"
                + "        and impactfactor.pubmedjournal_idpubmedjournal=pubmedjournal.idpubmedjournal\n"
                + "        and impactfactor.date = year(pubmed_article.publication)\n"
                + "        and impactfactor.ifac >" + minIfac + "\n"
                + "	group by pubYear"
                + "     order by pubYear asc;";
        System.out.println(query);
        ResultSet sqlQueryResult = stmt.executeQuery(query);
        boolean first = true;
        int columns = 0;
        ArrayList<ArrayList<Float>> dataPoints = new ArrayList<>();
        ArrayList<ArrayList<Float>> years = new ArrayList<>();
        ArrayList<String> titles = new ArrayList<>();

        final int AVG = 0;
        final int MAX = 1;
        final int MIN = 2;

        titles.add("Avg. impact factor");
        dataPoints.add(new ArrayList<Float>());
        years.add(new ArrayList<Float>());
        titles.add("Max. impact factor");
        dataPoints.add(new ArrayList<Float>());
        years.add(new ArrayList<Float>());
        titles.add("Min. impact factor");
        dataPoints.add(new ArrayList<Float>());
        years.add(new ArrayList<Float>());

        while (sqlQueryResult.next()) {
            if (first) {
                first = false;
                columns = sqlQueryResult.getMetaData().getColumnCount();
                for (int i = 1; i <= columns; i++) {
                    qResult.addHeaderValue(sqlQueryResult.getMetaData().getColumnName(i));
                }

            }
            for (int i = 1; i <= columns; i++) {
                if (sqlQueryResult.getMetaData().getColumnName(i).equalsIgnoreCase("avg(ifac)")) {
                    dataPoints.get(AVG).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                }
                if (sqlQueryResult.getMetaData().getColumnName(i).equalsIgnoreCase("min(ifac)")) {
                    dataPoints.get(MIN).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                }
                if (sqlQueryResult.getMetaData().getColumnName(i).equalsIgnoreCase("max(ifac)")) {
                    dataPoints.get(MAX).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                }
                if (sqlQueryResult.getMetaData().getColumnName(i).equalsIgnoreCase("pubYear")) {
                    years.get(AVG).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                    years.get(MAX).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                    years.get(MIN).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                }

                qResult.addValue(sqlQueryResult.getMetaData().getColumnName(i), sqlQueryResult.getObject(i).toString());
                //System.out.print(sqlQueryResult.getObject(i).toString() + "\t\t");
            }

        }
        stmt.close();

        qResult.setXyLineChart(new XYLineChart_AWT("Information regarding " + topic,
                topic + ": Impact Factor per Year", "Year", "Impact Factor", dataPoints, years, titles));

        //System.out.println(qResult.toString());
        return qResult;
    }
     */
    //Computes the quality of the publications of a topic along time. Counts how many papers of a particular topic have been published to different quality (impact factor) journals. It classificates the publications into low impact factor (<lowerTH), medium (>=lowerTH <=upperTH), high (>upperTH).
    public QueryResult getPublicationCount(String topic, float lowerTH, float upperTH, boolean rounded) throws SQLException {
        QueryResult qResult = new QueryResult();

        Statement stmt = sqlConnection.createStatement();
        String query = "select\n"
                + "		count(*) as total, \n"
                + "        SUM(CASE WHEN impactfactor.ifac < " + lowerTH + " THEN 1 ELSE 0 END) as 'low', \n"
                + "        SUM(CASE WHEN (impactfactor.ifac >= " + lowerTH + " and impactfactor.ifac <" + upperTH + ") THEN 1 ELSE 0 END) as 'medium', \n"
                + "        SUM(CASE WHEN impactfactor.ifac >= " + upperTH + " THEN 1 ELSE 0 END) as 'high', \n"
                + "        year(pubmed_article.publication) as pubYear\n"
                + "       \n"
                + "	from test.pubmedjournal,test.pubmed_article,test.impactfactor\n"
                + "    where \n"
                + "		(pubmed_article.title like '%" + topic + "%' or pubmed_article.abstract like '%" + topic + "%') 		and \n"
                + "        pubmed_article.pubmedjournal_idpubmedjournal = pubmedjournal.idpubmedjournal         and \n"
                + "        impactfactor.pubmedjournal_idpubmedjournal=pubmedjournal.idpubmedjournal\n"
                + "        and impactfactor.date = year(pubmed_article.publication)       \n"
                + "        and impactfactor.ifac > 0\n"
                + "	group by year(pubmed_article.publication)"
                + "     order by pubYear asc;";
        System.out.println(query);
        ResultSet sqlQueryResult = stmt.executeQuery(query);
        boolean first = true;
        int columns = 0;
        ArrayList<ArrayList<Float>> dataPoints = new ArrayList<>();
        ArrayList<ArrayList<Float>> years = new ArrayList<>();
        ArrayList<String> titles = new ArrayList<>();

        titles.add("Published articles");
        dataPoints.add(new ArrayList<Float>());
        years.add(new ArrayList<Float>());
        titles.add("Low impact factor articles (ifac<" + lowerTH + ")");
        dataPoints.add(new ArrayList<Float>());
        years.add(new ArrayList<Float>());
        titles.add("Medium impact factor articles (" + lowerTH + "<ifac>" + upperTH + ")");
        dataPoints.add(new ArrayList<Float>());
        years.add(new ArrayList<Float>());
        titles.add("High impact factor articles (ifac>" + upperTH + ")");
        dataPoints.add(new ArrayList<Float>());
        years.add(new ArrayList<Float>());

        final int TOT = 0;
        final int LOW = 1;
        final int MED = 2;
        final int HIG = 3;

        while (sqlQueryResult.next()) {
            if (first) {
                first = false;
                columns = sqlQueryResult.getMetaData().getColumnCount();
                for (int i = 1; i <= columns; i++) {
                    qResult.addHeaderValue(sqlQueryResult.getMetaData().getColumnName(i));
                }

            }
            for (int i = 1; i <= columns; i++) {
                if (sqlQueryResult.getMetaData().getColumnName(i).equalsIgnoreCase("total")) {
                    dataPoints.get(TOT).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                }
                if (sqlQueryResult.getMetaData().getColumnName(i).equalsIgnoreCase("low")) {
                    dataPoints.get(LOW).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                }
                if (sqlQueryResult.getMetaData().getColumnName(i).equalsIgnoreCase("medium")) {
                    dataPoints.get(MED).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                }
                if (sqlQueryResult.getMetaData().getColumnName(i).equalsIgnoreCase("high")) {
                    dataPoints.get(HIG).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                }
                if (sqlQueryResult.getMetaData().getColumnName(i).equalsIgnoreCase("pubYear")) {
                    years.get(TOT).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                    years.get(LOW).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                    years.get(MED).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                    years.get(HIG).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                }

                qResult.addValue(sqlQueryResult.getMetaData().getColumnName(i), sqlQueryResult.getObject(i).toString());
                //System.out.print(sqlQueryResult.getObject(i).toString() + "\t\t");
            }

        }
        stmt.close();

        qResult.setXyLineChart(new XYLineChart_AWT("Publications per year regarding " + topic,
                topic + ": Publications per Year", "Publications", "Year", dataPoints, years, titles));

        ArrayList<ArrayList<Float>> dataPoints2 = new ArrayList<>();
        dataPoints2.add(dataPoints.get(LOW));
        dataPoints2.add(dataPoints.get(MED));
        dataPoints2.add(dataPoints.get(HIG));

        ArrayList<String> titles2 = new ArrayList<>();
        titles2.add(titles.get(LOW));
        titles2.add(titles.get(MED));
        titles2.add(titles.get(HIG));
        qResult.setStackedChart(new StackedChart_AWT("Publications per year regarding " + topic,
                topic + ": Publications per Year", "Publications", "Year", dataPoints2, years, titles2, dataPoints.size() - dataPoints2.size()));

        return qResult;
    }

    //Computes the quality of the publications of a topic along time. Counts how many papers of a particular topic have been published to different quality (impact factor) journals. It classificates the publications into low impact factor (<4), medium (>=4 <=10), high (>10).
    public QueryResult getPublicationCount(String topic) throws SQLException {
        return SQLQueries.this.getPublicationCount(topic, 4, 10, false);
    }

    //Computes the quality of the publications of a topic along time. Counts the % papers of a particular topic have been published to different quality (impact factor) journals. It classificates the publications into low impact factor (<lowerTH), medium (>=lowerTH <=upperTH), high (>upperTH).
    public QueryResult getPublicationCountSelfnormalized(String topic, float lowerTH, float upperTH, boolean rounded) throws SQLException {
        QueryResult qResult = new QueryResult();

        Statement stmt = sqlConnection.createStatement();
        String query = "select\n"
                + "		count(*)/count(*) as total, \n"
                + "        SUM(CASE WHEN impactfactor.ifac < " + lowerTH + " THEN 1 ELSE 0 END)/count(*) as 'low', \n"
                + "        SUM(CASE WHEN (impactfactor.ifac >= " + lowerTH + " and impactfactor.ifac <" + upperTH + ") THEN 1 ELSE 0 END)/count(*) as 'medium', \n"
                + "        SUM(CASE WHEN impactfactor.ifac >= " + upperTH + " THEN 1 ELSE 0 END)/count(*) as 'high', \n"
                + "        year(pubmed_article.publication) as pubYear\n"
                + "       \n"
                + "	from test.pubmedjournal,test.pubmed_article,test.impactfactor\n"
                + "    where \n"
                + "		(pubmed_article.title like '%" + topic + "%' or pubmed_article.abstract like '%" + topic + "%') 		and \n"
                + "        pubmed_article.pubmedjournal_idpubmedjournal = pubmedjournal.idpubmedjournal         and \n"
                + "        impactfactor.pubmedjournal_idpubmedjournal=pubmedjournal.idpubmedjournal\n"
                + "        and impactfactor.date = year(pubmed_article.publication)       \n"
                + "        and impactfactor.ifac > 0\n"
                + "	group by year(pubmed_article.publication)"
                + "     order by year(pubmed_article.publication) asc;";
        System.out.println(query);
        ResultSet sqlQueryResult = stmt.executeQuery(query);
        boolean first = true;
        int columns = 0;
        ArrayList<ArrayList<Float>> dataPoints = new ArrayList<>();
        ArrayList<ArrayList<Float>> years = new ArrayList<>();
        ArrayList<String> titles = new ArrayList<>();

        titles.add("% Published articles");
        dataPoints.add(new ArrayList<Float>());
        years.add(new ArrayList<Float>());
        titles.add("% Low impact factor articles (ifac<" + lowerTH + ")");
        dataPoints.add(new ArrayList<Float>());
        years.add(new ArrayList<Float>());
        titles.add("% Medium impact factor articles (" + lowerTH + "<ifac>" + upperTH + ")");
        dataPoints.add(new ArrayList<Float>());
        years.add(new ArrayList<Float>());
        titles.add("% High impact factor articles (ifac>" + upperTH + ")");
        dataPoints.add(new ArrayList<Float>());
        years.add(new ArrayList<Float>());

        final int TOT = 0;
        final int LOW = 1;
        final int MED = 2;
        final int HIG = 3;

        while (sqlQueryResult.next()) {
            if (first) {
                first = false;
                columns = sqlQueryResult.getMetaData().getColumnCount();
                for (int i = 1; i <= columns; i++) {
                    qResult.addHeaderValue(sqlQueryResult.getMetaData().getColumnName(i));
                }

            }
            for (int i = 1; i <= columns; i++) {
                if (sqlQueryResult.getMetaData().getColumnName(i).equalsIgnoreCase("total")) {
                    dataPoints.get(TOT).add(100 * Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                }
                if (sqlQueryResult.getMetaData().getColumnName(i).equalsIgnoreCase("low")) {
                    dataPoints.get(LOW).add(100 * Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                }
                if (sqlQueryResult.getMetaData().getColumnName(i).equalsIgnoreCase("medium")) {
                    dataPoints.get(MED).add(100 * Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                }
                if (sqlQueryResult.getMetaData().getColumnName(i).equalsIgnoreCase("high")) {
                    dataPoints.get(HIG).add(100 * Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                }
                if (sqlQueryResult.getMetaData().getColumnName(i).equalsIgnoreCase("pubYear")) {
                    years.get(TOT).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                    years.get(LOW).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                    years.get(MED).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                    years.get(HIG).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                }

                qResult.addValue(sqlQueryResult.getMetaData().getColumnName(i), sqlQueryResult.getObject(i).toString());
                //System.out.print(sqlQueryResult.getObject(i).toString() + "\t\t");
            }

        }
        stmt.close();

        qResult.setXyLineChart(new XYLineChart_AWT("% of publications per year regarding " + topic,
                topic + ": Publications per Year normalized by topic", "% Publications", "Year", dataPoints, years, titles));

        ArrayList<ArrayList<Float>> dataPoints2 = new ArrayList<>();
        dataPoints2.add(dataPoints.get(LOW));
        dataPoints2.add(dataPoints.get(MED));
        dataPoints2.add(dataPoints.get(HIG));

        ArrayList<String> titles2 = new ArrayList<>();
        titles2.add(titles.get(LOW));
        titles2.add(titles.get(MED));
        titles2.add(titles.get(HIG));
        qResult.setStackedChart(new StackedChart_AWT("% of publications per year regarding " + topic,
                topic + ": Publications per Year normalized by topic", "% Publications", "Year", dataPoints2, years, titles2, dataPoints.size() - dataPoints2.size()));

        return qResult;
    }

    //Computes the quality of the publications of a topic along time. Counts the % papers of a particular topic have been published to different quality (impact factor) journals. It classificates the publications into low impact factor (<4), medium (>=4 <=10), high (>10).
    public QueryResult getPublicationCountSelfNormalized(String topic) throws SQLException {
        return SQLQueries.this.getPublicationCountSelfnormalized(topic, 4, 10, false);
    }

    //Computes the quality of the publications of a topic along time. Counts the % papers of a particular topic have been published to different quality (impact factor) journals. It classificates the publications into low impact factor (<lowerTH), medium (>=lowerTH <=upperTH), high (>upperTH).
    public QueryResult getRelativePublicationCount(String topic, float lowerTH, float upperTH, boolean rounded) throws SQLException {
        QueryResult qResult = new QueryResult();

        Statement stmt = sqlConnection.createStatement();
        String query = "select\n"
                + "        year(pubmed_article.publication) as pubYear,\n"
                + "        (select count(*) from test.pubmed_article where year(pubmed_article.publication)=pubYear) as totalPubsThisYear,\n"
                + "	   count(*) as totalPubsAboutTopic, \n"
                + "        SUM(CASE WHEN impactfactor.ifac < " + lowerTH + " THEN 1 ELSE 0 END) as 'low', \n"
                + "        SUM(CASE WHEN (impactfactor.ifac >= " + lowerTH + " and impactfactor.ifac <" + upperTH + ") THEN 1 ELSE 0 END) as 'medium', \n"
                + "        SUM(CASE WHEN impactfactor.ifac >= " + upperTH + " THEN 1 ELSE 0 END) as 'high' \n"
                + "       \n"
                + "	from test.pubmedjournal,test.pubmed_article,test.impactfactor\n"
                + "    where \n"
                + "		(pubmed_article.title like '%" + topic + "%' or pubmed_article.abstract like '%" + topic + "%') 		and \n"
                + "        pubmed_article.pubmedjournal_idpubmedjournal = pubmedjournal.idpubmedjournal         and \n"
                + "        impactfactor.pubmedjournal_idpubmedjournal=pubmedjournal.idpubmedjournal\n"
                + "        and impactfactor.date = year(pubmed_article.publication)       \n"
                + "        and impactfactor.ifac > 0\n"
                + "	group by year(pubmed_article.publication)"
                + "     order by pubYear asc;";
        System.out.println(query);
        ResultSet sqlQueryResult = stmt.executeQuery(query);
        boolean first = true;
        int columns = 0;
        ArrayList<ArrayList<Float>> dataPoints = new ArrayList<>();
        ArrayList<ArrayList<Float>> years = new ArrayList<>();
        ArrayList<String> titles = new ArrayList<>();

        titles.add("Published articles");
        dataPoints.add(new ArrayList<Float>());
        years.add(new ArrayList<Float>());
        titles.add("Low impact factor articles (ifac<" + lowerTH + ")");
        dataPoints.add(new ArrayList<Float>());
        years.add(new ArrayList<Float>());
        titles.add("Medium impact factor articles (" + lowerTH + "<ifac>" + upperTH + ")");
        dataPoints.add(new ArrayList<Float>());
        years.add(new ArrayList<Float>());
        titles.add("High impact factor articles (ifac>" + upperTH + ")");
        dataPoints.add(new ArrayList<Float>());
        years.add(new ArrayList<Float>());

        final int TOT = 0;
        final int LOW = 1;
        final int MED = 2;
        final int HIG = 3;

        while (sqlQueryResult.next()) {
            if (first) {
                first = false;
                columns = sqlQueryResult.getMetaData().getColumnCount();
                for (int i = 1; i <= columns; i++) {
                    qResult.addHeaderValue(sqlQueryResult.getMetaData().getColumnName(i));
                }

            }
            float pubsThisYear = 1;
            for (int i = 1; i <= columns; i++) {
                if (sqlQueryResult.getMetaData().getColumnName(i).equalsIgnoreCase("totalPubsThisYear")) {
                    pubsThisYear = (Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                    //System.out.println("PubsThisYear="+pubsThisYear);
                }
                if (sqlQueryResult.getMetaData().getColumnName(i).equalsIgnoreCase("totalPubsAboutTopic")) {
                    dataPoints.get(TOT).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()) / pubsThisYear);
                    //System.out.println("PubsThisYear="+pubsThisYear+"   Pubs of the topic: "+Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                }
                if (sqlQueryResult.getMetaData().getColumnName(i).equalsIgnoreCase("low")) {
                    dataPoints.get(LOW).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()) / pubsThisYear);
                    //System.out.println("PubsThisYear="+pubsThisYear+"   Pubs of the topic: "+Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                }
                if (sqlQueryResult.getMetaData().getColumnName(i).equalsIgnoreCase("medium")) {
                    dataPoints.get(MED).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()) / pubsThisYear);
                    //System.out.println("PubsThisYear="+pubsThisYear+"   Pubs of the topic: "+Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                }
                if (sqlQueryResult.getMetaData().getColumnName(i).equalsIgnoreCase("high")) {
                    dataPoints.get(HIG).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()) / pubsThisYear);
                    System.out.println("PubsThisYear=" + pubsThisYear + "   Pubs of the topic: " + Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                }
                if (sqlQueryResult.getMetaData().getColumnName(i).equalsIgnoreCase("pubYear")) {
                    years.get(TOT).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                    years.get(LOW).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                    years.get(MED).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                    years.get(HIG).add(Float.parseFloat(sqlQueryResult.getObject(i).toString()));
                }

                qResult.addValue(sqlQueryResult.getMetaData().getColumnName(i), sqlQueryResult.getObject(i).toString());
                //System.out.print(sqlQueryResult.getObject(i).toString() + "\t\t");
            }

        }
        stmt.close();

        qResult.setXyLineChart(new XYLineChart_AWT("Publications per year regarding " + topic,
                topic + ": Publications per Year normalized by total publications", "Publications", "Year", dataPoints, years, titles));

        ArrayList<ArrayList<Float>> dataPoints2 = new ArrayList<>();
        dataPoints2.add(dataPoints.get(LOW));
        dataPoints2.add(dataPoints.get(MED));
        dataPoints2.add(dataPoints.get(HIG));

        ArrayList<String> titles2 = new ArrayList<>();
        titles2.add(titles.get(LOW));
        titles2.add(titles.get(MED));
        titles2.add(titles.get(HIG));
        qResult.setStackedChart(new StackedChart_AWT("Publications per year regarding " + topic,
                topic + ": Publications per Year normalized by total publications", "% Publications", "Year", dataPoints2, years, titles2, dataPoints.size() - dataPoints2.size()));

        return qResult;
    }

    //Computes the quality of the publications of a topic along time. Counts the % papers of a particular topic have been published to different quality (impact factor) journals. It classificates the publications into low impact factor (<4), medium (>=4 <=10), high (>10).
    public QueryResult getRelativePublicationCount(String topic) throws SQLException {
        return SQLQueries.this.getRelativePublicationCount(topic, 4, 10, false);
    }

    public QueryResult getJournalImpactFactors() throws SQLException {
        String query = "select impactfactor.ifac, pubmedjournal.abbreviated_title from test.impactfactor, test.pubmedjournal where impactfactor.pubmedjournal_idpubmedjournal=pubmedjournal.idpubmedjournal order by pubmedjournal.abbreviated_title;";
        QueryResult qResult = new QueryResult();
        Statement stmt = sqlConnection.createStatement();
        System.out.println(query);
        ResultSet sqlQueryResult = stmt.executeQuery(query);

        String currentJournal = "";

        ArrayList<ArrayList<Float>> impactFactors = new ArrayList<>();
        ArrayList<String> journals = new ArrayList<>();
        boolean first = true;
        int count = -1;

        while (sqlQueryResult.next()) {
            if (first) {
                for (int i = 1; i <= sqlQueryResult.getMetaData().getColumnCount(); i++) {
                    qResult.addHeaderValue(sqlQueryResult.getMetaData().getColumnName(i));
                }
                first = false;

            }

            if (!currentJournal.equalsIgnoreCase(sqlQueryResult.getObject(2).toString())) {
                count++;
                impactFactors.add(new ArrayList<>());

                currentJournal = sqlQueryResult.getObject(2).toString();
                journals.add(currentJournal);
            }
            impactFactors.get(count).add(Float.parseFloat(sqlQueryResult.getObject(1).toString()));

            qResult.addValue(sqlQueryResult.getMetaData().getColumnName(1), sqlQueryResult.getObject(1).toString());
            qResult.addValue(sqlQueryResult.getMetaData().getColumnName(2), sqlQueryResult.getObject(2).toString());

        }
        BoxAndWhiskerChart_AWT boxplotChart = new BoxAndWhiskerChart_AWT("Journals Impact Factor", "Journals Impact Factor", "Journal", "Impact Factor", impactFactors, journals, "Impact Factor");

        qResult.setBoxplotChart(boxplotChart);

        return qResult;
    }

    public QueryResult getJournalWithVariableImpactFactors(float minimumVariability) throws SQLException {

        String query = "select impactfactor.ifac, pubmedjournal.abbreviated_title from test.impactfactor, test.pubmedjournal where impactfactor.pubmedjournal_idpubmedjournal=pubmedjournal.idpubmedjournal order by pubmedjournal.abbreviated_title;";
        QueryResult qResult = new QueryResult();
        Statement stmt = sqlConnection.createStatement();
        System.out.println(query);
        ResultSet sqlQueryResult = stmt.executeQuery(query);

        String currentJournal = "";

        ArrayList<ArrayList<Float>> impactFactors = new ArrayList<>();
        ArrayList<String> journals = new ArrayList<>();
        boolean first = true;
        int count = -1;

        while (sqlQueryResult.next()) {
            if (first) {
                for (int i = 1; i <= sqlQueryResult.getMetaData().getColumnCount(); i++) {
                    qResult.addHeaderValue(sqlQueryResult.getMetaData().getColumnName(i));
                }
                first = false;

            }

            if (!currentJournal.equalsIgnoreCase(sqlQueryResult.getObject(2).toString())) {
                count++;
                impactFactors.add(new ArrayList<>());

                currentJournal = sqlQueryResult.getObject(2).toString();
                journals.add(currentJournal);
            }
            impactFactors.get(count).add(Float.parseFloat(sqlQueryResult.getObject(1).toString()));

        }

        ArrayList<Integer> toBeRemoved = new ArrayList<>();
        System.out.println("Journals with a minimum impact factor variation of " + minimumVariability);
        for (String journal : journals) {
            int ind = journals.indexOf(journal);
            ArrayList<Float> impacts = impactFactors.get(ind);
            float dif = Collections.max(impacts) - Collections.min(impacts);
            if (dif >= minimumVariability) {
                for (float f : impacts) {
                    qResult.addValue(qResult.getHeader().get(0), f + "");
                    qResult.addValue(qResult.getHeader().get(1), journal);
                }
                System.out.println(journal + "\t" + dif);
            } else {
                toBeRemoved.add(ind);
            }

        }
        System.out.println();
        Collections.sort(toBeRemoved);
        Collections.reverse(toBeRemoved);
        for (int r : toBeRemoved) {
            journals.remove(r);
            impactFactors.remove(r);
        }

        BoxAndWhiskerChart_AWT boxplotChart = new BoxAndWhiskerChart_AWT("Journals Impact Factor Variabiilty Info", "Journals with a minimum I.F. variation of " + minimumVariability, "Journal", "Impact Factor", impactFactors, journals, "Impact Factor");

        qResult.setBoxplotChart(boxplotChart);

        return qResult;

    }

    public QueryResult getJournalWithVariableImpactFactors(float minimumVariability, int behaviour) throws SQLException {
        switch (behaviour) {
            case -1:
                return getJournalWithDecreasingImpactFactors(minimumVariability);
            case 0:
                return getJournalWithVariableImpactFactors(minimumVariability);                
            case 1:
                return getJournalWithIncreasingImpactFactors(minimumVariability);                
        }
        
        return null;

    }

    public QueryResult getJournalEvolution(Collection<String> journalNames) throws SQLException {

        QueryResult qResult = new QueryResult();

        ArrayList<String> journals = new ArrayList<>();
        ArrayList<ArrayList<Float>> impacts = new ArrayList<>();
        ArrayList<ArrayList<Float>> years = new ArrayList<>();

        boolean first = true;
        int columns = 0;
        for (String journalN : journalNames) {

            ArrayList<Float> currentImpacts = new ArrayList<>();
            ArrayList<Float> currentYears = new ArrayList<>();

            String query = "select impactFactor.ifac, impactfactor.date from test.impactfactor, test.pubmedjournal where pubmedjournal.idpubmedjournal=impactfactor.pubmedjournal_idpubmedjournal and (pubmedjournal.title='" + journalN + "' or pubmedjournal.abbreviated_title='" + journalN + "');";
            Statement stmt = sqlConnection.createStatement();
            System.out.println(query);
            ResultSet sqlQueryResult = stmt.executeQuery(query);

            while (sqlQueryResult.next()) {

                if (first) {
                    first = false;
                    columns = sqlQueryResult.getMetaData().getColumnCount();
                    for (int i = 1; i <= columns; i++) {
                        qResult.addHeaderValue(sqlQueryResult.getMetaData().getColumnName(i));

                    }
                    qResult.addHeaderValue("Journal");
                }
                qResult.addValue(sqlQueryResult.getMetaData().getColumnName(1), sqlQueryResult.getString(1));
                currentImpacts.add(Float.parseFloat(sqlQueryResult.getString(1)));

                qResult.addValue(sqlQueryResult.getMetaData().getColumnName(2), sqlQueryResult.getString(2));
                currentYears.add(Float.parseFloat(sqlQueryResult.getString(2)));
                qResult.addValue("Journal", journalN);
            }
            journals.add(journalN);
            impacts.add(currentImpacts);
            years.add(currentYears);

        }

        XYLineChart_AWT chart = new XYLineChart_AWT("Journals evolution", "Journals evolution",
                "Year", "Impact Factor", impacts,
                years, journals);

        qResult.setXyLineChart(chart);

        return qResult;
    }

    private void defineCalendar() {
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

    private QueryResult getJournalWithIncreasingImpactFactors(float minimumVariability) throws SQLException {
        String query = "select impactfactor.ifac, pubmedjournal.abbreviated_title from test.impactfactor, test.pubmedjournal where impactfactor.pubmedjournal_idpubmedjournal=pubmedjournal.idpubmedjournal order by pubmedjournal.abbreviated_title;";
        QueryResult qResult = new QueryResult();
        Statement stmt = sqlConnection.createStatement();
        System.out.println(query);
        ResultSet sqlQueryResult = stmt.executeQuery(query);

        String currentJournal = "";

        ArrayList<ArrayList<Float>> impactFactors = new ArrayList<>();
        ArrayList<String> journals = new ArrayList<>();
        boolean first = true;
        int count = -1;

        while (sqlQueryResult.next()) {
            if (first) {
                for (int i = 1; i <= sqlQueryResult.getMetaData().getColumnCount(); i++) {
                    qResult.addHeaderValue(sqlQueryResult.getMetaData().getColumnName(i));
                }
                first = false;

            }

            if (!currentJournal.equalsIgnoreCase(sqlQueryResult.getObject(2).toString())) {
                count++;
                impactFactors.add(new ArrayList<>());

                currentJournal = sqlQueryResult.getObject(2).toString();
                journals.add(currentJournal);
            }
            impactFactors.get(count).add(Float.parseFloat(sqlQueryResult.getObject(1).toString()));

        }

        ArrayList<Integer> toBeRemoved = new ArrayList<>();
        System.out.println("Journals with a minimum impact factor variation of " + minimumVariability);
        for (String journal : journals) {
            int ind = journals.indexOf(journal);
            ArrayList<Float> impacts = impactFactors.get(ind);
            
            float gradient=0;
            int i;
            for (i=1; i<impacts.size();i++){
                gradient=gradient+(impacts.get(i)-impacts.get(i-1));
            }
            gradient=gradient/i;
            
            float dif = Collections.max(impacts) - Collections.min(impacts);
            if (gradient >0 && dif >= minimumVariability) {
                for (float f : impacts) {
                    qResult.addValue(qResult.getHeader().get(0), f + "");
                    qResult.addValue(qResult.getHeader().get(1), journal);
                }
                System.out.println(journal + "\t" + dif);
            } else {
                toBeRemoved.add(ind);
            }

        }
        System.out.println();
        Collections.sort(toBeRemoved);
        Collections.reverse(toBeRemoved);
        for (int r : toBeRemoved) {
            journals.remove(r);
            impactFactors.remove(r);
        }

        BoxAndWhiskerChart_AWT boxplotChart = new BoxAndWhiskerChart_AWT("Journals Impact Factor Variabiilty Info", "Journals with an increasing I.F. (minimum variation of " + minimumVariability+")", "Journal", "Impact Factor", impactFactors, journals, "Impact Factor");

        qResult.setBoxplotChart(boxplotChart);

        return qResult;
    }

    public QueryResult getJournalWithDecreasingImpactFactors(float minimumVariability) throws SQLException {
        String query = "select impactfactor.ifac, pubmedjournal.abbreviated_title from test.impactfactor, test.pubmedjournal where impactfactor.pubmedjournal_idpubmedjournal=pubmedjournal.idpubmedjournal order by pubmedjournal.abbreviated_title;";
        QueryResult qResult = new QueryResult();
        Statement stmt = sqlConnection.createStatement();
        System.out.println(query);
        ResultSet sqlQueryResult = stmt.executeQuery(query);

        String currentJournal = "";

        ArrayList<ArrayList<Float>> impactFactors = new ArrayList<>();
        ArrayList<String> journals = new ArrayList<>();
        boolean first = true;
        int count = -1;

        while (sqlQueryResult.next()) {
            if (first) {
                for (int i = 1; i <= sqlQueryResult.getMetaData().getColumnCount(); i++) {
                    qResult.addHeaderValue(sqlQueryResult.getMetaData().getColumnName(i));
                }
                first = false;

            }

            if (!currentJournal.equalsIgnoreCase(sqlQueryResult.getObject(2).toString())) {
                count++;
                impactFactors.add(new ArrayList<>());

                currentJournal = sqlQueryResult.getObject(2).toString();
                journals.add(currentJournal);
            }
            impactFactors.get(count).add(Float.parseFloat(sqlQueryResult.getObject(1).toString()));

        }

        ArrayList<Integer> toBeRemoved = new ArrayList<>();
        System.out.println("Journals with a minimum impact factor variation of " + minimumVariability);
        for (String journal : journals) {
            int ind = journals.indexOf(journal);
            ArrayList<Float> impacts = impactFactors.get(ind);
            
            float gradient=0;
            int i;
            for (i=1; i<impacts.size();i++){
                gradient=gradient+(impacts.get(i)-impacts.get(i-1));
            }
            gradient=gradient/i;
            
            float dif = Collections.max(impacts) - Collections.min(impacts);
            if (gradient < 0 && dif >= minimumVariability) {
                for (float f : impacts) {
                    qResult.addValue(qResult.getHeader().get(0), f + "");
                    qResult.addValue(qResult.getHeader().get(1), journal);
                }
                System.out.println(journal + "\t" + dif);
            } else {
                toBeRemoved.add(ind);
            }

        }
        System.out.println();
        Collections.sort(toBeRemoved);
        Collections.reverse(toBeRemoved);
        for (int r : toBeRemoved) {
            journals.remove(r);
            impactFactors.remove(r);
        }

        BoxAndWhiskerChart_AWT boxplotChart = new BoxAndWhiskerChart_AWT("Journals Impact Factor Variabiilty Info", "Journals with a decreasing I.F. (minimum variation of " + minimumVariability+")", "Journal", "Impact Factor", impactFactors, journals, "Impact Factor");

        qResult.setBoxplotChart(boxplotChart);

        return qResult;
    }

}
