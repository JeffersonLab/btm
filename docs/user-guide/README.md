# Beam Time Manager User Guide

Beam Time Manager (BTM) accounts for how Jefferson Lab's accelerator and experimental halls spend
their time, hour by hour. At the end of each shift the Crew Chief and each hall fill out a
**timesheet**, started from automated measurements; BTM **cross-checks** the two against each
other and against Downtime Manager (DTM); and the **reports** turn the saved timesheets into the
beam time, availability and schedule figures that lab management reviews. BTM also keeps the
accelerator's program as a **State Timeline**, and the NPES **schedule**.

At JLab, BTM is at [ace.jlab.org/btm](https://ace.jlab.org/btm). You can view it without logging
in; log in to make changes.

The [guide's website](https://jeffersonlab.github.io/btm/) has a copy for each release; pick the
version the Help tab shows. On GitHub, this guide describes the `main` branch, which may be newer
than the version you are using.

## Contents

1. [Concepts](concepts.md): modes, programs and states, the schedules, planned, measured and
   reported values, the derived metrics, and roles.
2. [Timesheets](timesheets.md): filling out, saving and signing the Crew Chief and experimenter
   timesheets. For Crew Chiefs and the halls.
3. [Cross-checks](cross-checks.md): how the Crew Chief's figures are compared with the halls' and
   with DTM, and resolving discrepancies.
4. [State Timeline](state-timeline.md): the accelerator program over time, and correcting it.
5. [Schedule and programs](schedule.md): the NPES schedule's versions, editing and publishing
   it, and the hall programs. For the scheduling committee.
6. [Reports](reports.md): the Reports tab, and how it counts.
7. [Administration](administration.md): roles, the Setup tab and settings. For administrators.
8. [Common issues](common-issues.md): situations that come up again and again, and how to record
   them.

## Tabs at a glance

| Tab | What it is for |
|---|---|
| **State Timeline** | The accelerator program now and in the past; Crew Chiefs correct it here. |
| **Timesheets** | The Crew Chief and experimenter timesheets, shift by shift. |
| **Schedule** | The NPES schedule, month by month. |
| **Programs** | Each hall's programs: experiments and other tasks. |
| **Metrics** | A glossary of the hall, multiplicity and experimenter states. |
| **Reports** | Beam time, physics, availability and charge reports, the review calendar, the activity audit and the EPICS connection status. |
| **Setup** | Administrators only: settings and the user directory cache. |
| **Help** | Version, contacts, documentation and a feedback form. |

## Quick start for Crew Chiefs

1. During the shift, check that the [State Timeline](state-timeline.md) shows the right program,
   and correct it if not.
2. Before the end of the shift, ask each hall to save all but the last hour of its timesheet.
3. At the end of the shift, open **Timesheets**: during the first hour of the next shift it opens
   the one that just ended.
4. On the **Hourly** tab, check the Reported hours of the accelerator, each hall and the
   multiplicity; edit the wrong ones, and save. Fill out the **Shift Information**.
5. Check the [cross-checks](cross-checks.md). Resolve discrepancies with the halls, or explain
   them in **Crew Chief Comments**.
6. **Sign**. BTM saves whatever is still unsaved and posts a summary to the logbook.

## Quick start for the halls

1. Open **Timesheets**, choose your hall's experimenter timesheet, or bookmark it from **Current
   Shift (bookmarkable)**.
2. Before the end of the shift, use **Edit CC Hours** to check and save all but the last hour, for
   the Crew Chief.
3. After the shift ends, check and save every hour: ABU, BANU, BNA and OFF, and ER, PCC, UED and
   OFF, each adding up to one hour.
4. Explain any UED with **Reasons not Ready**, fill out the **Shift Information**, and **Sign**.

See [Timesheets](timesheets.md) for the details.

## Questions and changes

Use the feedback form on the Help tab, or contact the content and technical contacts listed
there. To suggest a change to this guide, open an issue or pull request on
[GitHub](https://github.com/JeffersonLab/btm).

This guide replaces the "Machine Time Accounting User Guide" (version 13, August 2025); its
definitions and common issues are included here.
