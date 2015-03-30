#!/bin/bash
mkdir -p bin
CP1="src:commons-lang3-3.3.1.jar:google-http-client-1.17.0-rc-sources.jar:google-http-client-1.17.0-rc.jar:json-path-0.5.3.jar:json-simple-1-1.1.1.jar:java-json.jar"
CP2="bin:commons-lang3-3.3.1.jar:google-http-client-1.17.0-rc-sources.jar:google-http-client-1.17.0-rc.jar:json-path-0.5.3.jar:json-simple-1-1.1.1.jar:java-json.jar"
javac -d bin/ -cp ${CP1} ./*java
java -cp ${CP2} KnowledgeGraph $1 $2 $3 "$4" $5 $6
#-key AIzaSyD0e2ClA78Lkjr_cVKSqe6bbz133RA_LmM -f "queries.txt" -t question