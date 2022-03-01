AppStore Metadata Service
---

Though, there several ways to run the source codes mentioned in the current repository,
this manual uses the docker-compose as the most simple approach.

Docker compose
---

In order to run the docker-compose stack please execute:
```
docker-compose -f docker-compose/docker-compose.yml build
docker-compose -f docker-compose/docker-compose.yml up
```

Now the relevant services should be available - which can be verified by doing:

```
docker ps
```

Running application for development purposes
---

In order to run application for development purposes, set following environment variables:
```
BUNDLES_STORAGE_HOST = localhost
BUNDLES_STORAGE_PROTOCOL = http
spring.profiles.active = dev
```

Package repository:
```
mvn clean package
```

Next, run DB docker image:
```
docker run -d --name ASMS -p 5432:5432 -e POSTGRES_HOST_AUTH_METHOD=trust postgres:12.5
```

Finally, run src/main/java/com/lgi/appstore/metadata/Application.java

Pushing 'jar' files and 'docker images' to remote repository
---

There is also possibility to push 'jar' files and 'docker images' to remote repository using command below. All credentials should be set in Maven settings file.
```
mvn deploy -DskipITs=true -Djib.to.tags=<yourCommitHash> -Dregistry.url=<yourImageRegistryUrl> -Dregistry.namespace=<yourImageRegistryNamespace> -Djar.repository.url=<yourJarRegistryUrl> -Djar.repository.name=<yourJarRegistryName> -Djar.repository.id=<yourJarRegistryId>
```
