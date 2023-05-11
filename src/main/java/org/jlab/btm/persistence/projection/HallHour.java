package org.jlab.btm.persistence.projection;

import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * A generic hall hour.
 *
 * @author ryans
 */
public abstract class HallHour extends Hour {
    public abstract Hall getHall();

    public abstract void setHall(Hall hall);

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.getHall() != null ? this.getHall().hashCode() : 0);
        hash += (this.getDayAndHour() != null ? this.getDayAndHour().hashCode() : 0);
        return hash;
    }

    /**
     * Compares this object with another for meaningful equality.
     *
     * @param object The HallHour to compare to.
     * @return true if meaningfully equivalent, false otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof HallHour)) {
            return false;
        }
        HallHour other = (HallHour) object;
        if ((this.getHall() == null && other.getHall() != null) ||
                (this.getHall() != null &&
                        !this.getHall().equals(other.getHall()))) {
            return false;
        }
        return (this.getDayAndHour() != null || other.getDayAndHour() == null) &&
                (this.getDayAndHour() == null ||
                        this.getDayAndHour().equals(other.getDayAndHour()));
    }

    /*
     * Comparison is done using the alternate key (also a natural key and a
     * composite key) hall and hour.
     *
     * The primary key id is not used because it isn't available on newly
     * created objects (it is a surrogate key) and because it wouldn't provide a
     * natural ordering (just insertion order).
     *
     * Fields are accessed via get methods to prompt lazy initializer if needed.
     *
     * @param o The HallHour to compare to.
     * @return -1 if less than, 0 if equal, and 1 if greater than.
     */
    @Override
    public int compareTo(Hour o) {
        if (!(o instanceof HallHour)) {
            return -1;
        }
        HallHour oh = (HallHour) o;

        int result = this.getHall().compareTo(oh.getHall());

        if (result == 0) {
            result = this.getDayAndHour().compareTo(oh.getDayAndHour());
        }

        return result;
    }
}
