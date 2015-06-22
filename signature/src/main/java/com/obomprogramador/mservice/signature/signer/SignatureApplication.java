package com.obomprogramador.mservice.signature.signer;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import com.obomprogramador.mservice.signature.signer.SignatureConfiguration;


public class SignatureApplication extends Application<SignatureConfiguration>{

    public static void main(String[] args) throws Exception {
        new SignatureApplication().run(args);
    }
	
    @Override
    public String getName() {
        return "signature";
    }
    
    @Override
    public void initialize(Bootstrap<SignatureConfiguration> bootstrap) {
    	
    }    
	
	@Override
	public void run(SignatureConfiguration configuration, Environment environment)
			throws Exception {
        final SignatureResource resource = new SignatureResource(
                configuration.getKeystorePath(),
                configuration.getAlias(),
                configuration.getKeystorePassword(),
                configuration.getZpServerAddress()
            );
        final SignatureHealthCheck healthCheck =
                new SignatureHealthCheck();
        environment.healthChecks().register("signature", healthCheck);
        environment.jersey().register(resource);		
	}

}
