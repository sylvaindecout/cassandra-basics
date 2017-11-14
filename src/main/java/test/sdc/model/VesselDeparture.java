package test.sdc.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Vessel departure information.
 */
public final class VesselDeparture
        implements Serializable {

    private PortReference departurePort;
    private Instant departureTime;

    /**
     * Private constructor.
     */
    private VesselDeparture() {
    }

    /**
     * Initialize instance from input departure port and time.
     *
     * @param port departure port
     * @param time departure time
     * @return new instance
     */
    public static VesselDeparture of(final PortReference port, final Instant time) {
        requireNonNull(port, "Departure port is mandatory");
        requireNonNull(time, "Departure time is mandatory");
        final VesselDeparture instance = new VesselDeparture();
        instance.setDeparturePort(port);
        instance.setDepartureTime(time);
        return instance;
    }

    /**
     * Get departure port.
     *
     * @return departure port
     */
    public PortReference getDeparturePort() {
        return this.departurePort;
    }

    /**
     * Set departure port.
     *
     * @param departurePort departure port
     */
    private void setDeparturePort(final PortReference departurePort) {
        this.departurePort = departurePort;
    }

    /**
     * Get departure time.
     *
     * @return departure time
     */
    public Instant getDepartureTime() {
        return this.departureTime;
    }

    /**
     * Set departure time.
     *
     * @param departureTime departure time
     */
    private void setDepartureTime(final Instant departureTime) {
        this.departureTime = departureTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return other instanceof VesselDeparture
                && Objects.equals(this.departurePort, ((VesselDeparture) other).departurePort)
                && Objects.equals(this.departureTime, ((VesselDeparture) other).departureTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.departurePort, this.departureTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("Left %s at %s",
                this.departurePort, this.departureTime);
    }

}