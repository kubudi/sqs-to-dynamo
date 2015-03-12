import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.slf4j.LazyLogging
import akka.actor._

import actor.DynamoDBWriter._
import actor.SQSReader._
import actor.Transformer._


object Main extends LazyLogging {

  def main(args: Array[String]): Unit = {
    lazy val config = ConfigFactory.load()

    implicit lazy val workerSystem = ActorSystem("workers")
    val actorSystem = ActorSystem("sqs-to-dynamo")

    (for {
      dynamoWriter <- getDynamoWriter(config.getString("dynamo.region"), config.getString("dynamo.table"))
      count = config.getInt("sqs.request.count")
      wait =  config.getInt("sqs.request.wait")
      sqsReader <- getSqsReader(config.getString("sqs.region"), config.getString("sqs.queue"), count, wait)
      transformer = actorSystem.actorOf(Props(classOf[Transformer], sqsReader, dynamoWriter))
    } yield transformer ! Check
    ).recover {
      case e =>
        logger.error(e.getMessage)
        throw e
    }

  }


}





