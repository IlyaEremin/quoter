package com.example.quoter;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;

import com.example.quoter.Contract.Quotes;
import com.foxykeep.datadroid.exception.ConnectionException;
import com.foxykeep.datadroid.exception.CustomRequestException;
import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.network.NetworkConnection;
import com.foxykeep.datadroid.network.NetworkConnection.ConnectionResult;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.service.RequestService.Operation;

import org.json.JSONArray;
import org.json.JSONException;

public class QuotesOperation implements Operation {

	@Override
	public Bundle execute(Context context, Request request) throws ConnectionException, DataException, CustomRequestException {
		NetworkConnection connection = new NetworkConnection(context, "http://quoter.100ms.ru/getQuotes.php");

		ConnectionResult result = connection.execute();
		ContentValues[] quotesValues;
		try {
			String json = result.body;
			json = json.substring(1);
			JSONArray quotesJson =  new JSONArray(json);	
			quotesValues = new ContentValues[quotesJson.length()];
			for (int i = 0; i < quotesJson.length(); ++i) {
				ContentValues quote = new ContentValues();
				quote.put(Quotes.ID, quotesJson.getJSONObject(i).getString(Quotes.ID));
				
				// � ������ ����� �� ���� ������
				if(!quotesJson.getJSONObject(i).isNull(Quotes.AUTHOR)){
					quote.put(Quotes.AUTHOR, quotesJson.getJSONObject(i).getString(Quotes.AUTHOR));
				}
				
				quote.put(Quotes.TEXT, quotesJson.getJSONObject(i).getString(Quotes.TEXT));
				quotesValues[i] = quote;
			}
			
			
		} catch (JSONException e) {
			throw new DataException(e.getMessage());
		}
		
		context.getContentResolver().delete(Quotes.CONTENT_URI, null, null);
		context.getContentResolver().bulkInsert(Quotes.CONTENT_URI, quotesValues);
		return null;
	}

}
