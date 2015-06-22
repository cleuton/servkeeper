package com.obomprogramador.microservice.servkeeper.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.obomprogramador.microservice.servkeeper.it.RestResponse;
import com.obomprogramador.microservice.servkeeper.model.AppConfig;
import com.obomprogramador.microservice.servkeeper.model.Instance;
import com.obomprogramador.microservice.servkeeper.model.ServerAddress;
import com.obomprogramador.microservice.servkeeper.wrappers.DockerWrapper;
import com.obomprogramador.microservice.servkeeper.wrappers.Server;
import com.obomprogramador.microservice.servkeeper.wrappers.ZookeeperWrapper;



@Path("/servkeeper")
public class ServerResource {
	
	public ZookeeperWrapper zkw;
	public DockerWrapper dcw;
	public AppConfig config;
	private Logger logger = Logger.getLogger(this.getClass());
	private List<Server> serversToDelete = new ArrayList<Server>();
	
    @GET
    @Path("stopserver")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
	public Response stopMe() {
    	int httpStatus = 200;
    	String status = "ok";
    	String mensagem = "";
    	String saidaJSON = "";
    	
    	try {
    		logger.debug("@ Will stop all instances");
    		stopAllInstances();
    		logger.debug("@ All instances stopped.");
    		mensagem = "Exiting server.";
        	saidaJSON = "{ \"status\": \"" + status + "\","
   				 + "\"data\": {"
   				 + "\"mensagem\": \"" + mensagem+ "\"}}"; 

    	}
    	catch (Exception ex) {
    		logger.error("*** Error stopping the server: " + ex.getMessage());
    		mensagem = "Exception: " + ex.getMessage();
        	saidaJSON = "{ \"status\": \"" + status + "\","
   				 + "\"data\": {"
   				 + "\"mensagem\": \"" + mensagem+ "\"}}"; 

    	}
    	finally {
    		logger.debug("@ Stopping server.");
    		System.exit(0);
    	}
 
    	return Response.status(httpStatus).entity(saidaJSON).build();

	}
	
    @GET
    @Path("supervise")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * Verifies all intances, checking if it is ok, and if it is time to scale up, down or nothing.
     * 
     * @return Response	JSON.
     */
    public Response verify() {
    	boolean resultado = false;
    	String mensagem = "nothing to do";
    	String status = "";
    	int httpStatus = 200;
    	try {
    		deleteMarkedServers();
    		verifyAllInstances();
    		long requestsSinceLastVerification = getRequests();
    		if (requestsSinceLastVerification >= this.config.getMaxRequestLimit()) {
    			if (this.zkw.getServers().size() < this.config.getMaxServerInstances()) {
    				logger.info("@ Will scale up");
        			scaleUp();    
    				logger.info("@ Successfully scale up. There are now: " 
    						+ this.zkw.getServers().size() + " instances");
    				mensagem = "scaledup";
    			}
    			else {
    				logger.info("@ Cannot scale up. There are: " 
    						+ this.zkw.getServers().size() + " instances and "
    						+ " maximum limit is " + this.config.getMaxServerInstances());
    				mensagem = "cannot scale up";
    			}
    		}
    		else if (requestsSinceLastVerification < this.config.getMinRequestLimit()) {
    				if (this.zkw.getServers().size() > this.config.getMinServerInstances()) {
        				logger.info("@ Will scale down");
    					scaleDown();
    					mensagem = "scaledown " + this.serversToDelete.size();
        				logger.info("@ Successfully scale down. Instances to delete: " 
        						+ this.serversToDelete.size());
    				}
    				else {
        				logger.info("@ Cannot scale down. There are: " 
        						+ this.zkw.getServers().size() + " instances and "
        						+ " minimum limit is " + this.config.getMinServerInstances());
        				mensagem = "cannot scale down";
    					
    				}
    		}
    		
    		// Zero the counter: 
    		this.zkw.getCounter().forceSet((long) 0);
    		logger.debug("@ Set Conter. After: " + this.zkw.getCounter().get().postValue().intValue());

		} catch (Exception e) {
			status = "error";
			mensagem = e.getClass().getName() + ": " + e.getLocalizedMessage();
			httpStatus = 500;
		} 

    	String saidaJSON = "{ \"status\": \"" + status + "\","
    					 + "\"data\": {"
    					 + "\"mensagem\": \"" + mensagem + "\"}}"; 
        return Response.status(httpStatus).entity(saidaJSON).build();
    }
    
