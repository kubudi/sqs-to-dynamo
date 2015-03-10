import com.typesafe.config.ConfigFactory
import akka.actor._
import com.typesafe.scalalogging.slf4j.LazyLogging
import actor.DynamoDBWriter._
import actor.SQSReader._


object Main extends LazyLogging {

  def main(args: Array[String]): Unit = {
    lazy val config = ConfigFactory.load()

    implicit lazy val actorSystem = ActorSystem("sqs-to-dynamo")

    (for {
      dynamoWriter <- getDynamoWriter(config.getString("dynamo.region"), config.getString("dynamo.table"))
      count = config.getInt("sqs.request.count")
      wait =  config.getInt("sqs.request.wait")
      sqsReader <- getSqsReader(config.getString("sqs.region"), config.getString("sqs.queue"), count, wait, dynamoWriter)
    } yield sqsReader ! Read
    ).recover {
      case e =>
        logger.error(e.getMessage)
        println(e.getMessage)
        throw e
    }

  }


}





