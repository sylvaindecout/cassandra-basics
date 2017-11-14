package test.sdc.cassandra;

import test.sdc.cassandra.model.VesselsByUuidTable;
import test.sdc.cassandra.model.VesselsTable;
import test.sdc.model.Vessel;

import java.util.UUID;

/**
 * Vessel tables.
 */
enum VesselTableType {

    VESSELS(VesselsTable.class) {
        @Override
        public <T> T getEntity(final UUID uuid, final Vessel vessel) {
            return (T) VesselsTable.from(uuid, vessel);
        }
    },

    VESSELS_BY_UUID(VesselsByUuidTable.class) {
        @Override
        public <T> T getEntity(final UUID uuid, final Vessel vessel) {
            return (T) VesselsByUuidTable.from(uuid, vessel);
        }
    },;

    private final Class<?> tableClass;

    /**
     * Constructor.
     *
     * @param tableClass class that models table
     */
    VesselTableType(final Class<?> tableClass) {
        this.tableClass = tableClass;
    }

    /**
     * Get class that models table.
     *
     * @param <T> class that models table
     * @return class that models table
     */
    public <T> Class<T> getTableClass() {
        return (Class<T>) this.tableClass;
    }

    /**
     * Get table entity for vessel with input information.
     *
     * @param uuid   UUID
     * @param vessel vessel
     * @param <T>    class that models table
     * @return table entity for vessel with input information
     */
    public abstract <T> T getEntity(final UUID uuid, final Vessel vessel);

    /**
     * Check if table is relevant for input vessel (true unless overridden).
     *
     * @param vessel vessel
     * @return is table relevant for input vessel
     */
    public boolean isRelevant(final Vessel vessel) {
        return true;
    }

}