/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uio.medicine.virsurveillance.charts;

import java.awt.Color;
import java.awt.BasicStroke;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;

/**
 *
 * @author Albert
 */
public class XYLineChart_AWT extends ApplicationFrame {

    private String Title;
    private String xAxisTitle;
    private String chartTitle;
    private String yAxisTitle;
    private AbstractXYItemRenderer renderer;
    private ChartPanel chartPanel;
    private XYPlot plot;
    private int colorOffset = 0;

    ArrayList<ArrayList<Float>> dataPoints;
    ArrayList<ArrayList<Float>> xAxis;
    ArrayList<String> titles;

    private boolean printable = false;
    
    

    public XYLineChart_AWT(String applicationTitle, String chartTitle,
            String xTitle, String yTitle, ArrayList<ArrayList<Float>> dataPoints,
            ArrayList<ArrayList<Float>> xAxis, ArrayList<String> titles) {
        super(applicationTitle);

        this.xAxis = xAxis;
        this.dataPoints = dataPoints;
        this.titles = titles;

        if (this.dataPoints.size() != xAxis.size() && this.dataPoints.size() != titles.size()) {
            System.out.println("Error: Data size not match");
        }

        this.chartTitle = chartTitle;
        this.xAxisTitle = xTitle;
        this.yAxisTitle = yTitle;

        JFreeChart xylineChart = ChartFactory.createXYLineChart(
                chartTitle,
                this.yAxisTitle,
                this.xAxisTitle,
                createDataset(this.dataPoints, this.xAxis, this.titles),
                PlotOrientation.VERTICAL,
                true, true, false);

        this.chartPanel = new ChartPanel(xylineChart);
        this.chartPanel.setPreferredSize(new java.awt.Dimension(560, 367));
        this.plot = xylineChart.getXYPlot();
        this.renderer = new XYLineAndShapeRenderer();

        for (int i = 0; i < this.dataPoints.size(); i++) {
            this.renderer.setSeriesPaint(i, getColor(i));
        }

        //renderer.setSeriesStroke(0, new BasicStroke(4.0f));
        //renderer.setSeriesStroke(1, new BasicStroke(3.0f));
        //renderer.setSeriesStroke(2, new BasicStroke(2.0f));
        this.plot.setRenderer(this.renderer);
        setContentPane(this.chartPanel);
    }

    private XYDataset createDataset(ArrayList<ArrayList<Float>> dataPoints, ArrayList<ArrayList<Float>> xAxis, ArrayList<String> titles) {

        int max = 0;
        int min = 9999;
        boolean allInt = true;
        int roundF;
        for (ArrayList<Float> xList : xAxis) {
            for (Float f : xList) {
                roundF = Math.round(f);
                if (roundF != f) {
                    allInt = false;
                    break;
                } else {
                    if (max < roundF) {
                        max = roundF;
                    }
                    if (min > roundF) {
                        min = roundF;
                    }
                }
            }
            if (!allInt) {
                break;
            }
        }
        if (allInt) {
            return createIntegerXDataset(dataPoints, xAxis, titles, max, min);
        }

        XYSeries[] lines = new XYSeries[titles.size()];
        final XYSeriesCollection dataset = new XYSeriesCollection();
        for (int i = 0; i < titles.size(); i++) {
            try {
                lines[i] = new XYSeries(titles.get(i));
                for (int j = 0; j < xAxis.get(i).size(); j++) {
                    lines[i].add(xAxis.get(i).get(j), dataPoints.get(i).get(j));
                }
                dataset.addSeries(lines[i]);
            } catch (Exception ex) {
                System.out.println("ERROR: xAxis[" + i + "] and dataPoints[" + i + "] have different lenghts");

            }
        }

        return dataset;
    }

    private XYDataset createIntegerXDataset(ArrayList<ArrayList<Float>> dataPoints, ArrayList<ArrayList<Float>> xAxis, ArrayList<String> titles, int max, int min) {
        XYSeries[] lines = new XYSeries[titles.size()];
        final XYSeriesCollection dataset = new XYSeriesCollection();
        for (int i = 0; i < titles.size(); i++) {
            try {
                lines[i] = new XYSeries(titles.get(i));
                for (int j = 0; j < xAxis.get(i).size(); j++) {
                    lines[i].add(xAxis.get(i).get(j), dataPoints.get(i).get(j));
                }
                dataset.addSeries(lines[i]);
            } catch (Exception ex) {
                System.out.println("ERROR: xAxis[" + i + "] and dataPoints[" + i + "] have different lenghts");

            }
        }

        return dataset;
    }

    public int getColorOffset() {
        return colorOffset;
    }

    public void setColorOffset(int colorOffset) {
        this.colorOffset = colorOffset;
    }
    
