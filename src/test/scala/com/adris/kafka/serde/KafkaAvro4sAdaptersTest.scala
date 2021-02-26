package com.adris.kafka.serde

import akka.Done
import akka.kafka.scaladsl.{ Consumer, Producer }
import akka.kafka.testkit.KafkaTestkitTestcontainersSettings
import akka.kafka.testkit.scaladsl.TestcontainersKafkaLike
import akka.kafka.{ ConsumerSettings, ProducerSettings, Subscriptions }
import akka.stream.scaladsl.{ Sink, Source }
import com.sksamuel.avro4s.{ Decoder, Encoder }
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ StringDeserializer, StringSerializer }

import scala.concurrent.{ ExecutionContext, Future }
import scala.jdk.CollectionConverters._

class KafkaAvro4sAdaptersTest extends SpecBase with TestcontainersKafkaLike {

  implicit val implicitEc: ExecutionContext = executionContext

  case class Person(name: String, address: Address, properties: Map[String, String])

  case class Address(street: String, number: Int)

  val elementsToSend = List(
    Person("Foo", Address("AAAA", 12), Map("favorite-color" -> "red")),
    Person("Bar", Address("BBBB", 42), Map("favorite-color" -> "blue")),
    Person("Abc", Address("CCC", 8), Map("favorite-color" -> "red"))
  )

  val Topic = "test"

  override val testcontainersSettings =
    KafkaTestkitTestcontainersSettings(system).withConfigureKafka { brokerContainers =>
      brokerContainers.foreach { b =>
        b.addEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")

      }
    }

  test("KafkaAvroSerializer from Avro4s serialization round-trip with schema-registry") {
    import KafkaAvro4sAdapters._

    val personSerializer = Encoder.gen[Person].toKafkaAvroSerializer
    personSerializer.configure(
      Map(
        AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> schemaRegistryUrl
      ).asJava,
      false
    )

    val personDeserializer = Decoder.gen[Person].toKafkaAvroDeserializer
    personDeserializer.configure(
      Map(
        AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> schemaRegistryUrl
      ).asJava,
      false
    )

    val producerConfig = system.settings.config.getConfig("akka.kafka.producer")
    val producerSettings =
      ProducerSettings(producerConfig, new StringSerializer, personSerializer)
        .withBootstrapServers(bootstrapServers)

    val produced: Future[Done] =
      Source(elementsToSend)
        .map(value => new ProducerRecord[String, Person](Topic, value))
        .runWith(Producer.plainSink(producerSettings))

    val consumerConfig = system.settings.config.getConfig("akka.kafka.consumer")
    val consumerSettings =
      ConsumerSettings(consumerConfig, new StringDeserializer, personDeserializer)
        .withBootstrapServers(bootstrapServers)
        .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
        .withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000")
        .withGroupId("group1")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    val consumer = Consumer.plainSource(consumerSettings, Subscriptions.topics(Topic))
    val consumed = consumer.map(_.value()).take(3).runWith(Sink.seq[Person])

    for {
      _                <- produced
      receivedElements <- consumed
    } yield receivedElements should contain theSameElementsAs elementsToSend

  }
}
