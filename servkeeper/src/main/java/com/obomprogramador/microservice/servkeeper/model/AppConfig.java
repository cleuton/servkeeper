package com.obomprogramador.microservice.servkeeper.model;

import io.dropwizard.Configuration;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Service configuration
 * @author cleutonsampaio
 *
 */
public class AppConfig extends Configuration {
	private String dockerHost;
	private String dockerCertPath;
	private String zkHost;
	private String path; 
	private String imageName;
	private String containerBaseName;
	private String serviceHostNameIP;
	private int    sourcePortNumber;
	private int    startServerInstances;
	private int    minServerInstances;
	private int	   maxServerInstances;
	private int	   maxRequestLimit;
	private int    minRequestLimit;
	private String RestCommand;
	private String description;
	private String serviceTestPah;
	private List<ServerAddress> serverAddresses;

	@JsonProperty
	public List<ServerAddress> getServerAddresses() {
		return serverAddresses;
	}

	@JsonProperty
	public String getDockerCertPath() {
		return dockerCertPath;
	}

	public void setDockerCertPath(String dockerCertPath) {
		this.dockerCertPath = dockerCertPath;
	}

	public void setServerAddresses(List<ServerAddress> serverAddresses) {
		this.serverAddresses = serverAddresses;
	}

	@JsonProperty
	public String getZkHost() {
		return zkHost;
	}

	public void setZkHost(String zkHost) {
		this.zkHost = zkHost;
	}

	@JsonProperty
	public String getDockerHost() {
		return dockerHost;
	}

	public void setDockerHost(String dockerHost) {
		this.dockerHost = dockerHost;
	}

	@JsonProperty
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public AppConfig() {
		super();
	}

	/**
	 * App config.
	 * @param path String. Path of the Dockerfile.
	 * @param imageName String. Image name.
	 * @param containerBaseName. String. Base name of generated containers.
	 * @param sourcePortNumber. Source port.
	 * @param restCommand.      Rest command.
	 */
	public AppConfig(String path, String imageName, String containerBaseName, int sourcePortNumber,
			int mappedPortNumber, String restCommand) {
		this();
		this.path = path;
		this.imageName = imageName;
		this.containerBaseName = containerBaseName;
		this.sourcePortNumber = sourcePortNumber;
		this.serverAddresses = new ArrayList<ServerAddress>();
		RestCommand = restCommand;
	}

	
	@JsonProperty
	public String getServiceHostNameIP() {
		return serviceHostNameIP;
	}

	public void setServiceHostNameIP(String serviceHostNameIP) {
		this.serviceHostNameIP = serviceHostNameIP;
	}

	@JsonProperty
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@JsonProperty
	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	@JsonProperty
	public String getContainerBaseName() {
		return containerBaseName;
	}

	public void setContainerBaseName(String containerBaseName) {
		this.containerBaseName = containerBaseName;
	}

	@JsonProperty
	public int getSourcePortNumber() {
		return sourcePortNumber;
	}

	public void setSourcePortNumber(int sourcePortNumber) {
		this.sourcePortNumber = sourcePortNumber;
	}

	@JsonProperty
	public String getRestCommand() {
		return RestCommand;
	}

	public void setRestCommand(String restCommand) {
		RestCommand = restCommand;
	}

	@JsonProperty
	public int getMinServerInstances() {
		return minServerInstances;
	}

	public void setMinServerInstances(int minServerInstances) {
		this.minServerInstances = minServerInstances;
	}

	@JsonProperty
	public int getMaxServerInstances() {
		return maxServerInstances;
	}

	public void setMaxServerInstances(int maxServerInstances) {
		this.maxServerInstances = maxServerInstances;
	}

	@JsonProperty
	public int getMaxRequestLimit() {
		return maxRequestLimit;
	}

	public void setMaxRequestLimit(int maxRequestLimit) {
		this.maxRequestLimit = maxRequestLimit;
	}

	@JsonProperty
	public int getMinRequestLimit() {
		return minRequestLimit;
	}

	public void setMinRequestLimit(int minRequestLimit) {
		this.minRequestLimit = minRequestLimit;
	}

	@JsonProperty
	public String getServiceTestPah() {
		return serviceTestPah;
	}

	public void setServiceTestPah(String serviceTestPah) {
		this.serviceTestPah = serviceTestPah;
	}

	@JsonProperty
	public int getStartServerInstances() {
		return startServerInstances;
	}

	public void setStartServerInstances(int startServerInstances) {
		this.startServerInstances = startServerInstances;
	}
	
	
	
	
}
