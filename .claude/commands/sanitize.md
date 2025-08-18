# Code sanitization for Claude Code

Due to constant refactors, tests and investigations we often write code a move things in a way tha t disorganizes the
code. That's we need to constantly sanitize it.

## How we do it

Always follow this order when performing the actions listed here

1. c3po-desktop module
2. c3po-plugin module
3. c3po-core module

Starting from simpler tasks going to the most complicated ones

- Check if all classes are located on their correct paths accordingly with the package names
- Search and remove unused variables, functions, classes
- Search and remove unused imports
- Consolidate constants, extension functions and classes that can be used by both versions of the solution in the core
  module
- Remove any logs that were introduced during an investigation
- Remove duplicated code always paying attention to the above rules.

**Important**

- Keep TODOS and comments that have meanings (different than commented line codes)
- DO NOT CHANGE LOGIC