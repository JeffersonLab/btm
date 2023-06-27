package org.jlab.btm.persistence.projection;

/**
 * Sum of Crew Chief Accelerator Programs.
 *
 * @author ryans
 */
public class CcAccSum {

    private final long programSeconds; // anything but OFF (SAD) or implied OFF
    private final long upSeconds;
    private final long sadSeconds;
    private final long downSeconds;
    private final long studiesSeconds;
    private final long restoreSeconds;
    private final long accSeconds;

    public CcAccSum(Number upSeconds, Number sadSeconds, Number downSeconds,
                    Number studiesSeconds, Number restoreSeconds, Number accSeconds) {
        this.upSeconds = upSeconds.longValue();
        this.sadSeconds = sadSeconds.longValue();
        this.downSeconds = downSeconds.longValue();
        this.studiesSeconds = studiesSeconds.longValue();
        this.restoreSeconds = restoreSeconds.longValue();
        this.accSeconds = accSeconds.longValue();

        this.programSeconds = this.getUpSeconds() +
                this.getStudiesSeconds() +
                this.getRestoreSeconds() +
                this.getAccSeconds() +
                this.getDownSeconds();
    }

    public long getUpSeconds() {
        return upSeconds;
    }

    public long getSadSeconds() {
        return sadSeconds;
    }

    public long getDownSeconds() {
        return downSeconds;
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
