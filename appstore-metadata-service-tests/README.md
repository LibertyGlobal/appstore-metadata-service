# AppStore Metadata Service Functional Tests

This module contains functional tests on system level integration.
These tests are created with black box approach and rely on functional specification.

BDD and DDT approaches are applied for scenarios design.

Build and execution from Maven level
---

First build and package the `appstore-metadata-service`.

When that is ready one can execute functional tests by running the following command:

```
mvn -pl appstore-metadata-service-tests verify -P tests-for-development
```

NOTE: This profile disables unit tests.

There are 3 test suites located in appropriate packages (functional, sanity, smoke),
 which get triggered in different combinations depending on env. variables:

 * when `BASE_URL=<host:port>` is set (e.g. on CI) then framework assumes execution towards master deployment and considers only:
    * functional test suite
    * smoke test suite
 * when `BASE_URL_PR=<host:port>` is set then it considers:  
    * functional test suite
    * sanity test suite

It is designed this way because of assuming using persistent DB on DEV environment for testing deployments from master branch 
and there might be teams using this environment for validation of their integration which we don't want to interfere with. 
However we still want to be able to evaluate the current state of the environment after each merge (smoke testing).
Note that smoke tests require some pre-loaded test data that can be found at the end of this readme.

On the other hand builds executed for PR's are run against deployments from feature branches where each instance has own temporary DB setup
so we can execute more extensive tests there (sanity testing).

Having that said, still most of new tests should be added within functional scenarios suite which are the fastest ones (most of functional testing).

Functional (local) and smoke tests require `db.schema=appstore_metadata_service` env. var to be set. While sanity tests might be run against parallel deployments
from different branches and each might set up own DB instance on same environment and namespace therefore they should provide unique schema name as well. 

Build and execution from IntelliJ level - Add Groovy framework support instead of dependency library
---

 1. Open context menu on ```src/test/groovy``` directory in test module and ```Mark as...``` -> ```Test Sources Root```.
 1. Go to project settings and remove groovy from test module dependencies.
 1. Open context menu on test module and select ```Add framework support...``` choose Groovy from Maven dependency.
 1. Make sure that Groovy is last on module dependencies list. 


# Test reports

Allure output (which is a reports input) is generated in ```target\allure-results``` directory.

To view them one needs Allure CLI tool

Allure installation on Mac OS
---
You can install Allure CLI via http://brew.sh/

```
$ brew tap qameta/allure
$ brew install allure
```

Allure installation on Windows
---

Download the latest version as zip archive from
https://repo.maven.apache.org/maven2/io/qameta/allure/allure-commandline/
Consider adding ```<allure-commandline>/bin``` directory to your system PATH var.

Reports generating and viewing
---

Reports need to be generated from test output and published as HTTP:

```
allure generate target/allure-results -o target/allure-report
allure open target/allure-report
```

e.g.:
```
c:\allure-cli-2.13.5\bin\allure generate target\allure-results -o target\allure-report
c:\allure-cli-2.13.5\bin\allure open target\allure-report
```

Running smoke test against deployment on DEV
---

One needs to provide env. var. with information about the deployment location. Otherwise smoke tests will be ignored.
```
BASE_URL=appstore-metadata-service.dac.appdev.io
```

#Some test data examples

This data existence is checked by smoke test suite so be sure to load it before running the integration tests.

`<host>:<port>` should specify the location of deployed service.

Sample data
---

* default maintainer

```
curl -X POST -H  "accept: application/json" -H  "x-maintainer-id: 1234abcd" -H  "Content-Type: application/json" -d '{
{
  "code": "lgi",
  "name": "Liberty Global",
  "address": "Liberty Global B.V., Boeing Avenue 53, 1119 PE Schiphol Rijk, The Netherlands",
  "homepage": "https://www.libertyglobal.com",
  "email": "developer@libertyglobal.com"
}' 'http://<host>:<port>/maintainers'
```