    /**
     * Deletes all marked servers. This gives some time for them to complete tasks.
     * @throws IOException
     */
    private void deleteMarkedServers() throws IOException {
		for (Server server : this.serversToDelete) {
			server.close();
			Instance instance = server.getThisInstance().getPayload();
			this.dcw.delete(instance);
			logger.debug("@ Server: "+  instance.getContainerName() 
				+ ", " + instance.getPort() + " deleted.");
		}
		this.serversToDelete.clear();
	}

	/**
     * Marks an arbitrary instance for deletion.
     * Removes it from the available server's list, and put it into a list for deletion.
     */
    private void scaleDown() {
		Server server = this.zkw.getServers().get(this.zkw.getServers().size() - 1);
		this.zkw.getServers().remove(server);
		this.serversToDelete.add(server);
		Instance instance = server.getThisInstance().getPayload();
		logger.debug("@ Server: " +  instance.getContainerName() 
				+ ", " + instance.getPort() + " marked to delete.");
	}

    /**
     * Time to generate a new instance.
     * @throws Exception 
     * 
     */
	private void scaleUp() throws Exception {
		ServerAddress serverAddress = null;
		for (ServerAddress sa : this.config.getServerAddresses()) {
			if (sa.server == null) {
				serverAddress = sa;
				break;
			}
		}
		if (serverAddress == null) {
			String message = "@ There are not enough server addresses to scale up!";
			logger.error(message);
			throw new Exception(message);
		}
		this.startNewInstance(serverAddress);
	}

	/**
	 * Return the shared counter.
	 * @return
	 * @throws Exception 
	 */
	private long getRequests() throws Exception {
		long contador = 0;
		AtomicValue<Long> value = this.zkw.getCounter().get();
		if (value.succeeded()) {
			contador = value.postValue();
		}
		else {
			contador = value.preValue();
		}
		return contador;
	}
	
