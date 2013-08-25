package com.example.quoter;

import com.foxykeep.datadroid.requestmanager.Request;

public final class RequestFactory {
	
	public static final int REQUEST_QUOTES = 1;
	
	public static Request getTweetsRequest(String screenName) {
		Request request = new Request(REQUEST_QUOTES);
		request.put("screen_name", screenName);
		return request;
	}
	
	private RequestFactory() {
	}
}
