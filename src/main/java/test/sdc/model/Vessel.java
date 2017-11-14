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

    /**
     * Get UUID.
     *
     * @return UUID
     */
    public String getUuid() {
        return this.uuid;
    }

    /**
     * Set UUID
     *
     * @param uuid UUID
     */
    private void setUuid(String uuid) {
        this.uuid = uuid;
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
     * Set name.
     *
     * @param name name
     */
    private void setName(final String name) {
        this.name = name;
    }

    /**
     * Get vessel category.
     *
     * @return vessel category
     */
    public VesselCategoryReference getCategory() {
        return this.category;
    }

    /**
     * Set vessel category.
     *
     * @param category vessel category
     */
    private void setCategory(final VesselCategoryReference category) {
        this.category = category;
    }

    /**
     * Get visibility type.
     *
     * @return visibility type
     */
    public VisibilityType getVisibility() {
        return this.visibility;
    }

    /**
     * Set visibility type.
     *
     * @param visibility visibility type
     */
    private void setVisibility(final VisibilityType visibility) {
        this.visibility = visibility;
    }

    /**
     * Get creation center.
     *
     * @return creation center
     */
    public CenterReference getCreationCenter() {
        return this.creationCenter;
    }

    /**
     * Set creation center.
     *
     * @param creationCenter creation center
     */
    private void setCreationCenter(final CenterReference creationCenter) {
        this.creationCenter = creationCenter;
    }

    /**
     * Get last departure info.
     *
     * @return last departure info
     */
    public Optional<VesselDeparture> getLastDeparture() {
        return Optional.ofNullable(this.lastDeparture);
    }

    /**
     * Set last departure info.
     *
     * @param lastDeparture last departure info
     */
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

        /**
         * Update builder with input name.
         *
         * @param name name
         * @return current builder instance
         */
        public Builder withName(final String name) {
            this.instance.setName(name);
            return this;
        }

        /**
         * Update builder with input vessel category.
         *
         * @param category vessel category
         * @return current builder instance
         */
        public Builder withCategory(final VesselCategoryReference category) {
            this.instance.setCategory(category);
            return this;
        }

        /**
         * Update builder with input vessel category.
         *
         * @param categoryUuid vessel category
         * @return current builder instance
         */
        public Builder withCategory(final String categoryUuid) {
            final VesselCategoryReference category = VesselCategoryReference.of(categoryUuid);
            return this.withCategory(category);
        }

        /**
         * Update builder with input visibility type.
         *
         * @param visibility visibility type
         * @return current builder instance
         */
        public Builder withVisibility(final VisibilityType visibility) {
            this.instance.setVisibility(visibility);
            return this;
        }

        /**
         * Update builder with input creation center.
         *
         * @param creationCenter creation center
         * @return current builder instance
         */
        public Builder withCreationCenter(final CenterReference creationCenter) {
            this.instance.setCreationCenter(creationCenter);
            return this;
        }

        /**
         * Update builder with input creation center.
         *
         * @param creationCenterUuid creation center
         * @return current builder instance
         */
        public Builder withCreationCenter(final String creationCenterUuid) {
            final CenterReference creationCenter = CenterReference.of(creationCenterUuid);
            return this.withCreationCenter(creationCenter);
        }

        /**
         * Update builder with input last departure info.
         *
         * @param lastDeparturePort last departure port
         * @param lastDepartureTime last departure time
         * @return current builder instance
         */
        public Builder withDeparture(final PortReference lastDeparturePort, final Instant lastDepartureTime) {
            final VesselDeparture departureInfo = VesselDeparture.of(lastDeparturePort, lastDepartureTime);
            this.instance.setLastDeparture(departureInfo);
            return this;
        }

        /**
         * Update builder with input last departure info.
         *
         * @param lastDeparturePortUuid last departure port
         * @param lastDepartureTime     last departure time
         * @return current builder instance
         */
        public Builder withDeparture(final String lastDeparturePortUuid, final Instant lastDepartureTime) {
            final PortReference lastDeparturePort = PortReference.of(lastDeparturePortUuid);
            return this.withDeparture(lastDeparturePort, lastDepartureTime);
        }

        /**
         * Build vessel instance.
         *
         * @return vessel
         */
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