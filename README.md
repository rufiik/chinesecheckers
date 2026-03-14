# Chinese Checkers - Java Project

## Project Overview

**Chinese Checkers** is a Java implementation of the classic board game, developed as part of a Software Engineering course. The project demonstrates object-oriented design principles, network communication, and software development best practices.

## Technologies Used

- **Java** - Core programming language
- **Maven** - Build automation and dependency management
- **JUnit** - Unit testing framework
- **JavaDoc** - Documentation generation

## Features

-  Fully playable Chinese Checkers game
-  Client-server architecture
-  Multiplayer support
-  Object-oriented design following SOLID principles
-  Comprehensive unit tests
-  Auto-generated documentation

## Prerequisites

- Java Development Kit (JDK) 8 or higher
- Apache Maven

## Build & Run Instructions

### Compilation

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
Tests
bash

# Run all unit tests
mvn clean test

Project Structure
text

chinesecheckers/
├── src/
│   ├── main/
│   │   ├── java/           # Source code
│   │   └── resources/       # Resources and configs
│   └── test/
│       └── java/            # Unit tests
├── pom.xml                   # Maven configuration
└── README.md                 # Project documentation

Architecture

The project follows a client-server architecture:

    Server: Manages game state, player connections, and game logic

    Client: Provides GUI and handles user interactions

Key Implementation Aspects

    Object-Oriented Design: Clean separation of concerns with dedicated classes for board, pieces, players, and game rules

    Network Communication: Socket-based communication between clients and server

    Concurrency: Multi-threaded server handling multiple clients simultaneously

    Testing: Comprehensive unit tests ensuring code reliability

    Documentation: Detailed JavaDoc documentation for all public APIs
```bash
# Compile the entire project
mvn clean compile

# Compile server and client:
mvn exec:java -Pserver    # Start the server
mvn exec:java -Pclient    # Start a client

# Run tests:
mvn clean test
```
