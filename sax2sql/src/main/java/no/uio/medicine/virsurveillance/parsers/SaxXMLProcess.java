/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uio.medicine.virsurveillance.parsers;

import java.sql.SQLException;
import no.uio.medicine.virsurveillance.datamodels.PubmedArticle;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uio.medicine.virsurveillance.DDBB.SQLManagement;
import no.uio.medicine.virsurveillance.datamodels.PubmedAuthor;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Albert
 */
public class SaxXMLProcess extends DefaultHandler {

    private ArrayList<PubmedArticle> articles;

    private int numelements = 0;
    private int currentBatch = 0;

    private boolean flagJournalTitle = false;
    private boolean flagAuthor = false;
    private boolean flagArticleTitle = false;
    private boolean flagAbstractText = false;
    private boolean flagJournalYear = false;
    private boolean flagJournalMonth = false;

    private boolean flagJournalSection = false;
    private boolean flagAbstractSection = false;
    private boolean flagAuthorSection = false;
    private boolean flagAuthorInfo;

    private int batchSize = 1;
    private PubmedAuthor currentAuthor;
    private boolean flagAuthorLastName;
    private boolean flagAuthorForeName;
    private boolean flagAuthorInitials;

    private SQLManagement sqlM;

    int inDatabase=0;
    int notSaved=0;
    int toPush;
    
    public SaxXMLProcess() {
        super();
        articles = new ArrayList<>();
        this.currentBatch = 0;
    }

    public SaxXMLProcess(int batchSize, SQLManagement sqlM) {
        super();
        this.articles = new ArrayList<>();
        this.batchSize = batchSize;
        this.currentBatch = 0;
        this.sqlM = sqlM;
    }

    public void setBatchSize(int bs) {
        this.batchSize = bs;
    }

    @Override
    public void endDocument() throws SAXException {
        push2DDBB();
        System.out.println("Total etiquetes: " + numelements);
    }

    @Override
    public void startElement(String uri, String localName,
            String qName, Attributes attributes) {
        if (qName.equalsIgnoreCase("PubmedArticle")) {
            if (currentBatch > batchSize) {
                System.out.println("Curent batch: "+currentBatch);
                System.out.println(articles.size()+" articles in the current batch");
                System.out.println(inDatabase+" articles parsed. "+ notSaved +" not processed");
                
                push2DDBB();
                articles = new ArrayList<>();
                currentBatch=0;
            }
            PubmedArticle pma = new PubmedArticle();
            pma.setId("ART" + String.format("%010d", numelements));
            articles.add(pma);
            //System.out.println("Element "+numelements);
            numelements++;
            currentBatch++;
        } else if (qName.equalsIgnoreCase("ArticleTitle")) {
            flagArticleTitle = true;
        } else if (qName.equalsIgnoreCase("Journal")) {
            flagJournalSection = true;
        } else if (qName.equalsIgnoreCase("Abstract")) {
            flagAbstractSection = true;
        } else if (qName.equalsIgnoreCase("AuthorList")) {
            flagAuthorSection = true;
        }

        if (flagAbstractSection) {
            if (qName.equalsIgnoreCase("AbstractText")) {
                flagAbstractText = true;
            }
        }

        if (flagJournalSection) {
            if (qName.equalsIgnoreCase("Title")) {
                flagJournalTitle = true;
            } else if (qName.equalsIgnoreCase("Year")) {
                flagJournalYear = true;
            } else if (qName.equalsIgnoreCase("Month")) {
                flagJournalMonth = true;
            }
        }

        if (flagAuthorSection) {
            if (qName.equalsIgnoreCase("Author")) {
                flagAuthorInfo = true;
            }

            if (flagAuthorInfo) {
                if (currentAuthor == null) {
                    currentAuthor = new PubmedAuthor();
                }
                if (qName.equalsIgnoreCase("lastName")) {
                    flagAuthorLastName = true;
                } else if (qName.equalsIgnoreCase("forename")) {
                    flagAuthorForeName = true;
                } else if (qName.equalsIgnoreCase("initials")) {
                    flagAuthorInitials = true;
                }

            }
        }

    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("Journal")) {
            flagJournalSection = false;
        } else if (qName.equalsIgnoreCase("Abstract")) {
            flagAbstractSection = false;
        } else if (qName.equalsIgnoreCase("Author")) {
            flagAuthorInfo = false;
            this.articles.get(articles.size() - 1).addAuthor(currentAuthor);
            currentAuthor = null;
        } else if (qName.equalsIgnoreCase("AuthorList")) {
            flagAuthorSection = false;
        } else if (qName.equalsIgnoreCase("Author")) {
            flagAuthorInfo = false;
        }

    }

    @Override
    @SuppressWarnings("empty-statement")
    public void characters(char ch[],
            int start, int length) throws SAXException {
        //System.out.println((new String(ch, start, length)).replace("'","\\'"));
        if (flagArticleTitle) {
            articles.get(articles.size() - 1).setTitle((new String(ch, start, length)).replace("'", "\\'"));
            flagArticleTitle = false;
        } else if (flagJournalSection) {
            if (flagJournalTitle) {
                articles.get(articles.size() - 1).setJournalName((new String(ch, start, length)).replace("'", "\\'"));
                flagJournalTitle = false;
            } else if (flagJournalYear) {
                articles.get(articles.size() - 1).setPublicationYear(Integer.parseInt((new String(ch, start, length)).replace("'", "\\'")));
                flagJournalYear = false;
            } else if (flagJournalMonth) {
                articles.get(articles.size() - 1).setPublicationMonth((new String(ch, start, length)).replace("'", "\\'"));
                flagJournalMonth = false;
            };
        } else if (flagAbstractSection) {
            if (flagAbstractText) {
                articles.get(articles.size() - 1).addAbstract((new String(ch, start, length)).replace("'", "\\'"));
                flagAbstractText = false;

            }
        } else if (flagAuthorSection) {
            if (currentAuthor == null) {
                currentAuthor = new PubmedAuthor();
            }

            if (flagAuthorLastName) {
                currentAuthor.setLastName((new String(ch, start, length)).replace("'", "\\'"));
                flagAuthorLastName = false;
            } else if (flagAuthorForeName) {
                currentAuthor.setForeName((new String(ch, start, length)).replace("'", "\\'"));
                flagAuthorForeName = false;
            } else if (flagAuthorInitials) {
                currentAuthor.setInitials((new String(ch, start, length)).replace("'", "\\'"));
                flagAuthorInitials = false;
            }
        }

    }

   
    private void push2DDBB() {

        //parallel version of this for 
        /*for (PubmedArticle pa:articles){
            try {
                sqlM.addArticle2DB(pa);
            } catch (SQLException ex) {
                Logger.getLogger(SaxXMLProcess.class.getName()).log(Level.SEVERE, null, ex);
            }
        }//**/
        toPush=0;
        System.out.println(articles.size()+" articles will be sent to DDBB)");
        articles.stream().forEach((PubmedArticle pa) -> {
            
            try {
                inDatabase++;
                toPush++;
                sqlM.addArticle2DB(pa);
            } catch (Exception ex) {
                notSaved++;
                Logger.getLogger(SaxXMLProcess.class.getName()).log(Level.SEVERE, null, ex);
            }
        });//**/ //end of parallel loop
        System.out.println(toPush+" have been pushed");

    }
}
