package com.obomprogramador.microservice.servkeeper.model;

import com.obomprogramador.microservice.servkeeper.wrappers.Server;

/**
 * This class represents a possible Host / Ip Combination, valid for the service.
 * @author cleutonsampaio
 *
 */
public class ServerAddress implements Comparable<ServerAddress> {
	public String host;
	public int port;
	public Server server;
	
	public ServerAddress() {
		super();
	}

	/**
	 * Constructs a new Server Address.
	 * All Server Addresses are free, unless property "server" is not null, 
	 * indicating that this address is already taken.
	 * @param host String. Hostname or IP Address
	 * @param port int. Port
	 */
	public ServerAddress(String host, int port) {
		this();
		this.host = host;
		this.port = port;
	}

	@Override
	public int hashCode() {
		return ((String) (this.host + Integer.toString(this.port))).hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		return this.host.equalsIgnoreCase(((ServerAddress) obj).host) 
				&& this.port == ((ServerAddress) obj).port;
	}
	@Override
	public String toString() {
		return this.host + ":" + this.port;
	}
	@Override
	public int compareTo(ServerAddress o) {
		return (this.host + this.port).compareToIgnoreCase(o.host + o.port);
	}
	
}
