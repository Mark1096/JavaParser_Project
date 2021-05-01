# JavaParser_Project

### Introduction:

The goal of this thesis is to analyze a Java file containing a class and its methods, check which of them are recursive, and replace the recursive versions, known to the program, with iterative ones, through a series of specific and detailed checks.

### Technologies used:
- Java 8
- Maven

### Building

Run the following commands from the project root: 
- mvn clean compile install 

### Execution
- cd target 
- java -jar JavaParser_Project-1.0-SNAPSHOT-jar-with-dependencies.jar

### Javadoc
To view the Javadoc documentation for the project follow these steps:
- mvn install
- Open the index.html file in the following path: {projectPath}/target/apidocs

### Author
Marco Raciti (GitHub reference: www.github.com/Mark1096)