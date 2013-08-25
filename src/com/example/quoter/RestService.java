package com.example.quoter;

import com.foxykeep.datadroid.service.RequestService;

public class RestService extends RequestService {

	@Override
	public Operation getOperationForType(int requestType) {
		switch (requestType) {
		case RequestFactory.REQUEST_QUOTES:
			return new QuotesOperation();
		default:
			return null;
		}
	}

}
