package com.obomprogramador.mservice.signature.signer;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SampleDocument {
	private String texto;
	private String hexSignature;
	
	@JsonProperty
	public String getTexto() {
		return texto;
	}
	public void setTexto(String texto) {
		this.texto = texto;
	}
	
	@JsonProperty
	public String getHexSignature() {
		return hexSignature;
	}
	public void setHexSignature(String hexSignature) {
		this.hexSignature = hexSignature;
	}
	public SampleDocument(String texto, String hexSignature) {
		super();
		this.texto = texto;
		this.hexSignature = hexSignature;
	}
	public SampleDocument() {
		super();
	}
	
}
