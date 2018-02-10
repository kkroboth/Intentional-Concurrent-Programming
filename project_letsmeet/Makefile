default: build

build:
	mkdir -p dist/
	javac -d dist/ -cp "./libs/*:./src" src/edu/unh/letsmeet/Main.java

run:
	java -cp "./libs/*:./dist" -Djava.system.class.loader=icp.core.ICPLoader \
	 -Djava.util.logging.config.file=logging.properties \
	 -Dedu.unh.letsmeet.config.file=server.properties \
	 -Dicp.core.config.file=icp.properties \
	  edu.unh.letsmeet.Main

debug:
	java -cp "./libs/*:./dist" -Djava.system.class.loader=icp.core.ICPLoader \
	 -Djava.util.logging.config.file=logging.properties \
	 -Dedu.unh.letsmeet.config.file=server.properties \
	 -Dicp.core.config.file=icp.properties \
	 -agentlib:jdwp=transport=dt_socket,address=localhost:5005,server=y,suspend=y \
	  edu.unh.letsmeet.Main


# Currently not working -- classpath from manifest not same as classpath when run
jar-artifact:
	jar cvfm server.jar MANIFEST.MF -C ./dist .

clean:
	rm -rf ./dist