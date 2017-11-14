package test.sdc.cassandra.model;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import test.sdc.cassandra.model.udt.VesselUdt;
import test.sdc.model.Vessel;

import java.util.Objects;
import java.util.UUID;

/**
 * Get list of all vessels that are visible to site.
 */
@Table(keyspace = "vessel",
        name = "vessels",
        readConsistency = "LOCAL_QUORUM",
        writeConsistency = "LOCAL_QUORUM")
public final class VesselsTable {

    @PartitionKey
    private String visibility;

    @ClusteringColumn
    private UUID uuid;

    private VesselUdt vessel;
    private String name; // Used to create secondary index
    private String category; // Used to create materialized view

    /**
     * Default constructor.
     */
    private VesselsTable() {
    }

    /**
     * Map input domain data model object into new object.
     *
     * @param uuid        UUID
     * @param inputObject domain data model object
     * @return new object
     */
    public static VesselsTable from(final UUID uuid, final Vessel inputObject) {
        final VesselsTable res = new VesselsTable();
        res.uuid = uuid;
        res.vessel = VesselUdt.from(inputObject);
        res.name = inputObject.getName();
        res.category = inputObject.getCategory().getUuid();
        switch (inputObject.getVisibility()) {
            case ALL_CENTERS:
                res.visibility = getGlobalVisibilityKey();
                break;
            case CREATION_CENTER_ONLY:
                res.visibility = inputObject.getCreationCenter().getUuid();
                break;
            default:
                throw new IllegalStateException("Unexpected vessel visibility: " + inputObject.getVisibility());
        }
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
     * Get key used to identify vessels with global visibility.
     *
     * @return key used to identify vessels with global visibility
     */
    public static String getGlobalVisibilityKey() {
        return "_ALL";
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

    public String getVisibility() {
        return this.visibility;
    }

    protected void setVisibility(final String visibility) {
        this.visibility = visibility;
    }

    public String getName() {
        return this.name;
    }

    protected void setName(final String name) {
        this.name = name;
    }

    public String getCategory() {
        return this.category;
    }

    protected void setCategory(final String category) {
        this.category = category;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.uuid, this.vessel, this.category, this.name, this.visibility);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return other != null
                && other instanceof VesselsTable
                && Objects.equals(this.uuid, ((VesselsTable) other).uuid)
                && Objects.equals(this.vessel, ((VesselsTable) other).vessel)
                && Objects.equals(this.category, ((VesselsTable) other).category)
                && Objects.equals(this.name, ((VesselsTable) other).name)
                && Objects.equals(this.visibility, ((VesselsTable) other).visibility);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("%s (%s)", this.vessel.getName(), this.uuid);
    }

}