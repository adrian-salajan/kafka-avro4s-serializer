package com.adris.serde

import akka.Done
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.testkit.KafkaTestkitTestcontainersSettings
import akka.kafka.testkit.scaladsl.{ScalatestKafkaSpec, TestcontainersKafkaLike}
import akka.stream.scaladsl.{Sink, Source}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.funspec.AsyncFunSpecLike
import org.scalatest.funsuite.{AnyFunSuiteLike, AsyncFunSuiteLike}
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

abstract class SpecBase(kafkaPort: Int)
  extends ScalatestKafkaSpec(kafkaPort)
    with AsyncFunSuiteLike
    with Matchers
    with ScalaFutures
    with Eventually {

  protected def this() = this(kafkaPort = 9999)


}

class TestIt extends SpecBase with TestcontainersKafkaLike {

  implicit val implicitEc: ExecutionContext = executionContext


  override val testcontainersSettings = KafkaTestkitTestcontainersSettings(system)
    .withConfigureKafka { brokerContainers =>
      brokerContainers.foreach { b =>
        b.addEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")

      }
    }


  test("test-it") {
    val producerConfig = system.settings.config.getConfig("akka.kafka.producer")
    val producerSettings =
      ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)
        .withBootstrapServers(bootstrapServers)

    val elementsToSend = List(1, 2, 3, 4, 5)
    val produced: Future[Done] =
      Source(elementsToSend)
        .map(_.toString)
        .map(value => new ProducerRecord[String, String]("test", value))
        .runWith(Producer.plainSink(producerSettings))

    val consumerConfig = system.settings.config.getConfig("akka.kafka.consumer")
    val consumerSettings =
      ConsumerSettings(consumerConfig, new StringDeserializer, new StringDeserializer)
        .withBootstrapServers(bootstrapServers)
        .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
        .withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000")
        .withGroupId("group1")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
//        .withPollInterval()

    val consumer = Consumer.plainSource(consumerSettings, Subscriptions.topics("test"))
    val consumed = consumer.map(_.value()).take(5).runWith(Sink.seq[String])

    for {
      _ <- produced
      _ <- Future { this.log.warn("i am hereeeeeeeee") }
      c <- consumed
      _ <- Future { this.log.warn("i am hereeeeeeeee" + c.mkString(",")) }
    } yield c.map(_.toInt) should contain theSameElementsAs elementsToSend


  }
}
