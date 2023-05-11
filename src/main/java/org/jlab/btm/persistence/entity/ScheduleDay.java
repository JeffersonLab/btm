package org.jlab.btm.persistence.entity;

import javax.ejb.Schedule;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

/**
 * @author ryans
 */
@Entity
@Table(name = "SCHEDULE_DAY", schema = "BTM_OWNER", uniqueConstraints
        = {
        @UniqueConstraint(columnNames = {"DAY_MONTH_YEAR", "MONTHLY_SCHEDULE_ID"})})
public class ScheduleDay implements Serializable, Comparable<ScheduleDay> {
    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name = "ScheduleDayId", sequenceName = "SCHEDULE_DAY_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ScheduleDayId")
    @Basic(optional = false)
    @NotNull
    @Column(name = "SCHEDULE_DAY_ID", nullable = false, precision = 22, scale = 0)
    protected BigInteger scheduleDayId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "DAY_MONTH_YEAR", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    protected Date dayMonthYear;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 24)
    @Column(name = "ACC_PROGRAM", nullable = false, length = 24)
    protected String accProgram;
    @Column(name = "KILO_VOLTS_PER_PASS")
    protected Integer kiloVoltsPerPass;
    @Column(name = "MIN_HALL_COUNT")
    protected Integer minHallCount;
    @Size(max = 256)
    @Column(length = 256)
    protected String note;
    @Size(max = 256)
    @Column(name = "HALL_A_NOTE", length = 256)
    protected String hallANote;
    @Size(max = 256)
    @Column(name = "HALL_B_NOTE", length = 256)
    protected String hallBNote;
    @Size(max = 256)
    @Column(name = "HALL_C_NOTE", length = 256)
    protected String hallCNote;
    @Size(max = 256)
    @Column(name = "HALL_D_NOTE", length = 256)
    protected String hallDNote;
    @Basic(optional = false)
    @NotNull
    @Column(name = "HALL_A_PROGRAM_ID", nullable = false)
    protected Integer hallAProgramId;
    @Column(name = "HALL_A_NANO_AMPS")
    protected Integer hallANanoAmps;
    @Column(name = "HALL_A_KILO_VOLTS")
    protected Integer hallAKiloVolts;
    @Column(name = "HALL_A_PASSES")
    protected Integer hallAPasses;
    @Column(name = "HALL_A_PRIORITY")
    protected Integer hallAPriority;
    @Basic(optional = false)
    @NotNull
    @Column(name = "HALL_A_POLARIZED", nullable = false)
    protected Boolean hallAPolarized;
    @Basic(optional = false)
    @NotNull
    @Column(name = "HALL_B_PROGRAM_ID", nullable = false)
    protected Integer hallBProgramId;
    @Column(name = "HALL_B_NANO_AMPS")
    protected Integer hallBNanoAmps;
    @Column(name = "HALL_B_KILO_VOLTS")
    protected Integer hallBKiloVolts;
    @Column(name = "HALL_B_PASSES")
    protected Integer hallBPasses;
    @Column(name = "HALL_B_PRIORITY")
    protected Integer hallBPriority;
    @Basic(optional = false)
    @NotNull
    @Column(name = "HALL_B_POLARIZED", nullable = false)
    protected Boolean hallBPolarized;
    @Column(name = "HALL_C_PROGRAM_ID")
    protected Integer hallCProgramId;
    @Column(name = "HALL_C_NANO_AMPS")
    protected Integer hallCNanoAmps;
    @Column(name = "HALL_C_KILO_VOLTS")
    protected Integer hallCKiloVolts;
    @Column(name = "HALL_C_PASSES")
    protected Integer hallCPasses;
    @Column(name = "HALL_C_PRIORITY")
    protected Integer hallCPriority;
    @Basic(optional = false)
    @NotNull
    @Column(name = "HALL_C_POLARIZED", nullable = false)
    protected Boolean hallCPolarized;
    @Column(name = "HALL_D_PROGRAM_ID")
    protected Integer hallDProgramId;
    @Column(name = "HALL_D_NANO_AMPS")
    protected Integer hallDNanoAmps;
    @Column(name = "HALL_D_KILO_VOLTS")
    protected Integer hallDKiloVolts;
    @Column(name = "HALL_D_PASSES")
    protected Integer hallDPasses;
    @Column(name = "HALL_D_PRIORITY")
    protected Integer hallDPriority;
    @Basic(optional = false)
    @NotNull
    @Column(name = "HALL_D_POLARIZED", nullable = false)
    protected Boolean hallDPolarized;
    @JoinColumn(name = "MONTHLY_SCHEDULE_ID", referencedColumnName = "MONTHLY_SCHEDULE_ID", nullable = false)
    @ManyToOne(optional = false)
    protected MonthlySchedule monthlySchedule;

    public BigInteger getScheduleDayId() {
        return scheduleDayId;
    }

    public void setScheduleDayId(BigInteger scheduleDayId) {
        this.scheduleDayId = scheduleDayId;
    }

    public Date getDayMonthYear() {
        return dayMonthYear;
    }

    public void setDayMonthYear(Date dayMonthYear) {
        this.dayMonthYear = dayMonthYear;
    }

    public String getAccProgram() {
        return accProgram;
    }

    public void setAccProgram(String accProgram) {
        this.accProgram = accProgram;
    }

    public Integer getKiloVoltsPerPass() {
        return kiloVoltsPerPass;
    }

    public void setKiloVoltsPerPass(Integer kiloVoltsPerPass) {
        this.kiloVoltsPerPass = kiloVoltsPerPass;
    }

    public Integer getMinHallCount() {
        return minHallCount;
    }

    public void setMinHallCount(Integer minHallCount) {
        this.minHallCount = minHallCount;
    }

    public String getHallANote() {
        return hallANote;
    }

    public void setHallANote(String hallANote) {
        this.hallANote = hallANote;
    }

    public String getHallBNote() {
        return hallBNote;
    }

    public void setHallBNote(String hallBNote) {
        this.hallBNote = hallBNote;
    }

    public String getHallCNote() {
        return hallCNote;
    }

    public void setHallCNote(String hallCNote) {
        this.hallCNote = hallCNote;
    }

    public String getHallDNote() {
        return hallDNote;
    }

    public void setHallDNote(String hallDNote) {
        this.hallDNote = hallDNote;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getHallAProgramId() {
        return hallAProgramId;
    }

    public void setHallAProgramId(Integer hallAProgramId) {
        this.hallAProgramId = hallAProgramId;
    }

    public Integer getHallANanoAmps() {
        return hallANanoAmps;
    }

    public void setHallANanoAmps(Integer hallANanoAmps) {
        this.hallANanoAmps = hallANanoAmps;
    }

    public Integer getHallAKiloVolts() {
        return hallAKiloVolts;
    }

    public void setHallAKiloVolts(Integer hallAKiloVolts) {
        this.hallAKiloVolts = hallAKiloVolts;
    }

    public Integer getHallAPasses() {
        return hallAPasses;
    }

    public void setHallAPasses(Integer hallAPasses) {
        this.hallAPasses = hallAPasses;
    }

    public Integer getHallAPriority() {
        return hallAPriority;
    }

    public void setHallAPriority(Integer hallAPriority) {
        this.hallAPriority = hallAPriority;
    }

    public Boolean getHallAPolarized() {
        return hallAPolarized;
    }

    public void setHallAPolarized(Boolean hallAPolarized) {
        this.hallAPolarized = hallAPolarized;
    }

    public Integer getHallBProgramId() {
        return hallBProgramId;
    }

    public void setHallBProgramId(Integer hallBProgramId) {
        this.hallBProgramId = hallBProgramId;
    }

    public Integer getHallBNanoAmps() {
        return hallBNanoAmps;
    }

    public void setHallBNanoAmps(Integer hallBNanoAmps) {
        this.hallBNanoAmps = hallBNanoAmps;
    }

    public Integer getHallBKiloVolts() {
        return hallBKiloVolts;
    }

    public void setHallBKiloVolts(Integer hallBKiloVolts) {
        this.hallBKiloVolts = hallBKiloVolts;
    }

    public Integer getHallBPasses() {
        return hallBPasses;
    }

    public void setHallBPasses(Integer hallBPasses) {
        this.hallBPasses = hallBPasses;
    }

    public Integer getHallBPriority() {
        return hallBPriority;
    }

    public void setHallBPriority(Integer hallBPriority) {
        this.hallBPriority = hallBPriority;
    }

    public Boolean getHallBPolarized() {
        return hallBPolarized;
    }

    public void setHallBPolarized(Boolean hallBPolarized) {
        this.hallBPolarized = hallBPolarized;
    }

    public Integer getHallCProgramId() {
        return hallCProgramId;
    }

    public void setHallCProgramId(Integer hallCProgramId) {
        this.hallCProgramId = hallCProgramId;
    }

    public Integer getHallCNanoAmps() {
        return hallCNanoAmps;
    }

    public void setHallCNanoAmps(Integer hallCNanoAmps) {
        this.hallCNanoAmps = hallCNanoAmps;
    }

    public Integer getHallCKiloVolts() {
        return hallCKiloVolts;
    }

    public void setHallCKiloVolts(Integer hallCKiloVolts) {
        this.hallCKiloVolts = hallCKiloVolts;
    }

    public Integer getHallCPasses() {
        return hallCPasses;
    }

    public void setHallCPasses(Integer hallCPasses) {
        this.hallCPasses = hallCPasses;
    }

    public Integer getHallCPriority() {
        return hallCPriority;
    }

    public void setHallCPriority(Integer hallCPriority) {
        this.hallCPriority = hallCPriority;
    }

    public Boolean getHallCPolarized() {
        return hallCPolarized;
    }

    public void setHallCPolarized(Boolean hallCPolarized) {
        this.hallCPolarized = hallCPolarized;
    }

    public Integer getHallDProgramId() {
        return hallDProgramId;
    }

    public void setHallDProgramId(Integer hallDProgramId) {
        this.hallDProgramId = hallDProgramId;
    }

    public Integer getHallDNanoAmps() {
        return hallDNanoAmps;
    }

    public void setHallDNanoAmps(Integer hallDNanoAmps) {
        this.hallDNanoAmps = hallDNanoAmps;
    }

    public Integer getHallDKiloVolts() {
        return hallDKiloVolts;
    }

    public void setHallDKiloVolts(Integer hallDKiloVolts) {
        this.hallDKiloVolts = hallDKiloVolts;
    }

    public Integer getHallDPasses() {
        return hallDPasses;
    }

    public void setHallDPasses(Integer hallDPasses) {
        this.hallDPasses = hallDPasses;
    }

    public Integer getHallDPriority() {
        return hallDPriority;
    }

    public void setHallDPriority(Integer hallDPriority) {
        this.hallDPriority = hallDPriority;
    }

    public Boolean getHallDPolarized() {
        return hallDPolarized;
    }

    public void setHallDPolarized(Boolean hallDPolarized) {
        this.hallDPolarized = hallDPolarized;
    }

    public MonthlySchedule getMonthlySchedule() {
        return monthlySchedule;
    }

    public void setMonthlySchedule(MonthlySchedule monthlySchedule) {
        this.monthlySchedule = monthlySchedule;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (scheduleDayId != null ? scheduleDayId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ScheduleDay)) {
            return false;
        }
        ScheduleDay other = (ScheduleDay) object;
        return (this.scheduleDayId != null || other.scheduleDayId == null) &&
                (this.scheduleDayId == null || this.scheduleDayId.equals(other.scheduleDayId));
    }

    @Override
    public String toString() {
        return "id:" + this.scheduleDayId + ", Schedule Day: " + dayMonthYear + ", KeV/pass: " + this.kiloVoltsPerPass + ", Accelerator Program: " + this.accProgram + ", Hall A Program/KeV/nA: " + this.hallAProgramId + " / " + this.hallAKiloVolts + " / " + this.hallANanoAmps + " / " + this.hallAPolarized;
    }

    public ScheduleDay scheduleCopy() {
        ScheduleDay day = new ScheduleDay();

        day.setDayMonthYear(new Date(dayMonthYear.getTime()));
        day.setAccProgram(accProgram);
        day.setKiloVoltsPerPass(kiloVoltsPerPass);
        day.setNote(note);

        // Hall A
        day.setHallAProgramId(hallAProgramId);
        day.setHallAKiloVolts(hallAKiloVolts);
        day.setHallANanoAmps(hallANanoAmps);
        day.setHallAPolarized(hallAPolarized);
        day.setHallAPasses(hallAPasses);
        day.setHallAPriority(hallAPriority);

        // Hall B
        day.setHallBProgramId(hallBProgramId);
        day.setHallBKiloVolts(hallBKiloVolts);
        day.setHallBNanoAmps(hallBNanoAmps);
        day.setHallBPolarized(hallBPolarized);
        day.setHallBPasses(hallBPasses);
        day.setHallBPriority(hallBPriority);

        // Hall C
        day.setHallCProgramId(hallCProgramId);
        day.setHallCKiloVolts(hallCKiloVolts);
        day.setHallCNanoAmps(hallCNanoAmps);
        day.setHallCPolarized(hallCPolarized);
        day.setHallCPasses(hallCPasses);
        day.setHallCPriority(hallCPriority);

        // Hall D
        day.setHallDProgramId(hallDProgramId);
        day.setHallDKiloVolts(hallDKiloVolts);
        day.setHallDNanoAmps(hallDNanoAmps);
        day.setHallDPolarized(hallDPolarized);
        day.setHallDPasses(hallDPasses);
        day.setHallDPriority(hallDPriority);

        return day;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure
     * {@code sgn(x.compareTo(y)) == -sgn(y.compareTo(x))}
     * for all {@code x} and {@code y}.  (This
     * implies that {@code x.compareTo(y)} must throw an exception iff
     * {@code y.compareTo(x)} throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
     * {@code x.compareTo(z) > 0}.
     *
     * <p>Finally, the implementor must ensure that {@code x.compareTo(y)==0}
     * implies that {@code sgn(x.compareTo(z)) == sgn(y.compareTo(z))}, for
     * all {@code z}.
     *
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
     * class that implements the {@code Comparable} interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * <p>In the foregoing description, the notation
     * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
     * <i>signum</i> function, which is defined to return one of {@code -1},
     * {@code 0}, or {@code 1} according to whether the value of
     * <i>expression</i> is negative, zero, or positive, respectively.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(ScheduleDay o) {
        return dayMonthYear.compareTo(o.dayMonthYear);
    }
}
