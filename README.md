# housinggame-player

Player app for the housing game. This app is optimized for use on a mobile phone. Interactions are minimal to ensure playing on the board and interacting with other players as well.

## Technical choices

The following technical choices have been made for the player app:
- built in Java (no php or nodejs needed)
- deployed in [Tomcat](https://tomcat.apache.org/) (version 9)
- [HikariCP](https://github.com/brettwooldridge/HikariCP) connection pool for scalability
- [jsp pages and servlets](https://www.oracle.com/technical-resources/articles/javase/servlets-jsp.html) to display pages to the user
- content is data-driven with a [mariadb](https://mariadb.org/) database for persistence
- [jOOQ](https://www.jooq.org/doc/3.19/manual/getting-started/) libraries to write SQL code in Java to work with the game database
- cached dynamic data object per player for quick response
- [material UI](https://mui.com/material-ui/) 'look and feel' with [Roboto font](https://fonts.google.com/specimen/Roboto) and [MUI icons](https://mui.com/material-ui/material-icons/)
- [propeller](https://propeller.in/frameworks/open-source/) static library for material-like user design elements
- polling by player app for updates (pull-based instead of push-based)
- communication using XHR (using post and not get to decrease tampering risks)
- json for communication between the servlets and the web page
- accordion design for the player page where new information is added to the bottom of the accordion per round
- strict state machine to align player's progress with the group's progress


## Documentation

1. [Game flow](docs/flow.md)
2. [Use of servlets and jsp-pages](docs/servlets.md)
3. [Design choices](docs/design-choices.md)
