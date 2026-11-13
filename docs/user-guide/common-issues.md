# Common issues

[User Guide](README.md) › Common issues

Situations that come up again and again in time accounting, and how to record them.

## Bonus ABU

A hall sometimes records more ABU than the Crew Chief's Up and Tune, or records ABU during beam
studies. It looks like a gift, but it complicates the numbers. The
[cross-checks](cross-checks.md) are lenient about it; make sure it really is bonus beam and not a
data error.

## Opportunistic beam studies during a long downtime

When a major failure causes a long downtime, the machine is often kept busy with an alternative
program. That's good, but keep recording Down: hall Down (BNA) if the program is Physics, or
Internal Down if it is Internal. If the program really is changed, then record the downtime in
DTM as Non-Program. Otherwise BTM and DTM disagree: downtime in DTM while BTM says the machine was
Off, for example, breaks the availability figures.

## Whose downtime is it?

The choice between BANU and BNA depends on where the failed component is. Who maintains it, owns
it or paid for it doesn't matter: a component located in the hall makes BANU, and one located in
the accelerator makes BNA. As in DTM, a failure that doesn't stop the program isn't a blocking
DTM event; keep recording Up and ABU. DTM can still record it as non-program downtime.

## Do trips count as down?

Yes: in total they add up to a lot of time. BOOM switches to Down on a trip, even when the Crew
Chief has forced a mode, and DTM records each trip's duration too. Some reports, such as DTM's
Joule report, leave trips out.

## Restore: BTM, DTM and after studies

BTM's **NPES Restore** is the initial restore at the start of a run, as scheduled by the NPES.
DTM's **restore** is the recovery after a component failure. Restoring after beam studies is not
a state of its own: it is still Studies. So if the PD's plan says beam studies until 5 and
restore to physics until 6, record Studies until 6.

## Hall tuning, accelerator tuning and perfection tuning

**Accelerator tuning** sets up the accelerator, up to but not into a hall, to the program's
requirements. **Hall tuning** sets up the hall's own equipment.

| | Crew Chief | Hall |
|---|---|---|
| Accelerator tuning | Down, or Internal Down in Internal mode | BNA if the hall expects beam, otherwise Off |
| Hall tuning | Up or Tune (tuning can use CW or pulsed beam) | ABU |

A hall that receives beam meeting the agreed program requirements may still ask for better beam.
The PD then decides between quantity and quality, which is harder when the other halls are happy
with the beam. The Director of Operations has the final say.

## Time-restricted states

NPES Restore and ACC each have an allowance (see
[Restore, ACC and their allowances](concepts.md#restore-acc-and-their-allowances)). It's hard to
remember the allowances and to switch to Internal Down once one is used up. An auditor can look
at the data afterwards for Restore or ACC time beyond the allowance.

## Which plan decides?

Many states depend on the plan: a hall records BNA when it expects beam and doesn't get it. But
who decides whether the hall should expect it?

- The plans are coarse. The NPES schedule, the whiteboard and the PD shift plan say how many
  hours a hall gets in a shift, not which ones. The PD, the Crew Chief and the hall may need to
  talk it through.
- The plan changes quickly. If the whiteboard gives Hall A beam tomorrow, but a failure in the
  accelerator today means it won't get it, does Hall A record Down or Off tomorrow?

BOOM's rule is that Off must be decided at least 8 hours in advance. As with opportunistic beam
studies, changing the plan can hide downtime. Generally record Down, and keep the machine busy
with opportunistic studies if possible (not recorded as scheduled studies). If the program really
is changed to scheduled studies, stop recording the downtime in DTM.

## Møller measurements

The Crew Chief records Up or Tune; the hall records ABU. The hall may record more ABU than the
Crew Chief's Up and Tune, since setting up takes time and trips may not show on the operations
side while the hall records ABU for the whole interval. The Crew Chief resolves it by raising Up
or Tune to match the ABU.

## BNA during Internal mode

During a scheduled Internal program, such as NPES Restore or Studies, the halls are not scheduled
for physics beam: they record Off, not BNA.

## Trusting the automated accounting

The measured values are a starting point. If your hall received beam and used it in any way,
such as for a Møller measurement, a target position scan or a current calibration, that time is
ABU, and you will probably have to enter it yourself: the automated accounting usually gets
procedures wrong. If your hall is scheduled to be off, the Crew Chief may force the hall's Off
mode in BOOM, which in effect turns the automated accounting off.

## Known difficulties

- **The cross-checks are complex.** The Crew Chief and the halls don't measure the same things,
  so the figures can't simply be compared.
- **End-of-shift cross-checks come late**, as the Crew Chief is trying to leave. Comparing hour by
  hour during the shift avoids that, at the cost of more calls between the Crew Chief and the
  halls; the timesheets support it.
- **Checking beam quality may need disruptive diagnostics.**
- **Not all ABU is equal:** some programs are harder to deliver than others, and the figures
  don't weight them.
