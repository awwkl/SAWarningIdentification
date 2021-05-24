# SAWarningIdentification (SAWI)
## Introduction
* This repository is a fork of a project that holds the source code and experimental data of the paper `Is There A "Golden" Feature Set for Static Warning Identification? - An Experimental Evaluation` in ESEM2018. 
* Most of the files (and hard work) in this project should be attributed to the authors of the original project/paper, and the resources they referenced.
* This fork was created to provide documentation on how to run the project, as well as (possible) future extensions

## Details of directories
* `archive-bin`: previously `bin` directory, archived for reference
* `archive-data`: previously `data` directory, archived for reference
* `classes` directory to hold class files
* `working` directory to hold files for the input/output for this project
* `features and experimental results`: the values of features of the 40 project revisions used in the paper, as well as some experimental results
* `lib`: the JAR files the program requires
* `src`: the source code

## Getting Started (running this project)
### 0. Overview
This SAWI project (the "tool") can be used to extract static analysis warning features from a Java project (the "target"). It is recommended to run this tool in the following order:
1. Prepare target project files: Prepare the target project and required files for subsequent steps
2. Parse Commit Data: Parse and load into database the git logs of target project
3. Feature Extraction: Obtain SA warning feature values for target project
4. Feature Selection: Perform greedy backward elimination algorithm for feature selection

### 1. Prepare target project files
* Perform `git clone` and `git checkout` to obtain the specific revision of the target project you wish to run this tool on
* Generate the logs of the project's commit info (log.txt) and commit content (logCode.txt)
```
git log -p --pretty="format:GitDiffStart: %H | %ad" > logCode.txt --abbrev=7 --no-renames --no-merges
git log --name-status --pretty="format:GitCommitStart: %H |#& %an |#& %ae |#& %ad |#& %at |#& %s |#& %b %n---------------filePathSplit---------------" > log.txt --no-renames --no-merges
```
* Compile `*.java` source files that you wish to analyze into `*.class` class files.
* Run SpotBugs on the compiled class files, and generate XML bug report for detected bugs, saved as `spotbugs.xml`

### 2. Parse Commit Data
* Place the commit logs (`log.txt` and `logCode.txt`) in the `working/` directory
* Set up SQL database using the `working/create.sql` file
* Compile and run the LogParser program to parse the commit data of the target project and load into SQL database
```
javac -d classes -cp "src;lib/*" src/com/git/LogParser.java
java -cp "classes;lib/*" com.git.LogParser
```

### 3. Feature Extraction
* Place the SpotBugs XML bug report (`spotbugs.xml`) in the `working/` directory
* Set appropriate values for variables in `src\com\comon\Constants.java` for your system
* Compile and run the OverallFeatureExtraction program to extract SA warning features
```
javac -d classes -cp "src;lib/*" src/com/featureExtractionRefined/OverallFeatureExtraction.java
java -cp "classes;lib/*" com.featureExtractionRefined.OverallFeatureExtraction
```

### 4. Feature Selection