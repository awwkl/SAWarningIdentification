compile-log:
	javac -d classes -cp "src;lib/*" src/com/git/LogParser.java

run-log:
	java -cp "classes;lib/*" com.git.LogParser

compile:
	javac -d classes -cp "src;lib/*" src/com/featureExtractionRefined/OverallFeatureExtraction.java
	
run:
	java -cp "classes;lib/*" com.featureExtractionRefined.OverallFeatureExtraction

logCode:
	git log -p --pretty="format:GitDiffStart: %H | %ad" > logCode.txt --abbrev=7 --no-renames --no-merges
	
log:
	git log --name-status --pretty="format:GitCommitStart: %H |#& %an |#& %ae |#& %ad |#& %at |#& %s |#& %b %n---------------filePathSplit---------------" > log.txt --no-renames --no-merges
