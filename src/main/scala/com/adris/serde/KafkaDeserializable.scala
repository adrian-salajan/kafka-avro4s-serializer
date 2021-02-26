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

package com.adris
package serde

import com.sksamuel.avro4s.{Decoder, FromRecord}
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, Deserializer, StringDeserializer}

import scala.jdk.CollectionConverters.MapHasAsJava


trait KafkaDeserializable[A] {
  def deserializer(configs: Option[Map[String, String]]): Deserializer[A]
}

object KafkaDeserializable {
  def apply[A](implicit instance: KafkaDeserializable[A]): KafkaDeserializable[A] = instance

  def apply[A: Decoder]: KafkaDeserializable[A] = new KafkaDeserializable[A] {
    override def deserializer(configs: Option[Map[String, String]]): Deserializer[A] =
      new Deserializer[A] {

        private val kafkaAvroDeserializer = {
          val ser = new KafkaAvroDeserializer
          ser.configure(configs.getOrElse(Map.empty).asJava, false)
          ser
        }

        private val recordDeserializer = kafkaAvroDeserializer.asInstanceOf[Deserializer[SpecificRecord]]

        override def deserialize(topic: String, data: Array[Byte]): A = {
          val record = recordDeserializer.deserialize(topic, data)
          FromRecord[A].from(record)
        }
      }
  }

  implicit val byteArraySerializer: KafkaDeserializable[Array[Byte]] =
    new KafkaDeserializable[Array[Byte]] {
      override def deserializer(configs: Option[Map[String, String]]): Deserializer[Array[Byte]] =
        new ByteArrayDeserializer
    }

  implicit val stringSerializer: KafkaDeserializable[String] =
    new KafkaDeserializable[String] {
      override def deserializer(configs: Option[Map[String, String]]): Deserializer[String] =
        new StringDeserializer
    }
}
