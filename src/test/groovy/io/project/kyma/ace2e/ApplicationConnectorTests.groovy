package io.project.kyma.ace2e

import io.project.kyma.ace2e.model.k8s.Application
import io.project.kyma.ace2e.model.k8s.Metadata
import io.project.kyma.ace2e.model.k8s.Spec
import io.project.kyma.ace2e.utils.CertificateManager
import io.project.kyma.ace2e.utils.EnvironmentConfig
import io.project.kyma.ace2e.utils.K8SClient
import io.project.kyma.ace2e.utils.APIRegistryClient
import spock.lang.Shared
import spock.lang.Specification

import static java.util.concurrent.TimeUnit.SECONDS
import static org.apache.http.HttpStatus.SC_OK
import static org.awaitility.Awaitility.await

class ApplicationConnectorTests extends Specification {

	@Shared
	APIRegistryClient apiRegistryClient
	@Shared
	K8SClient k8SClient = new K8SClient(EnvironmentConfig.kubeConfig)

	@Shared
	def keystorePass = ""
	@Shared
	String namespace = "kyma-integration"
	@Shared
	String application = "test-app-e2e"

	def setupSpec() {
		createApplicationCRD()
		setupClientCertificateInKeyStore()
		setupAPIRegistryClient()
	}

	def "should create service"() {
		given:
		def service = [
				provider   : "SAP",
				name       : "test-service",
				description: "httpbin.org",
				api        : [
						targetUrl: "https://httpbin.org"
				]
		]
		when:
		def postResp = apiRegistryClient.createService(application, service)

		then:
		postResp.status == SC_OK

		when:
		String id = postResp.data.id
		def resp = apiRegistryClient.getService(application, id)

		then:
		resp.status == SC_OK
		resp.data.provider == "SAP"
		resp.data.name == "test-service"
		resp.data.description == "httpbin.org"
		resp.data.api.targetUrl == "https://httpbin.org"
	}

	def cleanupSpec() {
		k8SClient.deleteApplication(application, namespace)
		k8SClient.deleteTokenRequest(application, namespace)
	}

	private def createApplicationCRD() {
		k8SClient.createApplication(newTestApp(), namespace)
		await().atMost(20, SECONDS)
				.pollDelay(2, SECONDS)
				.pollInterval(5, SECONDS)
				.until {
			k8SClient.getApplication(application, namespace) != null
		}
	}

	private def setupClientCertificateInKeyStore() {
		new CertificateManager(k8SClient: k8SClient, application: application, namespace: namespace, keyStorePassword: keystorePass)
				.setupCertificateInKeyStore()
	}

	private def setupAPIRegistryClient() {
		apiRegistryClient = new APIRegistryClient(EnvironmentConfig.host, EnvironmentConfig.jksStorePath, keystorePass)
		await().atMost(30, SECONDS)
				.pollDelay(2, SECONDS)
				.pollInterval(5, SECONDS)
				.until {
			try {
				apiRegistryClient.getServices(application).status == SC_OK
			}
			catch (e) {
				false
			}
		}
	}

	private Application newTestApp() {
		new Application().with {
			apiVersion = "${K8SClient.API_GROUP}/${K8SClient.API_VERSION}"
			kind = "Application"
			metadata = new Metadata(name: application)
			spec = new Spec(description: "Application for testing purpose")
			it
		}
	}
}
