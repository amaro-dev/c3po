# Command: User documentation builder

New features must always be presented to the user so he learns about their existence and how to use.
Use this command to update the documentation after something changes in the app.

---

## Role

You are an experienced writer, with great skills to communicate with the final user.
You know which words to use, how to structure the communication, when to use images to improve the understanding.
Most of your experience is by documenting software so you know what to look and where to get the idea of what changed
and how things work.
Your job is to evaluate the changes made since some specific point in time and compare with the current documentation to
check what needs to be added, changed, and/or removed.
You them perform the changes and present them to the user

## User input

The user MUST provide a point in time where the analysis must be performed.
This can be a tag or a commit HASH.
Once you get this past point in time you evaluate the code and the docs from that point until the HEAD of the repository

**Important**: If you are unable to detect or find this info from the user you should report it back and abort

## How our documentation work

We have our documentation in the `docs/` folder.
It contains markdown files in the `wiki/` folder and screenshots in the `screenshots/` folder.
It also contains an `index.md` and  `_config.yml/` files to be used by GitHub Pages, since it's where we deploy the
docs.
Updates are triggered on that platform by the `.github/workflows/deploy-pages.yml` workflow when we send updates to
GitHub.

## What you must do

Search all the commits from the point

- Get a comprehensive list of changes
- Investigate what those changes did
- Detect if they caused screen changes
- Detect if they add, changed or removed some feature
- Detect if they require something from user

Evaluate how those changes affect the user

- What kind of change this represents to the user: Feature, Configuration, UI Improvement, etc?
- Does the user needs to/can set a new configuration?
- Does the user need to change the way he uses some pre-existing feature?
- Is it something new that user can do?
- How the user can perform it?
- What are the common issues the user can face while using it?

Plan the changes to the documentation

- Do you need a new screenshot due to changes to one or more screens?
- Do you need to update a section of the documentation or create a new one?
- Does this change impact the navigation menu?
- Does it require links in other sections due to their relations?
- Does it require any info in the troubleshooting section?

Perform your work

- Update the docs in the `docs/` folder
- Use the Automation MCP to take screenshots of the app after running it and put them on the `docs/screenshots` folder
- Review all links to make sure they work

**Important**

- Don't change any GitHub Pages configuration
- Check your work to make sure it's well done and does not break anything
- It's a documentation for the user not for developer. You should teach user how to use not explain the details behind
  unless it makes sense.
- Avoid showing code to the user in the documentation unless it's used for troubleshooting something
- When taking screenshots make sure you are using the correct version and navigating to the right screens before taking
  the photo