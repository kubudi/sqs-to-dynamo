package actor

import actor.DynamoDBWriter._
import actor.SQSReader._
import akka.actor.{ActorRef, Actor}
import akka.pattern.ask
import akka.util.Timeout
import awscala.sqs.Message
import scala.concurrent.duration._

object Transformer {

  case object Check

  class Transformer(sqsReader: ActorRef, dynamoWriter: ActorRef) extends Actor {
    implicit val timeout = Timeout(3 seconds)

    def receive: Receive = {
      case Check =>
        implicit val exCon = context.dispatcher

        for {
          messagesToWrite <- getMessages
          if messagesToWrite.nonEmpty
          messagesToDelete <- writeMessages(messagesToWrite)
          _ <- deleteMessages(messagesToDelete)
        } yield self ! Check
    }

    def getMessages = (sqsReader ? Read).mapTo[Seq[Message]]
    def writeMessages(m: Seq[Message]) = (dynamoWriter ? Write(m)).mapTo[Seq[Message]]
    def deleteMessages(m: Seq[Message]) = (sqsReader ? Delete(m)).mapTo[Seq[Message]]
  }

}
