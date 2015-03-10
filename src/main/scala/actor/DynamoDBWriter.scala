package actor

import actor.SQSReader._
import akka.actor._
import awscala.Region
import awscala.dynamodbv2.{DynamoDB, Table}
import awscala.sqs.Message
import play.api.libs.json._

import scala.util.Try

object DynamoDBWriter {

  //messages
  case class Write(messages: Seq[Message])

  class DynamoWriter(table: Table, implicit val dynamoDB: DynamoDB) extends Actor {
    def uuid = java.util.UUID.randomUUID.toString

    def receive: Receive = {
      case Write(messages: Seq[Message]) =>
        messages.map { m =>
          val values = Json.parse(m.body).as[Map[String, String]]
          //we can make this batch maybe
          table.put(uuid, values.toSeq:_*)
        }
        sender ! Delete(messages)
    }
  }

  def getDynamoWriter(region: String, table: String)(implicit actorSystem: ActorSystem): Try[ActorRef] = {
    val a = for {
      region <- Try(Region(region))
      dynamoDb <- Try(DynamoDB.at(region))
      table <- Try(dynamoDb.table(table).get)
      dynamoWriter = actorSystem.actorOf(Props(classOf[DynamoWriter], table, dynamoDb))
    } yield dynamoWriter
    a
  }
}
