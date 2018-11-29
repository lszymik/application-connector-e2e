package io.project.kyma.ace2e.utils

import pemreader.PemReader

class KeyStoreInitializer {

    static createJKSFileWithCert(File certFile, File keyFile, String pass, String jksStorePath) {

        def keyStore = PemReader.loadKeyStore(certFile, keyFile, Optional.empty())

        def stream = new FileOutputStream(jksStorePath)

        keyStore.store(stream, pass.toCharArray())
    }
}
