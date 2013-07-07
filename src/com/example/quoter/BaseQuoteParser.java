package com.example.quoter;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public abstract class BaseQuoteParser implements QuoteParser  {
	
	static final String QUOTE = "quote";
	static final String AUTHORNAME = "authorName";
	static final String TEXT = "text";
	static final String ID = "id";
	
	final URL quoteUrl;
	
	 protected BaseQuoteParser(String openingUrl){
        try {
            this.quoteUrl = new URL(openingUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    protected InputStream getInputStream() {
    	URLConnection urlConn;
		try {
			urlConn = quoteUrl.openConnection();
			return urlConn.getInputStream();
		}
		catch (IOException e) {
			return null;
		}
    }

}
