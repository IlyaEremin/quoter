package com.example.quoter;

public class Quote implements Comparable<Quote> {
	
	private String authorName, text, location;
	private int id;
	
	public int getId(){
		return id;
	}
	
	public String getQuoteText(){
		return text;
	}
	
	public String getQuoteAuthor(){
		return authorName;
	}
	
	public void setAuthorName(String authorName){
		this.authorName = authorName;
	}
	
	public String getLocation() {
		return location;
	}
	
	public void setText(String text){
		this.text = text;
	}
	
	public void setId(String id){
		this.id = Integer.parseInt(id);
	}
	
	public void setLocation(String location) {
		this.location = location;
	}
	
	@Override
	public int compareTo(Quote another) {
		if (another == null) return 1;
		return 0;
	}
	
	public Quote copy(){
		Quote copy = new Quote();
		copy.authorName = authorName;
		copy.text = text;
		copy.id = id;
		copy.location = location;
		return copy;
	}

}
