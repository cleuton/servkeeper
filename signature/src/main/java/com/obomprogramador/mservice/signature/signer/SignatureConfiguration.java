package com.obomprogramador.mservice.signature.signer;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

public class SignatureConfiguration extends Configuration {
	/*
	 * Asterisco indica que o Serviço deve usar a keystore que está dentro do Jar. 
	 * Caso contrário, será o path da keystore a ser utilizada.
	 */
	private String keystorePath; 
	
	/*
	 * O alias da chave a ser recuperada da keystore.
	 */
	private String alias;
	
	/*
	 * Senha da keystore.
	 */
	private String keystorePassword;
	
	/**
	 * Endereço e Porta do servidor Zookeeper
	 * @return
	 */
	private String zpServerAddress;
	
	@JsonProperty
	public String getZpServerAddress() {
		return zpServerAddress;
	}

	public void setZpServerAddress(String zpServerAddress) {
		this.zpServerAddress = zpServerAddress;
	}

	@JsonProperty
	public String getKeystorePath() {
		return keystorePath;
	}

	public void setKeystorePath(String keystorePath) {
		this.keystorePath = keystorePath;
	}

	@JsonProperty
	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	@JsonProperty
	public String getKeystorePassword() {
		return keystorePassword;
	}

	public void setKeystorePassword(String keystorePassword) {
		this.keystorePassword = keystorePassword;
	}
	
	
}
