AppStore Metadata Service
---

Though, there several ways to run the source codes mentioned in the current repository,
this manual uses the docker-compose as the most simple approach.

Docker compose
---

On the root folder, execute
```
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
