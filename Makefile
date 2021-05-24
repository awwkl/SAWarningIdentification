### 1. Prepare target project files
get-commit-log-info:
	git log --name-status --no-renames --no-merges --pretty="format:GitCommitStart: %H |#& %an |#& %ae |#& %ad |#& %at |#& %s |#& %b %n---------------filePathSplit---------------" > log.txt

get-commit-log-content:
	git log -p --abbrev=7 --no-renames --no-merges --pretty="format:GitDiffStart: %H | %ad" > logCode.txt
	
get-commit-time-in-iso:
	git log --date=iso

get-first-commit-time:
	git log --date=iso --reverse --no-renames --no-merges

get-total-number-of-commits:
	git rev-list HEAD --count --no-renames --no-merges

### 2. Parse Commit Data
compile-log-parser:
	javac -d classes -cp "src;lib/*" src/com/git/LogParser.java

run-log-parser:
	java -cp "classes;lib/*" com.git.LogParser

### 3. Feature Extraction
compile-feature-extraction:
	javac -d classes -cp "src;lib/*" src/com/featureExtractionRefined/OverallFeatureExtraction.java
	
run-feature-extraction:
	java -cp "classes;lib/*" com.featureExtractionRefined.OverallFeatureExtraction


