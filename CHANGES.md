# Changes from the previous version of the project to current version

### Changes to directories or files 
* `bin` renamed to `archive-bin`, archived for reference
* `data` renamed to `archive-data`, archived for reference
* `classes` directory created to hold class files
* `working` directory created to hold files for the input/output for this project
* `lib\mysql-connector-java-5.1.7-bin.jar` moved to `lib\extra\mysql-connector-java-5.1.7-bin.jar`
* `lib\mysql-connector-java-8.0.23.jar` added, because previous version of jar did not work with my newer version of mySQL
* Added a `Makefile`, for commands to run.
* Updated the existing `README.md` to provide better documentation and steps for running the project
* Added this changes document, `CHANGES.md`. This document explains the changes from the previous version of the project to the current.

### Changes to project's functionality or purpose
* Works with SpotBugs (the spiritual successor of FindBugs) 
* This allows the tool to work with more updated versions of JDK, because FindBugs only works for programs compiled for Java versions from 1.0 to 1.8
* Extensions (possibly in the future ...)
