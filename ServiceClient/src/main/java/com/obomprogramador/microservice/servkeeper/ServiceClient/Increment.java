package com.obomprogramador.microservice.servkeeper.ServiceClient;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.retry.RetryUntilElapsed;

public class Increment {
	private CuratorFramework client;
	private String counterPath;
	private DistributedAtomicLong counter;
	
	public Increment(String zkAddress, String counterPath) {
		this.client = 
				CuratorFrameworkFactory.newClient(zkAddress, new RetryNTimes(5, 1000));
		client.start();
		this.counterPath = counterPath;
	}
	
	public void increment() throws Exception {
		int tempoMaximoDeTentativasMilissegundos = 1000;
		int intervaloEntreTentativasMilissegundos = 100;
		RetryPolicy rp = new RetryUntilElapsed(tempoMaximoDeTentativasMilissegundos, 
				intervaloEntreTentativasMilissegundos);
		this.counter = new DistributedAtomicLong(this.client,
                this.counterPath,
                rp);
		this.counter.increment();

	}
}
