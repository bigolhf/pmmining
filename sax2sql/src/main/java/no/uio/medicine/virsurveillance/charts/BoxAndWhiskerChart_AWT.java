/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uio.medicine.virsurveillance.charts;

/**
 *
 * @author apla
 */
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.ChartFactory;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.util.Log;
import org.jfree.util.LogContext;

/**
 * Demonstration of a box-and-whisker chart using a {@link CategoryPlot}.
 *
 * @author David Browning
 */
public class BoxAndWhiskerChart_AWT extends ApplicationFrame {

    private ChartPanel chartPanel;

    private String Title;
    private String xAxisTitle;
    private String chartTitle;
    private String yAxisTitle;

    private BoxAndWhiskerRenderer renderer;

    private CategoryPlot plot;
    private int colorOffset = 0;

    ArrayList<ArrayList<ArrayList<Float>>> dataPoints;
    ArrayList<ArrayList<String>> categories;
    ArrayList<String> seriesTitles;

    boolean printable = false;

    /**
     * Creates a new demo.
     *
     * @param applicationTitle
     * @param chartTitle
     * @param xTitle
     * @param yTitle
     * @param dataPoints
     * @param categories
     * @param seriesTitles
     */
    public BoxAndWhiskerChart_AWT(String applicationTitle, String chartTitle,
            String xTitle, String yTitle,
            ArrayList<ArrayList<ArrayList<Float>>> dataPoints,
            ArrayList<ArrayList<String>> categories,
            ArrayList<String> seriesTitles) {

        super(applicationTitle);

        this.dataPoints = dataPoints;
        this.categories = categories;
        this.seriesTitles = seriesTitles;

        this.chartTitle = chartTitle;
        this.xAxisTitle = xTitle;
        this.yAxisTitle = yTitle;
        this.Title = applicationTitle;

        BoxAndWhiskerCategoryDataset dataset = (BoxAndWhiskerCategoryDataset) createDataset(this.dataPoints, this.categories, this.seriesTitles);

        final CategoryAxis xAxis = new CategoryAxis(this.xAxisTitle);
        final NumberAxis yAxis = new NumberAxis(this.yAxisTitle);
        yAxis.setAutoRangeIncludesZero(false);
        this.renderer = new BoxAndWhiskerRenderer();
        this.renderer.setFillBox(false);
        renderer.setToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
        this.plot = new CategoryPlot(dataset, xAxis, yAxis, this.renderer);

        final JFreeChart chart = new JFreeChart(
                this.chartTitle,
                new Font("SansSerif", Font.BOLD, 14),
                this.plot,
                true
        );
        chart.setBackgroundPaint(Color.white);
        this.chartPanel = new ChartPanel(chart);
        this.chartPanel.setBackground(Color.white);
        this.chartPanel.setPreferredSize(new java.awt.Dimension(800, 500));
        setContentPane(this.chartPanel);

    }

    public BoxAndWhiskerChart_AWT(String applicationTitle, String chartTitle,
            String xTitle, String yTitle,
            ArrayList<ArrayList<Float>> singleSerieDataPoints,
            ArrayList<String> singleSerieCategories,
            String singleSerieTitle) {

        super(applicationTitle);

        this.chartTitle = chartTitle;
        this.xAxisTitle = xTitle;
        this.yAxisTitle = yTitle;
        this.Title = applicationTitle;

        this.dataPoints = new ArrayList<>();
        this.dataPoints.add(singleSerieDataPoints);
        this.categories = new ArrayList<>();
        this.categories.add(singleSerieCategories);
        this.seriesTitles = new ArrayList<>();
        this.seriesTitles.add(singleSerieTitle);

        BoxAndWhiskerCategoryDataset dataset = (BoxAndWhiskerCategoryDataset) createDataset(this.dataPoints, this.categories, this.seriesTitles);

        final CategoryAxis xAxis = new CategoryAxis(xTitle);
        final NumberAxis yAxis = new NumberAxis(yTitle);
        yAxis.setAutoRangeIncludesZero(false);
        this.renderer = new BoxAndWhiskerRenderer();
        this.renderer.setFillBox(false);

        this.renderer.setToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
        this.plot = new CategoryPlot(dataset, xAxis, yAxis, this.renderer);

        final JFreeChart chart = new JFreeChart(
                chartTitle,
                new Font("SansSerif", Font.BOLD, 14),
                this.plot,
                true
        );
        chart.setBackgroundPaint(Color.white);
        this.chartPanel = new ChartPanel(chart);
        this.chartPanel.setBackground(Color.white);
        this.chartPanel.setPreferredSize(new java.awt.Dimension(1200, 500));
        setContentPane(chartPanel);

    }

