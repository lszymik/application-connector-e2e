package io.project.kyma.ace2e.utils

class RetryClosure {

    final static RETRY_NUMBER = 5
    final static WAIT_MILLISECONDS = 2000
    
    def static retry(Closure function, Closure condition) {
        def object = function()
        for(def i = 0; !condition(object) && i < RETRY_NUMBER; i++) {
            println("Retrying...")
            sleep(WAIT_MILLISECONDS)
            object = function()
        }
        return object
    }
}