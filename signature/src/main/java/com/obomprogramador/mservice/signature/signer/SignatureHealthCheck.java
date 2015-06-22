package com.obomprogramador.mservice.signature.signer;

import com.codahale.metrics.health.HealthCheck;

public class SignatureHealthCheck extends HealthCheck {

	@Override
	protected Result check() throws Exception {
		// TODO Auto-generated method stub
		return Result.healthy();
	}

}
