AppStore Metadata Service
---

In order to build the service you need to:

```
mvn clean package
```

Then to run it you need to:

```
java -jar target/appstore-metadata-service-0.0.1-SNAPSHOT.jar
```

The service is available by default at:
 
```
http://localhost:8080
```

Docker
---

In order to dockerize the service a Dockerfile is provided, which can be used to build a docker image:

```
docker build -t appstore-metadata-service .
```

After the image will be build as above, then it's ready to be started:

```
docker run -d -p 8080:8080 --name appstore-metadata-service appstore-metadata-service
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
