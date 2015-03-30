#!/bin/bash
mkdir -p bin
CP1="src:commons-codec-1.9.jar:google-api-client-1.4.0-alpha.jar:commons-lang3-3.3.1.jar:google-http-client-1.17.0-rc-sources.jar:google-http-client-1.17.0-rc.jar:json-path-0.5.3.jar:json-simple-1-1.1.1.jar:java-json.jar"
CP2="bin:commons-codec-1.9.jar:google-api-client-1.4.0-alpha.jar:commons-lang3-3.3.1.jar:google-http-client-1.17.0-rc-sources.jar:google-http-client-1.17.0-rc.jar:json-path-0.5.3.jar:json-simple-1-1.1.1.jar:java-json.jar"
javac -d bin/ -cp ${CP1} ./*java
java -cp ${CP2} KnowledgeGraph -key AIzaSyD0e2ClA78Lkjr_cVKSqe6bbz133RA_LmM -q "bill gates" -t infobox
java -cp ${CP2} KnowledgeGraph -key AIzaSyD0e2ClA78Lkjr_cVKSqe6bbz133RA_LmM -q "who created google" -t question