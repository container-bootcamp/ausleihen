import Dependencies._

lazy val ausleihen_scs = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "container.bootcamp",
      scalaVersion := "2.12.3",
      version      := "0.0.1"
    )),
    name := "ausleihen",
    libraryDependencies ++= dependencies,
    mainClass := Some("container.bootcamp.ausleihen.BootcampBookLoanSCSApp")
  ).enablePlugins(JavaServerAppPackaging)

scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation")

packageSummary in Docker := "Ausleihen SCS"

daemonUser in Docker := "root"

dockerUpdateLatest := true

dockerRepository := Some("quay.io/containerbootcamp")

dockerBaseImage := "anapsix/alpine-java:latest"

dockerExposedPorts := Seq(80)




