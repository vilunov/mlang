organization := "university.innopolis"
name := "mlang"
version := "0.1"

scalaVersion := "2.12.7"

enablePlugins(Antlr4Plugin)

lazy val root = (project in file(".")).settings(
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.5",
  ),
  antlr4Version in Antlr4 := "4.7.1",
  antlr4PackageName in Antlr4 := Some("university.innopolis.mlang.parser"),
  mainClass in run := Some("university.innopolis.mlang.Main"),
)
