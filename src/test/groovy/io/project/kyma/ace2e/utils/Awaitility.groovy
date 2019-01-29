package io.project.kyma.ace2e.utils

import static java.util.concurrent.TimeUnit.SECONDS
import static org.awaitility.Awaitility.await

class Awaitility {
	static private POLL_DELAY = 2

	static def awaitUntil(predicate, Integer interval, Integer timeout){
		await().atMost(timeout, SECONDS)
				.pollDelay(POLL_DELAY, SECONDS)
				.pollInterval(interval, SECONDS)
				.until {
			try {
				predicate()
			}catch(e){
				false
			}
		}
	}
}
