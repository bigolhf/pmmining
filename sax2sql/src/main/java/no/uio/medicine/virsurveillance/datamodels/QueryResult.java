/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uio.medicine.virsurveillance.datamodels;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uio.medicine.virsurveillance.charts.BoxAndWhiskerChart_AWT;
import no.uio.medicine.virsurveillance.charts.StackedChart_AWT;
import no.uio.medicine.virsurveillance.charts.XYLineChart_AWT;

/**
 *
 * @author Albert
 */
public class QueryResult {

    private XYLineChart_AWT xyLineChart;
    private StackedChart_AWT stackedChart;
    private BoxAndWhiskerChart_AWT boxplotChart;
    private ArrayList<String> header;
    private HashMap<String, ArrayList<String>> values;
    
    

    public QueryResult() {
        header = new ArrayList<>();
        values = new HashMap<>();
    }

    public void addHeaderValue(String head) {
        if (!header.contains(head)) {
            header.add(head);
            values.put(head, new ArrayList<String>());
        }
    }

    public void addValue(String attribute, String value) {
        values.get(attribute).add(value);
    }

    public void setXyLineChart(XYLineChart_AWT xyLineChart) {
        this.xyLineChart = xyLineChart;
    }

    @Override
    public String toString() {
        String val = "";
        if (header.size() > 0) {

            for (String s : header) {
                val = val + s + ",";
            }
            val = val + "\n";

            for (int i = 0; i < values.get(header.get(0)).size(); i++) {
                for (String s : header) {
                    val = val + values.get(s).get(i) + ",";
                }
                val = val + "\n";
            }
        } else {
            val = "No results obtained";
        }

        return val;
    }

    public void printResult() {
        System.out.println(this.toString());
    }

    public ArrayList<String> getHeader() {
        return header;
    }

    public void setHeader(ArrayList<String> header) {
        this.header = header;
    }

    public HashMap<String, ArrayList<String>> getValues() {
        return values;
    }

    public void setValues(HashMap<String, ArrayList<String>> values) {
        this.values = values;
    }

    public XYLineChart_AWT getXyLineChart() {
        return xyLineChart;
    }

    public void save2File(String outputFile) {
        try {
            writeCSV(outputFile + ".csv");

            if (this.xyLineChart != null) {
                File outputImg = new File(outputFile + "_xy.png");
                Thread.sleep(500); //TODO: for some concurrency problem the image saving needs to wait a few time. Need to check it
                this.xyLineChart.save2File(outputImg);
            }
            
            if (this.stackedChart != null) {
                File outputImg = new File(outputFile + "_stacked.png");
                Thread.sleep(500); //TODO: for some concurrency problem the image saving needs to wait a few time. Need to check it
                this.stackedChart.save2File(outputImg);
            }
            
            if (this.boxplotChart != null) {
                File outputImg = new File(outputFile + "_bw.png");
                Thread.sleep(500); //TODO: for some concurrency problem the image saving needs to wait a few time. Need to check it
                this.boxplotChart.save2File(outputImg);
            }
            
        } catch (IOException ex) {
            Logger.getLogger(QueryResult.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(QueryResult.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void writeCSV(String outputFile) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(outputFile);
        pw.print(this.toString());
        pw.close();
    }

    public StackedChart_AWT getStackedChart() {
        return stackedChart;
    }

    public void setStackedChart(StackedChart_AWT stackedChart) {
        this.stackedChart = stackedChart;
    }

    public BoxAndWhiskerChart_AWT getBoxplotChart() {
        return boxplotChart;
    }

    public void setBoxplotChart(BoxAndWhiskerChart_AWT boxplotChart) {
        this.boxplotChart = boxplotChart;
    }
    
    
    public void updateCharts(){
         if (xyLineChart!=null){
             xyLineChart.updateChartData();             
         }
         if (stackedChart!=null){
             stackedChart.updateChartData();             
         }
         if (boxplotChart!=null){
            boxplotChart.updateChartData();             
         }
         
    }
    
    public void closeCharts(){
        if (xyLineChart!=null){
             xyLineChart.setVisible(false);
         }
         if (stackedChart!=null){
             stackedChart.setVisible(false);         
         }
         if (boxplotChart!=null){
            boxplotChart.setVisible(false);            
         }
         
    }

}
