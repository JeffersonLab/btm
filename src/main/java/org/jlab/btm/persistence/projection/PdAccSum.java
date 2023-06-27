package org.jlab.btm.persistence.projection;

/**
 * Sum of PD Accelerator Programs.
 *
 * @author ryans
 */
public class PdAccSum {

    private final long programSeconds; // anything but OFF (SAD) or implied OFF
    private final long physicsSeconds;
    private final long offSeconds;
    private final long studiesSeconds;
    private final long restoreSeconds;
    private final long accSeconds;

    public PdAccSum(Number physicsSeconds, Number offSeconds,
                    Number studiesSeconds, Number restoreSeconds, Number accSeconds) {
        this.physicsSeconds = physicsSeconds.longValue();
        this.offSeconds = offSeconds.longValue();
        this.studiesSeconds = studiesSeconds.longValue();
        this.restoreSeconds = restoreSeconds.longValue();
        this.accSeconds = accSeconds.longValue();

        this.programSeconds = this.getPhysicsSeconds() +
                this.getStudiesSeconds() +
                this.getRestoreSeconds() +
                this.getAccSeconds();
    }

    public long getPhysicsSeconds() {
        return physicsSeconds;
    }

    public long getOffSeconds() {
        return offSeconds;
    }

    public long getStudiesSeconds() {
        return studiesSeconds;
    }

    public long getRestoreSeconds() {
        return restoreSeconds;
    }

    public long getAccSeconds() {
        return accSeconds;
    }

    public long getProgramSeconds() {
        return programSeconds;
    }
}
