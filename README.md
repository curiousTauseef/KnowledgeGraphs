# KnowledgeGraphs
Repository for Project 2 of Advanced Database Systems.

a) Mahd Tauseef (mt2932) and Diwakar Mahajan (dm3084)
b) List of files
	-KnowledgeGraph.java
	-Question.java
	-FreeBase.java
	-README.md
	-json-path-0.5.3.jar
	-commons-codec-1.9.jar
	-google-http-client-1.17.0-rc.jar
	-google-api-client-1.4.0-alpha.jar
	-google-http-client-1.17.0-rc-sources.jar  
	-json-simple-1-1.1.1.jar  
	-commons-lang3-3.3.1.jar
	-run.sh
	-/transcripts

c) Ensure that you are in /KnowledgeGraph directory. To execute the code:
	./run.sh -key <Freebase API key> -q <"query"> -t <infobox|question>
	./run.sh -key <Freebase API key> -f <file of queries> -t <infobox|question>
	./run.sh -key <Freebase API key>
where:
    <Freebase API key > is your Google account key for a project that has enabled Freebase
    -q <query> is your query (either an entity for infobox creation or a question)
    -f <file of queries> is a text file with one query per line.
    -t <infobox|question> indicates whether you are invoking Part 1 of the project, by specifying -t infobox, or Part 2 of the project, by specifying -t question.

d) 
############ Part 1 #############


############ Part 2 #############
The code for Part 2 (Question answering) resides in the Question class. The main function of the class is called by KnowledgeGraph.main() with the correct arguments.
First, the input/argument from the user is sanitized. In case of incorrect formatting of the question, the user is informed by an appropriate message. Separate MQL queries are then constructed; one to find out about authors and one to discover businesspersons. They each respond with the results in JSON format. The JSON is parsed to extract relevant results and are stored in a TreeMap (so that the List is kept sorted) with the name of the person acting as the key and a list of his relevant creations as value.
Then we check whether the user was in the interactive exploration mode or the regular question mode and the results are displayed to the user in the correct format accordingly.



f) freebase api_key = AIzaSyD0e2ClA78Lkjr_cVKSqe6bbz133RA_LmM
	-requests per second = 50
