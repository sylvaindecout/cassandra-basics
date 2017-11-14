package test.sdc.cassandra.model;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import test.sdc.cassandra.model.udt.VesselUdt;
import test.sdc.model.Vessel;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Get list of vessels that departed recently from a selected port.
 */
@Table(keyspace = "vessel",
        name = "vessels_by_departure_port",
        readConsistency = "LOCAL_QUORUM",
        writeConsistency = "LOCAL_QUORUM")
public final class VesselsByDeparturePortTable {

    @PartitionKey
    @Column(name = "last_departure_port")
    private String lastDeparturePort;

    @ClusteringColumn
    @Column(name = "last_departure_time")
    private Date lastDepartureTime;
    @ClusteringColumn(1)
    private UUID uuid;

    private VesselUdt vessel;

    /**
     * Default constructor.
     */
    private VesselsByDeparturePortTable() {
    }

    /**
     * Map input domain data model object into new object.
     *
     * @param uuid        UUID
     * @param inputObject domain data model object
     * @return new object
     */
    public static VesselsByDeparturePortTable from(final UUID uuid, final Vessel inputObject) {
        checkArgument(inputObject.getLastDeparture().isPresent(), "Last departure info is mandatory");
        final VesselsByDeparturePortTable res = new VesselsByDeparturePortTable();
        res.uuid = uuid;
        res.vessel = VesselUdt.from(inputObject);
        inputObject.getLastDeparture().ifPresent(lastDeparture -> {
            res.lastDeparturePort = lastDeparture.getDeparturePort().getUuid();
            res.lastDepartureTime = Date.from(lastDeparture.getDepartureTime());
        });
        return res;
    }

    /**
     * Map current object into domain data model.
     *
     * @return domain data model object
     */
    public Vessel toDomainModel() {
        return this.vessel.toDomainModel(this.uuid);
    }

    /**
     * Get UUID.
     *
     * @return UUID
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
     * Get last departure port.
     *
     * @return last departure port
     */
    public String getLastDeparturePort() {
        return lastDeparturePort;
    }

    /**
     * Set last departure port.
     *
     * @param lastDeparturePort last departure port
     */
    protected void setLastDeparturePort(String lastDeparturePort) {
        this.lastDeparturePort = lastDeparturePort;
    }

    /**
     * Get last departure time.
     *
     * @return last departure time
     */
    public Date getLastDepartureTime() {
        return lastDepartureTime;
    }

    /**
     * Set last departure time.
     *
     * @param lastDepartureTime last departure time
     */
    protected void setLastDepartureTime(Date lastDepartureTime) {
        this.lastDepartureTime = lastDepartureTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.uuid, this.vessel, this.lastDeparturePort, this.lastDepartureTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return other != null
                && other instanceof VesselsByDeparturePortTable
                && Objects.equals(this.uuid, ((VesselsByDeparturePortTable) other).uuid)
                && Objects.equals(this.vessel, ((VesselsByDeparturePortTable) other).vessel)
                && Objects.equals(this.lastDeparturePort, ((VesselsByDeparturePortTable) other).lastDeparturePort)
                && Objects.equals(this.lastDepartureTime, ((VesselsByDeparturePortTable) other).lastDepartureTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("%s (#%s)", this.vessel.getName(), this.uuid);
    }

}