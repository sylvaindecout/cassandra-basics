package test.sdc.cassandra.model.udt;

import com.datastax.driver.mapping.annotations.Field;
import com.datastax.driver.mapping.annotations.UDT;
import test.sdc.model.Vessel;
import test.sdc.model.VisibilityType;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * Vessel UDT.
 */
@UDT(keyspace = "vessel", name = "vessel")
public final class VesselUdt {

    private String name;
    private String category;
    private VisibilityType visibility;
    @Field(name = "creation_center")
    private String creationCenter;
    @Field(name = "last_departure_port")
    private String lastDeparturePort;
    @Field(name = "last_departure_time")
    private Date lastDepartureTime;

    /**
     * Default constructor.
     */
    private VesselUdt() {
    }

    /**
     * Map input domain data model object into new object.
     *
     * @param inputObject domain data model object
     * @return new object
     */
    public static VesselUdt from(final Vessel inputObject) {
        final VesselUdt res = new VesselUdt();
        res.name = inputObject.getName();
        res.category = inputObject.getCategory().getUuid();
        res.visibility = inputObject.getVisibility();
        res.creationCenter = inputObject.getCreationCenter().getUuid();
        inputObject.getLastDeparture().ifPresent(lastDeparture -> {
            res.lastDeparturePort = lastDeparture.getDeparturePort().getUuid();
            res.lastDepartureTime = Date.from(lastDeparture.getDepartureTime());
        });
        return res;
    }

    /**
     * Map current object into domain data model.
     *
     * @param uuid UUID
     * @return domain data model object
     */
    public Vessel toDomainModel(final UUID uuid) {
        final Vessel.Builder builder = Vessel.fromUuid(uuid.toString())
                .withName(this.name)
                .withCategory(this.category)
                .withVisibility(this.visibility)
                .withCreationCenter(this.creationCenter);
        if (this.lastDeparturePort != null || this.lastDepartureTime != null) {
            builder.withDeparture(this.lastDeparturePort, this.lastDepartureTime.toInstant());
        }
        return builder.build();
    }

    /**
     * Get name.
     *
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get vessel category.
     *
     * @return vessel category
     */
    public String getCategory() {
        return this.category;
    }

    /**
     * Get visibility.
     *
     * @return visibility
     */
    public VisibilityType getVisibility() {
        return this.visibility;
    }

    /**
     * Get center that created the vessel instance.
     *
     * @return center that created the vessel instance
     */
    public String getCreationCenter() {
        return this.creationCenter;
    }

    /**
     * Get last departure port.
     *
     * @return last departure port
     */
    public String getLastDeparturePort() {
        return this.lastDeparturePort;
    }

    /**
     * Get last departure time.
     *
     * @return last departure time
     */
    public Date getLastDepartureTime() {
        return this.lastDepartureTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return other instanceof VesselUdt
                && Objects.equals(this.name, ((VesselUdt) other).name)
                && Objects.equals(this.category, ((VesselUdt) other).category)
                && Objects.equals(this.visibility, ((VesselUdt) other).visibility)
                && Objects.equals(this.creationCenter, ((VesselUdt) other).creationCenter)
                && Objects.equals(this.lastDeparturePort, ((VesselUdt) other).lastDeparturePort)
                && Objects.equals(this.lastDepartureTime, ((VesselUdt) other).lastDepartureTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.category, this.visibility,
                this.creationCenter, this.lastDeparturePort, this.lastDepartureTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder(this.name)
                .append(" (").append("visible to ");
        switch (this.visibility) {
            case ALL_CENTERS:
                str.append("all");
                break;
            case CREATION_CENTER_ONLY:
                str.append(this.creationCenter).append(" only");
                break;
            default:
                throw new IllegalStateException("Unexpected visibility type: " + this.visibility);
        }
        return str.append(")")
                .toString();
    }

}