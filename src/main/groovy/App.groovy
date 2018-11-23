import client.K8SClient
import model.RemoteEnviroment

class App {
    static void main(String[] args) {
        def client = new K8SClient()
        def enviroment = client.createRemoteEnviroment(RemoteEnviroment.buildTestRE())
        println enviroment

        def env = client.deleteRemoteEnviroment("another-test2")
        println env
    }
}