	/**
	 * Verify each active instance, using the serviceTestPah.
	 * All services must return a JSON Object with this structure: 
	 * { "status": "request status - must be 'ok'", 
    			  "data": { "mensagem" : "status message. Must be: 'status ok'" } }
	 * @throws Exception 
	 */
	private void verifyAllInstances() throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		for (Server server : this.zkw.getServers()) {
			Instance instance = server.getThisInstance().getPayload();
			if (!statusOk(server, httpclient)) {
				logger.error("@ Server instance not ok: " 
						+ instance.getContainerName() 
						+ ", " + instance.getPort());
				deleteInstance(server);
			}
		}
	}

	private void deleteInstance(Server server) throws Exception {
    	this.dcw.delete(server.getThisInstance().getPayload());
    	this.zkw.deleteInstance(server.getThisInstance().getPayload());
    	// Tem que remover o server da lista manualmente: 
    	this.zkw.getServers().remove(server);
    	this.freeServerAddress(server);
	}

	private boolean statusOk(Server server, CloseableHttpClient httpclient)  {
		boolean returnStatus = true;
		Instance instance = server.getThisInstance().getPayload();
		String URL = "http://" 
				+ instance.getIpAddress() + ":" + instance.getPort()
				+ this.config.getServiceTestPah();
		HttpGet httpGet = new HttpGet(URL);
		CloseableHttpResponse response1 = null;
		try {
			response1 = httpclient.execute(httpGet);
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
			if(!rr.data.mensagem.equalsIgnoreCase("status ok")) {
				logger.error("@ Instance: "+ instance.getContainerName() 
						+ ", " + instance.getPort() + " service: " 
						+ this.config.getContainerBaseName() + " error!");
				returnStatus = false;
			}
			else {
				logger.info("@ Server: " 
						+ URL + ", response ok: " + sb.toString());
			}
		    EntityUtils.consume(entity1);
		} catch (IOException e) {
			logger.error("@ IOException checking instance 1.");
			returnStatus = false;
		} finally {
		    try {
				response1.close();
			} catch (IOException e) {
				logger.error("@ IOException checking instance - close.");
				returnStatus = false;
			}
		}
		
		return returnStatus;
	}
	
	@GET
	@Path("getinstance")
	@Timed
	@Produces(MediaType.APPLICATION_JSON)
	public Response getInstance() {
    	int httpStatus = 200;
    	String status = "ok";
    	String mensagem = "";
    	int countZk = this.zkw.getServers().size();
    	int countDocker = 0;
    	
    	try {
    		Instance instance = this.zkw.getInstance(this.config);
    		logger.debug("@ Get Instance: " 
    				+ instance.getIpAddress()
    				+ ":" + instance.getMappedPort());
    		logger.debug("@ Set Conter. After: " + this.zkw.getCounter().get().postValue().intValue());
    		mensagem = instance.getIpAddress()
    				+ ":" + instance.getMappedPort();
    	}
    	catch (Exception ex) {
    		logger.error("*** Error getting instance: " + ex.getMessage());
    		mensagem = "Exception: " + ex.getMessage();
    	}
    	
    	String saidaJSON = "{ \"status\": \"" + status + "\","
				 + "\"data\": {"
				 + "\"mensagem\": \"" + mensagem+ "\"}}"; 
 
    	return Response.status(httpStatus).entity(saidaJSON).build();

	}
	
	@GET 
	@Path("setcounter")
	@Timed
	@Produces(MediaType.APPLICATION_JSON)
	
	public Response setCounter(@QueryParam("value") Optional<Integer> value) {
    	int httpStatus = 200;
    	String status = "ok";
    	String mensagem = "";
    	int countZk = this.zkw.getServers().size();
    	int countDocker = 0;
    	
    	try {
    		int valor = value.or(0);
    		logger.debug("@ Set Conter. Pre-value: " + this.zkw.getCounter().get().postValue().intValue());
    		this.zkw.getCounter().forceSet((long) valor);
    		logger.debug("@ Set Conter. After: " + this.zkw.getCounter().get().postValue().intValue());
    		mensagem = "value " + this.zkw.getCounter().get().postValue().intValue();
    	}
    	catch (Exception ex) {
    		logger.error("*** Error setcounter: " + ex.getMessage());
    		mensagem = "Exception: " + ex.getMessage();
    	}
    	
    	String saidaJSON = "{ \"status\": \"" + status + "\","
				 + "\"data\": {"
				 + "\"mensagem\": \"" + mensagem+ "\"}}"; 
 
    	return Response.status(httpStatus).entity(saidaJSON).build();

	}

	@GET
    @Path("instancescount")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * Return the number of active instances.
     * @return
     */
    public Response getInstancesCount() {
    	int httpStatus = 200;
    	String status = "ok";
    	String mensagem = "Zookeeper servers: %d, Docker containers: %d";
    	int countZk = this.zkw.getServers().size();
    	int countDocker = 0;
    	
    	try {
    		countDocker = this.dcw.getCount();
    		mensagem = String.format(mensagem, countZk, countDocker);
    	}
    	catch (Exception ex) {
    		logger.error("*** Error listing docker containers: " + ex.getMessage());
    		mensagem = "Exception: " + ex.getMessage();
    	}
    	
    	String saidaJSON = "{ \"status\": \"" + status + "\","
				 + "\"data\": {"
				 + "\"mensagem\": \"" + mensagem+ "\"}}"; 
 
    	return Response.status(httpStatus).entity(saidaJSON).build();

    }

    @GET
    @Path("start")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * Init the service with the initial minimum instances.
     * @return
     */
    public Response init() throws Exception {
    	int httpStatus = 200;
    	String status = "ok";
    	String mensagem = "Initialized";
    	logger.debug("@ Processing start request. Instances to start: " + this.config.getStartServerInstances());

		this.zkw.getCounter().forceSet((long) 0);
		logger.debug("@ Zero Conter. After: " + this.zkw.getCounter().get().postValue().intValue());

    	startInstance(this.config.getStartServerInstances());
    	
    	String saidaJSON = "{ \"status\": \"" + status + "\","
				 + "\"data\": {"
				 + "\"mensagem\": \"" + mensagem + "\"}}"; 
 
    	return Response.status(httpStatus).entity(saidaJSON).build();
    }
    
    @GET
    @Path("requests")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRequestCounter() {
    	int httpStatus = 200;
    	String status = "ok";
    	String mensagem = "All stop.";
    	
    	try {
    		mensagem = "requests " 
    				+ this.getRequests();
    	}
    	catch (Exception ex) {
    		logger.error("*** Error getting request counter: " + ex.getMessage());
    		mensagem = "Exception: " + ex.getMessage();
    	}

    	
    	String saidaJSON = "{ \"status\": \"" + status + "\","
				 + "\"data\": {"
				 + "\"mensagem\": \"" + mensagem + "\"}}"; 
 
    	return Response.status(httpStatus).entity(saidaJSON).build();

    }
    
    
    @GET
    @Path("stopall")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * Stop all instances.
     * @return
     */
	public Response stopAll() throws Exception {
    	int httpStatus = 200;
    	String status = "ok";
    	String mensagem = "All stop.";
    	
    	try {
        	stopAllInstances();    		
    	}
    	catch (Exception ex) {
    		logger.error("*** Error stopping instances: " + ex.getMessage());
    		mensagem = "Exception: " + ex.getMessage();
    	}

    	
    	String saidaJSON = "{ \"status\": \"" + status + "\","
				 + "\"data\": {"
				 + "\"mensagem\": \"" + mensagem + "\"}}"; 
 
    	return Response.status(httpStatus).entity(saidaJSON).build();

	}

    /**
     * Stop all instances in docker and remove them from zooleeper.
     * @throws Exception 
     */
    private void stopAllInstances() throws Exception {
    	for (Server server : this.zkw.getServers()) {
    		this.dcw.delete(server.getThisInstance().getPayload());
    		this.zkw.deleteInstance(server.getThisInstance().getPayload());
    		this.freeServerAddress(server);
     	}
    	// Tem que remover o server da lista manualmente: 
    	this.zkw.getServers().clear();
    }
    
    /**
     * Start all required instances.
     * @param minServerInstances Number of instances to start
     * @throws Exception 
     */
	private void startInstance(int minServerInstances) throws Exception {
		logger.debug("@ Will start " + minServerInstances + " instance(s).");
		for (int x=0; x< minServerInstances; x++) {
			ServerAddress sa = this.config.getServerAddresses().get(x);
			logger.debug("@ Instance " + x + ": " + sa);
			try {
				startNewInstance(sa);
			}
			catch(Exception ex) {
				logger.error("*** Exception initializing instances: " + ex.getMessage());
				throw new Exception(ex);
			}
		}
	}

	private void startNewInstance(ServerAddress sa) throws Exception {
		Instance i = this.dcw.up(sa, 
				this.config.getSourcePortNumber(),
				this.config.getPath(), 
				this.config.getImageName(),
				this.config.getContainerBaseName());
		Server server = this.zkw.registerNewInstance(this.config, i);
		// Lock this address:
		sa.server = server;
	}

	/**
	 * Unregister server from address.
	 * The address is then free to be used.
	 * @param server
	 */
	private void freeServerAddress(Server server) {
		for (ServerAddress sa : this.config.getServerAddresses()) {
			if (sa.server != null && sa.server.equals(server)) {
				sa.server = null;
				break;
			}
		}
	}
	
	public ServerResource() {
		super();
	}	
    
    
}
