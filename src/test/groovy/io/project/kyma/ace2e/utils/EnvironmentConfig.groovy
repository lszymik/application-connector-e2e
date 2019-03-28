package io.project.kyma.ace2e.utils

import java.nio.file.Files

class EnvironmentConfig {

	static final String kubeConfig = System.getenv("KUBECONFIG")

	static final String tempDirectory = System.getProperty("java.io.tmpdir")
	static final File certFile
	static final File keyFile
	static final File jksStoreFile
	static final File keyChainFile
	static final String JSK_STORE_PASSWORD = ""

	static final String domain = System.getenv("DOMAIN")
	static final String nodePort = System.getenv("NODEPORT")

	static {
		certFile = new File(tempDirectory + "certificate.crt")
		keyFile = new File(tempDirectory + "private.key")

		jksStoreFile = Files.createTempFile("store", "jks").toFile()
		keyChainFile = Files.createTempFile("re-cert", "pem").toFile()

		certFile.deleteOnExit()
		keyFile.deleteOnExit()
		jksStoreFile.deleteOnExit()
		keyChainFile.deleteOnExit()
	}
}