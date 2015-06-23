# servkeeper
## Microservice Curator Server

Can you imagine how difficult it must be to distribute micro efficiently services with elastic scalability, continuous delivery and active monitoring?

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

## Using SerKeeper:

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

dockerHost: "https://192.168.59.103:2376"
dockerCertPath: "/Users/cleutonsampaio/.boot2docker/certs/boot2docker-vm"
zkHost: "localhost:2181"
path:  "/Users/cleutonsampaio/Documents/projetos/dockertest"
imageName: "signatureimage"
containerBaseName: "signatureserver"
sourcePortNumber: 3000
startServerInstances: 2
minServerInstances: 1
maxServerInstances: 5
maxRequestLimit: 10
minRequestLimit: 5
serviceTestPah: "/signature/checkstatus"
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
    
server:
  applicationConnectors:
  - type: http
    port: 3000
  adminConnectors:
  - type: http
    port: 3300

