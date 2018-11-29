package io.project.kyma.ace2e.utils

class EnvStore {

    static String savePath
    static String certPath
    static String keyPath
    static String jskStorePath
    static String host
    static String kubeConfig

    static readEnv() {
        def env = System.getenv()
        savePath = env["SAVEPATH"]
        keyPath = savePath + "/private.key"
        certPath = savePath + "/certificate.crt"
        jskStorePath = savePath + "/store.jks"
        host = env["KYMAHOST"]
        kubeConfig = env["KUBECONFIG"]
    }
}
