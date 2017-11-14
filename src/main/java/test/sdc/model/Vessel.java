package test.sdc.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

public final class Vessel
        implements Serializable {

    private String uuid;
    private String name;
    private VesselCategoryReference category;
    private VisibilityType visibility;
    private CenterReference creationCenter;
    private VesselDeparture lastDeparture;

    /**
     * Initialize builder instance with random UUID.
     *
     * @return new builder instance
     */
    public static Builder newInstance() {
        final String generatedUuid = UUID.randomUUID().toString();
        return new Builder(generatedUuid);
    }

    /**
     * Initialize builder instance from input UUID.
     *
     * @param uuid UUID
     * @return new builder instance
     */
    public static Builder fromUuid(final String uuid) {
        return new Builder(uuid);
    }

    public String getUuid() {
        return this.uuid;
    }

    private void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return this.name;
    }

    private void setName(final String name) {
        this.name = name;
    }

    public VesselCategoryReference getCategory() {
        return this.category;
    }

    private void setCategory(final VesselCategoryReference category) {
        this.category = category;
    }

    public VisibilityType getVisibility() {
        return this.visibility;
    }

    private void setVisibility(final VisibilityType visibility) {
        this.visibility = visibility;
    }

    public CenterReference getCreationCenter() {
        return this.creationCenter;
    }

    private void setCreationCenter(final CenterReference creationCenter) {
        this.creationCenter = creationCenter;
    }

    public Optional<VesselDeparture> getLastDeparture() {
        return Optional.ofNullable(this.lastDeparture);
    }

    private void setLastDeparture(final VesselDeparture lastDeparture) {
        this.lastDeparture = lastDeparture;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return other instanceof Vessel
                && Objects.equals(this.uuid, ((Vessel) other).uuid)
                && Objects.equals(this.name, ((Vessel) other).name)
                && Objects.equals(this.category, ((Vessel) other).category)
                && Objects.equals(this.visibility, ((Vessel) other).visibility)
                && Objects.equals(this.creationCenter, ((Vessel) other).creationCenter)
                && Objects.equals(this.lastDeparture, ((Vessel) other).lastDeparture);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.uuid, this.name, this.category,
                this.visibility, this.creationCenter, this.lastDeparture);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder(this.name)
                .append(" (#").append(this.uuid).append(", visible to ");
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

    /**
     * Builder.
     */
    public static final class Builder {

        private final Vessel instance;

        /**
         * Private constructor.
         */
        private Builder(final String uuid) {
            this.instance = new Vessel();
            this.instance.setUuid(uuid);
        }

        public Builder withName(final String name) {
            this.instance.setName(name);
            return this;
        }

        public Builder withCategory(final VesselCategoryReference category) {
            this.instance.setCategory(category);
            return this;
        }

        public Builder withCategory(final String categoryUuid) {
            final VesselCategoryReference category = VesselCategoryReference.of(categoryUuid);
            return this.withCategory(category);
        }

        public Builder withVisibility(final VisibilityType visibility) {
            this.instance.setVisibility(visibility);
            return this;
        }

        public Builder withCreationCenter(final CenterReference creationCenter) {
            this.instance.setCreationCenter(creationCenter);
            return this;
        }

        public Builder withCreationCenter(final String creationCenterUuid) {
            final CenterReference creationCenter = CenterReference.of(creationCenterUuid);
            return this.withCreationCenter(creationCenter);
        }

        public Builder withDeparture(final PortReference lastDeparturePort, final Instant lastDepartureTime) {
            final VesselDeparture departureInfo = VesselDeparture.of(lastDeparturePort, lastDepartureTime);
            this.instance.setLastDeparture(departureInfo);
            return this;
        }

        public Builder withDeparture(final String lastDeparturePortUuid, final Instant lastDepartureTime) {
            final PortReference lastDeparturePort = PortReference.of(lastDeparturePortUuid);
            return this.withDeparture(lastDeparturePort, lastDepartureTime);
        }

        public Vessel build() {
            requireNonNull(this.instance.name, "Name is mandatory");
            requireNonNull(this.instance.category, "Category is mandatory");
            requireNonNull(this.instance.creationCenter, "Creation center is mandatory");
            if (this.instance.visibility == null) {
                this.instance.setVisibility(VisibilityType.CREATION_CENTER_ONLY);
            }
            return this.instance;
        }
    }

}