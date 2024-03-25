# btm [![CI](https://github.com/JeffersonLab/btm/actions/workflows/ci.yml/badge.svg)](https://github.com/JeffersonLab/btm/actions/workflows/ci.yml) [![Docker](https://img.shields.io/docker/v/jeffersonlab/btm?sort=semver&label=DockerHub)](https://hub.docker.com/r/jeffersonlab/btm)
A [Java EE 8](https://en.wikipedia.org/wiki/Jakarta_EE) web application for managing beam time at Jefferson Lab built with the [Smoothness](https://github.com/JeffersonLab/smoothness) web template.

![Screenshot](https://github.com/JeffersonLab/btm/raw/main/Screenshot.png?raw=true "Screenshot")

---
- [Overview](https://github.com/JeffersonLab/btm#overview)
- [Quick Start with Compose](https://github.com/JeffersonLab/btm#quick-start-with-compose)
- [Install](https://github.com/JeffersonLab/btm#install)
- [Configure](https://github.com/JeffersonLab/btm#configure)
- [Build](https://github.com/JeffersonLab/btm#build)
- [Develop](https://github.com/JeffersonLab/btm#develop) 
- [Release](https://github.com/JeffersonLab/btm#release)
- [Deploy](https://github.com/JeffersonLab/btm#deploy)
- [See Also](https://github.com/JeffersonLab/btm#see-also)  
---

## Overview
The Beam Time Manager application allows crew chiefs and experimenters to track machine time accounting while allowing management to review reports.  The app prompts crew chiefs and experimenters with shift timesheets pre-filled with data estimated via beam position sensor measurements.  The app also holds a database view of the PAC schedule, exposes programmatic access via a JSON REST service, and integrates with Program Deputy [shift plans](https://github.com/JeffersonLab/pd-shiftplans) and [DTM](https://github.com/JeffersonLab/dtm) to enable prompting timekeepers with the expected plan as well.  The timesheets therefore consist of Planned, Measured, and Reported values.

## Quick Start with Compose
1. Grab project
```
git clone https://github.com/JeffersonLab/btm
cd btm
```
2. Launch [Compose](https://github.com/docker/compose)
```
docker compose up
```
3. Navigate to page
```
http://localhost:8080/btm
```

**Note**: Login with demo username "tbrown" and password "password".

See: [Docker Compose Strategy](https://gist.github.com/slominskir/a7da801e8259f5974c978f9c3091d52c)

## Install
This application requires a Java 11+ JVM and standard library to run, plus a Java EE 8+ application server (developed with Wildfly).


1. Install service [dependencies](https://github.com/JeffersonLab/btm/blob/main/deps.yml)
2. Download [Wildfly 26.1.3](https://www.wildfly.org/downloads/)
3. [Configure](https://github.com/JeffersonLab/btm#configure) Wildfly and start it
4. Download [btm.war](https://github.com/JeffersonLab/srm/releases) and deploy it to Wildfly
5. Navigate your web browser to [localhost:8080/btm](http://localhost:8080/btm)

## Configure

### Configtime
Wildfly must be pre-configured before the first deployment of the app. The [wildfly bash scripts](https://github.com/JeffersonLab/wildfly#configure) can be used to accomplish this. See the [Dockerfile](https://github.com/JeffersonLab/btm/blob/main/Dockerfile) for an example.

**Note**: In production users often leave multiple instances of the app open per counting house and concurrent hourly refreshes may result in concurrent DB request contention for the BTM connection pool.   Bumping up the [max allowed concurrent](https://github.com/JeffersonLab/wildfly/blob/92972bd45dc363c2ed4959bb725e0ba3ba052a6b/scripts/app-setup.sh#L99) DB connections can ameliorate this.

### Runtime
Uses the [Smoothness Environment Variables](https://github.com/JeffersonLab/smoothness#environment-variables) plus the following application specific:

| Name                | Description                                                    |
|---------------------|----------------------------------------------------------------|
| LOGBOOK_DEBUG       | If set to "true" then TLOG is used as logbook                  |
| BTM_EPICS_ADDR_LIST | EPICS CA Address List for obtaining time keeping measurements. |

## Build
This project is built with [Java 17](https://adoptium.net/) (compiled to Java 11 bytecode), and uses the [Gradle 7](https://gradle.org/) build tool to automatically download dependencies and build the project from source:

```
git clone https://github.com/JeffersonLab/btm
cd btm
gradlew build
```
**Note**: If you do not already have Gradle installed, it will be installed automatically by the wrapper script included in the source

**Note for JLab On-Site Users**: Jefferson Lab has an intercepting [proxy](https://gist.github.com/slominskir/92c25a033db93a90184a5994e71d0b78)

See: [Docker Development Quick Reference](https://gist.github.com/slominskir/a7da801e8259f5974c978f9c3091d52c#development-quick-reference)

## Develop
In order to iterate rapidly when making changes it's often useful to run the app directly on the local workstation, perhaps leveraging an IDE.  In this scenario run the service dependencies with:
```
docker compose -f deps.yml up
```
**Note**: The local install of Wildfly should be [configured](https://github.com/JeffersonLab/btm#configure) to proxy connections to services via localhost and therefore the environment variables should contain:
```
KEYCLOAK_BACKEND_SERVER_URL=http://localhost:8081
FRONTEND_SERVER_URL=https://localhost:8443
BTM_EPICS_ADDR_LIST=localhost
```
Further, the local DataSource must also leverage localhost port forwarding so the `standalone.xml` connection-url field should be: `jdbc:oracle:thin:@//localhost:1521/xepdb1`.  

The [server](https://github.com/JeffersonLab/wildfly/blob/main/scripts/server-setup.sh) and [app](https://github.com/JeffersonLab/wildfly/blob/main/scripts/app-setup.sh) setup scripts can be used to setup a local instance of Wildfly. 

## Release
1. Bump the version number in the VERSION file and commit and push to GitHub (using [Semantic Versioning](https://semver.org/)).
2. The [CD](https://github.com/JeffersonLab/btm/blob/main/.github/workflows/cd.yml) GitHub Action should run automatically invoking:
    - The [Create release](https://github.com/JeffersonLab/java-workflows/blob/main/.github/workflows/release.yml) GitHub Action to tag the source and create release notes summarizing any pull requests.   Edit the release notes to add any missing details.  A war file artifact is attached to the release.
    - The [Publish docker image](https://github.com/JeffersonLab/container-workflows/blob/main/.github/workflows/docker-publish.yml) GitHub Action to create a new demo Docker image, and bump the [compose.override.yaml](https://github.com/JeffersonLab/btm/blob/main/compose.override.yaml) to use the new image.

## Deploy
At JLab this app is found at [ace.jlab.org/btm](https://ace.jlab.org/btm) and internally at [acctest.acc.jlab.org/btm](https://acctest.acc.jlab.org/btm).  However, those servers are proxies for `wildfly5.acc.jlab.org` and `wildflytest5.acc.jlab.org` respectively.   A [deploy script](https://github.com/JeffersonLab/wildfly/blob/main/scripts/deploy.sh) is provided to automate wget and deploy.  Example:

```
/root/setup/deploy.sh btm v1.2.3
```

**JLab Internal Docs**:  [InstallGuideWildflyRHEL9](https://accwiki.acc.jlab.org/do/view/SysAdmin/InstallGuideWildflyRHEL9)

## See Also
 - [JLab ACE management-app list](https://github.com/search?q=org%3Ajeffersonlab+topic%3Aace+topic%3Amanagement-app&type=repositories)
