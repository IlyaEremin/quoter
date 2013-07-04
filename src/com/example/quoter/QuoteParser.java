package com.example.quoter;

import java.net.SocketTimeoutException;
import java.util.List;

public interface QuoteParser {
	List<Quote> parse() throws SocketTimeoutException;
}
