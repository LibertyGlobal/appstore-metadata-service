AppStore Metadata Service
---

In order to build the service you need to run:

```
mvn -pl tcp-msg-maven-plugin clean install plugin:descriptor
```

Then execute to package it:

```
mvn package
```

Then to run it you need to be in `target` directory:

```
cd target
java -jar appstore-metadata-service-0.0.1-SNAPSHOT.jar
```

The service is available by default at:
 
```
http://localhost:8080
```

Docker
---

In order to dockerize the service a plugin jib-maven-plugin is provided, which can be used to build a docker image:

```
mvn compile jib:dockerBuild -Djib.to.image=appstore-metadata-service:0.0.1-SNAPSHOT
```

Because service needs a database to connect, next step is to create a docker network and run database inside it: 

```
docker network create asms_network
docker run -e POSTGRES_HOST_AUTH_METHOD=trust --name asmsdb --network asms_network postgres:11
```

At the end, application can be run:

```
docker run -d -p 8080:8080 -e JDBC_HOST=asmsdb --network=asms_network --name appstore-metadata-service appstore-metadata-service:0.0.1-SNAPSHOT
```

Docker compose
---

```
mvn clean install
docker-compose -f docker-compose/docker-compose.yml build
docker-compose -f docker-compose/docker-compose.yml up
```

Now the relevant services should be available - which can be verified by doing:

```
docker ps
```

Pushing 'jar' files and 'docker images' to remote repository
---

There is also possibility to push 'jar' files and 'docker images' to remote repository using command below. All credentials should be set in Maven settings file.
```
mvn deploy -DskipITs=true -Djib.to.tags=<yourCommitHash> -Dregistry.url=<yourImageRegistryUrl> -Dregistry.namespace=<yourImageRegistryNamespace> -Djar.repository.url=<yourJarRegistryUrl> -Djar.repository.name=<yourJarRegistryName> -Djar.repository.id=<yourJarRegistryId>
```
