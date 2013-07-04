package com.example.quoter;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
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

    protected InputStream getInputStream() throws SocketTimeoutException {
    	URLConnection urlConn;
		try {
			urlConn = quoteUrl.openConnection();
			urlConn.setConnectTimeout(5000);
        	urlConn.setReadTimeout(10000);
			return urlConn.getInputStream();
		}
		catch(SocketTimeoutException e){
			throw new SocketTimeoutException();
		}
		catch (IOException e) {
			return null;
		}
    }

}
