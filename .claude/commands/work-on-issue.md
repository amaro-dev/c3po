# Command: Work on GitHub Issue

Executes the full development workflow for a GitHub issue by creating a feature branch, planning, implementing, and
submitting a pull request — while also interacting with GitHub Projects to track task status.

---

# Preparation

This command receives **exactly one argument**: the issue number of the task to work on.

Before executing any steps:

1. **Validate that the working directory is clean**:
    - No unstaged or uncommitted changes.
    - If this check fails, stop and return an error message.

2. **Ensure Git setup is ready**:
    - Confirm that the current branch is `main`.
    - Pull the latest changes from the remote using `git pull`.
    - If any of these steps fail, stop and return an appropriate error message.

3. **Initialize project context**:
    - Run the `primer` command to gain project context before continuing.
    - It is imperative that you get some project context prior to executing this task
    - If this step fails, stop and return an error.

---

# Validation

- Check that **only one argument** was passed.
- If **no** argument or **more than one** is provided, return an error and stop.

---

# Execution

1. **Create a feature branch** using the format:
   `git checkout -b feature/$ARGUMENT`
2. **Read the issue details** from GitHub using the Model Context Protocol (MCP) or the GitHub API:

- Extract the title and description for context.
- Summarize it in your internal memory to plan the implementation.
- If the Issue provides an execution plan or tasks that needs to be accomplished, you can you use them to prepare your
  plan

3. **Update GitHub Projects status** to reflect work in progress:

- Locate the GitHub Project where this issue is tracked (via MCP or GitHub GraphQL API).
- Identify the `Status`, `Workflow`, or equivalent project field.
- Set its value to `In Progress` using one of the following:
    - `updateProjectV2ItemFieldValue` mutation (GraphQL), OR
    - The appropriate MCP instruction.
- **Do not comment or update the issue body** to indicate status.
- If the update fails, stop and return a descriptive error.

4. **Plan your work**:

- Break the implementation into subtasks or steps.
- Present your plan to the user for validation.
- If the work is too big try to set some milestones that can be use as checkpoints to validate and save the work so far
  upon user validation
- Only proceed after user approval.

5. **Implement the solution** based on the approved plan:

- Code directly in the `feature/$ARGUMENT` branch.
- Run tests or validations to ensure correctness.

6. **Gather evidence** that the work satisfies the issue requirements:

- Logs, test results, screenshots (if applicable), or other forms of validation.
- Present the solution and evidences to the user so he can approve

7. **Commit your work**:

- NEVER commit anything without user approval
- Use a descriptive commit message explaining how the change solves the issue.
- Ensure commits are logically grouped and clean.

---

# Finalization

1. **Push the feature branch** to the remote repository.

2. **Create a Pull Request**:

- **Title**: clear and concise summary of the change.
- **Description** must include:
    - A summary of what was done and why.
    - Evidence of correctness (e.g., test results, screenshots).
    - The tag: `[closes #$ARGUMENT]` to auto-close the issue upon PR merge.
    - Link the PR to the appropriate GitHub Project or assign to the `C3po` project.

3. **Update the issue status again**:

- Move the GitHub Project status from `In Progress` to `Waiting Approval`.
- Use the same method as antes (MCP or GraphQL mutation).
- Validate that the status was changed. If not, present an error.

---

# Notes

- If the issue contains visual or UI elements, include screenshots in the PR.
- All interactions with GitHub Projects must use **structured updates to fields**, not comments or edits to the issue
  body.
- Keep the interaction transactional — always check for failure and stop cleanly if needed.