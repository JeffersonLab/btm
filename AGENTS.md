# Agent guidelines

Instructions for AI coding agents working on Beam Time Manager (BTM), a Jakarta EE 10
application. See [README.md](README.md) for setup and architecture. Based on the
[coding-agents starter](https://code.jlab.org/acc/iac/docs/coding-agents/-/blob/main/agent-instructions/AGENTS.md).

## Development

- Build with Java 21 and the Gradle wrapper: `./gradlew build`, matching
  [.github/workflows/ci.yaml](.github/workflows/ci.yaml). Java compiles to Java 17 bytecode;
  the WAR is `build/libs/btm.war`.
- For Java or build changes, run `./gradlew build` before opening a PR. Spotless uses Google
  Java Format; use `./gradlew spotlessApply` for formatting and review the diff. There is
  a JUnit 5 suite under `src/test/java`; run it with `./gradlew test`. Tests run in
  `America/New_York` and require no application server, database or EPICS services.
  The database integration suite under `src/integrationTest/java` runs with
  `./gradlew integrationTest` and is also part of `build`/`check`. It requires Docker,
  starts its own disposable Oracle XE with random ports, and uses the project's demo DDL.
  For rootless Docker set `DOCKER_HOST=unix://$XDG_RUNTIME_DIR/docker.sock`. It tests service
  queries and persistence, not WildFly EJB security/JTA or HTTP. See README's Test section.
- For documentation-only changes, check links, examples and `git diff --check`.
- Demo setup and local WildFly development are documented in
  [README.md](README.md#develop). [deps.yaml](deps.yaml) provides Oracle, Keycloak, Puppet,
  MailHog and a soft IOC. [build.yaml](build.yaml) builds the app from this worktree;
  the default [compose.override.yaml](compose.override.yaml) uses a published image.
- Before starting services, check existing listeners and containers. The Compose files use
  fixed container names and host ports: app 8080/8443/9990, Oracle 1521/5500, Keycloak
  8081/9991, Puppet 3000, MailHog 1025/8025 and EPICS 5064/5065 (TCP and UDP). A Compose
  project name alone does not isolate them. Use a worktree-specific override for names,
  ports and affected URLs, and record ownership and connection settings before startup.
  Use the same project name, files and settings for tests and cleanup.
- Demo schema and seed data are in [container/oracle/initdb.d](container/oracle/initdb.d);
  Keycloak and EPICS fixtures are under `container/keycloak/initdb.d` and
  `container/softioc/db`. Reset only your own disposable services; never reset another
  worktree's or shared resources. Use synthetic data only.
- Keep development on the demo services. The Dockerfile defaults to a real JLab logbook URL;
  do not send development requests to live logbooks, EPICS channels, databases or other JLab
  systems. Use an isolated stand-in for integrations not covered by the demo stack.
- For UI changes, build and deploy the changed WAR to local WildFly or build the app image
  with `build.yaml`, then check the affected pages at `/btm` (default app port 8080).
  The default demo login is documented in [README.md](README.md#quick-start-with-compose).
- Runtime settings are documented in [README.md](README.md#configure); app-server setup
  is in `container/app/app-setup.env` and the Dockerfile. Keep local overrides and
  credentials out of git. Preserve the Smoothness web library in the WAR as required by
  [build.gradle](build.gradle).

<!-- BEGIN coding-agents shared rules 7.0.0 (generated: edit in acc/iac/docs/coding-agents) -->

## Shared rules

These rules are the same in every project that uses them. Where this file's other sections say
otherwise, follow those. On GitHub, "merge request" means "pull request", and "pipeline" means
the pull request's required checks.

### Checking your work

- Run the Development section's checks before you call a change done, fix what fails, and
  report what you ran and what it returned.
- Verify each change in proportion to its behavior and regression risk: add or update tests
  where the risk warrants it; meaningful existing coverage can suffice. For a bug fix, show the
  regression test failing without the fix and passing with it when practical, or explain why
  not. For documentation or formatting, check links, examples or formatting. Never skip or
  weaken a check merely to make it pass.
- Where tests are thin, characterization tests can record a baseline: existing behavior, not
  necessarily correct behavior. Put them in a test-only merge request when risk or review needs
  warrant it.
- Never use production data, and never commit secrets.

### Task authorization

- Use the request and authorization already given in the session; do not ask again for an
  authorized action. "Look around and suggest improvements" means investigate, run local
  checks and report, not implement or publish. "Fix this bug" authorizes the local changes and
  verification needed to finish it; ask before a material expansion of scope.
- Commit and push only when asked. "Create a merge request for issue #14" also authorizes
  implementing, verifying, committing, pushing the branch and opening the merge request, and a
  request to address its review authorizes the follow-up commits and pushes.
- A requested review authorizes checks and posting findings as comments; commit and push fixes
  only when asked.
- Releases, deployments, production data changes and settings changes in external systems need
  authorization for the action and the target environment; a request to implement a change or
  open a merge request does not give it. Never approve, merge, or enable auto-merge.
- Ask only when a consequential ambiguity remains; continue independent work meanwhile.

### Commits

- Commit as the agent's bot, never as a person: the repository-local `user.name` and
  `user.email` (never `--global`) must be the bot's whose token you push with, so the Git host
  links the commits to it and a squash commit shows the same name. In an agent VM the clone
  helpers set them; on GitLab, `glab api user` shows the bot's `name` and `username`, and its
  address is `<username>@noreply.<host>`.
- Imperative summary line, then a body explaining what changed and why. Name the issue the
  change is for: `Fixes #<issue>`, or `Part of #<issue>` if some of it stays open; work you were
  asked to do needs no issue.
- Trailers come last, in one paragraph with no blank line among them, since git reads trailers
  only from the last paragraph: the release trailers this file asks for, if any (such as
  `Changelog:`), then attribution. Every agent-written commit, including follow-ups and a
  reviewer's, names the agent and model in a trailer: keep the one your tool adds (such as
  `Co-Authored-By`), or add `Assisted-by: <agent>:<model>`. An agent VM's commit hook refuses a
  message that breaks either rule; elsewhere, check with
  `git log -1 --format=%B | git interpret-trailers --parse`.
- The squash commit message template uses the first commit's message: write it for `main`, put
  release trailers on it only, and keep it current for the whole change, adding any distinct
  agent/model attribution from later commits. Later commits omit release trailers but keep
  attribution.

### Branches and merge requests

- Start each task on a new branch from an up-to-date `main`, in your own clone or worktree, and
  target `main`. To build on a merge request still in review, branch from its branch, target it,
  and say so in the description; once it merges, check that yours now targets `main` (GitHub
  retargets it only if the merged branch was deleted).
- Label every merge request `source::ai`, make the person who reviews it both reviewer and
  assignee (ask for their username if you don't know it), set the active milestone where the
  project uses one, and delete the source branch on merge. An agent VM's `glab` wrapper adds
  these when you leave them out.
- Describe what changed, any deployment steps, and the checks you ran and could not run, in the
  project's merge request template where it has one. Add a short Decisions part: what the person
  questioned or changed during the session, which alternatives were dropped and why, and any
  second opinion that changed something.
- End every agent-prepared merge request, pull request or issue description with a plain-text
  `Prepared-by: <vendor> <agent>:<model identifier>` footer, such as
  `Prepared-by: OpenAI Codex:GPT-6`: the specific model identifier when known, otherwise the
  known model family, and `unavailable` only when neither is known; never guess. Preserve
  existing credits, including a tool's equivalent footer; on a substantive update, add yours if
  that vendor, agent and model are not credited yet; routine edits need none. Keep all
  attribution at the end, and in a release merge request under its own heading after "Release
  notes".
- After pushing, wait for your last commit's pipeline to finish before reporting the merge
  request ready: read a failed job's log and fix the cause, never skip or weaken a check, and
  report how it ended with its link, saying if it failed for a reason outside your change. On
  GitLab, `glab mr view <number> -F json | jq .head_pipeline` shows it; until its `sha` is the
  commit you pushed, your push's pipeline has not started.
- Before pushing to a merge request's branch, check that it is still open: commits pushed after
  it merged never reach `main`.
- For another GitLab project's issue or merge request, write `project#123` or `project!456` with
  no space when it resolves from the current context, otherwise the full namespace path or URL,
  and check the rendered link; `project #123` links the bare number to the current project.

### Issues

- For something outside your task (another project, code another agent is working on, a change
  that needs a decision), don't fix it in passing: offer to open an issue with what you found,
  the evidence and a suggested fix. File issues as the bot, labeled `source::ai` and assigned to
  the person you work with, with the project's labels for its kind where it has them: one
  `type::` label and, for a bug, one `bug-origin::` label (on GitLab,
  `glab api 'projects/:id/labels?include_ancestor_groups=true'` lists them); `effort::` only as
  an estimate, and `priority::` left to the person.
- When asked to plan work for other agents: check claims before filing, list the draft issues
  before filing them, make each self-contained with "Done when" and what not to do, say which can
  start now and which are blocked or share files, and ask before filing anything
  security-sensitive (file it confidential).

### Review

- When asked to address a review, reply in each thread with what you changed, and push content
  fixes as new commits; the reviewer resolves the threads. Only the author may reword the first
  commit's message, for its description, release metadata or attribution, following the Git
  workflow guide's "Responding to review": save the remote tip, keep the base, patches and
  order, check `git range-diff` and an empty `git diff`, push with an explicit
  `--force-with-lease`, and report the old and new tips.
- When asked to review a merge request, comment by default, one thread per finding; commit and
  push fixes only when asked, coordinating with the author if they are still working on the
  branch. Add one commit per recommendation on top of the author's, never rewriting theirs, with
  your attribution and no release trailers, then post one comment listing each commit, what you
  tested, that the author must pull, and any first-message update needed. Never approve, merge,
  or resolve threads.
- When the person corrects you on something any agent here should know, propose adding it to
  this file's project rules, or better a check that catches it. Keep one developer's
  preferences out of it.

<!-- END coding-agents shared rules -->

## Project rules

- Changelog eligibility/types and Breaking trailers: unused; GitHub generates release notes.
- Milestones and issue status labels: unused. Queue: report opened PRs/issues for maintainer
  triage; the maintainer manages any organization project.
- Release trigger: changing `VERSION` invokes [.github/workflows/cd.yaml](.github/workflows/cd.yaml),
  publishing a GitHub release with the WAR, publishing the demo Docker image and deploying
  to JLab acctest. Release-only change: `VERSION` only. Change `VERSION` only when asked
  to release; a request to implement a change or open a PR does not authorize a release.
- Release notes: PR description for the maintainer to add to GitHub's generated notes;
  describe manual upgrade steps there. Follow Semantic Versioning as documented in
  [README.md](README.md#release): major for incompatible changes, minor for compatible
  features, patch for fixes.
