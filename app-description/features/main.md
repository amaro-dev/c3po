# Main 
[Layout definition](../layout/main-window.md)

## In/Out

**Trigger:** When user starts the app

**Exit:** When user selects some plugin to use

## Use cases

### UC 1 - Application start

1. When it starts the app checks if there are devices available
2. If just one device is available, app shows it as selected
3. If more than one is available, it allows the user to select it from a list
4. If no device is available, it shows a message to invite user to connect one


### UC 2 - Fail to list devices

1. If by some reason the app fails to list the devices
2. It should show a message to user explaining that it was not possible to retrieve them and inviting the user to check the logs where more information can be found

### UC 3 - No ADB setup

1. To work properly, the application needs to know the path to ADB in the machine. If not configured the app will ask it to the user
2. After the user provide it, the app will save it
3. Next time, it won't ask for the path again

### UC 4 - Companion app install 

1. When starting after the ADB path is available, the app will check for companion app presence.
2. If not available, it will try to install
3. If/once available, it will try to connect it
4. Once connected a status will inform it
5. Any errors will be reported

