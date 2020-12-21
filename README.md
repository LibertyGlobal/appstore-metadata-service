# Appstore Metadata Service

Appstore Metadata Service is a group of components purposed to serve the needs of RDK to store and provide the Applications metadata.

## Perspectives

The primary component Appstore Metadata Service (ASMS) provides its functionality in a form of REST API.
API is split into 2 perspectives:
* STB
    It provides the methods that make it possible to search applications using different criteria, and fetch information about a particular application.
    This perspective provides read-only access to the information hosted by the ASMS.
* Maintainer
    Methods exposed to the application developers. Using this set of methods an application developer can manage metadata of the applications. Maintainers are grouped into Companies, they can manage applications that belong to their companies. Thus, they cannot anyhow modify information that is not in their area of responsibility.

## Components

Functions provided by ASMS components are split between:
* ASMS.

    The central component of the ASMS architecture
* ASMS DB.

    The database used to store applications' metadata
* ASMS Proxy.

    Proxy in front of ASMS provided in a form of the Nginx server. It limits the set of API methods exposed to the STB devices. Exposed methods could be used only for fetching data. This component should be providing unique authentication/authorization capabilities in future versions of the ASMS initiative.
* AS3 Proxy.

    One more Nginx proxy in front of ASMS. It exposes ASMS methods used to manage the applications' metadata. Methods provide extended capabilities related to the create/update/delete methods. Also, it takes of authorization of different maintainers to their applications.

## Packaging

Current artifacts can be deployed in a form of docker-compose [stack](./appstore-metadata-service/docker-compose).

## REST API

API is specified in a form of [yaml swagger file](./appstore-metadata-service/src/main/resources/static/appstore-metadata-service.yaml) according to the principles of OPEN API 3.0

## Request samples.


### Create application

Create application in ASMS using AS3 API:

```http

POST http://as3.server:8080/lgi/apps
Authorization: Basic YXMzOmFzMw==
Content-Type: application/json
Accept-Language: fr-CH, fr;q=0.9

{
    "specVersion": 1,
    "application": {
        "header": {
            "id": "demo.id.appl",
            "version": "2.2",
            "icon": "http://pretty.url/icon3.png",
            "name": "FancyApp",
            "description": "Description of Fancy application",
            "url": "http://url/fancyappl",
            "visible": true,
            "latest" : true,
            "type": "fancy_applications",
            "category": "application",
            "localisations": [
                {
                    "languageCode": "en",
                    "name": "Fancy application",
                    "description": "description"
                }
            ]
        },
        "requirements": {
            "dependencies": [
                {
                    "id": "1.2.3",
                    "version": "string"
                }
            ],
            "platform": {
                "architecture": "arm",
                "variant": "v1",
                "os": "linux"
            },
            "hardware": {
                "ram": "string",
                "dmips": "string",
                "image": "string",
                "persistent": "string",
                "cache": "string"
            },
            "features": [
                {
                    "name": "string",
                    "version": "string",
                    "required": true
                }
            ]
        },
        "maintainer": {
            "name": "Liberty Global",
            "address": "address",
            "homepage": "http://homepage",
            "email": "email@mail.org"
        }
    }
}

```

Response:

```http
HTTP/1.1 201 Created
Date: Sat, 02 Apr 2016 12:22:40 GMT
```


### Search application

A sample of request that is used by STBs to the search an application with the name starting 'Fancy' and provided by the 'Liberty Global' maintainer.

Request:

```http
GET  http://asms.server:8082/maintainers/apps?name=Fancy&maintainerName=Liberty%20Global
Authorization: Basic c3RiOnN0Yg==

```

Response:

```http
{
  "applications": [
    {
      "icon": "http://pretty.url/icon3.png",
      "name": "FancyApp",
      "description": "Description of Fancy application",
      "url": "http://url/fancyappl",
      "type": "application/vnd.rdk-app.dac.native",
      "category": "application",
      "localisations": [
        {
            "languageCode": "en",
            "name": "Fancy application",
            "description": "description"
        }
      ],
      "id": "demo.id.appl",
      "version": "1.2.3"
    }
  ],
  "meta": {
    "resultSet": {
      "count": 1,
      "offset": 0,
      "limit": 10,
      "total": 1
    }
  }
}
```
