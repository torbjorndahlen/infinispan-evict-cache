# infinispan-evict-cache
Server Task that evicts and reloads an SQL cache store

## PostgreSQL

Deploy Posgresql in the same Openshift namespace as Data Grid.
Create a table to be used as input to the Data Grid SQL cache store.

```
$ oc new-project infinispan-demo
$ oc new-build \ 
> https://github.com/torbjorndahlen/infinispan-evict-cache \
> --strategy=docker \
> --name='postgresql-12-custom'

$ oc new-app \
> -e POSTGRESQL_USER=db \
> -e POSTGRESQL_PASSWORD=secret \
> -e POSTGRESQL_DATABASE=rpi-store \
> postgresql-12-custom:latest
```

After deployment is complete verify that the user infinispan and the DB rpi-store and the model table has been created as expected:

```
$ oc get pods
NAME                                    READY	STATUS		RESTARTS	AGE
postgresql-12-custom-1-build           	0/1	Completed   	0          	2m40s
postgresql-12-custom-54cb5fbd6-55rkf   	1/1	Running     	0          	38s

$ oc exec postgresql-12-custom-54cb5fbd6-55rkf -- psql -U infinispan -d rpi-store -c "select * from model;"
 id |       name        | model |   soc   | memory_mb | ethernet | release_year 
----+-------------------+-------+---------+-----------+----------+--------------
  1 | Raspberry Pi      | B     | BCM2835 |       256 | t        |         2012
  2 | Raspberry Pi Zero | Zero  | BCM2835 |       512 | f        |         2015
  3 | Raspberry Pi Zero | 2W    | BCM2835 |       512 | f        |         2021
(3 rows)

```

## Server Task

Build the server task:
```
mvn clean package
```

The server jar is located under ```server/target``` and needs to be added as a dependency artifact to the DataGrid Operator, for example by uploading to an HTTP server and providing the URL to the jar file to the Infinispan CR.

## Data Grid

Deploy Data Grid in Openshift using the DataGrid Operator

1. Create a ConfigMap containing an allow-list for serializing of the ServerTask class.
```
oc apply -f cluster-config.yaml
```

2. Install the DataGrid Operator

```
$ oc apply -f infinispan-operator.yaml
$ oc apply -f subscription.yaml
```

3. Create the DataGrid Server

```
$ oc create -f infinispan-cr.yaml
```

## Cache

Find the URL to the Data Grid admin console:
```
$ oc get svc
NAME                         TYPE          CLUSTER-IP   EXTERNAL-IP         PORT(S)           AGE
example-infinispan-external  LoadBalancer  172.30.5.74  my_host.example.com 11222:31797/TCP   6m39s
```

In this example, we exposed Data Grid through a loadbalancer. The URL to the Data Grid contains the loadbalancer hostname and port. In this example the Data Grid console can be accessed at http://my_host.example.com:11222. 

The console username and password is stored in the infinispan-generated-secret:

```
$ oc get secret infinispan-generated-secret -o yaml

apiVersion: v1
data:
  identities.yaml: Y3JlZGVudGlhbHM6Ci0gdXNlcm5hbWU6IGRldmVsb3BlcgogIHBhc3N3b3JkOiBTRllsbWJYZWJhUllxWWtwCiAgcm9sZXM6CiAgLSBhZG1pbgo=

... output omitted

$ echo Y3JlZGVudGlhbHM6Ci0gdXNlcm5hbWU6IGRldmVsb3BlcgogIHBhc3N3b3JkOiBTRllsbWJYZWJhUllxWWtwCiAgcm9sZXM6CiAgLSBhZG1pbgo= | base64 -d

credentials:
- username: developer
  password: <password>
  roles:
  - admin
```

Using the Data Grid console, create a cache using a protobuf schema and SQL cache store configuration.

1. Create a Protobuf schema using the ```example.proto``` file

2. Use the DataGrid console to create a cache named "rpi-store"

3. Use ```sql-cache-store-config.json``` to configure the cache to use the Postgrsql DB as SQL cache store


### Protobuf schema
```
package example;

message model_key {
   required int32 id = 1;
}

message model_value {
   required string name = 1;
   optional string model = 2;
   optional string soc = 3;
   optional int32 memory_mb = 4;
   optional bool ethernet = 5;
   optional int32 release_year = 6;
}
```

### Cache definition
```
{
   "distributed-cache": {
     "mode": "SYNC",
     "encoding": {
       "key": {
         "media-type": "application/x-protostream"
       },
       "value": {          
         "media-type": "application/x-protostream"        
       }
     },
     "persistence": {
       "table-jdbc-store": {
         "shared": true,
         "segmented": false,
         "dialect": "POSTGRES",
         "table-name": "model",
         "schema": {
           "message-name": "model_value",
           "package": "example"
         },
         "connection-pool": {            
           "connection-url": "jdbc:postgresql://postgresql-12-custom:5432/rpi-store",            
           "driver": "org.postgresql.Driver",            
           "username": "infinispan",            
           "password": "secret"          
         }
       }
     }
   }
 }
```

## Make an update to a row in the ```model``` table:

```
oc exec postgresql-12-custom-54cb5fbd6-55rkf -- psql -U postgres -d rpi-store -c "update model set name = 'Raspberry Pi One' where id = 1;"
```

## Client

### Build the client:

```
mvn clean package
mvn assembly:assembly -DdescriptorId=jar-with-dependencies
```

### Run the client
```
java -cp target/ServerTaskClient-jar-with-dependencies.jar \
> example.CacheServerTaskInvocation \
> <infinispan-external-loadbalancer-hostname:port> \
> <infinispan-username> <password> <cache-name>
```
Note: use ```oc get svc``` to find the LoadBalancer hostname and ```oc get secret infinispan-generated-secret -o yaml``` to get the username and password.


