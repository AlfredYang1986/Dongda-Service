import play.routes.compiler.InjectedRoutesGenerator
import play.sbt.PlayScala

lazy val commonSettings = Seq(
    organization := "com.blackmirror",
    version := "2.0",
    scalaVersion := "2.11.8"
)

resolvers += Resolver.mavenLocal

libraryDependencies ++= Seq(
    jdbc,
    cache,
    ws,
    "com.pharbers" % "pharbers-all-libs" % "1.0",
    "com.pharbers" % "pharbers-module" % "0.1",
    "com.pharbers" % "pharbers-errorcode" % "0.1",
    "com.pharbers" % "pharbers-mongodb" % "0.1",
    "com.pharbers" % "pharbers-third" % "0.1",
    "com.pharbers" % "pharbers-spark" % "0.1",
    "com.pharbers" % "pharbers-memory" % "0.1",
    "com.pharbers" % "pharbers-security" % "0.1",
    "com.pharbers" % "pharbers-message" % "0.1",
    "com.pharbers" % "pharbers-redis" % "0.1",
    "com.pharbers" % "pharbers-max" % "0.1",
    "com.pharbers" % "pharbers-pattern" % "0.1",

    "com.aliyun.oss" % "aliyun-sdk-oss" % "3.0.0",
    "com.aliyun" % "aliyun-java-sdk-sts" % "3.0.0",
    "com.aliyun" % "aliyun-java-sdk-core" % "3.5.0",

    "net.debasishg" % "redisclient_2.11" % "3.4",
    "org.apache.commons" % "commons-email" % "1.4",
    "org.apache.spark" %% "spark-core" % "2.0.0",
    "org.apache.spark" %% "spark-sql" % "2.0.0",
    "org.mongodb.scala" %% "mongo-scala-driver" % "1.2.1",
    "org.mongodb.spark" %% "mongo-spark-connector" % "2.0.0",
    "org.mongodb" % "casbah_2.11" % "3.1.1",
    "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
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
