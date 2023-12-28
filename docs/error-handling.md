# Error handling

There are several types of warnings and errors that can occur in the housing game player app. They are handled in different ways, as explained below.


## Logging

Warnings and fully recoverable errors are only registered in the log files for later analysis. The adapted JULI logging framework of Tomcat that replaces java.util.logging can be applied for logging, see [https://tomcat.apache.org/tomcat-9.0-doc/logging.html](https://tomcat.apache.org/tomcat-9.0-doc/logging.html). It is the intention that ALL warnings and errors are logged using the logger, so no `System.out.println()` and `System.err.println()` statements should remain in the final code. The logged messages can be of any level; typically they will be on the `INFO` or `WARNING` level, since these situations are recoverable.


## Recoverable errors

After, e.g., illegal user input that is recoverable by the user, a popup can be shown on the player app. The popup is activated in the jsp-page and its presence is indicated in the `playerData` object using the field `showModalWindow`, where the content of the modal popup window is contained in the field `playerData.modalWindowHtml`. The class `ModalWindowUtils` prepares the html content to be shown in the modal popup window, including a 'CLOSE' button.

In the case of recoverable errors, an `INFO` level log message is inserted into the log files, since the situation is recoverable by the user.


## Non-recoverable errors

When an error is not recoverable, e.g., after a timeout where the `playerData` object is not accessible anymore, or after exceptions in the code, the screen is redirected to the full-screen `/error` servlet that displays the information on the screen for the player and redirects to the login screen after clicking the confirmation button.

In the case of non-recoverable errors, typically an `ERROR` level log message is inserted into the log files.
