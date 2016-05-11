/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uio.medicine.virsurveillance.datamodels;

import java.util.ArrayList;

/**
 *
 * @author Albert
 */
public class PubmedArticle {

    private String id;
    private String title;
    private PubmedJournal journal;
    private ArrayList<String> abstracts;
    private ArrayList<PubmedAuthor> authorList;
    private int publicationYear;
    private String publicationMonth;

    public PubmedArticle() {
        abstracts = new ArrayList<>();
        authorList = new ArrayList<>();

    }

    public PubmedArticle(String title, PubmedJournal journal, ArrayList<String> abstracts, int publicationYear, String publicationMonth) {
        this.title = title;
        this.journal = journal;
        this.abstracts = abstracts;
        this.publicationYear = publicationYear;
        this.publicationMonth = publicationMonth;
    }
    
    

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public PubmedJournal getJournal() {
        return journal;
    }

    public void setJournal(PubmedJournal journal) {
        this.journal = journal;
    }
    
    public void setJournalName(String journalName){
        setJournal(new PubmedJournal(journalName.replaceAll("\\(.*?\\) ?", "")));        
    }

        public ArrayList<String> getAbstracts() {
        return abstracts;
    }

    public void setAbstracts(ArrayList<String> abstracts) {
        this.abstracts = abstracts;
    }

    public ArrayList<PubmedAuthor> getAuthorList() {
        return authorList;
    }

    public void setAuthorList(ArrayList<PubmedAuthor> authorList) {
        this.authorList = authorList;
    }

    public void setPublicationYear(int pubYear) {
        this.publicationYear = pubYear;
    }

    public void setPublicationMonth(String pubMonth) {
        this.publicationMonth = pubMonth;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public String getPublicationMonth() {
        return publicationMonth;
    }

    public void addAbstract(String abstractText) {
        this.abstracts.add(abstractText);
    }

    public void addAuthor(String lastName, String foreName) {
        PubmedAuthor auth = new PubmedAuthor(lastName, foreName);
        this.authorList.add(auth);
    }

    public void addAuthor(PubmedAuthor auth) {
        this.authorList.add(auth);
    }

    public void addAuthor(String lastName, String foreName, String initials) {
        PubmedAuthor auth = new PubmedAuthor(lastName, foreName, initials);
        this.authorList.add(auth);
    }

    @Override
    public String toString() {
        String toStr = this.id+": "+this.title + "\t in: ";
        if (this.getJournal()!=null){
            toStr=toStr+ this.getJournal().toString() + "\n";
        }
        else{
            toStr=toStr+ "unknown";
        }
        toStr = toStr + "\tAuthors:\n";
        for (PubmedAuthor auth : this.authorList) {
            toStr = toStr + "\t\t" + auth.toString() + "\n";
        }
        if (abstracts != null && abstracts.size() > 0) {
            toStr = toStr + "\tAbstract:\n";
            for (String s : this.abstracts) {
                toStr = toStr + "\t\t -" + s + "\n";
            }
        }
        return toStr;
    }

}
