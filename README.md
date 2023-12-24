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


## Contents

1. [Game flow](docs/flow.md)
2. [Use of servlets and jsp-pages](docs/servlets.md)


## Why use JSP/Servlets and not REST?

One of the core pillars of REST is statelessness. A typical game page is stateful and benefits from a stateful back-end. In theory, the game could be implemented in a stateless manner since all data is stored in the database, and the server does not contain any data that is not stored in the database. Having a stateful object (the `PlayerData` object) available on the server for each session is, however, beneficial for coding ease, code size, and speed. Apart from the `PlayerData` object, the application is built more-or-less in a stateless manner.

A second principle of REST is the decoupling of content (server) and interface (client). In the game, content and interface are tightly coupled. Precise information is made available for the designed player pages, and almost no freedom exists in displaying the information in another way. Yet, separation of concerns is a good thing, and servlets and JSP pages tend to get extremely messy. Therefore, strict guidelines must be followed to avoid a messy back-end and as a result, lower maintanability of the application.


## Choice for Tomcat/Java instead of Next.js or React.js

Tomcat and Java have proven to be extremely stable and upward compatible over the years. Java libraries of 2002 still work without problems in new applications. Building and deploying of a new war file is a matter of seconds. A game is not a series of pages for which Search Engine Optimization and Single Page Apps are necessarily important. State management is super easy in Java/Tomcat. Those were the main reasons sticking to the Java/Tomcat ecosystem.
