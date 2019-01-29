package io.project.kyma.ace2e.utils

class EnvironmentConfig {

	static final String savePath = System.getenv("SAVEPATH")
	static final String certPath = System.getenv("SAVEPATH") + "/certificate.crt"
	static final String keyPath = System.getenv("SAVEPATH") + "/private.key"
	static final String jksStorePath = System.getenv("SAVEPATH") + "/store.jks"
	static final String keyChainPath = System.getenv("SAVEPATH") + "/re-cert.pem"
	static final String domain = System.getenv("DOMAIN")
	static final String nodePort = System.getenv("NODEPORT")
	static final String kubeConfig = System.getenv("KUBECONFIG")
}
