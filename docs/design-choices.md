# Design choices

## Why use JSP/Servlets and not REST?

One of the core pillars of REST is statelessness. A typical game page is stateful and benefits from a stateful back-end. In theory, the game could be implemented in a stateless manner since all data is stored in the database, and the server does not contain any data that is not stored in the database. Having a stateful object (the `PlayerData` object) available on the server for each session is, however, beneficial for coding ease, code size, and speed. Apart from the `PlayerData` object, the application is built more-or-less in a stateless manner.

A second principle of REST is the decoupling of content (server) and interface (client). In the game, content and interface are tightly coupled. Precise information is made available for the designed player pages, and almost no freedom exists in displaying the information in another way. Yet, separation of concerns is a good thing, and servlets and JSP pages tend to get extremely messy. Therefore, strict guidelines must be followed to avoid a messy back-end and as a result, lower maintanability of the application.


## Choice for Tomcat/Java instead of Next.js or React.js

Tomcat and Java have proven to be extremely stable and upward compatible over the years. Java libraries of 2002 still work without problems in new applications. Building and deploying of a new war file is a matter of seconds. A game is not a series of pages for which Search Engine Optimization and Single Page Apps are necessarily important. State management is super easy in Java/Tomcat. Those were the main reasons sticking to the Java/Tomcat ecosystem.
