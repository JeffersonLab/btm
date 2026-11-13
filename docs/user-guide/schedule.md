# Schedule and programs

[User Guide](README.md) › Schedule and programs

The **Schedule** tab holds the NPES schedule: for each day of a month, the accelerator program
and each hall's program. The **Programs** tab lists the hall programs that the schedule and the
experimenter timesheets choose from.

## The schedule

Anyone can view published schedules. Members of the scheduling committee (the `schcom` role)
and administrators (`btm-admin`) can also see tentative versions, and create, edit and publish
them.

### Months and versions

Each month's schedule has numbered versions. A version is **tentative** while the committee works
on it, and **published** once it is final; a published version can't be changed.

The tab opens on the current month's latest published version, or, if none is published yet, its
highest-numbered tentative version. **Previous** and **Next** go to the neighbouring months. To
see another month or version, open the filter, choose a **Date** and a **Version #**, and
**Apply**. The version list says which are published, and when.

A month's address is `/btm/schedule/<month>-<year>`, such as `/btm/schedule/january-2026`, and
`/btm/schedule/january-2026/2` is its version 2.

### Calendar and table

The **Calendar** view, the default, shows the month as a grid of weeks, with a row for the
accelerator (**Acc.**) and one for each hall:

- Each hall's program for the day, linked to the program's web page where it has one; the
  polarized beam icon; and the energy (GeV), current (μA) and passes when the hall has an energy.
- The shaded hall is the day's top priority; the **Priority** key explains it.
- Today is circled in red.

The **Table** view shows a row for each day:

| Column | Meaning |
|---|---|
| Date | The day. |
| Accelerator: Program | PHYSICS, FACDEV, STUDIES, RESTORE, ACC, DOWN, OFF or TBD. |
| Accelerator: GeV / Pass | The energy gain per pass. |
| Accelerator: Min Hall Count | The least number of halls (1 to 4) that must take beam. |
| Hall: Program / Experiment | Each hall's program, from the [Programs](#programs) tab. |
| Hall: GeV, μA | Each hall's energy and current. |
| Hall: Polarized | ✔ for polarized beam. |
| Hall: Passes | 1 to 5 for Halls A, B and C; 0.5 or 5.5 for Hall D. |
| Priority | The halls in order of priority, such as `A,C,B`; only halls with an energy are listed. |
| Hall: Notes, General Notes | Free text, up to 256 characters each. |

**Full Screen** shows the schedule on its own, such as for a meeting room display.

### Creating and editing a version

If a month has no schedule, committee members see **Create** and **Upload** in its place:

