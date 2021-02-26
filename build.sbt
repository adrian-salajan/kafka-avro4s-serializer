name := "kafka-avro4s-serde-adapter"

version := "0.1.0"

scalaVersion := "2.13.5"

resolvers ++= Seq(
  "confluent".at("https://packages.confluent.io/maven/")
)

val AkkaVersion = "2.5.31"
val JacksonVersion = "2.10.5.1"

val avro4s = "com.sksamuel.avro4s" %% "avro4s-core" % "4.0.4"
val kafkaAvroSerializer = "io.confluent" % "kafka-avro-serializer" % "6.1.0"

val akkaStreamKafka = "com.typesafe.akka" %% "akka-stream-kafka" % "2.0.7"
val akkaStream = "com.typesafe.akka" %% "akka-stream" % AkkaVersion
val jackson = "com.fasterxml.jackson.core" % "jackson-databind" % JacksonVersion
val akkaStreamKafkaTestkit = "com.typesafe.akka" %% "akka-stream-kafka-testkit" % "2.0.7"
val akkaStreamTestkit = "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion

val scalactic = "org.scalactic" %% "scalactic" % "3.2.2"
val scalatest = "org.scalatest" %% "scalatest" % "3.2.2"

val testContainers = "org.testcontainers" % "testcontainers" % "1.15.2"

val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.30"
val slf4jSimple = "org.slf4j" % "slf4j-simple" % "1.7.30"

libraryDependencies ++= Seq(
  avro4s,
  kafkaAvroSerializer,
  akkaStreamKafkaTestkit % "test",
  akkaStreamTestkit % "test",
  scalatest % "test",
  scalactic % "test",
  testContainers % "test",
  slf4jApi % "test",
  slf4jSimple % "test",
)
