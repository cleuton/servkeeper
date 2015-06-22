package com.obomprogramador.microservice.servkeeper.it;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import com.google.gson.Gson;

public class ITstartRemove {

	@Test
	public void test() throws ClientProtocolException, IOException {
		
		
		// Test start: 
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet("http://localhost:3000/servkeeper/start");
		CloseableHttpResponse response1 = httpclient.execute(httpGet);
		try {
		    System.out.println(response1.getStatusLine());
		    HttpEntity entity1 = response1.getEntity();
		    BufferedReader br = new BufferedReader(
                    new InputStreamReader((entity1.getContent())));
		    
		    StringBuilder sb = new StringBuilder();
		    String output;
			while ((output = br.readLine()) != null) {
				sb.append(output);
			}
			
			Gson gson = new Gson();
			RestResponse rr = gson.fromJson(sb.toString(), RestResponse.class);
			assertTrue(rr.data.mensagem.equalsIgnoreCase("Initialized"));
		    EntityUtils.consume(entity1);
		} finally {
		    response1.close();
		}

		// Test instancescount: 
		httpGet = new HttpGet("http://localhost:3000/servkeeper/instancescount");
		response1 = httpclient.execute(httpGet);
		try {
		    System.out.println(response1.getStatusLine());
		    HttpEntity entity1 = response1.getEntity();
		    BufferedReader br = new BufferedReader(
                    new InputStreamReader((entity1.getContent())));
		    
		    StringBuilder sb = new StringBuilder();
		    String output;
			while ((output = br.readLine()) != null) {
				sb.append(output);
			}
			
			Gson gson = new Gson();
			RestResponse rr = gson.fromJson(sb.toString(), RestResponse.class);
			assertTrue(rr.data.mensagem.equalsIgnoreCase("Zookeeper servers: 1, Docker containers: 1"));
		    EntityUtils.consume(entity1);
		} finally {
		    response1.close();
		}
		
		// Test stopAll:
		httpGet = new HttpGet("http://localhost:3000/servkeeper/stopall");
		response1 = httpclient.execute(httpGet);
		try {
		    System.out.println(response1.getStatusLine());
		    HttpEntity entity1 = response1.getEntity();
		    BufferedReader br = new BufferedReader(
                    new InputStreamReader((entity1.getContent())));
		    
		    StringBuilder sb = new StringBuilder();
		    String output;
			while ((output = br.readLine()) != null) {
				sb.append(output);
			}
			
			Gson gson = new Gson();
			RestResponse rr = gson.fromJson(sb.toString(), RestResponse.class);
			assertTrue(rr.data.mensagem.equalsIgnoreCase("All stop."));
		    EntityUtils.consume(entity1);
		} finally {
		    response1.close();
		}

		// Test instancescount after stopAll:
		httpGet = new HttpGet("http://localhost:3000/servkeeper/instancescount");
		response1 = httpclient.execute(httpGet);
		try {
		    System.out.println(response1.getStatusLine());
		    HttpEntity entity1 = response1.getEntity();
		    BufferedReader br = new BufferedReader(
                    new InputStreamReader((entity1.getContent())));
		    
		    StringBuilder sb = new StringBuilder();
		    String output;
			while ((output = br.readLine()) != null) {
				sb.append(output);
			}
			
			Gson gson = new Gson();
			RestResponse rr = gson.fromJson(sb.toString(), RestResponse.class);
			assertTrue(rr.data.mensagem.equalsIgnoreCase("Zookeeper servers: 0, Docker containers: 0"));
		    EntityUtils.consume(entity1);
		} finally {
		    response1.close();
		}

	
	}

}
