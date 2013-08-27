package com.example.quoter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;

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

		ConnectionResult result = connection.execute();
		ContentValues[] quotesValues;
		
//		HttpClient client = new DefaultHttpClient();
//		HttpGet get = new HttpGet("http://quoter.100ms.ru/getQuotes.php");
		
		try {
//			HttpResponse getResponse = client.execute(get);
//			HttpEntity entity = getResponse.getEntity();
			String json = result.body;
			json = json.substring(1);
			JSONArray quotesJson =  new JSONArray(json);	
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
			}
			
			
		} catch (JSONException e) {
			throw new DataException(e.getMessage());
		}
		
		context.getContentResolver().delete(Quotes.CONTENT_URI, null, null);
		context.getContentResolver().bulkInsert(Quotes.CONTENT_URI, quotesValues);
		return null;
	}

}
