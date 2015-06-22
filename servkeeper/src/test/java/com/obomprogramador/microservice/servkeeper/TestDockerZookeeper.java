package com.obomprogramador.microservice.servkeeper;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.obomprogramador.microservice.servkeeper.model.AppConfig;
import com.obomprogramador.microservice.servkeeper.model.Instance;
import com.obomprogramador.microservice.servkeeper.model.ServerAddress;
import com.obomprogramador.microservice.servkeeper.wrappers.DockerWrapper;
import com.obomprogramador.microservice.servkeeper.wrappers.ZookeeperWrapper;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerException;

public class TestDockerZookeeper {

	private Logger logger = Logger.getLogger(this.getClass());
	@Test
	public void test() throws Exception {
		// Up some docker instances: 
		String dockerHost = "https://192.168.59.103:2376";
		String certPath = "/Users/cleutonsampaio/.boot2docker/certs/boot2docker-vm";
		DockerWrapper dw = new DockerWrapper(dockerHost, certPath, "i");

		// Port 3001:
		ServerAddress sa = new ServerAddress("localhost", 3001);
		Instance si = dw.up(sa,3000,
				"/Users/cleutonsampaio/Documents/projetos/dockertest",
				"signatureimage",
				"signatureserver");
		
		// Port 3002:
		ServerAddress sa2 = new ServerAddress("localhost", 3002);
		Instance si2 = dw.up(sa2,3000,
				"/Users/cleutonsampaio/Documents/projetos/dockertest",
				"signatureimage",
				"signatureserver");
		
		// Port 3003:
		ServerAddress sa3 = new ServerAddress("localhost", 3003);
		Instance si3 = dw.up(sa3,3000,
				"/Users/cleutonsampaio/Documents/projetos/dockertest",
				"signatureimage",
				"signatureserver");
		
		// Register them in Zookeeper:
		AppConfig config = new AppConfig();
		config.setPath("/Users/cleutonsampaio/Documents/projetos/dockertest");
		config.setContainerBaseName("signatureserver");
		config.setImageName("signatureimage");
		config.setSourcePortNumber(3000);
		config.setRestCommand("");
		
		// Test Zookeeper Wrapper:
		ZookeeperWrapper zkw = new ZookeeperWrapper("localhost:2181");
		zkw.registerNewInstance(config, si);
		zkw.registerNewInstance(config, si2);
		zkw.registerNewInstance(config, si3);

		// Get instances back
		Instance gs1 = zkw.getInstance(config);
		logger.debug("@ Instance 1: " + gs1);
		assertTrue(gs1.equals(si) || gs1.equals(si2) || gs1.equals(si3));
		Instance gs2 = zkw.getInstance(config);
		logger.debug("@ Instance 2: " + gs2);
		assertFalse(gs2.equals(gs1));
		Instance gs3 = zkw.getInstance(config);
		logger.debug("@ Instance 3: " + gs3);
		assertFalse(gs3.equals(gs2) || gs3.equals(gs1));
		
		assertTrue(zkw.deleteInstance(gs1));
		assertTrue(zkw.deleteInstance(gs2));
		assertTrue(zkw.deleteInstance(gs3));
		assertTrue(dw.delete(si));
		assertTrue(dw.delete(si2));
		assertTrue(dw.delete(si3));
		
	}

}
