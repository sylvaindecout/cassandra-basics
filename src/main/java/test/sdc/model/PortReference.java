package test.sdc.model;

import java.io.Serializable;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Reference to a port.
 */
public final class PortReference
        implements Serializable {

    private String uuid;

    /**
     * Private constructor.
     */
    private PortReference() {
    }

    /**
     * Initialize instance from input UUID.
     *
     * @param uuid UUID
     * @return new instance
     */
    public static PortReference of(final String uuid) {
        requireNonNull(uuid, "UUID is mandatory");
        final PortReference instance = new PortReference();
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
        return other instanceof PortReference
                && Objects.equals(this.uuid, ((PortReference) other).uuid);
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