    private Color getColor(int index) {
        int index2=index+this.colorOffset;
        switch (index2) {
            case 0:
                return Color.RED;
            case 1:
                return Color.BLUE;
            case 2:
                return Color.GREEN;
            case 3:
                return Color.YELLOW;
            case 4:
                return Color.DARK_GRAY;
            case 5:
                return Color.ORANGE;
            case 6:
                return Color.BLACK;
            case 7:
                return Color.CYAN;
            case 8:
                return Color.GRAY;
            case 9:
                return Color.LIGHT_GRAY;
            case 10:
                return Color.MAGENTA;
            case 11:
                return Color.PINK;
            case 12:
                return Color.WHITE;
            default:
                return getColor((int) Math.floor(Math.random() * 14));
        }

    }

    public void updateChartDataSoftLine() {
        this.printable = false;
        if (dataPoints.size() != xAxis.size() && dataPoints.size() != titles.size()) {
            System.out.println("Error: Data size not match");
        }

        JFreeChart xylineChart = ChartFactory.createXYLineChart(
                chartTitle,
                this.yAxisTitle,
                this.xAxisTitle,
                createDataset(dataPoints, xAxis, titles),
                PlotOrientation.VERTICAL,
                true, true, false);

        chartPanel = new ChartPanel(xylineChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 500));
        plot = xylineChart.getXYPlot();
        renderer = new XYSplineRenderer();

        for (int i = 0; i < dataPoints.size(); i++) {
            renderer.setSeriesPaint(i, getColor(i));
        }

        //renderer.setSeriesStroke(0, new BasicStroke(4.0f));
        //renderer.setSeriesStroke(1, new BasicStroke(3.0f));
        //renderer.setSeriesStroke(2, new BasicStroke(2.0f));
        plot.setRenderer(renderer);
        setContentPane(chartPanel);

        this.pack();
        this.setVisible(true);
        this.printable = true;

    }

    public void updateChartData() {
        this.printable = false;
        if (dataPoints.size() != xAxis.size() && dataPoints.size() != titles.size()) {
            System.out.println("Error: Data size not match");
        }

        JFreeChart xylineChart = ChartFactory.createXYLineChart(
                chartTitle,
                this.yAxisTitle,
                this.xAxisTitle,
                createDataset(dataPoints, xAxis, titles),
                PlotOrientation.VERTICAL,
                true, true, false);

        chartPanel = new ChartPanel(xylineChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 500));
        plot = xylineChart.getXYPlot();
        renderer = new XYLineAndShapeRenderer();

        for (int i = 0; i < dataPoints.size(); i++) {
            renderer.setSeriesPaint(i, getColor(i));
        }

        //renderer.setSeriesStroke(0, new BasicStroke(4.0f));
        //renderer.setSeriesStroke(1, new BasicStroke(3.0f));
        //renderer.setSeriesStroke(2, new BasicStroke(2.0f));
        plot.setRenderer(renderer);
        setContentPane(chartPanel);

        this.pack();
        this.setVisible(true);
        this.printable = true;

    }

    public int addSerie(ArrayList<Float> dataPointList, ArrayList<Float> axis, String title) {

        if (dataPointList.size() == axis.size()) {
            dataPoints.add(dataPointList);
            xAxis.add(axis);
            titles.add(title);
            return 0;
        }
        System.out.println("Dimensions do not match");
        return -1;
    }

    /*public static void main(String[] args) {
        int mida = 3;
        int llargada = 10;
        ArrayList<ArrayList<Float>> dataPoints = new ArrayList<>();
        ArrayList<ArrayList<Float>> xAxis = new ArrayList<>();
        ArrayList<String> titles = new ArrayList<>();

        for (int i = 0; i < mida; i++) {
            dataPoints.add(new ArrayList<>());
            xAxis.add(new ArrayList<>());
            titles.add("Serie " + i);
            for (int j = 0; j < llargada; j++) {
                dataPoints.get(i).add((float) Math.random() * 20);
                xAxis.get(i).add((float) j);
            }

        }

        XYLineChart_AWT chart = new XYLineChart_AWT("Impact Factor", "Impact factor per year by topic","Impact Factor","Year",dataPoints, xAxis, titles);
        chart.pack();
        //RefineryUtilities.centerFrameOnScreen(chart);
        //chart.setVisible(true);
        
        ArrayList<Float> dp = new ArrayList<>(); 
        dp.add((float)1.0); 
        dp.add((float)2.0); 
        dp.add((float)3.0);
        
        ArrayList<Float> ax = new ArrayList<>(); 
        ax.add((float)1.0); 
        ax.add((float)5.0); 
        ax.add((float)8.0);
        
        chart.addSerie(dp, ax, "Posteriori");
        
        chart.updateChartData();
        
    }*/
    public void save2File(File outputFile) throws IOException {
        ChartUtilities cu = new ChartUtilities() {
        };
        ChartUtilities.saveChartAsPNG(outputFile, this.chartPanel.getChart(), 800, 500);
        System.out.println("Saved at " + outputFile.toString() + " size = " + 800 + "x" + 500);
    }

}
