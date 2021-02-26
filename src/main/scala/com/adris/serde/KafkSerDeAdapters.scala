package com.adris.serde

import com.sksamuel.avro4s.{Decoder, Encoder, FromRecord, ToRecord}
import io.confluent.kafka.serializers.{KafkaAvroDeserializer, KafkaAvroSerializer}
import org.apache.avro.generic.GenericRecord
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.common.serialization.{Deserializer, Serializer}

import scala.jdk.CollectionConverters._

object KafkSerDeAdapters {

  implicit class EncoderAdapter[A](encoder: Encoder[A]) {

    def toKafkaAvroSerializer(configs: Map[String, String]): Serializer[A]= new Serializer[A] {

      private val kafkaAvroSerializer = {
        val ser = new KafkaAvroSerializer
        ser.configure(configs.asJava, false)
        ser
      }

      private val recordSerializer = kafkaAvroSerializer.asInstanceOf[Serializer[GenericRecord]]

      override def serialize(topic: String, data: A): Array[Byte] = {
        implicit val enc = encoder
        recordSerializer.serialize(topic, ToRecord[A].to(data))
      }
    }
  }

  implicit class DencoderAdapter[A](decoder: Decoder[A]) {

    def toKafkaAvroDeserializer(configs: Map[String, String]): Deserializer[A]= new Deserializer[A] {

      private val kafkaAvroSerializer = {
        val ser = new KafkaAvroDeserializer
        ser.configure(configs.asJava, false)
        ser
      }

      private val recordSerializer = kafkaAvroSerializer.asInstanceOf[Deserializer[GenericRecord]]

      override def deserialize(topic: String, data: Array[Byte]): A = {
        implicit val enc = decoder
        val specificRecord = recordSerializer.deserialize(topic, data)
        FromRecord[A].from(specificRecord)
      }
    }
  }
}

