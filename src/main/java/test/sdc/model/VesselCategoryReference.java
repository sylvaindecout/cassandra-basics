package test.sdc.model;

import java.io.Serializable;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Reference to a vessel category.
 */
public final class VesselCategoryReference
        implements Serializable {

    private String uuid;

    /**
     * Private constructor.
     */
    private VesselCategoryReference() {
    }

    /**
     * Initialize instance from input UUID.
     *
     * @param uuid UUID
     * @return new instance
     */
    public static VesselCategoryReference of(final String uuid) {
        requireNonNull(uuid, "UUID is mandatory");
        final VesselCategoryReference instance = new VesselCategoryReference();
        instance.setUuid(uuid);
        return instance;
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
     * Set UUID.
     *
     * @param uuid UUID
     */
    private void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return other instanceof VesselCategoryReference
                && Objects.equals(this.uuid, ((VesselCategoryReference) other).uuid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.uuid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.uuid;
    }

}