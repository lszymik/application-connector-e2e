# Application Connector end to end test tool

## Overview

Project application-connector-e2e is a testing tool that aggregates few features needed to perform end to end testing of Kyma's Application Connnector module.

## Usage

Before starting tests, the following environmental variables have to be exported:

- `DOMAIN` - Kyma cluster domain ; on minikube it will be `kyma.local`, on a cluster it will be of a form: `framefrog.cluster.kyma.cx`
- `KUBECONFIG` - (optional) path to Kubeconfig file. If left empty, K8SClient will create configuration based on default Kubeconfig.

In case of local Kyma development, Kyma server certificate has to be added to the Java Key Store. To do this, run:
`sudo {JAVA_HOME}/bin/keytool -import -alias “Kyma” -keystore {JAVA_HOME}/jre/lib/security/cacerts -file <KYMA_HOME>/installation/certs/workspace/raw/server.crt`

You will also have to add `counter-service.kyma.local` to `/etc/hosts` and map it to minikube ip.

After exporting variables, run `./gradlew clean test` in the project's main catalogue to build project and run tests, or `./gradlew clean test --info` for additional logs.

