# AppStore Metadata Service Functional Tests

This module contains functional tests on system level integration.
These tests are created with black box approach and rely on functional specification.

BDD and DDT approaches are applied for scenarios design.

Build and execution from Maven level
---

Local build and execution requires running the following command
from inside of the module: ```appstore-metadata-service-tests```


```
mvn clean verify -P tests-for-development
```

NOTE: This profile disables unit tests.

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