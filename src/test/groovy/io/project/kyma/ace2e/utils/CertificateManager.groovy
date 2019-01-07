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
	private String keyStorePassword

	def setupCertificateInKeyStore() {
		createTokenRequest(application, namespace)
		final TokenRequest tr = waitUntilTokenUrlAvailable(application)

		generateCertificate(tr.status.url, EnvironmentConfig.savePath)

		final File certFile = new File(EnvironmentConfig.certPath)
		final File keyFile = new File(EnvironmentConfig.keyPath)
		final File keyChainFile = new File(EnvironmentConfig.keyChainPath)
		final File keystoreFile = new File(EnvironmentConfig.jksStorePath)

		ensureFilesCleanUp(certFile, keyFile, keyChainFile)

		createKeyStore(certFile, keyFile, keystoreFile, keyStorePassword)
	}

	private def createTokenRequest(String application, String namespace) {
		k8SClient.createTokenRequest(application, namespace)
	}

	private waitUntilTokenUrlAvailable(String application) {
		await().atMost(30, SECONDS)
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

	private generateCertificate(String tokenUrl, String savePath) {
		new KymaConnector().generateCertificates(tokenUrl, savePath)
	}

	private ensureFilesCleanUp(File certFile, File keyFile, File keyChainFile) {
		certFile.deleteOnExit()
		keyFile.deleteOnExit()
		keyChainFile.deleteOnExit()
	}

	private createKeyStore(File certFile, File keyFile, File keyStoreFile, String password) {
		assert certFile.exists()
		assert keyFile.exists()

		keyStoreFile.withOutputStream { os ->
			PemReader.loadKeyStore(certFile, keyFile, Optional.empty())
					.store(os, password.toCharArray())

			os.close()
		}

		keyStoreFile.deleteOnExit()
	}
}
