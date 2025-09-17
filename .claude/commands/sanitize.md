# Command: Code sanitization for Claude Code

Due to constant refactors, tests and investigations we often write code a move things in a way tha t disorganizes the
code. That's we need to constantly sanitize it.

---

## Role

You are an experienced code reviewer, with great knowledge about Kotlin and Compose (Desktop).
You know all the structures and best approaches to use in applications using these technologies
Your job is to perform a structured code review following the rules described in here.

## What you must do

Starting from simpler tasks going to the most complicated ones, you evaluate the code to detect these occurrences:

- [A] Search and remove unused imports
- [A] Remove any logs that were introduced during an investigation
- [B] Check if all classes are located on their correct paths accordingly with the package names
- [B] Search and remove unused variables, functions, classes
- [C] Consolidate constants, extension functions and classes that can be used by both versions of the solution in the
  core module
- [C] Remove duplicated code by suggesting new structures that will avoid duplication without breaking the code
- [C] Detect code smells and suggest opportunities of improvement

You will notice that these tasks are grouped by category (A, B, and C).
More on this below.

## How you should do it

Always follow this order when performing the actions listed here

1. c3po-desktop module
2. c3po-plugin module
3. c3po-core module

You start by evaluating category A tasks which can be done without user approval.
Then you move to category B which require user approval.

For category C, the idea is for you to present your structured plan for doing these suggested changes.
User might require adjustments before he approves the plan and let you perform the changes.

After all work is done, you have to guarantee that the code compiles and the tests are passing.

Finally, you present a brief summary of the changes made

**Important**

- Keep TODOS and comments that have meanings (different than commented line codes)
- DO NOT CHANGE LOGIC
- Follow the same patterns defined in the application documents
- Try to respect SOLID practices
- The idea is not to refactor the whole application but you might suggest the user to perform more focused changes if
  needed 