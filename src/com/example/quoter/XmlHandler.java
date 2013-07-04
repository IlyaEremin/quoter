package com.example.quoter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.util.Xml;


public class XmlHandler {
	
	final static String likedQuotesFile = "liked.xml";
	File xmlLiked;
	private List<Integer> savedQuotes;
	
	public XmlHandler(Context context){
		savedQuotes = new ArrayList<Integer>();
		xmlLiked = new File(context.getFilesDir(), likedQuotesFile);
		File file = new File(likedQuotesFile);
		
		if(file != null){
			try {
				savedQuotes = SaxQuoteParser.parseIds(xmlLiked);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void removeQuote(Quote quote){
		if(!xmlLiked.exists()) return;
		try {
			DocumentBuilderFactory docFac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuild = docFac.newDocumentBuilder();
			Document doc = docBuild.parse(xmlLiked);
			NodeList list = doc.getElementsByTagName(BaseQuoteParser.QUOTE);
			for(int i = 0; i < list.getLength(); i++){
				Element q = (Element) list.item(i);
				Element id = (Element) q.getElementsByTagName(BaseQuoteParser.ID).item(0);
				if(id.getTextContent().equals(String.valueOf(quote.getId())))
					q.getParentNode().removeChild(q);
			}
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(xmlLiked);
			transformer.transform(source, result);
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public void addQuoteToFile(Quote quote){
		try {
			if(!xmlLiked.exists()){
				
				xmlLiked.createNewFile();
				FileOutputStream fos = new FileOutputStream(xmlLiked);
				XmlSerializer serializer = Xml.newSerializer();
                serializer.setOutput(fos, "UTF-8");
                serializer.startDocument(null, Boolean.valueOf(true));
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);        
                serializer.startTag(null, "quotes");
                serializer.endTag(null,"quotes");
                serializer.endDocument();
                //write xml data into the FileOutputStream
                serializer.flush();
                //finally we close the file stream
                fos.close();
			}
			
			DocumentBuilderFactory docFac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuild;
			docBuild = docFac.newDocumentBuilder();
			Document doc = docBuild.parse(xmlLiked);
			
			Element quotes =  doc.getDocumentElement();
			
			Element quoteTag = doc.createElement(BaseQuoteParser.QUOTE);
			
			Element id = doc.createElement(BaseQuoteParser.ID);
			id.appendChild(doc.createTextNode(String.valueOf(quote.getId())));
			quoteTag.appendChild(id);
			
			Element authorName = doc.createElement(BaseQuoteParser.AUTHORNAME);
			authorName.appendChild(doc.createTextNode(quote.getQuoteAuthor()));
			quoteTag.appendChild(authorName);
			
			Element text = doc.createElement(BaseQuoteParser.TEXT);
			text.appendChild(doc.createTextNode(quote.getQuoteText()));
			quoteTag.appendChild(text);
			
			Element loc = doc.createElement("location");
			loc.appendChild(doc.createTextNode(quote.getLocation()));
			quoteTag.appendChild(loc);
			
			quotes.appendChild(quoteTag);
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(xmlLiked);
			transformer.transform(source, result);
	 
			
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		// TODO координаты тоже должны приходить
	}
	
	public List<Integer> getSavedQuotes(){
		return savedQuotes;
	}
}


//public File getTempFile(Context context, String url) {
//  File file;
//  try {
//      String fileName = Uri.parse(url).getLastPathSegment();
//      file = File.createTempFile(fileName, null, context.getCacheDir());
//  catch (IOException e) {
//      // Error while creating file
//  }
//  return file;
//}
