package io.project.kyma.ace2e.utils

import io.project.kyma.ace2e.model.k8s.TokenRequest
import io.project.kyma.certificate.KymaConnector
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import pemreader.PemReader

import static java.util.concurrent.TimeUnit.SECONDS
import static org.awaitility.Awaitility.await

class CertificateManager {

	private K8SClient k8SClient
	private String application
	private String namespace

	def setupCertificateInKeyStore() {
		createTokenRequest(application, namespace)
		final TokenRequest tr = waitUntilTokenUrlAvailable(application)

		generateCertificate(tr.status.url)
		createKeyStore()

		removeTokenRequest()
	}

	private def createTokenRequest(String application, String namespace) {
		k8SClient.createTokenRequest(application, namespace)
	}

	private waitUntilTokenUrlAvailable(String application) {
		await().atMost(120, SECONDS)
				.pollDelay(2, SECONDS)
				.pollInterval(5, SECONDS)
				.until({
			(TokenRequest) k8SClient.getTokenRequest(application, namespace)
		}, new TypeSafeMatcher<TokenRequest>() {
			@Override
			protected boolean matchesSafely(final TokenRequest tr) {
				return tr.status != null && tr.status.url != null && tr.status.url != ""
			}

			@Override
			void describeTo(final Description description) {
			}
		})
	}

	private static generateCertificate(String connectionUrl) {
		new KymaConnector().generateCertificates(connectionUrl, EnvironmentConfig.tempDirectory)
	}

	private static createKeyStore() {
		assert EnvironmentConfig.certFile.exists()
		assert EnvironmentConfig.keyFile.exists()

		// TODO we need to apply exception handling here
		try {
			EnvironmentConfig.jksStoreFile.withOutputStream { os ->
				PemReader.loadKeyStore(EnvironmentConfig.certFile, EnvironmentConfig.keyFile, Optional.empty())
						.store(os, EnvironmentConfig.JSK_STORE_PASSWORD.toCharArray())

				os.close()
			}
		}
		catch (final Exception e) {
			print(e.toString())
		}
	}

	private def removeTokenRequest(){
		k8SClient.deleteTokenRequest(application, namespace)
	}
}
