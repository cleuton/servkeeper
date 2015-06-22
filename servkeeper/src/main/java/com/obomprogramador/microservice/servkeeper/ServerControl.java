package com.obomprogramador.microservice.servkeeper;



import org.apache.log4j.Logger;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import com.obomprogramador.microservice.servkeeper.model.AppConfig;
import com.obomprogramador.microservice.servkeeper.resources.ServerResource;
import com.obomprogramador.microservice.servkeeper.wrappers.DockerWrapper;
import com.obomprogramador.microservice.servkeeper.wrappers.ZookeeperWrapper;


public class ServerControl extends Application<AppConfig>{
	
	private Logger logger = Logger.getLogger(this.getClass());

    public static void main(String[] args) throws Exception {
        new ServerControl().run(args);
    }
	
    @Override
    public String getName() {
        return "servkeeper";
    }
    
    @Override
    public void initialize(Bootstrap<AppConfig> bootstrap) {

    }    
	
	@Override
	public void run(AppConfig configuration, Environment environment)
			throws Exception {
        final ServerResource resource = new ServerResource();
        resource.config = configuration;
        resource.dcw = new DockerWrapper(configuration.getDockerHost(), 
        		configuration.getDockerCertPath(), "i");
        resource.zkw = new ZookeeperWrapper(configuration.getZkHost());
        resource.zkw.createCounter("/" + configuration.getContainerBaseName() + "_counter");
        logger.info("@ Counter initialized: " + resource.zkw.getCounter().get().postValue());
        environment.jersey().register(resource);		
	}

}
