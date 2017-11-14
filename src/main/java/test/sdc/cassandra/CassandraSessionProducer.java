package test.sdc.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.DefaultRetryPolicy;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

/**
 * Cassandra session initializer, for vessel keyspace.
 */
@Singleton
public final class CassandraSessionProducer {

    private static final String KEYSPACE = "vessel";

    private Cluster cluster;
    private Session session;

    /**
     * Initialize session.
     */
    @PostConstruct
    public void init() {
        final String address = "localhost"; //FIXME: add to configuration
        this.cluster = Cluster.builder()
                .addContactPoint(address)
                .withRetryPolicy(DefaultRetryPolicy.INSTANCE)
                .build();
        this.session = this.cluster.connect(KEYSPACE);
    }

    /**
     * Expose session.
     *
     * @return session
     */
    @Produces
    public Session getSession() {
        return this.session;
    }

    /**
     * Free resources.
     */
    @PreDestroy
    public void dispose() {
        this.session.close();
        this.cluster.close();
    }

}