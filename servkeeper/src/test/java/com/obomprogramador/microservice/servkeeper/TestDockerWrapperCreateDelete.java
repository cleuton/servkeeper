package com.obomprogramador.microservice.servkeeper;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.obomprogramador.microservice.servkeeper.model.AppConfig;
import com.obomprogramador.microservice.servkeeper.model.Instance;
import com.obomprogramador.microservice.servkeeper.model.ServerAddress;
import com.obomprogramador.microservice.servkeeper.wrappers.DockerWrapper;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerException;

public class TestDockerWrapperCreateDelete {

	@Test
	public void test() throws DockerCertificateException, DockerException, InterruptedException, IOException {
		String dockerHost = "https://192.168.59.103:2376";
		String certPath = "/Users/cleutonsampaio/.boot2docker/certs/boot2docker-vm";
		DockerWrapper dw = new DockerWrapper(dockerHost, certPath, "i");
		ServerAddress sa = new ServerAddress("localhost", 3001);
		Instance si = dw.up(sa,3000,
				"/Users/cleutonsampaio/Documents/projetos/dockertest",
				"signatureimage",
				"signatureserver");
		assertTrue(si != null);
		assertTrue(si.isRunning());
		assertTrue(si.getId() != null);
		boolean retorno = dw.delete(si);
		assertTrue(retorno);
		assertFalse(si.isRunning());
		assertTrue(si.getId() == null);
	}

}
