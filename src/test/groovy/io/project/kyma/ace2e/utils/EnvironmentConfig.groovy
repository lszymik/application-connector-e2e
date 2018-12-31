package io.project.kyma.ace2e.utils

class EnvironmentConfig {

    static String savePath = System.getenv("SAVEPATH")
    static String certPath = System.getenv("SAVEPATH") + "/certificate.crt"
    static String keyPath = System.getenv("SAVEPATH") + "/private.key"
    static String jskStorePath = System.getenv("SAVEPATH") +  "/store.jks"
    static String host = System.getenv("KYMAHOST")
    static String kubeConfig = System.getenv("KUBECONFIG")
}
