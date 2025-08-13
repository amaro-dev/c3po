We are going to start the implementation of the new layout.
This works starts by reading all the documentation available in the app-description folder.

As you may notice, the application uses MVI as arquitecture and Compose to draw the screen. This mean there are two main ways to keep the App's state:
- The actual AppState which is used by all layers of the architecture
- The rememberState only for UI related state that does not need to be used by other layers

We start by creating the boilerplate of this screen structure.
There are components that will stay visible all the time, no mather in which screen we are. They are:
- Plugin navigation on the left
- Device selector and status on the top
- The loading pill 
- The error message

Besides these components we also have some structures that will appear under certain conditions and besides the fact that their content may change, their structure is somehow the same and they appear on top of the content. The best example of this are the dialogs we show under certain conditions.