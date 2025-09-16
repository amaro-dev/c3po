# CLAUDE.md

This file provides focused guidance to Claude Code when working with this repository.

## Where to Find Architecture & Conventions

For the canonical, tool-agnostic engineering guide (architecture, plugin conventions, ADB command pattern, logging, and
the full Feature Design Checklist), see `AGENTS.md` at the repository root. Use that as the source of truth for how to
structure actions, middlewares, reducers, and plugins in this codebase.

## Interaction Notes

- When the user asks a question, do not assume something is wrong — simply check and respond appropriately.
- Do not alter plugin registration, themes, or layout logic unless explicitly instructed.
- You must not commit test files or example fixtures. If generated, they must be sanitized before committing.
- The user wants to stay a critical and sharp analytical thinker. Whenever you see opportunities in the conversations,
  please push its critical thinking ability.
- If you don't know something, say it
- Be critical as well, user is not perfect and don't know everything
- Be precise and concrete. Don't assume things
- Before answering, walk the user through your thought process step by step

## Documentation

- When implementing user-facing changes, update end-user docs under `docs/` (GitHub Pages source):
    - Edit or add pages in `docs/wiki/` and keep `docs/index.md` links current.
    - Keep user docs separate from engineering guidance (see `AGENTS.md`).

## Execution Style

Claude must always follow this process unless explicitly told otherwise:

1. Propose a development **plan** before coding
2. Wait for approval
3. Follow the plan step-by-step
4. If the plan becomes invalid, stop and ask for adjustment
5. Never rename or duplicate classes (e.g., `ImprovedX`, `BetterY`) — always refactor in place
6. Do not rewrite code unless asked — prefer small, focused edits
7. Always use **Kotlin**, aligned with the Compose Desktop structure
8. Keep track of what was tried and avoid repeating past attempts
9. Use imports explicitly at the top of code blocks
10. Keep tests minimal, focused and meaningful — avoid duplicate validation
