name := "machine-learning"

version := "0.1.0"

scalaVersion := "2.10.4"

val sparkVersion = "1.5.2"

libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.2.9"

libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion

libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion

libraryDependencies += "org.apache.spark" %% "spark-mllib" % sparkVersion

libraryDependencies += "org.apache.spark" %% "spark-streaming" % sparkVersion

libraryDependencies += "org.elasticsearch" %% "elasticsearch-spark" % "2.2.0-m1"

libraryDependencies += "org.springframework" % "spring-context" % "4.2.4.RELEASE"

libraryDependencies += "com.thoughtworks.xstream" % "xstream" % "1.4.8"


assemblyJarName in assembly := s"${name.value}-fat.jar"

// Add exclusions, provided...
assemblyMergeStrategy in assembly := {
  {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case x => MergeStrategy.first
  }
}
