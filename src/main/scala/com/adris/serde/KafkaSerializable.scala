/*
 * Copyright 2021 Adrian Salajan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adris.serde

import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, Serializer, StringSerializer}

import scala.jdk.CollectionConverters._
import com.sksamuel.avro4s.{Encoder, ToRecord}


trait KafkaSerializable[A] {
  def kafkaSerializer(settings: Option[Map[String, String]]): Serializer[A]
}

object KafkaSerializable {
  def apply[A](implicit instance: KafkaSerializable[A]): KafkaSerializable[A] = instance

  def apply[A: Encoder]: KafkaSerializable[A] = new KafkaSerializable[A] {
    override def kafkaSerializer(configs: Option[Map[String, String]]): Serializer[A] =
      new Serializer[A] {

        private val kafkaAvroSerializer = {
          val ser = new KafkaAvroSerializer
          ser.configure(configs.getOrElse(Map.empty).asJava, false)
          ser
        }

        private val recordSerializer = kafkaAvroSerializer.asInstanceOf[Serializer[GenericRecord]]

        override def serialize(topic: String, data: A): Array[Byte] = {
          recordSerializer.serialize(topic, ToRecord[A].to(data))
        }
      }
  }

  implicit val byteArraySerializer: KafkaSerializable[Array[Byte]] =
    new KafkaSerializable[Array[Byte]] {
      override def kafkaSerializer(
                                    settings: Option[Map[String, String]]
                                  ): Serializer[Array[Byte]] = new ByteArraySerializer
    }

  implicit val stringSerializer: KafkaSerializable[String] =
    new KafkaSerializable[String] {
      override def kafkaSerializer(settings: Option[Map[String, String]]): Serializer[String] =
        new StringSerializer
    }
}