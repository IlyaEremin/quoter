package com.example.quoter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Xml;

public class SaxQuoteParser extends BaseQuoteParser {
	
	public SaxQuoteParser(String openingUrl) {
        super(openingUrl);
    }

    public List<Quote> parse() throws SocketTimeoutException {
        final Quote currentQuote = new Quote();
        RootElement root = new RootElement("quotes");
        final List<Quote> quotes = new ArrayList<Quote>();
        Element item = root.getChild(QUOTE);
        item.setEndElementListener(new EndElementListener(){
            public void end() {
                quotes.add(currentQuote.copy());
            }
        });
        item.getChild(ID).setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
                currentQuote.setId(body);
            }
        });
        item.getChild(AUTHORNAME).setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
                currentQuote.setAuthorName(body);
            }
        });
        item.getChild(TEXT).setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
                currentQuote.setText(body);
            }
        });
        item.getChild("location").setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
                currentQuote.setLocation(body);
            }
        });
        try {
            Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, root.getContentHandler());
        } catch(SocketTimeoutException e){
        	throw new SocketTimeoutException();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return quotes;
    }
    
    public static List<Integer> parseIds(File file) throws FileNotFoundException{
    	final List<Integer> quotes = new ArrayList<Integer>();
    	InputStream is = new FileInputStream(file);
    	if(is != null){
    		RootElement root = new RootElement("quotes");
            Element item = root.getChild(QUOTE);
            item.getChild(ID).setEndTextElementListener(new EndTextElementListener(){
                public void end(String body) {
                	try{
                		quotes.add(Integer.parseInt(body));
                	} catch(NumberFormatException nfe){}
                }
            });
            try {
                Xml.parse(is, Xml.Encoding.UTF_8, root.getContentHandler());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    	}
        return quotes;
    }

}
