# Identity Fraud Detection System

## Project Overview

A rule-based Java application that evaluates signup information
and assigns a risk decision.

The system aims to classify signups as:

- APPROVE
- REVIEW
- BLOCK

## Technologies

- Java 21
- Spring Boot
- Maven
- Spring Web
- Thymeleaf
- Spring Data JPA
- H2 Database

## How to Run

1. Install Java 21.
2. Open the project folder in VS Code.
3. Open a terminal in the project folder.
4. Run the tests:

   .\mvnw.cmd test

5. Start the application:

   .\mvnw.cmd spring-boot:run

## Team Development

The application will contain four fraud detection modules:

- EmailCheck
- BotCheck
- ImpersonationCheck
- DuplicateCheck

The RiskEngine will combine the findings from these modules.