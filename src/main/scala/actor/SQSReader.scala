package actor

import akka.actor.{ActorSystem, ActorRef, Props, Actor}
import awscala.Region
import awscala.sqs.{Message, SQS, Queue}

import scala.util.Try

object SQSReader {

  //messages
  case object Read
  case class Delete(messages: Seq[Message])

  class SqsReader(queue: Queue, count: Int, wait: Int, implicit val sqs: SQS) extends Actor {

    def receive: Receive = {
      case Read =>
        sender ! sqs.receiveMessage(queue, count, wait)
      case Delete(messages: Seq[Message]) =>
        queue.removeAll(messages)
    }
  }

  def getSqsReader(region: String, queue: String, count: Int, wait: Int)(implicit actorSystem: ActorSystem): Try[ActorRef] = for {
      region <- Try(Region(region))
      sqs <- Try(SQS.at(region))
      queue <- Try(sqs.queue(queue).get)
      sqsReader = actorSystem.actorOf(Props(classOf[SqsReader], queue, count, wait, sqs))
  } yield sqsReader

}
