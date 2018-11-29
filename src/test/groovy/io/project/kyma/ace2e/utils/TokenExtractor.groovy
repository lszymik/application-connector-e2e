package io.project.kyma.ace2e.utils

class TokenExtractor {

    def static extract(String tokenRequest) {
        return tokenRequest.substring(tokenRequest.lastIndexOf("url:") + 4).replace("]", "")
    }
}
