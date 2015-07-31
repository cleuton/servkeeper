# servkeeper
## Microservice Curator Server

Can you imagine how difficult it must be to efficiently deploy micro services with elastic scalability, continuous delivery and active monitoring?

* How will you scale up, down?
* How will you manage multiple instances?

ServKeeper is the answer to your problem!

Using the champion formula:
- Apache ZooKeeper for coordinating distributed services;
- Docker, to climb LXC containers on demand;
- Apache Curator, to facilitate the use of ZooKeeper;
- Jenkins, to deploy and verify instances.

## What servkeeper do?

Well, I think the picture explains better: 

![Image of architecture](https://lh3.googleusercontent.com/-a7NHCDkkrGk/VYk6V38hNqI/AAAAAAAAGLw/Z6cmKiY0Iow/s800/arquitetura.png)

ServKeeper controls and coordinates micro services instances. 

You configure ServKeeper to "watch" some micro service. It then takes care of scalling up (creating more instances, when needed), scalling down (remove some instances when needed), and check every instance.

It uses Zookeeper (via Apache Curator) to register instances. When a User needs an instance, he asks Zookeper (via Apache Curator) and get one address (it uses Round robin). 

ServKeeper runs the instances on a Docker environment. 

## So many projects... 

Ok, let me explain that: 
- servkeeper : The Micro services curator REST server;
- ServiceClient : The API for micro services. They use it to increment the shared request counter;
- signature : A micro service sample.

## Using ServKeeper:

Build and run the server. It has some REST routes: 
- ../servkeeper/requests : Show the total requests received by all services instances;
- ../servkeper/stopall : Stop all services instances, and removes them from Docker and Zookeeper;
- ../servkeeper/stopserver : Stop the ServKeeper REST server;
- ../servkeeper/supervise : Run a supervisation over all micro services instances. Deletes "trashed" instances, verifies the shared request counter, and scales up or down the number of instances. This must be invoked periodically;
- ../servkeeper/getinstance : Return one of the instances, by making a request to Zookeeper. You don't need to use it from the server, and you can query zookeeper instead. It is just a convenience method;
- ../servkeeper/setcounter?value=<value> : Set the shared requests counter to the provided value. Default is zero;
- ../servkeeper/instancescount : Return the number of micro services instances, in zookeeper and in docker. They may be different if the server is scalling down;
- ../servkeeper/start : Starts the server, booting all minimum micro services instances and reseting the shared request counter;

## Configuring ServKeeper

ServKeeper is a Dropwizard Server, so, we have a "servkeeper.yml" file, that controls it. When starting ServKeeper, the command line is: 

`java -jar <servkeeper.shaded.jar> server <path to servkeeper.yml file>`

The YML file have this options: 
```
# Docker host. If using boot2docker, this is the vm address:
dockerHost: "https://192.168.59.103:2376"
# Docker certificate path, to logon on docker:
dockerCertPath: "/Users/cleutonsampaio/.boot2docker/certs/boot2docker-vm"
# Zookeper host address and port:
zkHost: "localhost:2181"
# Path to where Jenkins deploy the compiled jars. This is the "appfolder", where Jenkins deploy the artifacts:
path:  "/Users/cleutonsampaio/Documents/projetos/dockertest"
# Docker image name. There must be a Dockerfile in the path, and this will be the generated image name:
imageName: "signatureimage"
# Docker container name. This is the service name, used to search for instances on Zookeeper:
containerBaseName: "signatureserver"
# Micro service source port. Must be exposed in the Docker file and will be mapped to a host port:
sourcePortNumber: 3000
# Minimum micro service instances when starting ServKeeper. They will be launched on startL
startServerInstances: 2
# Minimum micro service instances that must exist:
minServerInstances: 1
# Maximm server instances to scale up:
maxServerInstances: 5
# Maximum request limit before scalling up:
maxRequestLimit: 10
# Minimum request limit before scalling down:
minRequestLimit: 5
# Micro service test REST route:
serviceTestPah: "/signature/checkstatus"
# Array of micro service possible addresses and ports:
serverAddresses: 
  - host: "192.168.59.103"
    port: 3000
  - host: "192.168.59.103"
    port: 3001
  - host: "192.168.59.103"
    port: 3002
  - host: "192.168.59.103"
    port: 3003
  - host: "192.168.59.103"
    port: 3004
  - host: "192.168.59.103"
    port: 3005
# ServKeeper configuration:    
server:
  applicationConnectors:
  - type: http
    port: 3000
  adminConnectors:
  - type: http
    port: 3300
```

## Jenkins Jobs:

Each project has a jenkins xml job file, at the root path. You can import this jobs to a Jenkins server using the Jenkins CLI command: java -jar jenkins-cli.jar -s http://localhost:8080/ create-job NAME (reads a xml from stin).

The Jobs are: 
- ServiceClient/serviceclient_build_install.xml : Build ServiceClient and install it on the Jenkins .m2 folder;
- servkeeper/servkeeper_build.xml : Build ServKeeper and deploy it to the app folder.
- servkeeper/servkeeper_run_and_start.xml : Run ServKeeper process and, after a while, send a "start" request;
- servkeeper/servkeeper_stop_and_destroy.xml : Stop all micro services instances and stop ServKeeper process;
- servkeeper/servkeeper_supervise.xml : Send a supervise request to ServKeeper. It must be schedulled;
- signature/signaturejob.xml : Build and deploy the sample micro service to the app folder;

## The micro service:

You can use SerKeeper to take care of any micro service, written on any language. I am using Java with Dropwizard. The ServiceClient project creates a JAR file with a class. This class can be used even in Node.js (using node-java).

If you want, you can use Apache Curator RPC Proxy, which is generated by Apache Thrift, and create a proxy for any language. 

