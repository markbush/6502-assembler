name := "6502-assembler"

version := "2.0.0"

scalaVersion := "2.12.8"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-feature",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-Ywarn-dead-code"
)

libraryDependencies ++= Seq(
  "junit"               % "junit"           % "4.12" % Test,
  "org.scalatest"      %% "scalatest"       % "3.0.4" % Test,
  "ch.qos.logback"      % "logback-classic" % "1.2.3"
)
