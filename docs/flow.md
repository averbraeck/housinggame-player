# Flow with servlets and jsp pages

The application implements a state machine and works with servlets and jsp pages to provide the content. The following principles are applied.


##1. Login

When a player starts the application, `jsp/player/login.jsp` is displayed. The data of the logi screen is sent to `/login`, which corresponds to the `PlayerLoginServlet`, whose `doPost()` method is called. The `doPost()` method checks the login credentials. On successful login, the player's data is retrieved from the database (or created in the database on first login), and the player is redirected to the screen corresponding to its `PlayerState`. The method responsible for redirecting the player is `PlayerStateUtils.redirect()`. 


##2. Player screen

Every player screen consists of two parts: a servlet preparing the data (e.g., `ReadBudgetServlet` reachable as `/read-budget` that prepares the data for the 'read budget' screen)., and a jsp-page that renders the page server-side, based on the prepared data (e.g., `jsp/player/read-budget.jsp`. The servlet redirects to the jsp-page after preparation of the data. 

All communication between the servlet and the jsp-page goes through the stateful object `playerData` that contains the cached data of the player. Most of the data are copies from relevant and often used records that are retrieved directly from the database and updated before rendering the screen in case they are dynamic. One object is responsible for communicating more elaborately prepared data between the servlet and the jsp-page: `playerData.contentHtml`. This is a map of key-value pairs that can convey more complex objects to the servlet for rendering, such as parts of screens that have to be precisely built for their function.

An example is displaying the (prepared) budget information in an accordion on a jsp-page:

```jsp
<div class="panel-body">
  ${playerData.getContentHtml("panel/budget") }
</div>
```

In this case, the entire string with html content (several dozen lines of html code) have been prepared as value for the key `panel/budget` in the `playerData.contentHtml` map.


##3. Progress button on each player screen

Each player screen has a function (reading stuff or entering data for the game), and it has a button at the bottom of the screen that allows the player to potentially submit data and request to go to the next screen. Depending on the game state, this button is greyed out (disabled) or active, where it can turn active based on the entered data on the screen, and on the state of the game round for the group as initiated by the group's facilitator. 

Since the state of the progress button is dependent on the group round and influenced by the facilitator, the player app has to be informed of a change that can change the state of the progress button. In this case, we chose to poll for the state change through a servlet called `GetRoundStatusServlet` that is reachable by `/get-round-status`. It probes the database to see if the button should be active or disabled based on the information in the database, and returns the outcome to the player app. A small piece of javascript with a timer polls for this information every few seconds. Using a pull rather than a push method, the player app is in control, and no error handling code is needed server side when the player app fails to respond.


##4. Processing the information from the player screen

When a player screen needs to send data to the server to be processed and entered into the database, several ways to do so exist: forms or json records. In essence, they are the same, since the player app sends a request to the server in both cases. To keep the code maintainable and consistent, this always has to be done in the same manner. In this case, JSON objects are used to submit information to the server, and potential form elements, such as `<input>` or `<select>` are transformed into JSON objects by javascript code and sent to the server.

On the server side, methods from the gson library extract the relevant information from the json string, check the data, and insert the data into the database when correct. When the data is not correct, either the original screen can be shown to the player again (possibly with an error message), so the user can re-send the information, or a separate error screen can be shown that redirects to the login screen when the error has been read.


##5. Dynamic data on the user screen

There are different ways of displaying dynamic data on the player's screen. Example are buying measures or buying a house, where feedback has to be shown on the screen indicating whether the player can afford the measure or the house or not. Additional information about costs and satisfaction can be displayed alongside the choices made by the player. 

There are two ways to display this information. One is to buld in everything into the screen, where relevant parts are shown or hidded, depending on the player's choice. 


##5. Advancing to the next screen

The `AdvancestateServlet` called by `/advance-state` determines which screen to show to the player, depending on the player's state and the group's round state. 
