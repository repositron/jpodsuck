name := "jpodsuck"

organization := "com.ljw"

version := "0.0.1"

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "2.3.12" % "test",
  "org.scalatest" %% "scalatest" % "2.1.5" % "test",
  "junit" % "junit" % "4.+",
  "commons-io" % "commons-io" % "2.+",
  "log4j" % "log4j" % "1.+",
  "org.apache.commons" % "commons-lang3" % "3.+",
  "org" % "jaudiotagger" % "2.+",
  "org.apache.httpcomponents" % "httpclient" % "4.3.+",
  "org.codehaus.jackson" % "jackson-mapper-asl" % "1.9.+"
)

scalacOptions in Test ++= Seq("-Yrangepos")

// Read here for optional dependencies:
// http://etorreborre.github.io/specs2/guide/org.specs2.guide.Runners.html#Dependencies

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

initialCommands := "import com.ljw.jpodsuck._"
