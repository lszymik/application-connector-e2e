# Application Connector end to end test tool

## Overview

Project application-connector-e2e is a testing tool that aggregates few features needed to perform end to end testing of Kyma's Application Connnector module:

- KymaConnector - based on kymaconnector project, creates certificates and private key required to communicate with Applications on Kyma cluster, basing on one time integration token fetched from Kyma's application connector.
- K8SClient - based on Kubernetes Custom Objects API, allows connection to Kyma cluster and creates/deletes Applications.
- KeyStoreInitializer - reads PEM files created by KymaConnector and creates new Java Key Store. This allows to establish a secure connection with Kyma's Metadata Service.
- MetadataClient - provides REST methods for services associated with Applications.
- Spock testing framework

## Usage

Before starting tests, the following environmental variables has to be exported:

- `SAVEPATH` - it is a path where files (certificates, key, keystore) will be stored.
- `KYMAHOST` - Kyma DNS, for minikube it would be `https://gateway.kyma.local:{NODE_PORT}`, for cluster `https://gateway.{CLUSTER_DOMAIN}`
- `KUBECONFIG` - (optional) path to Kubeconfig file. If left empty, K8SClient will create configuration based on default Kubeconfig.

In case of local Kyma development, Kyma server certificate has to be added to the Java Key Store. To do this, run:
`sudo {JAVA_HOME}/bin/keytool -import -alias “Kyma” -keystore {JAVA_HOME}/jre/lib/security/cacerts -file <KYMA_HOME>/installation/certs/workspace/raw/server.crt`

After exporting variables, run `./gradlew clean test` in the project's main catalogue to build project and run tests, or `./gradlew clean test --info` for additional logs.

## Development

At this stage, all test are run on default Application (named `test-app-e2e`) that is automatically created. In case of need for another App, there is builder class available in `Application.groovy` file.