    private CategoryDataset createDataset(ArrayList<ArrayList<ArrayList<Float>>> dataPoints, ArrayList<ArrayList<String>> categories, ArrayList<String> titles) {
        final int nSeries = 3; //<--n caixes a cada titol equivalent a size de datapoints
        final int nCaixes = 4; //<--titols per caixa. titles. en principi ha de ser igual a dataPoints(x).size
        final int puntsPerCaixa = 22; //<-- dataPoints(i)(j).size()

        final DefaultBoxAndWhiskerCategoryDataset dataset
                = new DefaultBoxAndWhiskerCategoryDataset();

        int i = 0;
        for (ArrayList<ArrayList<Float>> boxSet : dataPoints) {
            int j = 0;

            for (ArrayList<Float> box : boxSet) {
                dataset.add(box, titles.get(i), categories.get(i).get(j));
                j++;
            }

            i++;
        }

        return dataset;
    }

    // ****************************************************************************
    // * JFREECHART DEVELOPER GUIDE                                               *
    // * The JFreeChart Developer Guide, written by David Gilbert, is available   *
    // * to purchase from Object Refinery Limited:                                *
    // *                                                                          *
    // * http://www.object-refinery.com/jfreechart/guide.html                     *
    // *                                                                          *
    // * Sales are used to provide funding for the JFreeChart project - please    * 
    // * support us so that we can continue developing free software.             *
    // ****************************************************************************
    /**
     * For testing from the command line.
     *
     * @param args ignored.
     */


    public void save2File(File outputFile) throws IOException {
        ChartUtilities cu = new ChartUtilities() {
        };
        ChartUtilities.saveChartAsPNG(outputFile, this.chartPanel.getChart(), 1200, 500);
        System.out.println("Saved at " + outputFile.toString() + " size = " + 1200 + "x" + 500);
    }

    public void updateChartData() {
        this.printable = false;

        BoxAndWhiskerCategoryDataset dataset = (BoxAndWhiskerCategoryDataset) createDataset(this.dataPoints, this.categories, this.seriesTitles);

        final CategoryAxis xAxis = new CategoryAxis(this.xAxisTitle);
        final NumberAxis yAxis = new NumberAxis(this.yAxisTitle);
        yAxis.setAutoRangeIncludesZero(false);
        this.renderer = new BoxAndWhiskerRenderer();
        this.renderer.setFillBox(false);
        renderer.setToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
        this.plot = new CategoryPlot(dataset, xAxis, yAxis, this.renderer);

        final JFreeChart chart = new JFreeChart(
                this.chartTitle,
                new Font("SansSerif", Font.BOLD, 14),
                this.plot,
                true
        );
        this.plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
        this.chartPanel = new ChartPanel(chart);
        this.chartPanel.setPreferredSize(new java.awt.Dimension(1200, 500));

        setContentPane(this.chartPanel);

        this.pack();
        this.setVisible(true);
        this.printable = true;

    }

        public static void main(final String[] args) {

        //Log.getInstance().addTarget(new PrintStreamLogTarget(System.out));
        ArrayList<ArrayList<ArrayList<Float>>> dataPoints = new ArrayList<>();
        ArrayList<String> titles = new ArrayList<>();
        ArrayList<ArrayList<String>> categories = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            ArrayList<ArrayList<Float>> serie = new ArrayList<>();
            titles.add("Serie " + i);

            ArrayList<String> categoriesPerSerie = new ArrayList<>();
            int max = i + 2;
            for (int j = 0; j < max; j++) {
                ArrayList<Float> points = new ArrayList<>();
                for (int k = 0; k < 50; k++) {
                    points.add((float) (i * 10 + Math.random() * 50));
                }
                serie.add(points);
                categoriesPerSerie.add("Categorie" + j);
            }
            dataPoints.add(serie);
            categories.add(categoriesPerSerie);

        }

        final BoxAndWhiskerChart_AWT demo = new BoxAndWhiskerChart_AWT("A", "B", "C", "D", dataPoints, categories, titles);

        final BoxAndWhiskerChart_AWT demo2 = new BoxAndWhiskerChart_AWT("A2", "B2", "C2", "D2", dataPoints.get(0), categories.get(0), titles.get(0));

        demo.updateChartData();
        demo2.updateChartData();

    }
    
}
