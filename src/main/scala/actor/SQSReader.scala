package actor

import actor.DynamoDBWriter._
import akka.actor.{ActorSystem, ActorRef, Props, Actor}
import awscala.Region
import awscala.sqs.{Message, SQS, Queue}

import scala.util.Try

object SQSReader {

  //messages
  case object Read
  case class Delete(messages: Seq[Message])

  class SqsReader(queue: Queue, dynamoWriter: ActorRef, count: Int, wait: Int, implicit val sqs: SQS) extends Actor {

    def receive: Receive = {
      case Read =>
        sqs.receiveMessage(queue, 10, 20) match {
          case x::xs =>
            dynamoWriter ! Write(x::xs)
          case Nil =>
            self ! Read
        }
      case Delete(messages: Seq[Message]) =>
        queue.removeAll(messages)
        self ! Read
    }
  }

  def getSqsReader(region: String, queue: String, count: Int, wait: Int, dynamoWriter: ActorRef)(implicit actorSystem: ActorSystem): Try[ActorRef] = for {
      region <- Try(Region(region))
      sqs <- Try(SQS.at(region))
      queue <- Try(sqs.queue(queue).get)
      sqsReader = actorSystem.actorOf(Props(classOf[SqsReader], queue, dynamoWriter, count, wait, sqs))
  } yield sqsReader

}
