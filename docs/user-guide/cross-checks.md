# Cross-checks

[User Guide](README.md) › Cross-checks

The Crew Chief and the halls measure different things, so their figures can't simply be
compared. The Crew Chief [timesheet](timesheets.md#crew-chief-timesheet) compares them roughly,
and also compares its figures with DTM's downtime. A failed cross-check is a prompt to look
again and talk to the hall, not an error: the timesheet can still be saved and signed, with a
comment explaining it.

## Comparing the two perspectives

| Crew Chief | Hall |
|---|---|
| Monitors the beam type: CW (Up) or pulsed (Tune). | Doesn't distinguish. |
| Is concerned mostly with whether beam is present. | Also considers the target, data acquisition and beam quality. |

Roughly:

- ABU + BANU corresponds to Up + Tune + BNR.
- BANU is at least BNR: the Crew Chief's BNR is part of the hall's BANU.
- BNA corresponds to Down.
- The hall's Off corresponds to the Crew Chief's Off.

They don't match exactly. A hall can reject beam for its quality, so ABU + BANU can be less than
Up + Tune + BNR and BNA more than Down. Crew Chiefs used to record BANU themselves, but couldn't
tell whether the hall was really using the beam, so they now record BNR. Hall B, for example, can
switch from ABU to BANU without stopping the beam or telling MCC: at low current it can turn off
its data acquisition for housekeeping while beam still runs.

## What is compared

The Crew Chief's side uses the timesheet's reported values, with the measured values for hours
not yet saved. The hall's side uses only the hall's saved hours in the Crew Chief's shift: the
last hour of its previous shift and all but the last hour of its current one. An unsaved hall
hour counts as zero, so ask the halls to save those hours first (see
[What the Crew Chief needs from the halls](timesheets.md#what-the-crew-chief-needs-from-the-halls)).

Most checks allow 10 minutes' slack, so that a few minutes' difference in how the figures were
recorded doesn't fail them.

## The cross-check panel

The panel on the Crew Chief timesheet has three tabs.

### Shift Summary

**BTM vs DTM** compares the timesheet with DTM's downtime in the shift:

| Figure | Meaning |
|---|---|
| BTM PHYSICS, INTERNAL DOWN | From the timesheet. |
| DTM BLOCKED | DTM's CEBAF Blocked downtime (linked to DTM). |
| DTM TUNING | DTM's CEBAF Tuning downtime (linked to DTM). |
| PHYSICS DOWN | Computed: BLOCKED − INTERNAL DOWN. |
| RESEARCH | Computed: PHYSICS − TUNING − PHYSICS DOWN. |

and checks:

| Check | Passes when |
|---|---|
| Low Program Check | PHYSICS + INTERNAL DOWN ≥ BLOCKED − 10 min: DTM doesn't have the machine down for longer than BTM had it scheduled to run. |
| High Tuning Check | PHYSICS ≥ TUNING − 10 min. |

**Hall Time Accounting** has a row for each hall:

- **CC Shift Hours Status**: whether the hall's hours needed for this shift are saved (the
  Experimenter cell links to the hall's timesheet), and whether the Crew Chief's are.
- The hall's ABU, BANU, BNA and OFF, and the Crew Chief's UP, TUNE, BNR, DOWN and OFF for it.
- **Cross Check Status**: ✔ if all of the hall's checks below pass, X if any fails. Select an X
  to see why.

**Crew Chief Comments** explain discrepancies; signing a timesheet whose cross-checks fail
requires one. **Reviewer Comments** are for administrators reviewing the timesheet later; a Crew Chief can open
them for editing, but only an administrator can save them.

The panel shows when it was calculated; **Reload** recalculates it, such as after a hall saved
its hours.

### Details

The shift's checks for each hall, with a message for each that fails:

| Check | Passes when |
|---|---|
| **Beam Mode**: Low Physics / High UP + TUNE + BNR + DOWN | The hall's UP + TUNE + BNR + DOWN ≤ the accelerator's PHYSICS + 10 min. |
| **Accelerator**: High ABU / Low PHYSICS + RESTORE + STUDIES | ABU ≤ PHYSICS + NPES RESTORE + STUDIES + 10 min. |
| **Accelerator**: High BANU / Low PHYSICS | BANU ≤ PHYSICS + 10 min. |
| **Accelerator**: High BNA / Low PHYSICS | BNA ≤ PHYSICS + 10 min. |
| **Hall**: High ABU / Low UP | (ABU + BANU) / 2 ≤ UP + TUNE + BNR, or UP ≤ 30 min: the hall didn't use much more beam than it got. |
| **Hall**: Low ABU / High UP | ABU + BANU ≥ (UP + TUNE + BNR) / 2, or UP ≤ 30 min: the hall didn't use much less beam than it got. |
| **Hall**: Low BANU | BANU + 10 min ≥ BNR. |
| **Hall**: High BNA / Low DOWN + UP + TUNE | BNA ≤ DOWN + UP + TUNE + 10 min. |
| **Hall**: High EXP OFF / Low OPS OFF | The hall's OFF ≤ the Crew Chief's OFF + 10 min. |
| **Multiplicity**: High UP / Low ANY UP | The hall's UP ≤ ANY UP + 10 min. |

The first two hall checks are skipped when the hall had no more than half an hour of UP. The
tooltips of some checks describe them more loosely than this table; the table is what BTM
calculates.

### Hourly

The same checks hour by hour, with each failure's message in minutes and prefixed by its hour,
and a **BTM vs DTM** downtime check for each hour. Use it to find the hour a shift-level
discrepancy comes from.

## Resolving a discrepancy

1. Check that the hall has saved the hours the Crew Chief's shift needs (CC Shift Hours Status).
2. Find the hour on the **Hourly** tab, and compare it with what happened.
3. Correct whichever side is wrong. For example, during a Møller measurement the hall may record
   more ABU than the Crew Chief's Up and Tune; raise Up or Tune to match. See
   [Common issues](common-issues.md).
4. If the difference is real, explain it in **Crew Chief Comments** before signing.
