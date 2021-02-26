package com.adris.kafka.serde

import com.sksamuel.avro4s.{ Decoder, Encoder, FromRecord, ToRecord }
import io.confluent.kafka.serializers.{ KafkaAvroDeserializer, KafkaAvroSerializer }
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.{ Deserializer, Serializer }

import java.util

object KafkaAvro4sAdapters {

  implicit class EncoderAdapter[A](encoder: Encoder[A]) {

    def toKafkaAvroSerializer: Serializer[A] =
      new Serializer[A] {

        private val kafkaAvroSerializer = {
          val ser = new KafkaAvroSerializer
          ser.asInstanceOf[Serializer[GenericRecord]]
        }

        override def configure(
          configs: util.Map[String, _],
          isKey: Boolean
        ): Unit =
          kafkaAvroSerializer.configure(configs, isKey)

        override def serialize(topic: String, data: A): Array[Byte] = {
          implicit val enc = encoder
          kafkaAvroSerializer.serialize(topic, ToRecord[A].to(data))
        }
      }
  }

  implicit class DecoderAdapter[A](decoder: Decoder[A]) {

    def toKafkaAvroDeserializer: Deserializer[A] =
      new Deserializer[A] {

        private val kafkaAvroDeserializer = {
          val deser = new KafkaAvroDeserializer
          deser.asInstanceOf[Deserializer[GenericRecord]]
        }

        override def configure(
          configs: util.Map[String, _],
          isKey: Boolean
        ): Unit =
          kafkaAvroDeserializer.configure(configs, isKey)

        override def deserialize(topic: String, data: Array[Byte]): A = {
          implicit val enc = decoder
          FromRecord[A].from(kafkaAvroDeserializer.deserialize(topic, data))
        }
      }
  }
}
