# State Timeline

[User Guide](README.md) › State Timeline

The **State Timeline** tab keeps the accelerator [program](concepts.md#modes-states-and-programs)
as a timeline: a list of intervals, each with a program and a start. Each interval runs until the
next one starts, so the timeline has no gaps or overlaps. The Crew Chief timesheet's
[measured](concepts.md#planned-measured-and-reported) accelerator hours are calculated from it.

Anyone can view the tab. Crew Chiefs and Operability managers (the `cc` and `btm-admin`
[roles](administration.md#roles)) can correct it.

## What the tab shows

**Current Program** shows the program now, in large text, with when it started and how many hours
ago: green for PHYSICS, yellow for STUDIES, NPES RESTORE and ACC, grey for OFF (SAM). An empty
timeline shows "Not Recorded".

Below it, a line says where the program changes come from:

- "Program changes are recorded from the PV *name*", with the PV's connection status and its
  latest value. The line is red when the PV is disconnected or its value isn't a program.
- "No value has been received yet from the program PV *name*", in red, until the PV first
  connects.
- "Program changes are not recorded from EPICS, since no program PV is configured; this timeline
  is kept by hand."

**History** lists the intervals, newest first, 25 to a page (**Previous** and **Next** page
through them):

| Column | Meaning |
|---|---|
| Program | PHYSICS, STUDIES, NPES RESTORE, ACC or OFF (SAM). |
| Start, End | When the interval started and ended; **Current** for the latest. Hover over a time to see its time zone. |
| Hours | The interval's length; the current interval counts up to now. |
| Recorded By | **EPICS** if BTM recorded it from the program PV; otherwise who entered or last changed it. Hover to see when. |

There is no Down program. Downtime is recorded in DTM, not on the timeline:
[timesheets](timesheets.md) count DTM downtime during Studies, NPES Restore and ACC as Internal
Down.

## How the timeline is recorded

BTM watches the program PV that BOOM sets, the one named by the server's `BTM_PROGRAM_PV`
[setting](administration.md#server-settings). When the PV's value changes, BTM starts a new
interval with that program, timed when the value arrived. It records only changes: a value equal
to the current program adds nothing, and nothing is recorded while the PV is disconnected.

| PV value | Program |
|---|---|
| `PHYSICS`, `NULL` | PHYSICS |
| `STUDIES`, `MD` | STUDIES |
| `RESTORE` | NPES RESTORE |
| `ACC` | ACC |
| `OFF` | OFF (SAM) |

Case and spaces don't matter. Any other value, such as `DOWN`, is ignored, and the program stays
what it was; the source line shows "(not a program, so it is ignored)".

A hand correction stands until the PV reports another program. One exception: when the app
restarts, it compares the PV's first value with the current program and, if they differ, records
the PV's program as of then. So a hand correction to the *current* program can be undone by a
restart; check the timeline after one.

## Correcting the timeline

If you are a Crew Chief or Operability manager, the tab has **Add**, **Edit**, **Remove** and
**Unselect** buttons; otherwise it says "You must authenticate as a Crew Chief or Operability
Manager to edit". Select an interval by clicking its row, and **Unselect** it to add one: **Add**
is disabled while a row is selected.

**Add** and **Edit** open a dialog with:

- **Program**: one of the five programs.
- **Start**: as `31-Jan-2026 13:45` or `31-Jan-2026 13:45:59`, in JLab's local time
  (America/New_York). Seconds are optional.

Adding an interval ends the one before it at the new start. Editing an interval can change its
program, or move its start between the starts of its neighbours, but not past them; to do that,
remove it and add it again. **Remove** asks first, then the interval before runs until the one
after.

The rules:

- A start can't be in the future, and two intervals can't start at the same time.
- You can't add an interval for a program that is already in effect at that time ("The program
  is already ... at ..."). Editing an interval to its neighbour's program merges the two.
- Neighbouring intervals with the same program are merged. Removing the only STUDIES interval
  between two PHYSICS intervals, for example, leaves one PHYSICS interval.

## Effect on timesheets

The **Measured** accelerator table of a Crew Chief timesheet takes each hour that the timeline
covers from the timeline:

- PHYSICS and OFF (SAM) time is counted as that program.
- STUDIES, NPES RESTORE and ACC time is counted as that program, except the time DTM has the
  machine down (CEBAF Blocked events), which is counted as **INTERNAL DOWN**.
- Values are rounded to hundredths of an hour (36 seconds), and each past hour still adds up to
  one hour; the current hour counts only up to now.

Hours the timeline doesn't fully cover, such as those before its first interval, are as BOOM
measured them. A note under the table says so.

Changing the timeline changes the measured values of the timesheets it overlaps, but not the
reported values already saved. To bring a saved timesheet into line, edit its reported values.
