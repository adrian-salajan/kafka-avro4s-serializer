name := "avro4s-schema-registry"

version := "0.1"

scalaVersion := "2.13.5"

//idePackagePrefix := Some("com.adris")

//ThisBuild / organization := "com.adris"
//
//ThisBuild / baseVersion  := "0.1"
//
//ThisBuild / publishGithubUser := "adrian-salajan"
//ThisBuild / publishFullName   := "Adrian Salajan"

resolvers ++= Seq(
  "confluent" at "https://packages.confluent.io/maven/"
  //"jitpack" at "https://jitpack.io"
)

val AkkaVersion = "2.5.31"
val JacksonVersion = "2.10.5.1"

val avro4s              = "com.sksamuel.avro4s" %% "avro4s-core"           % "4.0.4"
val kafkaAvroSerializer = "io.confluent"         % "kafka-avro-serializer" % "6.1.0"

val akkaStreamKafka="com.typesafe.akka" %% "akka-stream-kafka" % "2.0.7"
val akkaStream="com.typesafe.akka" %% "akka-stream" % AkkaVersion
val jackson = "com.fasterxml.jackson.core" % "jackson-databind" % JacksonVersion
val akkaStreamKafkaTestkit = "com.typesafe.akka" %% "akka-stream-kafka-testkit" % "2.0.7"
val akkaStreamTestkit = "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion

val scalactic = "org.scalactic" %% "scalactic" % "3.2.2"
val scalatest= "org.scalatest" %% "scalatest" % "3.2.2" % "test"

val testContainers = "org.testcontainers" % "testcontainers" % "1.15.2" % "test"

// https://mvnrepository.com/artifact/org.slf4j/slf4j-api
val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.30"
val slf4jSimple = "org.slf4j" % "slf4j-simple" % "1.7.30"


libraryDependencies ++=Seq(
  avro4s,
  kafkaAvroSerializer,
  akkaStreamKafkaTestkit,
  akkaStreamTestkit,
  scalatest,
  scalactic,
  testContainers,
  slf4jApi,
  slf4jSimple,

)