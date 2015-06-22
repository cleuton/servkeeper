package com.obomprogramador.microservice.servkeeper.wrappers;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.obomprogramador.microservice.servkeeper.model.AppConfig;
import com.obomprogramador.microservice.servkeeper.model.Instance;
import com.obomprogramador.microservice.servkeeper.model.ServerAddress;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.BuildParameter;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

/**
 * This class encapsulates all docker services.
 * @author cleutonsampaio
 *
 */
public class DockerWrapper {

	private Logger logger = Logger.getLogger(this.getClass());
	private String dockerHost;
	private String certPath;
	private DockerClient docker;
	
	/**
	 * Constructor.
	 * @param dockerHost String. The docker host ex: https://something:2376
	 * @param certPath String. The certificate path
	 * @param feedBackLevel String. The logging level: q = ERROR, v = DEBUG, i = info (default)
	 * @throws DockerCertificateException
	 */
	public DockerWrapper(String dockerHost, String certPath, String feedBackLevel) throws DockerCertificateException {
		this.dockerHost = dockerHost;
		this.certPath = certPath;
		this.docker = DefaultDockerClient.builder()
			    .uri(URI.create(this.dockerHost))
			    .dockerCertificates(new DockerCertificates(Paths.get(this.certPath)))
			    .build();
		if (feedBackLevel.equalsIgnoreCase("q")) {
			Logger.getRootLogger().setLevel(Level.ERROR);
			logger.setLevel(Level.ERROR);
		}
		else if (feedBackLevel.equalsIgnoreCase("v")) {
				Logger.getRootLogger().setLevel(Level.DEBUG);
				logger.setLevel(Level.DEBUG);
		}
	}

	/**
	 * Create a new instance.
	 * @param address String. ServerAddress instance
	 * @param serverPort int. Server listening port
	 * @param dockerFilePath String. Dockerfile path.
	 * @param dockerImageName String. Docker image name.
	 * @param serviceName String. Service base name (Zookeeper)
	 * @return
	 * @throws DockerCertificateException
	 * @throws DockerException
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public Instance up(ServerAddress address, 
			int    serverPort,
			String dockerFilePath,
			String dockerImageName,
			String serviceName
				) throws DockerCertificateException, DockerException, InterruptedException, IOException {
		Instance si = new Instance();
		logger.info("@ will start a new Container for docker Path: " + dockerFilePath);
		logger.debug("@ Sourceport: " + serverPort + ", hostPort: " + address);
		Logger.getRootLogger().setLevel(Level.INFO);
		BuildParameter [] params = { BuildParameter.QUIET };
		String imageId = docker.build(Paths.get(dockerFilePath), 
					 dockerImageName, params);
		logger.info("@ Image created. Image id: " + imageId);
		
		// Create container with exposed ports
/*
		 ContainerConfig containerConfig = ContainerConfig.builder()
			        .exposedPorts("8080")
			        // other container config
			        .build();
			    dockerClient.createContainer(containerConfig);

      Map<String, List<PortBinding>> portBindings = Maps.newHashMap();
      // you can leave the host IP empty for the PortBinding.of first parameter
      portBindings.put("80/tcp", Lists.newArrayList(PortBinding.of("", "8080"))); 
      HostConfig hostConfig = HostConfig.builder()
          .portBindings(portBindings)
          .networkMode("bridge")
          // other host config here
          .build();
      dockerClient.startContainer(containerId, hostConfig);			    			    
	*/	
		String exposedPort = "" + address.port;
		String containerPort = serverPort + "/tcp";
		
		final String[] ports = {serverPort +""};
		
		// Bind container ports to host ports
		final Map<String, List<PortBinding>> portBindings = new HashMap<String, List<PortBinding>>();
		List<PortBinding> hostPorts = new ArrayList<PortBinding>();
		hostPorts.add(PortBinding.of("", exposedPort));
		portBindings.put(containerPort + "", hostPorts);
		
		final HostConfig hostConfig = HostConfig.builder()
				.portBindings(portBindings).build();

		final ContainerConfig configCt = ContainerConfig.builder()
			    .image(dockerImageName).exposedPorts(exposedPort)
			    .hostConfig(hostConfig)
			    .build();
		

		final ContainerCreation creation = docker.createContainer(configCt);
		final String id = creation.id();

		// Inspect container
		final ContainerInfo info = docker.inspectContainer(id);
		// Start container
		docker.startContainer(id);
		logger.info("@ Container " + creation.toString() + " started. Info: " + info);
		ContainerInfo ci = docker.inspectContainer(id);
		si.setContainerName(info.name());
		si.setServiceName(serviceName);
		si.setId(id);
		si.setImageName(dockerImageName);
		si.setIpAddress(address.host);
		si.setPort(serverPort);
		si.setMappedPort(address.port);
		si.setRunning(true);
		si.setStartTime(new Date());
		return si;		
	}
	
	/**
	 * Delete a container instance.
	 * @param si ServiceInstance. The instance to kill.
	 * @return True if ok.
	 */
	public boolean delete(Instance si) {
		boolean returnCode = true;
		try {
			final ContainerInfo info = docker.inspectContainer(si.getId());
			logger.debug("@ Container " + si.getId() +" found. Will delete it.");
			// Kill container
			docker.killContainer(si.getId());
			// Remove container
			docker.removeContainer(si.getId());
			si.setRunning(false);
			logger.info("@ Container " + si.getContainerName() + " stopped and removed.");
		} catch (DockerException e) {
			returnCode = false;
			logger.error("# Exception: " + e.getMessage());

		} catch (InterruptedException e) {
			returnCode = false;
			logger.error("# Exception: " + e.getMessage());
		}
		
		return returnCode;
	}
	
	public int getCount() throws DockerException, InterruptedException {
		int containerCount = 0;
		List<Container> lista = this.docker.listContainers(ListContainersParam.allContainers(false));
		containerCount = lista.size();
		return containerCount;
	}

}
