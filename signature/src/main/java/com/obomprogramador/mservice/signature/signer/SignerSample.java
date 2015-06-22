package com.obomprogramador.mservice.signature.signer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class SignerSample {
	
	private static Logger logger = LogManager.getLogger(SignerSample.class);
	public static byte[] signTestFile() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException {
		InputStream keystoreLocation = SignerSample.class.getClassLoader().getResourceAsStream("minhakeystore.jks");
	    byte[] realSig = null;
	    KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
	    String keystorePassword = "teste001";
	    keystore.load(keystoreLocation, keystorePassword.toCharArray());
	    Key key = keystore.getKey("meucertificado", keystorePassword.toCharArray());
	    if (key instanceof PrivateKey) {
	    	try {
		    	Signature sig = Signature.getInstance("MD5withRSA", "SunRsaSign"); 
		    	sig.initSign((PrivateKey) key);
		    	InputStream filepath = SignerSample.class.getClassLoader().getResourceAsStream("arquivo.txt");
		    	BufferedInputStream bufin = new BufferedInputStream(filepath);
		    	byte[] buffer = new byte[1024];
		    	int len;
		    	while ((len = bufin.read(buffer)) >= 0) {
		    		sig.update(buffer, 0, len);
		    	};
		    	bufin.close();	
		    	realSig = sig.sign();
	    	}
	    	catch (Exception ex) {
	    		logger.error("@@@ Exception: " + ex.getLocalizedMessage());
	    		realSig = null;
	    	}
	    }
    	return realSig;
	}		
}
