import io.project.kyma.certificate.KymaConnector
import spock.lang.Specification

class ApplicationConnectorTests extends Specification {

    def "let's check Spock" () {
        given:
        def array = new ArrayList()
        assert array.empty
        when:
        array.add("something")
        then:
        array.size() == 1
    }

}
