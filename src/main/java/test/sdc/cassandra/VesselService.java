package test.sdc.cassandra;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.extras.codecs.enums.EnumNameCodec;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.sdc.cassandra.model.VesselsByDeparturePortTable;
import test.sdc.cassandra.model.VesselsByUuidTable;
import test.sdc.cassandra.model.VesselsTable;
import test.sdc.model.*;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;

/**
 * Interface of vessel service with Cassandra.
 */
public final class VesselService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VesselService.class);

    /**
     * Range that is considered as recent when providing the list of recent departures.
     */
    private static final Duration DEPARTURE_RANGE = Duration.ofHours(20L);

    private Session session;
    private MappingManager mappingManager;

    /**
     * Set session.
     *
     * @param session session
     */
    public void setSession(final Session session) {
        this.session = session;
    }

    /**
     * Method called on the newly constructed instance, after any dependency injection has been performed by the container and before the first business method is invoked on the bean.
     */
    @PostConstruct
    public void postConstruct() {
        CodecRegistry.DEFAULT_INSTANCE
                .register(new EnumNameCodec<>(VisibilityType.class));
        this.mappingManager = new MappingManager(this.session);
    }

    /**
     * Get list of all vessels that are visible to site.
     *
     * @param center center
     * @return list of all visible vessels
     */
    public List<Vessel> findAll(final CenterReference center) {
        LOGGER.trace("Find vessels by site ID: {}", center);
        final Mapper<VesselsTable> mapper = this.mappingManager.mapper(VesselsTable.class);
        final Select.Where query = select().all()
                .from("vessels")
                .where(in("visibility", VesselsTable.getGlobalVisibilityKey(), center.getUuid()));
        LOGGER.trace("CQL query: {}", query);
        final ResultSet result = this.session.execute(query);
        final List<VesselsTable> res = mapper.map(result).all();
        LOGGER.trace("Found {} match(es) for center={}", res.size(), center);
        return res.stream()
                .map(VesselsTable::toDomainModel)
                .collect(Collectors.toList());
    }

    /**
     * Get vessel from selected UUID.
     * @param uuid UUID
     * @return vessel
     */
    public Optional<Vessel> find(final String uuid) {
        LOGGER.trace("Find vessel by UUID '{}'", uuid);
        final Mapper<VesselsByUuidTable> mapper = this.mappingManager.mapper(VesselsByUuidTable.class);
        final VesselsByUuidTable entity = mapper.get(UUID.fromString(uuid));
        LOGGER.trace("Found {}match for vessel ID={}", entity == null ? "no " : "", uuid);
        return Optional.ofNullable(entity).map(VesselsByUuidTable::toDomainModel);
    }

    /**
     * Find vessels by name fragment (among vessels that are visible to site).
     * @param center center
     * @param nameFragment name fragment
     * @return list of visible vessels that match criterion
     */
    public List<Vessel> findByNameFragment(final CenterReference center, final String nameFragment) {
        LOGGER.trace("Find vessel by name fragment '{}'", nameFragment);
        final List<VesselsTable> res = new ArrayList<>();
        res.addAll(findByNameFragment(VesselsTable.getGlobalVisibilityKey(), nameFragment));
        res.addAll(findByNameFragment(center.getUuid(), nameFragment));
        LOGGER.trace("Found {}match(es) for name fragment={}", res.size(), nameFragment);
        return res.stream()
                .map(VesselsTable::toDomainModel)
                .collect(Collectors.toList());
    }

    /**
     * Get rows that match input visibility and name fragment.
     * @param visibility visibility
     * @param nameFragment name fragment
     * @return vessels table rows
     */
    private List<VesselsTable> findByNameFragment(final String visibility, final String nameFragment) {
        final Mapper<VesselsTable> mapper = this.mappingManager.mapper(VesselsTable.class);
        final Select.Where query = select().all()
                .from("vessels")
                .where(eq("visibility", visibility))
                .and(like("name", String.format("%%%s%%", nameFragment)));
        final ResultSet result = this.session.execute(query);
        return mapper.map(result).all();
    }

    /**
     * Find vessels by category (among vessels that are visible to site).
     * @param center center
     * @param category vessel category
     * @return list of visible vessels that match criterion
     */
    public List<Vessel> findByCategory(final CenterReference center, final VesselCategoryReference category) {
        LOGGER.trace("Find vessel by category '{}'", category);
        final List<VesselsTable> res = new ArrayList<>();
        res.addAll(findByCategory(VesselsTable.getGlobalVisibilityKey(), category));
        res.addAll(findByCategory(center.getUuid(), category));
        LOGGER.trace("Found {}match(es) for category={}", res.size(), category);
        return res.stream()
                .map(VesselsTable::toDomainModel)
                .collect(Collectors.toList());
    }

    /**
     * Get rows that match input visibility and category.
     * @param visibility visibility
     * @param category vessel category
     * @return vessels table rows
     */
    private List<VesselsTable> findByCategory(final String visibility, final VesselCategoryReference category) {
        final Mapper<VesselsTable> mapper = this.mappingManager.mapper(VesselsTable.class);
        final Select.Where query = select().all()
                .from("vessels_by_category")
                .where(eq("visibility", visibility))
                .and(eq("category", category.getUuid()));
        final ResultSet result = this.session.execute(query);
        return mapper.map(result).all();
    }

    /**
     * Get list of vessels that departed recently from a selected port.
     * @param departurePort departure port
     * @return list of vessels for which last departure port matches input port and departure time is in the last hours
     */
    public List<Vessel> findByDeparturePort(final PortReference departurePort) {
        LOGGER.trace("Find vessel by last departure port '{}'", departurePort);
        final Mapper<VesselsByDeparturePortTable> mapper = this.mappingManager.mapper(VesselsByDeparturePortTable.class);
        final Instant since = Instant.now().minus(DEPARTURE_RANGE);
        final Select.Where query = select().all()
                .from("vessels_by_departure_port")
                .where(eq("last_departure_port", departurePort.getUuid()))
                .and(gt("last_departure_time", Date.from(since)));
        final ResultSet result = this.session.execute(query);
        final List<VesselsByDeparturePortTable> res = mapper.map(result).all();
        LOGGER.trace("Found {}match(es) for last departure port={}", res.size(), departurePort);
        return res.stream()
                .map(VesselsByDeparturePortTable::toDomainModel)
                .collect(Collectors.toList());
    }

    /**
     * Create new vessel with input data.
     *
     * @param vessel vessel
     */
    public void add(final Vessel vessel) {
        LOGGER.trace("Create {}", vessel);
        final UUID uuid = UUID.randomUUID();
        save(uuid, vessel);
        LOGGER.trace("Creation of vessel {} completed", vessel);
    }

    /**
     * Update vessel with input data.
     *
     * @param vessel vessel
     */

    public void update(final Vessel vessel) {
        LOGGER.trace("Update {}", vessel);
        final UUID uuid = UUID.fromString(vessel.getUuid());
        save(uuid, vessel);
        LOGGER.trace("Update of vessel {} completed", vessel);
    }

    /**
     * Save vessel with input information into vessel tables.
     *
     * @param uuid   UUID
     * @param vessel vessel
     * @param <T>    vessel table type
     */
    private <T> void save(final UUID uuid, final Vessel vessel) {
        final BatchStatement batch = new BatchStatement();
        for (final VesselTableType table : VesselTableType.values()) {
            if (table.isRelevant(vessel)) {
                final T entity = table.getEntity(uuid, vessel);
                final Mapper<T> mapper = this.mappingManager.mapper(table.<T>getTableClass());
                batch.add(mapper.saveQuery(entity));
            }
        }
        this.session.execute(batch);
    }

    /**
     * Delete vessel with input UUID.
     *
     * @param uuid UUID
     */
    public void remove(final String uuid) {
        LOGGER.trace("Delete {}", uuid);
        final Optional<Vessel> vessel = this.find(uuid);
        if (vessel.isPresent()) {
            delete(UUID.fromString(uuid), vessel.get());
            LOGGER.trace("Removal of vessel {} completed", uuid);
        } else {
            LOGGER.warn("No vessel found with ID {}", uuid);
        }
    }

    /**
     * Delete vessel with input information from vessel tables.
     *
     * @param uuid   UUID
     * @param vessel vessel
     * @param <T>    vessel table type
     */
    private <T> void delete(final UUID uuid, final Vessel vessel) {
        final BatchStatement batch = new BatchStatement();
        for (final VesselTableType table : VesselTableType.values()) {
            if (table.isRelevant(vessel)) {
                final T entity = table.getEntity(uuid, vessel);
                final Mapper<T> mapper = this.mappingManager.mapper(table.<T>getTableClass());
                batch.add(mapper.deleteQuery(entity));
            }
        }
        this.session.execute(batch);
    }

}