import play.sbt.PlayScala

resolvers += Resolver.mavenLocal

lazy val commonSettings = Seq(
    organization := "com.blackmirror",
    version := "2.0",
    scalaVersion := "2.11.8"
)

libraryDependencies ++= Seq(
    jdbc,
    cache,
    ws,
    "com.pharbers" % "pharbers-modules" % "0.1",
    "com.pharbers" % "encrypt" % "0.1",
    "com.pharbers" % "mongodb-connect" % "0.1",
    "com.pharbers" % "mongodb-driver" % "0.1",
    "com.pharbers" % "errorcode" % "0.1",
    "com.pharbers" % "xmpp-em" % "0.1",
    "com.pharbers" % "http" % "0.1",
    "com.pharbers" % "auth-token" % "0.1",
    "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
    "org.mongodb.scala" %% "mongo-scala-driver" % "1.2.1",
    "org.mongodb.spark" %% "mongo-spark-connector" % "2.0.0",
    "org.apache.spark" %% "spark-core" % "2.0.0",
    "org.apache.spark" %% "spark-sql" % "2.0.0",
    "org.specs2" % "specs2_2.11" % "3.7" % "test"
)

lazy val root = (project in file(".")).
    settings(commonSettings: _*).
    settings(
	    routesGenerator := InjectedRoutesGenerator,
        name := "dongda-service",
        fork in run := true,
        javaOptions += "-Xmx5G"
    ).enablePlugins(PlayScala)
