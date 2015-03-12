package actor

import akka.util.Timeout
import awscala.sqs._
import awscala._
import akka.testkit._
import akka.actor.ActorSystem
import org.specs2.mutable._
import scala.util.Random

import SQSReader._


class SQSReaderSpec extends Specification {
  val region = Region.US_EAST_1.getName
  implicit val sqs: SQS = SQS.at(Region(region))
  val queueName = Random.alphanumeric.take(10).mkString
  val queue: Queue = sqs.createQueueAndReturnQueueName(queueName)
  Thread.sleep(1000)

  "Sqs Reader" should {
    //Initation tests
    //###############
    "Return the reader actor" in new TestCase {
      getSqsReader(region, queueName, 10, 10).isSuccess must beTrue
    }

    "Fail returning reader if region is wrong" in new TestCase {
      val wrongRegion = "wrong-region-name"

      getSqsReader(wrongRegion, queueName, 10, 10).isFailure must beTrue
    }

    "Fail returning reader if sqs is not present at given region" in new TestCase {
      val wrongRegion = Region.US_WEST_1.getName

      getSqsReader(wrongRegion, queueName, 10, 10).isFailure must beTrue
    }

    "Fail returning reader if queue is not present" in new TestCase {
      val wrongQueue = "not-exists"

      getSqsReader(region, wrongQueue, 10, 10).isFailure must beTrue
    }

//    //Functionality tests
//    //###################
//    "receive message correctly" in new TestCase {
//      println("reader alinacak")
//      val sqsReader = getSqsReader(region, queueName, 10, 20).get
//
//      println("reader alindi")
//      queue.add("test message")
//      queue.add("test message")
//
//
//      implicit val timeout = Timeout(Duration(21, SECONDS))
//      val future = sqsReader ? Read
//
//      println(future.value)
//      val Success(result: Seq[Message]) = future.value.get
//
//      result.head.body mustEqual "test message"
//
//    }


  }

  def cleanUp() = {
    queue.destroy()
  }
  step(cleanUp())

}

abstract class TestHelper extends TestKit(ActorSystem("test")) with ImplicitSender

class TestCase extends TestHelper with After {
  def after = {
    system.shutdown()
  }

}



//  val str = """{
//      "actor_name": "OpenTable Diner Since 2007",
//      "provider": "opentable",
//      "type": "comment",
//      "created_at": "2008-03-23T00:00:00+00:00",
//      "title": "I was very pleased with our",
//      "review": "I was very pleased with our Easter dinner experience. The wait staff, Natalie in particular, were fabulous. I felt well taken care of without being interupted or disturbed. The food was outstanding. The price was very reasonable for the quality of food and service. I highly recommend this restaurant.\r\nGary Thompson",
//      "food_rating": "5",
//      "ambience_rating": "4",
//      "service_rating": "5"
//    }"""

