name := """sfx"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.4.0",
  "com.adrianhurt" %% "play-bootstrap3" % "0.4.4-P24",
  "org.postgresql" % "postgresql" % "9.3-1102-jdbc41",
  "com.typesafe.play" %% "play-slick" % "1.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "1.0.0",
  "org.imgscalr" % "imgscalr-lib" % "4.2",
  "com.github.rjeschke" % "txtmark" % "0.11",
  "jp.t2v" %% "play2-auth" % "0.14.1",
  "jp.t2v" %% "play2-auth-test" % "0.14.1" % "test",
  play.sbt.Play.autoImport.cache,
  cache,
  specs2 % Test
)


resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

includeFilter in (Assets, LessKeys.less) := "*.less"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

TwirlKeys.templateImports += "dao._"

