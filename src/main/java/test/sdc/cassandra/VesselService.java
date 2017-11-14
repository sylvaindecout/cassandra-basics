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
import test.sdc.cassandra.model.VesselsByUuidTable;
import test.sdc.cassandra.model.VesselsTable;
import test.sdc.model.*;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;

/**
 * Interface of vessel service with Cassandra.
 */
public final class VesselService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(VesselService.class);

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

    public Optional<Vessel> find(final String uuid) {
        LOGGER.trace("Find vessel by UUID '{}'", uuid);
        final Mapper<VesselsByUuidTable> mapper = this.mappingManager.mapper(VesselsByUuidTable.class);
        final VesselsByUuidTable entity = mapper.get(UUID.fromString(uuid));
        LOGGER.trace("Found {}match for vessel ID={}", entity == null ? "no " : "", uuid);
        return Optional.ofNullable(entity).map(VesselsByUuidTable::toDomainModel);
    }

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

    private List<VesselsTable> findByNameFragment(final String visibility, final String nameFragment) {
        final Mapper<VesselsTable> mapper = this.mappingManager.mapper(VesselsTable.class);
        final Select.Where query = select().all()
                .from("vessels")
                .where(eq("visibility", visibility))
                .and(like("name", String.format("%%%s%%", nameFragment)));
        final ResultSet result = this.session.execute(query);
        return mapper.map(result).all();
    }

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

    private List<VesselsTable> findByCategory(final String visibility, final VesselCategoryReference category) {
        final Mapper<VesselsTable> mapper = this.mappingManager.mapper(VesselsTable.class);
        final Select.Where query = select().all()
                .from("vessels_by_category")
                .where(eq("visibility", visibility))
                .and(eq("category", category.getUuid()));
        final ResultSet result = this.session.execute(query);
        return mapper.map(result).all();
    }

    public List<Vessel> findByDeparturePort(final PortReference departurePort) {
        return null; //TODO
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

    public void remove(final String uuid) {
        LOGGER.trace("Delete {}", uuid);
        final Optional<Vessel> zone = this.find(uuid);
        if (zone.isPresent()) {
            delete(UUID.fromString(uuid), zone.get());
            LOGGER.trace("Removal of vessel {} completed", uuid);
        } else {
            LOGGER.warn("No vessel found with ID {}", uuid);
        }
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