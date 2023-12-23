# housinggame-player

Player app for the housing game. This app is optimized for use on a mobile phone. Interactions are minimal to ensure playing on the board and interacting with other players as well.

## Technical choices

The following technical choices have been made for the player app:
- built in Java (no php or nodejs needed)
- deployed in Tomcat (version 9)
- HikariCP connection pool for scalability
- jsp pages and servlets to display pages to the user
- content is data-driven with a mariadb database for persistence
- cached dynamic data object per player for quick response
- material UI 'look and feel' with Roboto font and MUI icons
- propeller static library for material-like user design elements
- polling by player app for updates (pull-based instead of push-based)
- communication using XHR (using post and not get to decrease tampering risks)
- json for communication between the servlets and the web page
- accordion design for the player page where new information is added to the bottom of the accordion per round
- strict state machine to align player's progress with the group's progress
