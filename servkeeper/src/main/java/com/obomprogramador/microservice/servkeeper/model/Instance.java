package com.obomprogramador.microservice.servkeeper.model;

import java.util.Date;

import org.codehaus.jackson.map.annotate.JsonRootName;

@JsonRootName("details")
public class Instance implements Comparable<Instance> {
	private String id;
	private String description;
	private String containerName;
	private String serviceName;
	private int    port;
	private int    mappedPort;
	private String ipAddress;
	private String imageName;
	private String restUrl;
	private Date   startTime;
	private boolean running; 
	
	
	public Instance() {
		super();
	}
	
	
	@Override
	public boolean equals(Object obj) {
		return this.getId().equals(((Instance) obj).getId());
	}

	

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}
	
	


	public Instance(String id, String description, String containerName,
			int port, int mappedPort, String ipAddress, String imageName, String restUrl) {
		super();
		this.id = id;
		this.description = description;
		this.containerName = containerName;
		this.port = port;
		this.mappedPort = mappedPort;
		this.ipAddress = ipAddress;
		this.imageName = imageName;
		this.restUrl = restUrl;
	}

	
	
	public String getServiceName() {
		return serviceName;
	}


	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}


	public int getMappedPort() {
		return mappedPort;
	}


	public void setMappedPort(int mappedPort) {
		this.mappedPort = mappedPort;
	}


	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getContainerName() {
		return containerName;
	}

	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public String getRestUrl() {
		return restUrl;
	}

	public void setRestUrl(String restUrl) {
		this.restUrl = restUrl;
	}


	@Override
	public int compareTo(Instance o) {
		return this.id.compareToIgnoreCase(o.id);
	}


	@Override
	public String toString() {
		return this.containerName + ":" + this.id
				+ ":" + this.ipAddress 
				+ ":" + this.port
				+ ":" + this.mappedPort;
	}
	
	
}
