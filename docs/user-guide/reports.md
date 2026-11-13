# Reports

[User Guide](README.md) › Reports

The **Reports** tab turns the saved timesheets into totals, and compares them with the schedules
and with DTM. The **Metrics** tab is a glossary of the hall, multiplicity and experimenter states that
[Concepts](concepts.md#accelerator-states) also describes; the accelerator programs and states
are on **Programs › Accelerator**.

## Where the numbers come from

| Source | Meaning |
|---|---|
| **CC** | The hours saved on Crew Chief timesheets. |
| **Experimenter** | The hours saved on experimenter timesheets. |
| **PD** | The Program Deputy shift plans. |
| **NPES** | The [schedule](schedule.md): each month's latest published version, or its latest tentative one if none is published. |
| **DTM** | DTM's CEBAF Blocked and CEBAF Tuning events. An event still open counts up to now. |

The reports count only hours saved on timesheets; an hour still showing measured values isn't
counted. Hours that nobody saved show up as **Implicit Off**: the part of the period that no
saved hour accounts for.

## Choosing the dates

Most reports have a date range. Its presets include the current and previous fiscal years
(starting 1 October) and their quarters, years, months, weeks (from Wednesday), Crew Chief
shifts, the past 10, 7 or 3 days, yesterday and today, or **Custom**: the start is inclusive and
the end exclusive. Where the server knows the run schedule, **Current Run** and **Previous Run**
are offered too. On the Beam Summary, Physics Summary and Hall Availability reports, the week
and day presets start at 7:00, at a Crew Chief shift boundary, and the three reports share the
range you choose. They open on the seven days to 7:00 today.

The schedules have coarser granularity than the timesheets: the PD shift plan is by shift and
the NPES schedule by day. Choose a start and end within Off periods, or on shift boundaries, so
that the comparison is fair.

**Full Screen** shows a report on its own, and **Export** saves it as an image or prints it; the
Beam Summary also exports to Excel.

## Beam Summary

The accelerator's time in the range, as the **Accelerator Beam Time** table and a pie chart:

| Column | Meaning |
|---|---|
| CC Hrs | The Crew Chief's reported hours. |
| CC % | CC Hrs as a share of the period. |
| PD Sched. Hrs | Scheduled by the PD shift plans, with the range widened to whole shifts. |
| NPES Sched. Hrs | Scheduled by the NPES schedule, with the range widened to whole days. Each scheduled day counts its real length: 23 or 25 hours on the days daylight saving time starts and ends. |

| Row | Meaning |
|---|---|
| Physics | Research + Tuning + Down. |
| Research | Physics − Tuning − Down. CC only. |
| Tuning | DTM's CEBAF Tuning downtime. CC only. |
| Down | DTM's CEBAF Blocked downtime − Internal Down: the downtime during Physics. CC only. |
| Studies, NPES Restore, ACC | The Internal programs. |
| Internal Down | Down during Studies, NPES Restore and ACC. CC only. |
| Program Time | Physics + Studies + NPES Restore + ACC + Internal Down. |
| SAM | Scheduled Accelerator Maintenance: Off. |
| Implicit Off | The period − Program Time − SAM: time no hour accounts for, such as unsaved hours or unscheduled days. |
| Off Time | SAM + Implicit Off. |

Research, Tuning and Down mix BTM's hours with DTM's, so if the two disagree, Down or Research
can come out negative: check the timesheets and DTM for those dates. The **\*** next to the
range shows the exact periods each column covers.

## Physics Summary

The beam the halls received and used. **Data** chooses the view:

- **Available** (the default): a chart and table of each hall's scheduled hours (PD), the Crew
  Chief's UP, and the hall's ABU and BANU, with ABU + BANU as a share of the scheduled hours, and
  the machine's Any Up hours.
- **Hall A** to **Hall D**: a pie chart of one hall's ABU, BANU, BNA, OFF and Implicit OFF.
- **Table**: the multiplicity hours (Four Up to Down Hard), and for each hall the experimenter's
  ABU, BANU, BNA, OFF and Implicit OFF, the Crew Chief's UP, TUNE, BNR, DOWN, OFF and Implicit
  OFF, and the PD's scheduled hours.

The PD's scheduled hall hours are accurate only for ranges on shift boundaries (7:00, 15:00,
23:00).

## Hall Availability

Each hall's availability, from the experimenter timesheets, as a bar chart or a table (**Chart**).
With the hall's program time **T** = ER + PCC + UED:

| Metric | Formula | Meaning |
|---|---|---|
| Accelerator Availability (AA) | (ABU + BANU) / T | How much of the hall's program time the accelerator could deliver acceptable beam. |
| Experiment Availability (EA) | (ER + PCC) / T | How much of it the experiment was ready, or in a planned change. |
| Simultaneous Availability (SA) | ABU / T | How much of it both were ready and beam was used. |

Each is 0 when T is 0. The table also shows the hours behind them, and the PD's scheduled hours
for comparison. **Explanation** links to the definitions document set by an administrator.

## Charge

The beam current delivered to each hall, and the charge accumulated, over a range of days, from
the EPICS archiver (not from the timesheets). It opens on the current run, if the server knows
it, or the current week. Each hall has a charge chart and a current chart; hovering shows the
day and the hall's scheduled program. Halls A and C are in μA and C, Halls B and D in nA and mC.

## Review Calendar

A month, day by day, for reviewing the time accounting. Each day shows:

- **Program Hrs**: the Crew Chief's program hours, linked to the Beam Summary for the day.
- **Down Hrs**: DTM's CEBAF Blocked hours, linked to DTM.
- **Owl / Day / Swing**: each Crew Chief timesheet's review state:

  | Mark | Meaning |
  |---|---|
  | ✔ | Signed, and the cross-checks pass. |
  | X | Not signed, or cross-checks fail, with no reviewer comment. The day is yellow: data needs review. Select the date to see why. |
  | \* | Not signed or cross-checks fail, but a reviewer commented; hover to read it. |
  | ? | The timesheet couldn't be checked; the shifts after it are not checked either. Reload the page. |

A day whose number is pink has more than half an hour more DTM CEBAF Blocked downtime than the
Crew Chief's PHYSICS + INTERNAL DOWN: a DTM discrepancy. Days run from midnight to midnight here.

To review a day, open the shift's timesheet, look at its cross-checks, and, as an administrator,
explain an accepted discrepancy in its **Reviewer Comments**.

## Activity Audit

Who changed timesheets, and when. Log in to see it. **Transactions** lists each saved change,
newest first, with who made it, from which computer, and what changed; filter by when it was
modified, or by timesheet (type, date and shift together). A timesheet's **History** link opens
it filtered to that timesheet.

Each changed item links to its history, version by version: Crew Chief and experimenter shift
information, experimenter hours and Crew Chief accelerator hours. Crew Chief hall and
multiplicity hours, Reasons not Ready, cross-check comments and signatures are not shown here.

## CA Status

The EPICS PVs that BTM monitors for the measured values (BOOM's hourly PVs, the halls' time
accounting PVs and the program PV), each with when its last value arrived and whether it is
connected. A PV that has never sent a value isn't listed.

If measured values stop updating, check here. Crew Chiefs and administrators can **Forcibly
Reset** the connection to EPICS.
