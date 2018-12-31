package io.project.kyma.ace2e.utils

import io.project.kyma.ace2e.model.TokenRequest
import io.project.kyma.certificate.KymaConnector
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import pemreader.PemReader

import static java.util.concurrent.TimeUnit.SECONDS
import static org.awaitility.Awaitility.await

class CertificateManager {

	private K8SClient k8SClient
	private String application

	def setupNewCertificate() {
		createTokenRequest(application)
		TokenRequest tr = waitUntilTokenUrlAvailable(application)

		generateCertificate(tr.status.url, EnvironmentConfig.savePath)
		ensureFilesCleanUp(EnvironmentConfig.certPath, EnvironmentConfig.keyPath)

		createKeyStore(EnvironmentConfig.certPath, EnvironmentConfig.keyPath, EnvironmentConfig.jskStorePath)
	}

	private TokenRequest createTokenRequest(String application) {
		k8SClient.createTokenRequest(application)
	}

	private waitUntilTokenUrlAvailable(String application){
		await().conditionEvaluationListener().atMost(30, SECONDS).until({
			(TokenRequest)k8SClient.getTokenRequest(application)
		}, new TypeSafeMatcher<TokenRequest>() {
			@Override
			protected boolean matchesSafely(final TokenRequest item) {
				return item.status.url != null && item.status.url != ""
			}

			@Override
			void describeTo(final Description description) {
			}
		})
	}

	private generateCertificate(String tokenUrl, String savePath){
		new KymaConnector().generateCertificates(tokenUrl, savePath)
	}

	private ensureFilesCleanUp(String certPath, String keyPath){
		assert new File(certPath).exists()
		assert new File(keyPath).exists()

		new File(certPath).deleteOnExit()
		new File(keyPath).deleteOnExit()
	}

	private createKeyStore(String certPath, String keyPath, String keyStorePath){
		File keyStoreFile = new File(keyStorePath)

		keyStoreFile.withOutputStream {os->
			PemReader.loadKeyStore(new File(certPath), new File(keyPath), Optional.empty())
					.store(os, "".toCharArray())
		 }

		keyStoreFile.deleteOnExit()
	}
}
