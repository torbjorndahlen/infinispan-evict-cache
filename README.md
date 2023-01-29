# infinispan-evict-cache
Server Task that evicts and reloads an SQL cache store

## Postgresql DB

Deploy a Posgresql DB in the same Openshift cluster as DataGrid.
Create a table to be used as input to the DataGrid SQL cache store.

````
DB servername: postgreqsl
DB name: rpi-store
DB User: user
DB password: secret
`````
### Create table

````
oc get pods
oc rsh postgreqsl
$>  pqsl rpi-store

rpi-store=# create table model (
    id integer primary key,
    name varchar(20),
    model varchar(20),
    soc varchar(20),
    memory_mb integer,
    ethernet boolean,
    release_year integer
);

rpi-store=# insert into model 
(id, name, model, soc, memory_mb, ethernet, release_year) 
values (1, 'Raspberry Pi', 'B', 'BCM2835', 256, TRUE, 2012);

rpi-store=# grant all privileges on model to public; 

rpi-store=# \q


## Server

### Build the server task
```
mvn clean package
````

The server jar is located under server/target and needs to be added as a dependency artifact to the DataGrid Operator, for example by uploading to an HTTP server and providing the URL to the jar file to the Infinispan CR.

### Deploy the server task in Openshift using the DataGrid Operator

1. Create a ConfigMap containing an allow-list for serializing of the ServerTask class.
```
oc apply -f cluster-config.yaml
````
2. Configure the DataGrid Operator

Under Dependencies add Artifact Maven: org.postgresql:posgresql:42.3.1

Under Dependencies add Artifact URL: 

https://github.com/torbjorndahlen/infinispan-evict-cache/raw/main/ServerTask/target/ServerTask-1.0-SNAPSHOT.jar

Under Service Type, select DataGrid

Under Expose select LoadBalancer (to allow unencrypted Hotrod clients)

Under Security: Select EndPoint Encryption: None

Under Config Map Name: <name of your config map for allowed Java serialization classes> e.g cluster-config

The username and password for the Admin Console is located under Secrets in infinispan-generated-secret

Get the LoadBalancer external hostname with ```oc get svc```.
Access the DataGrid console with http://<loadbalancer>:11222

3. Use the DataGrid console to create a cache named "rpi-store"

4. Use sql-cache-store-config.json to configure the cache to use the Postgrsql DB as SQL cache store

## Client

### Build the client

```
mvn clean package
mvn assembly:assembly -DdescriptorId=jar-with-dependencies
````

### Run the client
```
java -cp target/ServerTaskClient-jar-with-dependencies.jar \
> example.CacheServerTaskInvocation \
> <infinispan-external-loadbalancer-hostname:port> \
> <infinispan-username> <password> <cache-name>
````
Note: use ```oc get svc``` to find the LoadBalancer hostname.


