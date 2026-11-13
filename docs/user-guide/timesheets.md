# Timesheets

[User Guide](README.md) › Timesheets

At the end of each shift, the Crew Chief and each hall fill out a timesheet: the hours the
accelerator and the halls spent in each [state](concepts.md#modes-states-and-programs). The
**Timesheets** tab opens on the Crew Chief's timesheet for the current shift.

## Shifts

Each timesheet covers one eight-hour shift. Crew Chief shifts start an hour before experimenter
shifts:

| Shift | Crew Chief | Experimenter |
|---|---|---|
| Owl | 23:00 the day before to 07:00 | 00:00 to 08:00 |
| Day | 07:00 to 15:00 | 08:00 to 16:00 |
| Swing | 15:00 to 23:00 | 16:00 to 24:00 |

A Crew Chief Owl shift belongs to the day it ends on. Times are JLab's local time; on the days
daylight saving time starts and ends, the Owl shift has 7 or 9 hours.

## Finding a timesheet

Open the filter to choose:

- **Type**: Crew Chief, or Experimenter A, B, C or D.
- **Date** and **Shift**: Owl, Day or Swing.
- **Units**: Hours, Minutes or Seconds. Crew Chief timesheets open in hours, experimenter
  timesheets in minutes.

**Previous** and **Next** go to the neighbouring shifts. Under **Current Shift (bookmarkable)**
are links to each type's current shift, worth a bookmark:

| Timesheet | Address |
|---|---|
| Crew Chief | `/btm/timesheet/cc` |
| Experimenter A, B, C, D | `/btm/timesheet/ea`, `/eb`, `/ec`, `/ed` |

During the first hour of a Crew Chief shift, the Crew Chief link opens the shift that just ended,
the one to fill out. A timesheet's own address is like
`/btm/timesheet/cc/12-nov-2026/day/hours`.

Each timesheet shows its **Status**, Complete or Incomplete; select it to see which parts are
still missing. **History** opens the [Activity Audit](reports.md#activity-audit) of the shift:
who changed what (log in to see it).

Anyone can view timesheets without logging in.

## How a timesheet is filled out

The figures are entered hour by hour, never as shift totals. For each hour the timesheet shows:

- **Measured**: from automated time accounting, for shifts that ended within the last week.
  Older shifts have no measured hall or multiplicity values. See
  [Planned, measured and reported](concepts.md#planned-measured-and-reported).
- **Reported**: what was saved. Until an hour is saved, the Reported table shows the measured
  values, and its **Source** column says where each row's values come from:

  | Source | Meaning |
  |---|---|
  | DB | Saved. |
  | EPICS | Not saved: these are the measured values. |
  | NONE | Not saved, and nothing was measured. |

To change the hours:

- **Edit** under a table opens all of its rows; **Save** saves the whole table and **Cancel**
  discards your changes.
- The pencil on a row edits just that row, and the check mark saves it.

Enter values in the timesheet's units. In each hour the states must add up to exactly one hour
(60 minutes, 3600 seconds); a row that doesn't is red, and saving it fails with "Hour HH ...
must total 1 hour". The totals at the bottom update as you type.

An hour still shown with Source EPICS is not saved, even if its values are right: save it, so
it counts as reported. The [reports](reports.md) count only saved hours.

## Crew Chief timesheet

Crew Chiefs and Operability managers (the `cc` and `btm-admin` [roles](administration.md#roles))
can edit and sign it. Others see "You must authenticate as a Crew Chief or Operability Manager to
edit".

### Utilization and Availability

**Shift Summary** shows the shift's totals, with a Planned, Measured and Reported row each:

| Table | Columns |
|---|---|
| Accelerator Program | PHYSICS (Research + Tuning + Down); INTERNAL: STUDIES, NPES RESTORE, ACC, INTERNAL DOWN; OFF (SAM) |
| Hall Physics Progress, for each hall | UP, TUNE, BNR, PHYSICS DOWN, OFF |
| Multiplicity Physics Progress | FOUR UP, THREE UP, TWO UP, ONE UP, ANY UP, ALL ON UP, DOWN HARD (no Planned row) |

The hall columns name the experimenter state each corresponds to: UP {ABU}, TUNE {ABU}, BNR
{BANU}, PHYSICS DOWN {BNA}. Planned comes from the PD shift plan; it is empty when there is none.

**Hourly** has the hours behind the totals, in three sections, each with a Measured and a
Reported table: Accelerator Program, Halls Physics Progress (a Measured and Reported table for
each hall) and Multiplicity Physics Progress. Edit the Reported tables here.

The accelerator's measured hours come from the [State Timeline](state-timeline.md), with DTM's
downtime during Studies, NPES Restore and ACC counted as Internal Down; hours the timeline
doesn't cover are as BOOM measured them. The halls' and multiplicity's measured hours come from
BOOM. The multiplicity states overlap, so their hours needn't add up to one hour.

If the timeline is wrong, [correct it](state-timeline.md#correcting-the-timeline) before you
save the accelerator hours: a correction changes the measured values, not hours already saved.

### Shift Information

**Crew Chief**, **Operators**, **Program**, **Program Deputy** and **Comments**. Until saved, it
shows what BOOM has for the shift (Source EPICS), for the current, previous and next shifts.
**Edit** and **Save** it. When it was saved, **(View EPICS)** shows BOOM's values.

### Cross-checks

The timesheet compares the Crew Chief's figures with the halls' and with DTM. See
[Cross-checks](cross-checks.md).

### Signing

When the timesheet is right, select **Sign**. BTM then:

1. Asks you to confirm if the shift's last hour hasn't started yet ("is this the correct
   timesheet?"). Saving a part of the timesheet asks the same.
2. Runs the cross-checks again. If any fails, it asks whether to go on ("does the time accounting
   make sense?"), and then requires a saved **Crew Chief Comments** explaining the discrepancy.
3. **Saves every part not yet saved, with the values shown**: the accelerator, each hall, the
   multiplicity and the shift information. Unsaved hours are saved with their measured values,
   so check them before you sign. An hour with Source NONE has no values: fill it in first, or
   saving it fails.
4. Adds your signature: as Crew Chief, or as Operability Manager for an administrator.
5. Posts a "Crew Chief Timesheet Summary" entry to the ELOG logbook, tagged BeamAccounting, with
   the shift's totals and shift information. If the logbook can't be reached, the timesheet is
   still signed, without a warning: check that the entry is there.

The **Signatures** table lists who signed, as which role, and when. More than one person can
sign, but nobody twice in the same role. A signed Crew Chief timesheet can still be edited, and
the timesheet is Complete once signed.

## Experimenter timesheet

Each hall has its own timesheet. Any logged-in user can edit it, with
[limits once it is signed](#who-can-change-a-signed-timesheet).

### Utilization and Availability

- **Hall X Planned** explains the four plans (the NPES schedule, the MCC whiteboard, the PD shift
  plan and the Crew Chief's minute-by-minute plan). It has no figures: account against the Crew
  Chief's plan.
- **Hall X Measured**: the hall's automated time accounting.
- **Hall X Reported**: what you report. Each hour has two groups, each of which must add up to
  one hour:

  | Group | Columns |
  |---|---|
  | Accelerator Beam Time | ABU, BANU, BNA, OFF |
  | Experiment Beam Time | ER, PCC, UED, OFF |

  OFF is the same in both groups: enter it in the first, and the second follows. The **\*** by
  the table explains OFF.

Each hour also has a comment, of up to 128 characters.

**Edit All**, **Save** and **Cancel** edit the whole table; the pencil edits one row. **Edit CC
Hours** opens every hour except the last, the ones the Crew Chief needs (see below). **Reload
EPICS Measured** reloads the page, with the latest measured values. The page also reloads itself
a minute after each hour, unless you are editing.

### Reasons not Ready

Each hour's UED (Unplanned Experiment Down) must be explained by reasons, whose durations add up
to exactly the hour's UED. **UED Reason Discrepancies** lists the saved hours where they don't:
the UED, the reasons' total and the difference. **Add** a reason for one of those hours: choose
the **Hour**, the **Reason** (from the hall's list, kept in the database) and the
**Duration**, in the timesheet's units. The minus sign deletes a reason.

### Shift Information

**Leader**, **Workers**, **Program** and **Comments**. Program is required: choose it from the
hall's active [programs](schedule.md#programs), listed under General and Experiments.

### Signing

Before you can sign, save every hour, the shift information, and reasons for all the UED. Then
**Sign**: you sign as User, or as Operability Manager if you are an administrator. The timesheet
can have one signature of each. Signing posts an "Experimenter X Timesheet Summary" entry to the
hall's logbook (HALOG, HBLOG, HCLOG or HDLOG), tagged BeamAccounting; as for the Crew Chief, the
timesheet is signed even if the logbook can't be reached.

The status is Complete when the timesheet is signed and complete, and the previous shift's last
hour is saved too: the Crew Chief's shift needs it.

### Who can change a signed timesheet

| Signed by | Who can still change it |
|---|---|
| Nobody | Any logged-in user. |
| A User, up to 48 hours after the shift started | Any logged-in user. |
| A User, more than 48 hours after the shift started | The signer, the hall's manager or an administrator. |
| An Operability Manager | An administrator. |

Above the **Sign** button, the timesheet says why you can't change it. A refused save says
"Access Denied".

## What the Crew Chief needs from the halls

The Crew Chief's shift starts an hour before the halls', so a Crew Chief shift's
[cross-checks](cross-checks.md) use the halls' hours from two experimenter timesheets: the last
hour of the previous one, and all but the last hour of the current one. Only saved hall hours
count; an unsaved hour counts as zero.

So, towards the end of the shift:

1. The hall saves all but the last hour of its shift with **Edit CC Hours** and **Save**, and
   makes sure the previous shift's last hour is saved too. The status dialog's **Needed For Crew
   Chief Shift** part shows both.
2. The Crew Chief checks the cross-checks, and calls the hall about any discrepancy.
3. After its shift ends, the hall saves its last hour and signs.
