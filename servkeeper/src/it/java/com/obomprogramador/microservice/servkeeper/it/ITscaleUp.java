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
import org.apache.log4j.Logger;
import org.junit.Test;

import com.google.gson.Gson;

public class ITscaleUp {
	
	Logger logger = Logger.getLogger(this.getClass());

	@Test
	public void test() throws ClientProtocolException, IOException {

		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		/*
		 * You must start the server prior to this test.
		 * The server must have zero instances.
		 * Second Track: Scale down:
		 * - Start with 2 instances
		 * - Change the shared counter above the limit
		 * - Run "supervise", one more instance must be created
		 * - Check that are 3 instances
		 */
		
		// Run Start. There must be 2 instances (startServerInstances)
		this.makeRequest(httpclient, "http://localhost:3000/servkeeper/start", "Initialized");

		// Test instancescount = 2:
		this.makeRequest(httpclient, "http://localhost:3000/servkeeper/instancescount", 
				"Zookeeper servers: 2, Docker containers: 2");
	
		// Change counter: 
		this.makeRequest(httpclient, "http://localhost:3000/servkeeper/setcounter?value=1000", 
				"value 1000");
	
		// Run supervise: expected scale up
		this.makeRequest(httpclient, "http://localhost:3000/servkeeper/supervise", 
				"scaledup");		

		// Test instancescount = 3:
		this.makeRequest(httpclient, "http://localhost:3000/servkeeper/instancescount", 
				"Zookeeper servers: 3, Docker containers: 3");

		// StopAll
		this.makeRequest(httpclient, "http://localhost:3000/servkeeper/stopall", 
				"All stop.");

		// Test instancescount must be: docker 0 zookeeper 0
		this.makeRequest(httpclient, "http://localhost:3000/servkeeper/instancescount", 
				"Zookeeper servers: 0, Docker containers: 0");

		
	}

	private void makeRequest(CloseableHttpClient httpclient,
							 String requestPath,
							 String responseToCheck)
			throws IOException, ClientProtocolException {
		logger.info("@ Request: " + requestPath 
					+ ", response: " + responseToCheck);
					
		HttpGet httpGet;
		CloseableHttpResponse response1;
		httpGet = new HttpGet(requestPath);
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
			logger.info("  - Response: " + sb.toString());

			Gson gson = new Gson();
			RestResponse rr = gson.fromJson(sb.toString(), RestResponse.class);
			assertTrue(rr.data.mensagem.equalsIgnoreCase(responseToCheck));
		    EntityUtils.consume(entity1);
		} finally {
		    response1.close();
		}
	}

}