* Awesome Application / com.libertyglobal.app.awesome	
```
curl -v -X POST -H "Content-type: application/json" -d '{
  "header": {
    "icon": "default_app_collection.png",
    "name": "Awesome Application",
    "description": "This is Awesome App.",
    "type": "application/vnd.rdk-app.dac.native",
    "category": "application",
    "localisations": [
      {
        "name": "Geweldige applicatie",
        "description": "Dit is een geweldige applicatie.",
        "languageCode": "nld"
      }
    ],
    "id": "com.libertyglobal.app.awesome",
    "version": "1.2.3",
    "visible": true
  },
  "requirements": {
    "platform": {
      "architecture": "arm",
      "os": "linux"
    },
    "hardware": {
      "ram": "256",
      "dmips": "2000",
      "persistent": "20M",
      "cache": "50M"
    }
  }
}' 'http://<host>:<port>/maintainers/lgi/apps'
```

* you.i / com.libertyglobal.app.youi	
```
curl -v -X POST -H "Content-type: application/json" -d '{
  "header": {
    "icon": "default_app_collection.png",
    "name": "you.i1",
    "description": "Showcase application from the company youi.tv. The container package contains both the react native application and the You.i TV react native Gfx engine beneath.",
    "type": "application/vnd.rdk-app.dac.native",
    "category": "application",
    "localisations": [
      {
        "name": "Jij.ik",
        "description": "Showcase-applicatie van het bedrijf youi.tv. Het containerpakket bevat zowel de native-toepassing reageren als de You.i TV reageren native Gfx-engine eronder.",
        "languageCode": "nld"
      },
      {
        "name": "Ty.ja",
        "description": "Prezentacja aplikacji firmy youi.tv. Kontener zawiera zarówno natywną aplikację react, jak i znajdujący się poniżej natywny silnik Gfx.",
        "languageCode": "pol"
      }
    ],
    "id": "com.libertyglobal.app.youi",
    "version": "1.2.3",
    "visible": true
  },
  "requirements": {
    "platform": {
      "architecture": "arm", 
      "os": "linux"
    },
    "hardware": {
      "ram": "256",
      "dmips": "2000",
      "persistent": "20M",
      "cache": "50M"
    }
  }
}' 'http://<host>:<port>/maintainers/lgi/apps'
```
* flutter / com.libertyglobal.app.flutter	
```
curl -v -X POST -H "Content-type: application/json" -d '{
  "header": {
    "icon": "default_app_collection.png",
    "name": "flutter",
    "description": "Container contains both Flutter application and Flutter engine running on wayland-egl, developed by Liberty Global while evaluating Google Flutter UI toolkit.",
    "type": "application/vnd.rdk-app.dac.native",
    "category": "application",
    "localisations": [
      {
        "name": "Flutter",
        "description": "Container contains both Flutter application and Flutter engine running on wayland-egl, developed by Liberty Global while evaluating Google Flutter UI toolkit.",
        "languageCode": "eng"
      }
    ],
    "id": "com.libertyglobal.app.flutter",
    "version": "0.0.1",
    "visible": true
  },
  "requirements": {
    "platform": {
      "architecture": "arm",
      "os": "linux"
    },
    "hardware": {
      "ram": "256",
      "dmips": "2000",
      "persistent": "20M",
      "cache": "50M"
    }
  }
}' 'http://<host>:<port>/maintainers/lgi/apps'
```

API usage examples 
---

### Maintainers 

#### Retrieve created maintainer:  
```
curl -X GET "http://<host>:<port>/maintainers/lgi" -H  "accept: application/json" -H  "x-maintainer-id: 1234abcd"
```

```json
{
  "code": "lgi",
  "name": "Liberty Global",
  "address": "Liberty Global B.V., Boeing Avenue 53, 1119 PE Schiphol Rijk, The Netherlands",
  "homepage": "https://www.libertyglobal.com",
  "email": "developer@libertyglobal.com"
}
```

#### Update created maintainer:
```
curl -X PUT "http://<host>:<port>/maintainers/lgi" -H  "accept: */*" -H  "x-maintainer-id: 1234abcd" -H  "Content-Type: application/json" -d "{\"name\":\"Liberty Global\",\"address\":\"Liberty Global B.V., Boeing Avenue 53, 1119 PE Schiphol Rijk, The Netherlands\",\"homepage\":\"https://www.libertyglobal.com\",\"email\":\"developer@libertyglobal.com\"}"
```

#### Delete a created maintainer:
```
curl -X DELETE "http://<host>:<port>/maintainers/lgi" -H  "accept: application/json" -H  "x-maintainer-id: 1234abcd"
```