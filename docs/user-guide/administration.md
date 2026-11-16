# Administration

[User Guide](README.md) › Administration

For BTM's administrators, the Operability managers.

## Roles

BTM's roles are kept in JLab's single sign-on (Keycloak). A change to someone's roles takes
effect when they next log in.

| Role | Name in BTM | Can |
|---|---|---|
| `btm-admin` | Operability Manager | Everything below, and the Setup tab. Signs timesheets as Operability Manager, and writes Reviewer Comments on cross-checks. |
| `cc` | Crew Chief | Edit and sign Crew Chief timesheets; correct the State Timeline; edit the programs; reset the EPICS connection on CA Status. |
| `schcom` | Committee Member | Create, upload, edit and publish schedules, including tentative ones; edit the programs. |
| `halead`, `hblead`, `hclead`, `hdlead` | Hall A, B, C, D Manager | Change their hall's experimenter timesheets after they are locked to their signer, 48 hours after the shift started. |
| (any logged-in user) | User | Edit and sign experimenter timesheets; see the Activity Audit; send feedback. |

Everyone can view the timesheets, State Timeline, published schedules, programs, metrics and
reports without logging in.

An administrator's signature locks an experimenter timesheet: then only administrators can change
it.

## Setup

The **Setup** tab, for administrators only, has three pages.

### Settings

The settings are kept in the database. Filter by key or tag, and edit a value. A change made on
the page takes effect at once; one made directly in the database takes effect after **Refresh
Cache** or a redeploy.

| Key | Meaning |
|---|---|
| `ADMIN_ROLE_NAME` | The administrators' role, `btm-admin`. The Help tab lists its members, and feedback goes to them. |
| `HELP_CONTENT_CONTACT`, `HELP_TECHNICAL_CONTACT` | The contacts the Help tab shows. |
| `HELP_DOC_CSV` | The Help tab's documentation links: a comma-separated list of `URL\|Label`. |
| `HALL_AVAIL_DOC_URL` | The Hall Availability report's **Explanation** link. |
| `MYQUERY_SERVER_URL` | The EPICS archiver web service the Charge report reads. |
| `NOTIFICATION_ENABLED`, `NOTIFICATION_MESSAGE`, `NOTIFICATION_LINK_NAME`, `NOTIFICATION_LINK_URL` | A banner across every page, such as for planned maintenance. |
| `EMAIL_ENABLED`, `EMAIL_TESTING_ENABLED`, `EMAIL_DOMAIN_NAME`, `EMAIL_SENDER_ADDRESS` | Email, such as the feedback form. In testing mode, all mail goes to the testers. |
| `IP_READ_FILTER_ENABLED`, `IP_READ_ALLOWLIST_PATTERN`, `IP_READ_URL_PATTERN` | Let computers at allowed addresses view pages that need a login. Takes effect after a redeploy. |
| `SMOOTHNESS_CDN_ENABLED`, `SMOOTHNESS_SERVER` | Where the pages' shared styles and scripts are loaded from. |

### Runs

A run is a stretch of days the accelerator is scheduled to be on: consecutive days whose program
is not OFF in each month's latest published [schedule](schedule.md). A day, or a month, with no
published schedule counts as OFF. The reports' date pickers offer the current and previous runs
as **Current Run** and **Previous Run**, and the Charge report opens on the current run.

BTM works the runs out again whenever a schedule is published and when the app starts; **Recalculate
from Schedules** does it now. The page shows the **Current Run** and **Previous Run**, and lists
every run:

| Column | Meaning |
|---|---|
| First Day, Last Day, Days | The run's days. |
| Source | **Schedule**, worked out from the published schedules, or **Set by hand**. |
| Published Schedules | What the published schedules have for the run: **Same**, other days, or **No run**. |

When the actual run differs from the schedule, such as a restore that started two days before the
NPES schedule had it, select the run and **Edit** its **First Day** and **Last Day**, or **Add** a
run. A run set by hand stays as it is when the schedules change, and takes the place of any
scheduled run that shares its days; two runs can't share a day. **Remove** a run set by hand to
go back to the schedule. A run from the schedules can't be removed, since recalculating would add
it again: edit it instead.

### Directory Cache

BTM caches users and roles from single sign-on; the cache is cleared every night. **Users** and
**Roles** show what is cached. Search for a user or role to add them to the cache, or **Clear**
it, such as after a change to someone's roles.

## Server settings

Some settings are the server's, set where BTM is deployed, not on the Setup tab:

| Setting | Meaning |
|---|---|
| `BTM_EPICS_ADDR_LIST` | Where BTM finds the EPICS PVs it measures from. |
| `BTM_PROGRAM_PV` | The PV the [State Timeline](state-timeline.md) is recorded from. If it is not set, the timeline is kept by hand. |
| `LOGBOOK_SERVER_URL`, `LOGBOOK_DEBUG` | The logbook server signed timesheets post to; with `LOGBOOK_DEBUG` on, entries go to the TLOG test logbook. |
| `JLAB_RUN_URL` | Where the date pickers find the current and previous runs. |

The PD shift plans and DTM's events are read from their own databases.

See the [README](https://github.com/JeffersonLab/btm#configure) for all of them.

## Reasons not ready

The reasons that experimenters choose to explain UED (Unplanned Experiment Down), on the
[experimenter timesheet](timesheets.md#reasons-not-ready), are kept per hall in the database; there
is no page for them. Inactive reasons are not offered.

## Help tab

The **Help** tab shows BTM's version and release date, the content and technical contacts, the
administrators (when logged in), the documentation links from `HELP_DOC_CSV`, and a feedback form
that emails the administrators.
