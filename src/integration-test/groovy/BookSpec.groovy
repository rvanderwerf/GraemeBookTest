import geb.spock.GebSpec
import graemebooktest.Book
import grails.test.mixin.integration.Integration
import grails.transaction.*
import grails.util.Holders
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import spock.lang.Shared
import org.springframework.context.*
import org.grails.datastore.mapping.engine.event.*
/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@Integration
@Rollback
class BookSpec extends BaseFunctionalSpec {
    @Shared Future future
    @Shared def executor
    @Shared context
    def setupSpec() {
        context = Holders.findApplicationContext()
        if (context == null || !context.isActive()) {
            executor = Executors.newSingleThreadExecutor()
            future = executor.submit {
                while (Holders.findApplicationContext() == null) {
                    // wait
                    sleep 10
                }
                context = Holders.findApplicationContext()
                context.addApplicationListener { ApplicationEvent event ->
                    if (event instanceof DatastoreInitializedEvent) {
                        Book.withTransaction {
                            new Book(title: "Blah").save()
                        }
                    }
                }
            }
        } else {
            // context already exists so just use it
            Book.withTransaction {
                new Book(title: "Blah").save()
            }
        }
    }
    def cleanupSpec() {
        executor?.shutdownNow()
    }
    def setup() {
        if (future) {
            future.get(90, TimeUnit.SECONDS)
        }
    }
    def cleanup() {
    }
    void "test something else"() {
        when:
            new Book(title:"The Stand").save()
        then:
            Book.count() >= 2
    }
void "test something 2"() {
        expect:
            Book.count() >= 1
    }
}
