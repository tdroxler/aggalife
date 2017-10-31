name := "aggalife"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor"  % "2.5.0",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.0",
  "org.scalatest"  % "scalatest_2.12" % "3.0.4" % "test"
)

def latestScalafmt = "0.6.8"
commands += Command.args("scalafmt013", "Run scalafmt cli.") {
  case (state, args) =>
    val Right(scalafmt) =
      org.scalafmt.bootstrap.ScalafmtBootstrap.fromVersion(latestScalafmt)
    scalafmt.main("--non-interactive" +: args.toArray)
    state
}
