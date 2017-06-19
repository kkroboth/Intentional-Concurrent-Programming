import de.johoop.testngplugin.TestNGPlugin._

lazy val root = (project in file(".")).
  settings(
    name := "ICP",
    version := "0.4",
    crossPaths := false,
    autoScalaLibrary := false,
    javacOptions in (Compile,compile) ++= Seq("-deprecation", "-Xlint"),
    libraryDependencies ++=  Seq(
      "org.javassist" % "javassist" % "3.21.0-GA"
    ),

    artifactPath in (Compile, packageBin) :=
      baseDirectory.value / (artifact.value.name + "-" + version.value + ".jar"),

    javaSource in Compile := baseDirectory.value / "src" / "main",
    javaSource in Test := baseDirectory.value / "src" / "test",

    javacOptions in (Compile, doc) ++= Seq(
      "-author",
      "-version",
      "-doctitle", "Intentional Concurrent Programming",
      "-bottom", "Copyright UNH/CS, 2017",
      "-link", "http://docs.oracle.com/javase/8/docs/api/"
    ),

    // Select the ICP class loader
    // Enable assertions and logging
    // don't know if this will co-exist with JUnit...
    fork in Global := true,
    javaOptions in run ++= Seq(
      "-ea",
      "-Djava.util.logging.config.file=" + baseDirectory.value + "/logging.properties",
      "-Djava.system.class.loader=icp.core.ICPLoader"
    ),
    javaOptions in Global ++= Seq(
      "-Djava.system.class.loader=icp.core.ICPLoader"
    ),
    testNGSettings
  )
