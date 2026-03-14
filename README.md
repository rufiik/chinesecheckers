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
## Architecture

The project follows a client-server architecture:

    Server: Manages game state, player connections, and game logic

    Client: Provides GUI and handles user interactions
## Build & Run Instructions
```bash
# Compile the entire project
mvn clean compile

# Compile server and client:
mvn exec:java -Pserver    # Start the server
mvn exec:java -Pclient    # Start a client

# Run tests:
mvn clean test
```
