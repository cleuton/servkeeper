package com.obomprogramador.microservice.servkeeper;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.log4j.Logger;
import org.junit.Test;

import com.obomprogramador.microservice.servkeeper.model.AppConfig;
import com.obomprogramador.microservice.servkeeper.model.Instance;
import com.obomprogramador.microservice.servkeeper.wrappers.DockerWrapper;
import com.obomprogramador.microservice.servkeeper.wrappers.ZookeeperWrapper;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerException;

public class TestZkWrapper1 {
	
	private Logger logger = Logger.getLogger(this.getClass());

	@Test
	public void test() throws Exception {

		AppConfig config = new AppConfig();
		config.setPath("/Users/cleutonsampaio/Documents/projetos/dockertest");
		config.setContainerBaseName("signatureserver");
		config.setImageName("signatureimage");
		config.setSourcePortNumber(3000);
		config.setRestCommand("");
		Instance si = new Instance();
		si.setContainerName("signatureserver_0");
		si.setServiceName("signatureserver");
		si.setId("120A");
		si.setImageName("signatureimage");
		si.setIpAddress("127.0.0.1");
		si.setPort(3000);
		si.setMappedPort(3001);
		
		// Test Zookeeper Wrapper:
		ZookeeperWrapper zkw = new ZookeeperWrapper("localhost:2181");
		zkw.registerNewInstance(config, si);
		
		Instance instance2 = zkw.getInstance(config);
		assertTrue(instance2.equals(si));
		logger.info("@ InstanceID: " + instance2.getId());
		
		// Delete instance:
		boolean response = zkw.deleteInstance(instance2);
		assertTrue(response);

		
	}

}
