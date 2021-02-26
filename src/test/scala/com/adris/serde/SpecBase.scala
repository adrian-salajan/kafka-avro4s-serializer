package com.adris.serde

import akka.kafka.testkit.scaladsl.ScalatestKafkaSpec
import org.scalatest.concurrent.{ Eventually, ScalaFutures }
import org.scalatest.funsuite.AsyncFunSuiteLike
import org.scalatest.matchers.should.Matchers

abstract class SpecBase(kafkaPort: Int)
  extends ScalatestKafkaSpec(kafkaPort)
  with AsyncFunSuiteLike
  with Matchers
  with ScalaFutures
  with Eventually {

  protected def this() = this(kafkaPort = 9999)

}
