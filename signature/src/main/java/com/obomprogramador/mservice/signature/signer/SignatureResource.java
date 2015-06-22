package com.obomprogramador.mservice.signature.signer;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;
import com.obomprogramador.microservice.servkeeper.ServiceClient.Increment;

@Path("/signature")
public class SignatureResource {
	private String keystorePath;
	private String alias;
	private String keystorePassword;
	private String zkServerAddress;
	private boolean errorState;
	private String  errorMessage;
	private Increment increment;
	
	@GET
	@Path("checkstatus")
	@Timed
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkStatus() {
    	String mensagem = "";
    	String status = "ok";
    	int httpStatus = 200;
    	
    	// This is a fake status check. In a real service, you should check a

    	if (this.errorState) {
    		mensagem = this.errorMessage;
    	}
    	else {
    		mensagem = "status ok";
    	}
    	
    	String saidaJSON = "{ \"status\": \"" + status + "\","
    					 + "\"data\": {"
    					 + "\"mensagem\": \"" + mensagem + "\"}}"; 
    	
        return Response.status(httpStatus).entity(saidaJSON).build();

	}
	
	
    @POST
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    public Response verify(SampleDocument document) {
    	boolean resultado = false;
    	String mensagem = "";
    	String status = "";
    	int httpStatus = 200;
    	try {
			resultado = VerifySignature.verify(document.getHexSignature(), document.getTexto(),
					this.keystorePath, this.alias, this.keystorePassword);
	    	if (!resultado) {
	    		status = "fail";
	    		mensagem = "assinatura incorreta";
	    	}
	    	else {
	    		status = "success";
	    		mensagem = "assinatura ok!";    		
	    	}
	    	this.increment.increment();
		} catch (Exception e) {
			status = "error";
			mensagem = e.getClass().getName() + ": " + e.getLocalizedMessage();
			httpStatus = 500;
			this.errorState = true;
			this.errorMessage = "Exception: " + e.getMessage();
		} 

    	String saidaJSON = "{ \"status\": \"" + status + "\","
    					 + "\"data\": {"
    					 + "\"mensagem\": \"" + mensagem + "\"}}"; 
        return Response.status(httpStatus).entity(saidaJSON).build();
    }

	public SignatureResource(String keystorePath, String alias,
			String keystorePassword, 
			String zkServerAddress) {
		super();
		this.keystorePath = keystorePath;
		this.alias = alias;
		this.keystorePassword = keystorePassword;
		this.zkServerAddress = zkServerAddress;
		this.increment = new Increment(this.zkServerAddress, "/signatureserver_counter");
	}	
    
    
}
