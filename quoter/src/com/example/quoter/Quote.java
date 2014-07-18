package com.example.quoter;

public class Quote implements Comparable<Quote> {
	
	private String author, text;
	private long id;
	
	public Quote(long id, String author, String text){
		this.id = id;
		this.author = author;
		this.text = text;
	}
	
	public Quote(){}
	
	
	public long getId(){
		return id;
	}
	
	public String getQuoteText(){
		return text;
	}
	
	public String getQuoteAuthor(){
		return author;
	}
	
	public void setAuthorName(String authorName){
		this.author = authorName;
	}
	
	public void setText(String text){
		this.text = text;
	}
	
	public void setId(String id){
		this.id = Integer.parseInt(id);
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public void setId(long id){
		this.id = id;
	}
	
	@Override
	public int compareTo(Quote another) {
		if (another == null) return 1;
		return 0;
	}
	
	public Quote copy(){
		return new Quote(id, author, text);
	}

}