- **Create** makes an empty tentative version, for you to fill in.
- **Upload** makes versions from an Excel workbook; see [Uploading from Excel](#uploading-from-excel).
  It is also under every schedule, to add versions to months that have one.

Others see "-- Committee Member Login Required for Tentative Schedule --" for a month that has
only tentative versions.

To edit a tentative version, switch to the **Table** view and click a day's row. The row becomes
a form and a toolbar slides out:

- **Save** saves the row, and **Cancel** discards your changes.
- **Copy** keeps the row's values, and **Paste** writes them to the selected day and the days
  after it: **X** sets how many days, up to the end of the month. **Clipboard** shows what was
  copied. The copy is kept in your browser, so it can be pasted into another month or version.

Every day needs an accelerator program and a program for each hall. The accelerator's GeV / Pass
must be between 0 and 3, a hall's GeV between 0 and 13 and its μA between 0 and 500, or empty.
When Hall D is at 5.5 passes and Halls A, B and C all have passes, at least one of them must be at 5.

The **Clipboard** dialog's **Clear** link clears everything BTM keeps in your browser, not only
the schedule clipboard.

### Publishing

When the version is final, select **Publish** and confirm. A published version can't be changed:
to change a published month, select **New Version**, which copies the version you are viewing
into a new tentative version, numbered after the month's latest. Edit it and publish it in turn.

A newer tentative version doesn't replace the published one as the version everyone sees until
it is published.

### Uploading from Excel

**Upload** reads a workbook with a single sheet, whose first two rows are headings and whose
other rows are days, from the first of the month:

| Column | Content |
|---|---|
| A | The date, as a date cell. |
| B | The weekday (ignored). |
| C | The accelerator's GeV per pass. |
| D | The accelerator program: PHYSICS, STUDIES, RESTORE, ACC, DOWN or OFF. Empty means OFF. |
| E, G, I, K | Hall A, B, C and D's program, by its name on the Programs tab. Empty means the hall's program named OFF, which must exist. |
| F, H, J, L | Hall A, B, C and D's beam, as `GeV/current/pol/MHz`, such as `10.6/50/p/249`. The current is in μA for Halls A and C and in nA for Halls B and D; pol is `p` for polarized or `-`; the frequency must be a whole number, and is not kept. |
| M | The priority, such as `A,C,B`. |
| N | The passes, as `a/b/c/d`. |

Each month in the workbook becomes a new tentative version of that month, so one workbook can
hold several months. Notes and Min Hall Count are not read. The upload can't schedule FACDEV or
TBD; set those days in the Table view afterwards.

### Exporting

**HTML** and **Excel** download the version you are viewing, as `schedule.html` or
`schedule.xlsx`. The Excel export's layout is not the upload's, so an exported workbook can't be
uploaded again as it is.

The exports work for tentative versions too, for anyone who has the link.

### Where the schedule is used

- The [Beam Summary](reports.md#beam-summary) report's **NPES Sched. Hrs** column
  counts the scheduled hours. Each scheduled day counts as its real length: 24 hours, but 23 on
  the day daylight saving time starts and 25 on the day it ends.
- Other applications read it through BTM's [web service](#web-service).

For a month with nothing published, both use its latest tentative version.

### Web service

Other applications can read the schedule as JSON, without logging in:

| Request | Returns |
|---|---|
| `/btm/rest/schedule?start=2026-01-01&end=2026-01-31` | Each scheduled day from `start` to `end` (inclusive): its accelerator program and each hall's program. |
| `/btm/rest/scheduled-count?start=2026-01-01&end=2026-02-01` | The number of days from `start` to `end` (exclusive) whose program is not OFF. |
| `/btm/rest/monthly-schedule?month=january-2026` | The month's versions, with each one's published date (empty if tentative). |
| `/btm/rest/runs` | The current and previous runs' start and end dates, from the schedule. |
| `/btm/rest/shift-summary?shift-start=2026-01-12T07` | A Crew Chief shift's saved totals: its shift information, the accelerator's hours, and each hall's Crew Chief and experimenter hours. The start hour is 23, 7 or 15. |
| `/btm/rest/week-summary?week-start=2026-01-07T07` | The same totals for the week starting at that Crew Chief shift. |

## Programs

A hall program is what a hall is scheduled to do: an experiment, such as E12-06-102, or a task,
such as commissioning or maintenance. Each hall has a program named OFF for days without beam.

The **Programs** tab has a tab for each hall, listing its programs:

| Column | Meaning |
|---|---|
| Name | The program's name, unique within the hall. |
| Alias | A second, often shorter, name. Lists show the program as "Name (Alias)". |
| URL | A web page about the program; the schedule links to it. |
| Experiment | Yes for an experiment. Experimenter timesheets list experiments apart from the other programs. |
| Active | Only active programs can be chosen on the schedule and on timesheets. |

**Hide Inactive**, on by default, hides the inactive programs. The **Accelerator** tab is a fixed
glossary of the accelerator programs and states.

Crew Chiefs (`cc`), committee members (`schcom`) and administrators (`btm-admin`) can **Add**,
**Edit** and **Remove** programs. A program that a timesheet or a schedule uses can't be
removed: make it inactive instead. **Remove** doesn't ask for confirmation.

When an experiment ends, set its **Active** to No: it stays on the schedules and timesheets that
name it, and leaves the lists. A day whose hall program has been made inactive must be given an
active program before that row can be saved again.
