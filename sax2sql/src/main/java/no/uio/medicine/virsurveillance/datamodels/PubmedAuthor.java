/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uio.medicine.virsurveillance.datamodels;

/**
 *
 * @author Albert
 */
public class PubmedAuthor {
    private String lastName;
    private String foreName;
    private String initials;

    public PubmedAuthor() {
    }

    public PubmedAuthor(String lastName, String foreName) {
        this.lastName = lastName;
        this.foreName = foreName;
    }

    public PubmedAuthor(String lastName, String foreName, String initials) {
        this.lastName = lastName;
        this.foreName = foreName;
        this.initials = initials;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getForeName() {
        return foreName;
    }

    public void setForeName(String foreName) {
        this.foreName = foreName;
        String[] names=foreName.split(" ");
        this.initials="";
        for (String s:names){
            this.initials=this.initials+s.charAt(0)+".";
        }        
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    @Override
    public String toString() {
        return lastName+", "+foreName+" ("+initials+")";
    }
    
    
}
