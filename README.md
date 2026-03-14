Chinese Checkers - Java Project
Project Overview

Chinese Checkers is a Java implementation of the classic board game, developed as part of a Software Engineering course. The project demonstrates object-oriented design principles, network communication, and software development best practices.
Technologies Used

    Java - Core programming language

    Maven - Build automation and dependency management

    JUnit - Unit testing framework

    JavaDoc - Documentation generation

Features

    Fully playable Chinese Checkers game

    Client-server architecture

    Multiplayer support

    Object-oriented design following SOLID principles

    Comprehensive unit tests

    Auto-generated documentation

Build & Run Instructions
Prerequisites

    Java Development Kit (JDK) 8 or higher

    Apache Maven

Compilation
bash

# Compile the entire project
mvn clean compile

Running the Application

Start the server:
bash

mvn exec:java -Pserver

Start a client:
bash

mvn exec:java -Pclient

Generate Documentation
bash

# Generate JavaDoc documentation
mvn javadoc:javadoc

The documentation will be generated in target/site/apidocs/
Run Tests
bash

# Execute all unit tests
mvn clean test
