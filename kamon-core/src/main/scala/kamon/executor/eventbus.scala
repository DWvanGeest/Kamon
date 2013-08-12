package kamon.executor

import akka.event.ActorEventBus
import akka.event.LookupClassification
import akka.actor._
import java.util.concurrent.TimeUnit

import kamon.{Tracer, CodeBlockExecutionTime, Kamon, TraceContext}
import akka.util.Timeout
import scala.util.{Random, Success, Failure}
import scala.concurrent.Future

trait Message

case class PostMessage(text:String) extends Message

case class MessageEvent(val channel:String, val message:Message)

class AppActorEventBus extends ActorEventBus with LookupClassification{
  type Event = MessageEvent
  type Classifier=String
  protected def mapSize(): Int={
    10
  }

  protected def classify(event: Event): Classifier={
    event.channel
  }

  protected def publish(event: Event, subscriber: Subscriber): Unit={
    subscriber ! event
  }
}
case class Ping()
case class Pong()

class PingActor extends Actor with ActorLogging {

  val pong = context.actorOf(Props[PongActor])
  val random = new Random()
  def receive = {
    case Pong() => {
      //Thread.sleep(random.nextInt(2000))
      //log.info("Message from Ping")
      pong ! Ping()
    }
  }
}

class PongActor extends Actor with ActorLogging {
  def receive = {
    case Ping() => {
      sender ! Pong()
    }
  }
}


object TryAkka extends App{
  val system = ActorSystem("MySystem")
  val appActorEventBus=new AppActorEventBus
  val NEW_POST_CHANNEL="/posts/new"
  val subscriber = system.actorOf(Props(new Actor {
    def receive = {
      case d: MessageEvent => println(d)
    }
  }))

  Tracer.start
  for(i <- 1 to 4) {
    val ping = system.actorOf(Props[PingActor])
    ping ! Pong()
  }


  def threadPrintln(body: String) = println(s"[${Thread.currentThread().getName}] - [${Tracer.context}] : $body")

  /*
  val newRelicReporter = new NewRelicReporter(registry)
  newRelicReporter.start(1, TimeUnit.SECONDS)

*/
  import akka.pattern.ask
  implicit val timeout = Timeout(10, TimeUnit.SECONDS)
  implicit def execContext = system.dispatcher



  Tracer.start

  Tracer.context.get.append(CodeBlockExecutionTime("some-block", System.nanoTime(), System.nanoTime()))
  threadPrintln("Before doing it")
  val f = Future { threadPrintln("This is happening inside the future body") }

  Tracer.stop


  //Thread.sleep(3000)
  //system.shutdown()

/*  appActorEventBus.subscribe(subscriber, NEW_POST_CHANNEL)
  appActorEventBus.publish(MessageEvent(NEW_POST_CHANNEL,PostMessage(text="hello world")))*/
}