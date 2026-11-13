# Concepts

[User Guide](README.md) › Concepts

## Why account for beam time

Time accounting gathers data about how the accelerator is used and how available it is, so that
lab leadership can track and monitor performance. It is kept from two perspectives:

- **Operations** (Accelerator Division): the Crew Chief accounts for the accelerator's time and
  for the beam each hall received.
- **The halls** (Physics Division): each hall's experimenters account for the beam their hall
  could use and did use.

Time is accounted per hour: each figure is the time within the hour that the machine, or a
hall, was in a particular state. The hours are grouped into eight-hour shift
[timesheets](timesheets.md). To keep the bookkeeping light, automated time accounting
[measures](#planned-measured-and-reported) a best-effort figure for every hour, and the
timekeepers correct it where it is wrong.

BTM is not Downtime Manager (DTM). DTM records each failure separately, by system and component;
BTM accounts for the hours of the program. The two are linked (BTM reads DTM's downtime to
measure Internal Down), but DTM's failure records are not meant to add up to BTM's hours.

## Modes, states and programs

| Term | Meaning |
|---|---|
| **Mode** | The machine is always in exactly one of three time accounting modes: **Physics**, **Internal** or **Off**. |
| **State** | What the machine or a hall was doing within a mode, such as Up, BNR or Internal Down. Counting the time in each state is the core of time accounting. Most states are mutually exclusive, so the states of an hour add up to one hour; [Down Hard](#accelerator-states) and the multiplicity states are exceptions. |
| **Program** | The mode, or the more specific state, that is scheduled. The accelerator has a program, and when it is Physics each hall may have its own program: an experiment, or a task such as commissioning or maintenance. |

In **Physics** mode the machine is provided to the Physics Division, which does not by itself
mean that research is being done. In **Internal** mode the Accelerator Division uses the
machine itself: for beam studies, a configuration change (pass or energy change), or restoring
the machine after it was off. In **Off** mode the machine is off.

On the State Timeline and the timesheets, the accelerator program is one of five, fixed (the
[schedule](schedule.md) has a few more, such as FACDEV and TBD):

| Program | Mode | Meaning |
|---|---|---|
| **Physics** | Physics | Beam is scheduled for the halls. |
| **Studies** | Internal | The machine is studied, tested or developed as planned (beam studies, machine development). Recovering to physics after studies is still Studies. |
| **NPES Restore** | Internal | The machine is restored after being off, as planned. See [Restore](#restore-acc-and-their-allowances). |
| **ACC** | Internal | Accelerator Configuration Change: the machine is being configured for a different pass or energy. |
| **Off** | Off | The machine is off as planned, for a holiday, the budget or the schedule (not for a failure); also called Scheduled Accelerator Maintenance (SAM). |

Hall programs are kept on the [Programs](schedule.md#programs) tab and scheduled on the
[Schedule](schedule.md) tab. Other JLab facilities, such as LERF, UITF, CMTF and VTA, are not part
of time accounting.

## Schedules

Several schedules, each more detailed than the last, say what the machine should be doing:

1. The **NPES schedule** (Nuclear Physics Experiment Scheduling committee) plans months: the
   accelerator program and each hall's program, day by day. BTM keeps it on the
   [Schedule](schedule.md) tab.
2. The **MCC whiteboard** plans weeks.
3. The **Program Deputy (PD) shift plan** plans days: the hours of each program in a shift. BTM
   shows it as the **Planned** values of a timesheet.
4. The **Crew Chief** decides minute by minute, guided by the plans before.

The more detailed plans follow the less detailed ones, but they may differ in practice: a run
may start a few days earlier than the NPES schedule said, or a hall planned for beam at 3 PM may
be ready a few hours early.

The workflow, from the schedule to the reports:

1. The Program Advisory Committee (PAC) and Technical Advisory Committee (TAC) work with the
   hall liaisons to review, approve and schedule experiments, and the NPES schedule is published.
2. Operability staff make the whiteboard schedule from it.
3. The PD makes the shift plan from the whiteboard.
4. The Crew Chief runs the program in the shift plan, and watches the automated time
   accounting (BOOM) during the shift.
5. At the end of the shift, the Crew Chief checks the timesheet, filled in from the automated
   accounting, and corrects it.
6. The Crew Chief calls the halls to fill out their timesheets, compares their figures with the
   [cross-checks](cross-checks.md) and resolves differences.
7. The Crew Chief signs the timesheet, which makes a logbook entry.
8. The PD presents BTM's [reports](reports.md) in the daily and weekly meetings.

## Planned, measured and reported

A timesheet has up to three values for each figure:

| Value | Where it comes from |
|---|---|
| **Planned** | The PD shift plan. |
| **Measured** | Automated time accounting: for the Crew Chief, BOOM (Beam Objective Operational Monitor) and the [State Timeline](state-timeline.md); for the halls, the halls' EPICS time accounting. |
| **Reported** | What the timekeeper saved. This is the official figure the reports use. |

Until a timekeeper saves an hour, the timesheet shows its measured values as the reported ones,
to start from; the reports count only saved hours.

### Automated time accounting

**BOOM** is the Crew Chief's automated time accounting. It works out the operations states of
each hour from diagnostics, the state of the Fast ShutDown (FSD) system, and modes the Crew Chief
can force when the automatic choice is wrong (BNR, Down and Off). BOOM switches to Down on a
trip, even when a mode is forced.

The halls' automated time accounting runs in EPICS IOCs. Each hall sets two PVs, *beam present*
and *beam used*, and the IOCs combine them with BOOM's forced ACC, BANU and Off modes. When
nothing else is measured, the hour is BNA. Two problems are common: *beam used* often watches
only the data acquisition, so energy measurements and other procedures are left out, and *beam
present* measures whether beam is actually there, not whether the Crew Chief could send it.

The accelerator program comes from the [State Timeline](state-timeline.md), which follows the
program PV and which Crew Chiefs can correct.

## Accelerator states

The Crew Chief's timesheet uses these states.

### Internal mode

| State | Meaning |
|---|---|
| **Studies** | As planned: the machine is studied, tested or developed. |
| **NPES Restore** | As planned: the machine is restored after being off. |
| **ACC** | As planned: the machine is configured for a different pass or energy. |
| **Internal Down** | Also called SRA Down (Studies, Restore and ACC Down). The machine should be doing studies, a restore or a configuration change, but a component failed or it needs excessive tuning. It includes recovering from the failure. BTM measures it from DTM: the time DTM has the machine down (blocked events) during Studies, NPES Restore or ACC. |
| **Down Hard** | Shared with Physics mode, below. When recording Internal Down, Down Hard is recorded too. |

### Physics mode, for each hall

| State | Meaning |
|---|---|
| **Up** | CW beam was measured on the hall's beam current monitor (BCM). |
| **Tune** | Pulsed beam was measured on the hall's BCM. The name is a misnomer: tuning is sometimes done with CW beam, and a hall may use pulsed beam. |
| **BNR** | Beam Not Requested: the accelerator can deliver beam and is scheduled to, but the hall asked to hold it, for a hall component failure, a hall controlled access or a hall procedure. |
| **Down** | The hall is scheduled for beam but can't get it, because of an accelerator component failure, recovery from an accelerator failure, accelerator tuning, or unacceptable beam quality. The halls call this BNA. If neither the accelerator nor the hall is ready, but both are scheduled to be, record Down, not BNR: the hall gets the benefit. |
| **Off** | The hall is not scheduled for beam. During an ACC, as in any Internal mode, the Crew Chief records the hall Off. |

### Multiplicity and aggregate states

These overlap the others:

| State | Meaning |
|---|---|
| **One, Two, Three, Four Hall Up** | Exactly that many halls measured CW beam. |
| **Any Hall Up** | At least one hall measured CW beam. |
| **All Halls Up** | Every hall that is supposed to be up is up: at least one hall measured CW beam, and each other hall measured CW beam or is Off. |
| **Down Hard** | The whole machine is down because of a component failure or tuning, including every hall that is not Off, which records Down. It applies in both Physics and Internal mode, and usually starts at an FSD trip. It corresponds to DTM's accelerator downtime. |

## Hall states

Each hall's experimenters record Off unless the accelerator program is Physics. Hours nobody saved
are shown in the reports as Implicit Off, but a timesheet can't be signed until every hour is
saved. In Physics mode a hall may still be Off if it is not scheduled for beam.

| State | Meaning |
|---|---|
| **ABU** | Acceptable Beam Used: the hall is receiving beam and is satisfied with its quality. *Used* means used for anything with beam: harp scans, Møller measurements, data acquisition or any other hall procedure. |
| **BANU** | Beam Available, Not Used: the accelerator can deliver beam, and the hall is the one holding up the program, such as for a hall component failure or a hall controlled access. The Crew Chief's BNR is part of BANU. Beam may or may not be in the hall during BANU; the halls differ. |
| **BNA** | Beam Not Acceptable (or not available): the hall is scheduled for beam but isn't receiving it, or receives beam of unacceptable quality. The hall decides what is acceptable, though not against the beam requirements agreed with the program committee. Operations calls this Down. An accelerator component failure is BNA. If both the accelerator and the hall should be ready and neither is, the hall gets the benefit: BNA. |
| **Off** | The hall is not expected to receive beam. This includes the accelerator's Internal programs: Studies, NPES Restore and ACC. |

The halls also record the state of their own equipment:

| State | Meaning |
|---|---|
| **ER** | Experiment Ready: the experiment's equipment works and is ready for beam. |
| **PCC** | Planned Configuration Change: a controlled access or another planned configuration change is under way. |
| **UED** | Unplanned Experiment Down: the experiment's equipment is not working. |

Whether a failure is BANU or BNA depends on where the failed component is, not on who maintains
or paid for it: a component located in the hall makes BANU; one located in the accelerator
makes BNA.

## Restore, ACC and their allowances

**NPES Restore** is only the initial restore after the machine was off, as scheduled by the NPES:
it has its own allowance in the schedule. It includes commissioning and hall checkout done with
beam, the injector checkout, and threading beam out of the injector. Restoring to one hall at
the start of a run and to another later is fine: restore time need not be contiguous. Recovering
after a component failure is not NPES Restore but Internal Down, and recovering after studies is
still Studies.

**ACC** is for configuring the machine for a different pass or energy, not for the initial
restore and not for tuning, which is part of Down.

Both are allotted a set time by the Director of Operations: for ACC it depends on the change
(going from 1 pass to 5 is allotted more than from 5 to 4), and the PD usually schedules that
allotment. Once the allotment is used up, record Internal Down, as for excessive tuning. A
scheduled ACC may be extended if failures prevent it.

## Derived metrics

### Accelerator

DTM's reports derive these from the accelerator program time **P** = Physics + Studies + NPES
Restore + ACC + Internal Down (Physics already includes its own downtime):

| Metric | Formula |
|---|---|
| Uptime | P − (DTM event downtime + trip downtime) |
| Mean time between failures (MTBF) | Uptime / DTM event count |
| Mean time between trips (MTBT) | Uptime / trip count |
| Hourly failure rate | DTM event count / Uptime |
| Hourly trip rate | Trip count / Uptime |
| Availability | Uptime / P × 100 |

### Halls

BTM's [Hall Availability](reports.md#hall-availability) report derives these for each hall from
the hall's program time **T** = ER + PCC + UED, from the experimenter timesheets:

| Metric | Formula |
|---|---|
| Accelerator availability (AA) | (ABU + BANU) / T |
| Experiment availability (EA) | (ER + PCC) / T |
| Simultaneous availability (SA) | ABU / T |

## Roles

| Who | Can |
|---|---|
| Anyone | View the timesheets, the State Timeline, published schedules, the programs and the reports, without logging in. |
| Any logged-in user | Edit and sign experimenter timesheets, within [limits once signed](timesheets.md#who-can-change-a-signed-timesheet); see the Activity Audit. |
| Crew Chiefs | Edit and sign Crew Chief timesheets, correct the State Timeline, and edit the programs. |
| The scheduling committee | Create, edit and publish schedules, and edit the programs. |
| Administrators (Operability managers) | All of these, and the Setup tab. |

See [Administration](administration.md#roles) for the role names.
