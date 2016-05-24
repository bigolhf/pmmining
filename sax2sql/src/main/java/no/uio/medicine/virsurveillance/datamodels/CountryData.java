/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uio.medicine.virsurveillance.datamodels;

/**
 *
 * @author apla
 */
public class CountryData {
    
    private String name;
    private String code;
    private String region; //should it be change for an object region?
    private String incomeGroup; //should it be change for an "income" object?
    private boolean isAnAgregator;
    
    public CountryData(String name, String code, String region, String incomeGroup){
        this.name=name;
        this.code=code;
        this.region=region;
        this.incomeGroup=incomeGroup;
        this.isAnAgregator=false;
    }
    
    public CountryData(String name, String code, String region, String incomeGroup, boolean isAnAgregator){
        this(name,code,region,incomeGroup);
        this.isAnAgregator=isAnAgregator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getIncomeGroup() {
        return incomeGroup;
    }

    public void setIncomeGroup(String incomeGroup) {
        this.incomeGroup = incomeGroup;
    }

    public boolean isIsAnAgregator() {
        return isAnAgregator;
    }

    public void setIsAnAgregator(boolean isAnAgregator) {
        this.isAnAgregator = isAnAgregator;
    }

    @Override
    public String toString() {
        return "CountryData{" + "name=" + name + ", code=" + code + ", region=" + region + ", incomeGroup=" + incomeGroup + ", isAnAgregator=" + isAnAgregator + '}';
    }
    
    
    
    
}
