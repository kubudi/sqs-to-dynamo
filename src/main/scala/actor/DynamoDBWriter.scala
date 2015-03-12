package actor

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

    def receive: Receive = {
      //we can make this batch maybe
      case Write(messages: Seq[Message]) =>
        messages.foreach { m =>
          table.put(uuid, expand(m):_*)
        }
        sender ! messages
    }
  }


  def uuid = java.util.UUID.randomUUID.toString

  def expand(message: Message): Seq[(String, String)] =
    Json.parse(message.body).as[Map[String, String]].toSeq

  def getDynamoWriter(region: String, table: String)(implicit actorSystem: ActorSystem): Try[ActorRef] = for {
      region <- Try(Region(region))
      dynamoDb <- Try(DynamoDB.at(region))
      table <- Try(dynamoDb.table(table).get)
      dynamoWriter = actorSystem.actorOf(Props(classOf[DynamoWriter], table, dynamoDb))
  } yield dynamoWriter
}
