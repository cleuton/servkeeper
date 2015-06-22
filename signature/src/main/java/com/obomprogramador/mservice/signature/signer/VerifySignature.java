package com.obomprogramador.mservice.signature.signer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;

import javax.security.cert.Certificate;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class VerifySignature {
	public static boolean verify(String hexSignature, String texto,
				String keystorePath, String alias, String keystorePassword) 
						throws KeyStoreException, NoSuchAlgorithmException, 
								CertificateException, IOException, InvalidKeyException, 
								NoSuchProviderException, DecoderException, SignatureException {
		boolean resultado = false;
		InputStream keystoreLocation = null;
		if (!keystorePath.equals("*")) {
			FileInputStream fisKs = new FileInputStream(keystorePath);
			keystoreLocation = fisKs;
		}
		else {
			InputStream isKs = SignerSample.class.getClassLoader().getResourceAsStream("verifykeystore.jks");
			keystoreLocation = isKs;
		}
	    KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
	    keystore.load(keystoreLocation, keystorePassword.toCharArray());
	    java.security.cert.Certificate certificate = keystore.getCertificate(alias);
	    PublicKey pubKey = certificate.getPublicKey();
    	Signature sig = Signature.getInstance("MD5withRSA", "SunRsaSign"); 
    	sig.initVerify(pubKey);
    	Hex hex = new Hex();
    	byte [] textContent = texto.getBytes("UTF-8");
	    sig.update(textContent);
    	byte [] signature = (byte[]) hex.decode(hexSignature);
	    
    	resultado = sig.verify(signature);
    	
		return resultado;
	}
}
