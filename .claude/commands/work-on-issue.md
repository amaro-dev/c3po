# Command: Work on Github Issue

Gets the informed issue from Github issue list and perform its implementation

# Preparation

Before you perform this command you need to guarantee that there are no unstaged or uncommited files.
If this condition is not met, you should stop and present an error message informing this reason.

This command receives an argument which is the issue number of the task you will work on.
If no argument or more than one was passed you should stop and present an error message as well.

- Check the main branch
- Update it to get changes from the server
- Execute the `/primer` command to get context about the project

If any of these fail, stop and present the error message

# Performing your task

- Create a branch with the format `feature/$ARGUMENTS`
- Read the issue description from GitHub using its MCP and passing the $ARGUMENTS issue number
- Update the issue status to working
- Plan your work to fulfill what's asked in the issue description
- Start coding in the created branch
- Gather proofs that you did what was expected
- Commit your work passing a description on why it solves the problem
- Submit the branch and create a Pull Request.
    - In the description provide any proofs you have that the work was done correctly
    - Add the tag [closes #$ARGUMENT] to guarantee that it closes the issue when approved
    - Assign the PR to the C3po project
- Update the issue status to waiting approval