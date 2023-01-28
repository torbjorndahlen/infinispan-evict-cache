# infinispan-evict-cache
Server Task that evicts and reloads an SQL cache store

## Server

### Build the server
mvn clean package

The server jar is located under server/target and needs to be added as a dependency artifact to the DataGrid Operator, for example by uploading to an HTTP server and providing the URL to the jar file.

## Client

### Build the client

```
mvn clean package
mvn assembly:assembly -DdescriptorId=jar-with-dependencies
````

### Run the client
```
java -cp target/ServerTaskClient-jar-with-dependencies.jar <infinispan-server-hostname:port> <infinispan-username> <password>


