Instructions to compile and run this project.
====================================================================
0. Clone this repository from my git hub. Open terminal and type in the following command:
	git clone https://github.com/praharpatel/twitterChallenge.git
1. Go to the directory of the cloned project.
2. Once inside the directory of the project, run this command - 
	javac -cp ".:external_libs/*" src/*.java -d build
3. The above command compiled all the jars(dependencies) as well as the java source code files. 
4. Now lets start the servers. To do that run :
	java -cp ".:external_libs/*:build" ServerSimulator
5. Now lets run the application. To do that, open a new terminal window and go to the project directory and run: 	
	java -cp ".:external_libs/*:build" PollHealthStatus
6. The new terminal window should display all the result as well as a new JSON file will be created in the same directory. 
