# Use of servlets and jsp pages

The application implements a state machine and works with servlets and jsp pages to provide the content. The following principles are applied.

<p style="text-align: center;"><img src="images/servlet-flowchart.png" width="80%"></p>


## 1. Login

When a player starts the application, the login servlet class, `LoginServlet` that is reachable by `/login` prepares the data for the login page. The most important tasks are creation of the (only) persistent `playerData` object, setting this data object as a session object, and preparing the valid session options the player can choose from. The display of the page is handled by the file `jsp/player/login.jsp`. When the player has entered the login data, these are sent to `/login-done`, which corresponds to the `PlayerLoginDoneServlet`, whose `doPost()` method is called. The `doPost()` method checks the login credentials. On successful login, the player's data is retrieved from the database (or created in the database on first login), and the player is redirected to the screen corresponding to its `PlayerState`. The method responsible for redirecting the player is `PlayerLoginDoneServlet.redirect()`. 


## 2. Player screen

Every player screen consists of at least three parts. (1) A servlet preparing the data (e.g., `ReadBudgetServlet` reachable as `/read-budget` that prepares the data for the 'read budget' screen). (2) A jsp-page that renders the page server-side, based on the prepared data (e.g., `jsp/player/read-budget.jsp`. The servlet redirects to the jsp-page after preparation of the data. (3) A servlet that handles the submitted data after pressing the advance button at the bottom of the screen (e.g., `ReadBudgetDoneServlet` reachable as `/read-budget-done`). This last servlet is responsible for checking the data related to the state of the game and the player, and redirecting the plyer to the next screen, to an error screen, or back to the login screen.

All communication from the first servlet to the jsp-page goes through the stateful object `playerData` that contains the cached data of the player. Most of the data are copies from relevant and often used records that are retrieved directly from the database and updated before rendering the screen in case they are dynamic. One object is responsible for communicating more elaborately prepared data between the servlet and the jsp-page: `playerData.contentHtml`. This is a map of key-value pairs that can convey more complex objects to the servlet for rendering, such as parts of screens that have to be precisely built for their function.

An example is displaying the (prepared) budget information in an accordion on a jsp-page:

```jsp
<div class="panel-body">
  ${playerData.getContentHtml("panel/budget") }
</div>
```

In this case, the entire string with html content (several dozen lines of html code) have been prepared as value for the key `panel/budget` in the `playerData.contentHtml` map.

Note that the Java package with the servlets relating to the screen can contain additional servlets for checking or updating the data on the screen (see below) or classes for preparing data in the `playerData.contentHtml` map.


## 3. Progress button on each player screen

Each player screen has a function (reading stuff or entering data for the game), and it has a button at the bottom of the screen that allows the player to potentially submit data and request to go to the next screen (through the 'xxxDone' servlet). Depending on the game state, this button is greyed out (disabled) or enabled, where it can become enabled based on the entered data on the screen, and on the state of the game round for the group as initiated by the group's facilitator. 

Since the state of the progress button is dependent on the group round and influenced by the facilitator, the player app has to be informed of a server-side change that can influence the state of the progress button. In this case, we chose to poll for the state change through a servlet called `GetRoundStatusServlet` that is reachable by `/get-round-status`. It probes the database to see if the button should be enabled or disabled based on the information in the database, and returns the outcome to the player app. A small piece of javascript with a timer polls for this information every few seconds. Using a pull rather than a push method, the player app is in control, and no error handling code is needed server side when the player app fails to respond. It would also cater for situations where more than one player app would be active for the same player account.

The html-code for a button looks, e.g., as follows:

```html
<form action="/housinggame-player/read-budget-done" method="post">
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

Independent of whether a screen contains a payload with user-entered data or not, the `xxxDoneServlet` called by `/xxx-done` determines which screen to show to the player next, depending on the player's state and the group's round state. This can be seen in the html-code for the button at the bottom of the screen (n this case for the read-budget screen):

```html
<form action="/housinggame-player/read-budget-done" method="post">
  <div class="hg-button">
    <input type="hidden" name="nextScreen" value="read-news" />
    <input type="submit" value="READ NEWS" class="btn btn-primary" id="hg-submit" disabled />
  </div>
</form>
```

Here, the button indicates that the player wants to advance to the `read-news` screen for the player (the promise that the button makes, since its text reads 'READ NEWS'). When the button is enabled (see section 3 above), the player can click the button, and the server-side servlet `read-budget-done` encoded in the class `ReadBudgetDoneServlet` takes over. This class determines what actions to take, and what screen to show next; typically this will be the read-news screen. The piece of code in `read-budget-done` for handling the `read-news` request for `nextScreen` looks as follows:

```java
// the next screen button indicates the INTENTION of the player,
// not the screen it originates from.
String nextScreen = request.getParameter("nextScreen");

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
BudgetAccordion.makeBudgetAccordion(data);
NewsAccordion.makeNewsAccordion(data);
data.putContentHtml("buy-or-sell", data.getPlayerRoundNumber() == 1 
    ? "view-buy-house" : "view-sell-house");
response.sendRedirect("jsp/player/read-news.jsp");
```

As can be seen, the html content map is cleared, after which data for two accordions is filled: the budget information and the news information. Finally, some content with the key `buy-or-sell` is prepared for the jsp-page to decide which button will be displayed at the bottom of the screen (in round 1, it redirects to `view-buy-house` since players don't own a house yet; in rounds 2 and up, players get the opportunity to sell their house first, indicated by `view-sell-house` for the redirection of the button at the bottom of the screen).


## 5. Processing information from the player screen

When a player screen needs to send data to the server to be processed and entered into the database, several ways to do so exist: forms or json records. In essence, they are the same, since the player app sends a request to the server in both cases. To keep the code maintainable and consistent, this always has to be done in the same manner. In this case, forms are used to submit information to the server, for the reason already mentioned above: a submission of data typically leads to a redirect for the player screen, which is handled in a natural way using the submit of a form. 

```html
<form action="/housinggame-player/buy-house-done" method="post">
  <div class="hg-button">
    <input type="hidden" name="nextScreen" value="buy-house-wait" />
    <input type="hidden" id="form-house-code" name="house" value="" />
    <input type="hidden" id="form-house-price" name="price" value="" />
    <input type="submit" value="BUY HOUSE" class="btn btn-primary" id="hg-submit" disabled />
  </div>
</form>
```

The form contains a number of hidden fields that convey the choice of the player (this could also have been done in many other ways). These fields are filled by javascript code:

```js
$('#houses').on('change', function() {
  $("#form-house-code").val(this.value);
  $("#form-house-price").val($("#house-price-input-" + this.value).val());
});

$('.house-price-input').on('input', function() {
  $("#form-house-price").val($("#house-price-input-" + $("#form-house-code").val()).val());
});
```

On the server side, the relevant information is received by the `buy-house-done` servlet, data is extracted from the parameter strings, checked, and inserted into the database when they are correct. In this case the `makeHouseTransaction` method checks the data and inserts it into the database when correct. When the data is not correct, either the original screen can be shown to the player again (possibly with an error message), so the user can re-send the information, or a separate error screen can be shown that redirects to the login screen when the error has been read. In the case below, the player is redirected back to the `buy-house` screen in case of an error creating the buying transaction.

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

There are different ways of displaying dynamic data on the player's screen. Example where dynamic data is needed are buying measures or buying a house, where feedback has to be shown on the screen indicating whether the player can afford the measure or the house or not. Additional information about costs and satisfaction can be displayed alongside the choices made by the player. 

There are two typical ways to display this information. One is to build in everything into the screen, where relevant parts are shown or hidden, depending on the player's choice. Another is to make a post request to the server and ask the server for the appropriate response. The disadvantage of pre-processing is that it is static; the information on the player's screen cannot be updated with new information from the database. Therefore, dynamic data will typically be processed through post requests by special servlets on the server. An example is buying improvements in the game. Depending on whether the player can afford the improvements or not, different information is shown (and the 'BUY IMPROVEMENTS' button is greyed out when the player cannot afford the improvements). 

To allow for the button to be dependent on both the group round state (retrieved using the `get-round-status` servlet) and on the affordability of the bought measures, two global variables ``choicesOk` and `buttonOk` are used in javascript:

```js
var choicesOk = true;
var buttonOk = false;

$(document).ready(function() {
  check();
});

function check() {
  $.post("/housinggame-player/get-round-status", {jsp: 'view-improvements'},
    function(data, status) {
      if (data == "OK" && choicesOk) {
        buttonOk = true;
        $("#hg-submit").removeAttr("disabled");
      } else {
        buttonOk = false;
        setTimeout(check, 5000);
      }
    });
}
```

The html code contains checkboxes and a selection:

```html
<form id="improvements-form">
  <div class="checkbox pmd-default-theme">
    <label class="pmd-checkbox pmd-checkbox-ripple-effect">
      <input type="checkbox" name="measure-12" id="measure-12" value="12">
      <span>Green garden, costs: 20 k, satisfaction: 2</span>
    </label>
  </div>
  <!-- ... many more checkboxes -->
  <div class="form-group">
    <label for="regular1" class="control-label">Choose satisfaction to buy</label>
     <select class="form-control" id="selected-points">
       <option value="0">no extra satisfaction points</option>
       <option value="1">1x - cost = 6 k</option>
       <!-- ... more options -->
     </select>
  </div>
</form>
```

Several functions in the javascript code check via the server whether the entered options are affordable:

```js
$('input[type="checkbox"]').on('change', function() {
    checkCosts();
});

$('select').on('change', function() {
    checkCosts();
});

function checkCosts() {
  $.post("/housinggame-player/check-improvements-costs", {
    'jsp': 'view-improvements',
    'form': $('#improvements-form').serialize(),
    'selected-points': $('#selected-points').val()
  }, function(result, status) {
    json = JSON.parse(result);
    if (json.ok == "OK") {
      choicesOk = true;
      if (buttonOk)
        $("#hg-submit").removeAttr("disabled");
    } else {
      choicesOk = false;
      $("#hg-submit").prop("disabled", true);
    }
    $('#calculation-result').replaceWith(json.html);
  });
}
```

Both the input checkboxes and the select field are watched for changes. If the player makes a change, the `checkCosts()` method is called that submits three fields to the `check-improvements-costs` servlet on the server: the jsp calling, the ticked boxes on the form, and the selected choice from the pull-down menu. The servlet sends back two JSON fields: `'ok'` and `'html'`, where the ok-field can contain "OK" or "NO", and the html-field replaces the `<div>` on the page with the id `#calculation-result`:

```html
<div id="calculation-result">
  <p>
      Total measure cost: 0<br>
      Bought satisfaction cost: 0<br>
      Total cost: 0<br>
      Spendable income: -6 k<br>
  </p>
  <p>
      You did not select any measures (yet).
  </p>
</div>
```

This provides for a smooth feedback to the player's choices immediately when the player makes a change on the screen. The use of a servlet ensures that the feedback to the player is based on the most recent information in the database.

