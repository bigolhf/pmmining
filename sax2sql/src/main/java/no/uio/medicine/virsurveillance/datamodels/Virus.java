/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uio.medicine.virsurveillance.datamodels;

import java.util.ArrayList;

/**
 *
 * @author apla
 */
public class Virus {
    
    String virusName;
    ArrayList<String> virusSinonims;

    public Virus(String virusName) {
        this.virusName = virusName;
        this.virusSinonims = new ArrayList<>();
    }

    public String getVirusName() {
        return virusName;
    }

    public void setVirusName(String virusName) {
        this.virusName = virusName;
    }

    public ArrayList<String> getVirusSinonims() {
        return virusSinonims;
    }

    public void setVirusSinonims(ArrayList<String> virusSinonims) {
        this.virusSinonims = virusSinonims;
    }
    
    public void addSinonim(String sinonim){
        this.virusSinonims.add(sinonim);
    }
    
    public void removeSinonim(String sinonim){
        this.virusSinonims.remove(sinonim);
    }
    
    
    
}
