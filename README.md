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


