package test.sdc.cassandra.model;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import test.sdc.cassandra.model.udt.VesselUdt;
import test.sdc.model.Vessel;

import java.util.Objects;
import java.util.UUID;

/**
 * Get vessel from selected UUID.
 */
@Table(keyspace = "vessel",
        name = "vessels_by_uuid",
        readConsistency = "LOCAL_QUORUM",
        writeConsistency = "LOCAL_QUORUM")
public final class VesselsByUuidTable {

    @PartitionKey
    private UUID uuid;

    private VesselUdt vessel;

    /**
     * Default constructor.
     */
    protected VesselsByUuidTable() {
        super();
    }

    /**
     * Map input domain data model object into new object.
     *
     * @param uuid        UUID
     * @param inputObject domain data model object
     * @return new object
     */
    public static VesselsByUuidTable from(final UUID uuid, final Vessel inputObject) {
        final VesselsByUuidTable res = new VesselsByUuidTable();
        res.uuid = uuid;
        res.vessel = VesselUdt.from(inputObject);
        return res;
    }

    /**
     * Get unique zone identifier.
     *
     * @return unique zone identifier
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Set UUID.
     *
     * @param uuid UUID
     */
    protected void setUuid(final UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Get vessel.
     *
     * @return vessel
     */
    public VesselUdt getVessel() {
        return this.vessel;
    }

    /**
     * Set vessel.
     *
     * @param vessel vessel
     */
    protected void setVessel(final VesselUdt vessel) {
        this.vessel = vessel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.uuid, this.vessel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return other != null
                && other instanceof VesselsByUuidTable
                && Objects.equals(this.uuid, ((VesselsByUuidTable) other).uuid)
                && Objects.equals(this.vessel, ((VesselsByUuidTable) other).vessel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("%s (#%s)", this.vessel.getName(), this.uuid);
    }

    /**
     * Map current object into domain data model.
     *
     * @return domain data model object
     */
    public Vessel toDomainModel() {
        return this.vessel.toDomainModel(this.uuid);
    }

}