default: build

build:
	javac -d dist/ -cp "./libs/*:./src" src/edu/unh/letsmeet/Main.java

run:
	java -cp "./libs/*:./dist" -Djava.system.class.loader=icp.core.ICPLoader edu.unh.letsmeet.Main

# Currently not working -- classpath from manifest not same as classpath when run
jar-artifact:
	jar cvfm server.jar MANIFEST.MF -C ./dist .