package com.obomprogramador.microservice.servkeeper.wrappers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceInstanceBuilder;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.curator.x.discovery.UriSpec;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.strategies.RandomStrategy;
import org.apache.log4j.Logger;

import com.obomprogramador.microservice.servkeeper.model.AppConfig;
import com.obomprogramador.microservice.servkeeper.model.Instance;

public class ZookeeperWrapper {

	private String zkAddress;
	private Logger logger = Logger.getLogger(this.getClass());
	private final String BASE_NAME = "/";
	private String counterPath;
	private ServiceDiscovery<Instance> serviceDiscovery;
	private JsonInstanceSerializer<Instance> serializer;
	private CuratorFramework curatorFramework;
	private List<Server> servers;
	private ServiceProvider<Instance> serviceProvider;
	private DistributedAtomicLong counter;
	
	public ZookeeperWrapper(String zkAddress) throws Exception {
		this.zkAddress = zkAddress;
		this.curatorFramework = 
				CuratorFrameworkFactory.newClient(this.zkAddress, new RetryNTimes(5, 1000));
		curatorFramework.start();

		
        this.serializer = new JsonInstanceSerializer<>(Instance.class);

		this.serviceDiscovery = ServiceDiscoveryBuilder.builder(Instance.class)
				    .basePath(this.BASE_NAME)
				    .serializer(serializer)
				    .client(curatorFramework).build();
		serviceDiscovery.start();
		this.servers = new ArrayList<Server>();
	}
	
	@Override
	protected void finalize() throws Throwable {
		logger.debug("@ Closing Curator.");
		for(Server server : this.servers) {
			server.close();
		}
		this.serviceProvider.close();
		this.serviceDiscovery.close();
		this.curatorFramework.close();
		super.finalize();
	}


	/**
	 * Register a new Service Instance (or a New Service) into Zookeeper, 
	 * using apache curator.
	 * @param config AppConfig The app configuration
	 * @param instance Instance the Service Instance
	 * @throws Exception 
	 */
	public Server registerNewInstance(AppConfig config, Instance instance) throws Exception {
		logger.debug("@ Registering: " + config.getContainerBaseName() + ", " + instance.getContainerName());
		Server server = new Server(this.curatorFramework, instance, this.BASE_NAME);
		server.setInstanceId(instance.getId());
		server.start();
		servers.add(server);
		return server;
	}
	
	public Instance getInstance(AppConfig config) throws Exception {
		Instance instance = null;
		if (this.serviceProvider == null) {
			this.serviceProvider = 
					this.serviceDiscovery
					.serviceProviderBuilder()
					.serviceName(config.getContainerBaseName())
					.build();
			this.serviceProvider.start();
		}
		ServiceInstance<Instance> si = this.serviceProvider.getInstance();
		instance = si.getPayload();
		return instance;
	}
	
	public boolean deleteInstance(Instance instance) throws Exception {
		boolean resultCode = true;
		try {

			Server server = new Server();
			server.setInstanceId(instance.getId());
			int posicServer = this.servers.indexOf(server);
			if (posicServer < 0) {
				logger.error("*** Instance not found: " + instance.getId());
				resultCode = false;
			}
			else {
				server = this.servers.get(posicServer);
				server.close();		
				server = null;
				logger.debug("@ Instance deleted: " + instance.getId());
			}
		} catch (Exception e) {
			logger.error("*** Error closing server: " + e.getMessage());
			resultCode = false;
			throw new Exception(e);
		}
		return resultCode;
	}

	public void createCounter(String path) throws Exception {
		int tempoMaximoDeTentativasMilissegundos = 1000;
		int intervaloEntreTentativasMilissegundos = 100;
		this.counterPath = path;
		RetryPolicy rp = new RetryUntilElapsed(tempoMaximoDeTentativasMilissegundos, 
				intervaloEntreTentativasMilissegundos);
		this.counter = new DistributedAtomicLong(this.curatorFramework,
                this.counterPath,
                rp);
		this.counter.initialize((long) 0);
	}
	
	
	public List<Server> getServers() {
		return servers;
	}



	public void setServers(List<Server> servers) {
		this.servers = servers;
	}



	public String getZkAddress() {
		return zkAddress;
	}



	public void setZkAddress(String zkAddress) {
		this.zkAddress = zkAddress;
	}



	public Logger getLogger() {
		return logger;
	}



	public void setLogger(Logger logger) {
		this.logger = logger;
	}



	public ServiceDiscovery<Instance> getServiceDiscovery() {
		return serviceDiscovery;
	}



	public void setServiceDiscovery(ServiceDiscovery<Instance> serviceDiscovery) {
		this.serviceDiscovery = serviceDiscovery;
	}



	public JsonInstanceSerializer<Instance> getSerializer() {
		return serializer;
	}



	public void setSerializer(JsonInstanceSerializer<Instance> serializer) {
		this.serializer = serializer;
	}



	public CuratorFramework getCuratorFramework() {
		return curatorFramework;
	}



	public void setCuratorFramework(CuratorFramework curatorFramework) {
		this.curatorFramework = curatorFramework;
	}



	public String getBASE_NAME() {
		return BASE_NAME;
	}

	public ServiceProvider<Instance> getServiceProvider() {
		return serviceProvider;
	}

	public void setServiceProvider(ServiceProvider<Instance> serviceProvider) {
		this.serviceProvider = serviceProvider;
	}

	public DistributedAtomicLong getCounter() {
		return counter;
	}

	public void setCounter(DistributedAtomicLong counter) {
		this.counter = counter;
	}

	
}
