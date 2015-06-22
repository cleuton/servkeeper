/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.obomprogramador.microservice.servkeeper.wrappers;

import java.io.Closeable;
import java.io.IOException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import com.obomprogramador.microservice.servkeeper.model.Instance;

/**
 * This class represents a server instance.
 * @author cleutonsampaio
 *
 */
public class Server implements Closeable, Comparable<Server>
{
    private  ServiceDiscovery<Instance> serviceDiscovery;
    private  ServiceInstance<Instance> thisInstance;
    private  String basePath;
    private String instanceId;
    
    

    public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public ServiceDiscovery<Instance> getServiceDiscovery() {
		return serviceDiscovery;
	}

	public String getBasePath() {
		return basePath;
	}

	@Override
	public int hashCode() {
		return this.instanceId.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		Server other = (Server) obj;
		return this.instanceId.equalsIgnoreCase(other.instanceId);
	}

	public Server() {
		super();
	}
	
	public Server(CuratorFramework client, Instance instance, String basePath) throws Exception
    {
		this();
        UriSpec     uriSpec = new UriSpec("{scheme}://"+ instance.getIpAddress() + ":{port}");
        this.basePath = basePath;
        this.thisInstance = ServiceInstance.<Instance>builder()
            .name(instance.getServiceName())
            .payload(instance)
            .port(instance.getPort()) 
            .uriSpec(uriSpec)
            .build();

        JsonInstanceSerializer<Instance> serializer = new JsonInstanceSerializer<Instance>(Instance.class);

        this.serviceDiscovery = ServiceDiscoveryBuilder.builder(Instance.class)
            .client(client)
            .basePath(this.basePath)
            .serializer(serializer)
            .thisInstance(thisInstance)
            .build();
    }

    public ServiceInstance<Instance> getThisInstance()
    {
        return thisInstance;
    }

    public void start() throws Exception
    {
        this.serviceDiscovery.start();
    }

    @Override
    public void close() throws IOException
    {
        CloseableUtils.closeQuietly(serviceDiscovery);
    }

	@Override
	public int compareTo(Server o) {
		return this.instanceId.compareToIgnoreCase(o.instanceId);
	}
}