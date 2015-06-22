package com.obomprogramador.mservice.signature;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.obomprogramador.mservice.signature.signer.SignerSample;
import com.obomprogramador.mservice.signature.signer.VerifySignature;

public class TestSignature {
	
	private Logger logger = LogManager.getLogger(this.getClass());

	@Test
	public void test() throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, InvalidKeyException, NoSuchProviderException, SignatureException, DecoderException {
		byte [] assinatura = SignerSample.signTestFile();		
		assertTrue(assinatura != null);
		String saida = Hex.encodeHexString(assinatura);
		logger.debug("Assinatura: " + saida);
		
		// Verifica a assinatura:
    	InputStream filepath = SignerSample.class.getClassLoader().getResourceAsStream("arquivo.txt");
    	byte [] bTexto = IOUtils.toByteArray(filepath);
    	String texto = new String(bTexto, "UTF-8");
    	logger.debug("Texto: " + texto);
    	boolean resultado = VerifySignature.verify(saida, texto,
				"*", "meucertificado", "teste001");
    	assertTrue(resultado);
    	
    	/*
    	 
    	// Verifica com keystore externa (troque o path antes de rodar esse teste)
    	resultado = VerifySignature.verify(saida, texto,
				"/home/cleuton/wsDropwizard01/certstore/verifykeystore.jks", 
				"meucertificado", "teste001");
    	assertTrue(resultado);
		*/

    	// Altera o texto e verifica novamente: 
    	bTexto[5] = 61;
    	String texto2 = new String(bTexto, "UTF-8");
    	resultado = VerifySignature.verify(saida, texto2,
				"*", "meucertificado", "teste001");
    	assertFalse(resultado);
	}
	
	//C:/Users/55018335734/wsDropwizard01/certstore/verifykeystore.jks

}
