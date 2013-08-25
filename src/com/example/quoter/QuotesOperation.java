package com.example.quoter;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.example.quoter.Contract.Quotes;
import com.foxykeep.datadroid.exception.ConnectionException;
import com.foxykeep.datadroid.exception.CustomRequestException;
import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.network.NetworkConnection;
import com.foxykeep.datadroid.network.NetworkConnection.ConnectionResult;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.service.RequestService.Operation;

public class QuotesOperation implements Operation {

	@Override
	public Bundle execute(Context context, Request request) throws ConnectionException, DataException, CustomRequestException {
		NetworkConnection connection = new NetworkConnection(context, "http://quoter.100ms.ru/getQuotes.php");
		HashMap<String, String> params = new HashMap<String, String>();
		
		params.put("screen_name", request.getString("screen_name"));
		connection.setParameters(params);
		
		ConnectionResult result = connection.execute();
		ContentValues[] quotesValues;
		try {
			JSONObject rootObject = new JSONObject(result.body);
			JSONArray quotesJson =  rootObject.getJSONArray(Quotes.QUOTE);	
			quotesValues = new ContentValues[quotesJson.length()];
			
			for (int i = 0; i < quotesJson.length(); ++i) {
				ContentValues quote = new ContentValues();
				quote.put(Quotes.ID, quotesJson.getJSONObject(i).getString(Quotes.ID));
				
				// У цитаты может не быть автора
				if(!quotesJson.getJSONObject(i).isNull(Quotes.AUTHOR)){
					quote.put(Quotes.AUTHOR, quotesJson.getJSONObject(i).getString(Quotes.AUTHOR));
				}
				
				quote.put(Quotes.TEXT, quotesJson.getJSONObject(i).getString(Quotes.TEXT));
				quotesValues[i] = quote;
				Log.v("Ilya", quote.getAsString(Quotes.TEXT));
			}
		} catch (JSONException e) {
			throw new DataException(e.getMessage());
		}
		
		context.getContentResolver().delete(Quotes.CONTENT_URI, null, null);
		context.getContentResolver().bulkInsert(Quotes.CONTENT_URI, quotesValues);
		return null;
	}

}
