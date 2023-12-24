# Flow with servlets and jsp pages

The application implements a state machine and works with servlets and jsp pages to provide the content. The following principles are applied.


## 1. Login

When a player starts the application, `jsp/player/login.jsp` is displayed. The data of the login screen is sent to `/login`, which corresponds to the `PlayerLoginServlet`, whose `doPost()` method is called. The `doPost()` method checks the login credentials. On successful login, the player's data is retrieved from the database (or created in the database on first login), and the player is redirected to the screen corresponding to its `PlayerState`. The method responsible for redirecting the player is `PlayerStateUtils.redirect()`. 


## 2. Player screen

Every player screen consists of two parts: a servlet preparing the data (e.g., `ReadBudgetServlet` reachable as `/read-budget` that prepares the data for the 'read budget' screen)., and a jsp-page that renders the page server-side, based on the prepared data (e.g., `jsp/player/read-budget.jsp`. The servlet redirects to the jsp-page after preparation of the data. 

All communication between the servlet and the jsp-page goes through the stateful object `playerData` that contains the cached data of the player. Most of the data are copies from relevant and often used records that are retrieved directly from the database and updated before rendering the screen in case they are dynamic. One object is responsible for communicating more elaborately prepared data between the servlet and the jsp-page: `playerData.contentHtml`. This is a map of key-value pairs that can convey more complex objects to the servlet for rendering, such as parts of screens that have to be precisely built for their function.

An example is displaying the (prepared) budget information in an accordion on a jsp-page:

```jsp
<div class="panel-body">
  ${playerData.getContentHtml("panel/budget") }
</div>
```

In this case, the entire string with html content (several dozen lines of html code) have been prepared as value for the key `panel/budget` in the `playerData.contentHtml` map.


## 3. Progress button on each player screen

Each player screen has a function (reading stuff or entering data for the game), and it has a button at the bottom of the screen that allows the player to potentially submit data and request to go to the next screen. Depending on the game state, this button is greyed out (disabled) or enabled, where it can become enabled based on the entered data on the screen, and on the state of the game round for the group as initiated by the group's facilitator. 

Since the state of the progress button is dependent on the group round and influenced by the facilitator, the player app has to be informed of a server-side change that can influence the state of the progress button. In this case, we chose to poll for the state change through a servlet called `GetRoundStatusServlet` that is reachable by `/get-round-status`. It probes the database to see if the button should be enabled or disabled based on the information in the database, and returns the outcome to the player app. A small piece of javascript with a timer polls for this information every few seconds. Using a pull rather than a push method, the player app is in control, and no error handling code is needed server side when the player app fails to respond. It would also cater for situations where more than one player app would be active.

The html-code for a button looks, e.g., as follows:

```html
<form action="/housinggame-player/advance-state" method="post">
  <div class="hg-button">
    <input type="hidden" name="nextScreen" value="read-news" />
    <input type="submit" value="READ NEWS" class="btn btn-primary" id="hg-submit" disabled />
  </div>
</form>
```

Note that a form is used with a submit button. This is because an XHR POST request is **not** meant to handle redirects, where pressing the button at the bottom of the screen typically results in a redirect. Therefore, using a form with a submit is a more logical way to process the button at the bottom of the screen. The code responsible for checking whether the READ NEWS button is greyed out or enabled in javascript is as follows:

```js
$(document).ready(function() {
  check();
});

function check() {
  $.post("/housinggame-player/get-round-status", {jsp: 'read-budget'},
    function(data, status) {
      if (data == "OK") {
        $("#hg-submit").removeAttr("disabled");
      } else {
        setTimeout(check, 5000);
      }
    });
}
```

Using jQuery, the `check()` function asks the `get-round-status` servlet whether moving to the next screen is ok or not. If yes, the button is enabled (the disabled attribute is removed), and the polling timer stops. If no, the `check()` function is rescheduled in 5 seconds. The information provided to the `get-round-status` servlet is a JSON object telling it which jsp called the servlet (the `{jsp: 'read-budget'}` object). On the basis of this object, the `get-round-status` servlet checks: is it okay for this player to move to the next screen after read-budget, given the player's state and the group's round state?


## 4. Advancing to the next screen

When a screen does not contain a payload with user-entered data, the `AdvancestateServlet` called by `/advance-state` determines which screen to show to the player, depending on the player's state and the group's round state. This can be seen in the html-code for the button at the bottom of the screen:

```html
<form action="/housinggame-player/advance-state" method="post">
  <div class="hg-button">
    <input type="hidden" name="nextScreen" value="read-news" />
    <input type="submit" value="READ NEWS" class="btn btn-primary" id="hg-submit" disabled />
  </div>
</form>
```

Here, the button indicates it wants to advance to the `read-news` screen for the player (the promise that the button makes, since its text reads 'READ NEWS'). When the button is enabled (see section 3 above), the player can click the button, and the server-side servlet `advance-state` encoded in the class `AdvanceStateServlet` takes over. This class is the state machine for the player that determines what actions to take, and what screen to show next. The piece of code in `advance-state` for handling the `read-news` request looks as follows:

```java
// player clicked READ NEWS on the read-budget screen
if (nextScreen.equals("read-news"))
{
    data.getPlayerRound().setPlayerState(PlayerState.READ_NEWS.toString());
    data.getPlayerRound().store();
    response.sendRedirect("/housinggame-player/read-news");
    return;
}
```

Since the `read-budget` screen, where the request was made, is a read-only screen without user interactions, two actions need to take place. First, the player status has to be updated in the database, using its current `PlayerRound` record. Secondly, the page can be redirected to the `read-news` screen. Note that `/housinggame-player/read-news` redirects **first** to the `ReadNewsServlet` that retrieves the correct news item for the player from the database, which in turn redirects to the `jsp/player/read-news` jsp-page that is responsible for the server side rendering of the news page for the client side. This page will start again with a disabled button at the bottom, which will be enabled based on the development of the group round state of the game.

The central piece of code in the `ReadNewsServlet` to prepare the data for the next screen is:

```java
data.getContentHtml().clear();
ContentUtils.makeBudgetAccordion(data);
ContentUtils.makeNewsAccordion(data);
data.putContentHtml("buy-or-sell", data.getPlayerRoundNumber() == 1 
    ? "view-buy-house" : "view-sell-house");
response.sendRedirect("jsp/player/read-news.jsp");
```

As can be seen, the html content map is cleaned, after which data for two accordions is filled: the budget information and the news information. Finally, some content with the key `buy-or-sell` is prepared for the jsp-page to decide which button will be displayed at the bottom of the screen (in round 1, it redirects to `view-buy-house` since players don't own a house yet; in rounds 2 and up, players get the opportunity to sell their house first, indicated by `view-sell-house` for the redirection of the button at the bottom of the screen).


## 5. Processing information from the player screen

When a player screen needs to send data to the server to be processed and entered into the database, several ways to do so exist: forms or json records. In essence, they are the same, since the player app sends a request to the server in both cases. To keep the code maintainable and consistent, this always has to be done in the same manner. In this case, forms are used to submit information to the server, for the reason already mentioned above: a submission of data typically leads to a redirect for the player screen, which is handled in a natural way using the submit of a form. 

```html
<form action="/housinggame-player/advance-state" method="post">
  <div class="hg-button">
    <input type="hidden" name="next-screen" value="buy-house-wait" />
    <input type="hidden" id="form-house-code" name="house" value="" />
    <input type="hidden" id="form-house-price" name="price" value="" />
    <input type="submit" value="BUY HOUSE" class="btn btn-primary" id="hg-submit" disabled />
  </div>
</form>
```

The form contains a number of hidden fields that convey the choice of the player (this could also have been done in many other ways). These fields are filled by javascript code.

On the server side, the relevant information is extracted from the parameter strings, checked, and inserted into the database when they are correct. In this case the `makeHouseTransaction` method checks the data and inserts it into the database when correct. When the data is not correct, either the original screen can be shown to the player again (possibly with an error message), so the user can re-send the information, or a separate error screen can be shown that redirects to the login screen when the error has been read. In the case below, the player is redirected back to the `buy-house` screen in case of an error creating the buying transaction.

```java
// player decided which house to buy with BUY HOUSE 
// and has entered the price on the buy-house screen
if (nextScreen.equals("buy-house-wait"))
{
    // handle the entered buy-house data: 
    // Parameter house[e.g., N07], Parameter price[e.g., 105]
    String house = request.getParameter("house");
    String price = request.getParameter("price");
    if (!SqlUtils.makeHouseTransaction(data, house, price))
    {
        response.sendRedirect("/housinggame-player/buy-house");
        return;
    }
    data.getPlayerRound().setPlayerState(PlayerState.BUY_HOUSE_WAIT.toString());
    data.getPlayerRound().store();
    response.sendRedirect("/housinggame-player/buy-house-wait");
    return;
}
```


## 6. Dynamic data on the user screen

There are different ways of displaying dynamic data on the player's screen. Example are buying measures or buying a house, where feedback has to be shown on the screen indicating whether the player can afford the measure or the house or not. Additional information about costs and satisfaction can be displayed alongside the choices made by the player. 

There are two ways to display this information. One is to buld in everything into the screen, where relevant parts are shown or hidden, depending on the player's choice. 
