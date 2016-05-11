/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uio.medicine.virsurveillance.datamodels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Albert
 */
public class PubmedJournal {
    
    private String journalName;
    private String journalShortName;
    private ArrayList<String> synonimNames;
    private Map<Integer,Float> yearImpactFactor;
    
   public PubmedJournal(String journalName) {
        this.journalName = simplifyTitle(journalName);
        this.synonimNames = new ArrayList<>();
        this.yearImpactFactor = new HashMap<>();
    }
   
    public PubmedJournal(String journalName, String journalShortName) {
        this.journalName = simplifyTitle(journalName);
        this.journalShortName = journalShortName;
        this.synonimNames = new ArrayList<>();
        this.yearImpactFactor = new HashMap<>();
    }

    public String getJournalName() {
        return journalName;
    }

    public String getJournalShortName() {
        return journalShortName;
    }

    public void setJournalName(String journalName) {
        this.journalName = simplifyTitle(journalName);
    }

    public void setJournalShortName(String journalShortName) {
        this.journalShortName = journalShortName;
    }

    public void addSynonimName(String synonimName) {
        this.synonimNames.add(synonimName);
    }

    public void setYearImpactFactor(int year, float impactFactor) {
        this.yearImpactFactor.put(year,impactFactor);
    }
    
    public String toString(){
        return this.journalName;
    }

    public Map<Integer, Float> getYearImpactFactor() {
        return yearImpactFactor;
    }

    public void setYearImpactFactor(Map<Integer, Float> yearImpactFactor) {
        this.yearImpactFactor = yearImpactFactor;
    }
    
    private static String simplifyTitle(String title){
        String simpleTitle=title.replaceAll("\\(.*?\\) ?", "");
        simpleTitle=simpleTitle.replaceAll("'", "");
        simpleTitle=simpleTitle.split(":")[0];
        
        return simpleTitle;
    }
    
    
    
    
   
    
}
