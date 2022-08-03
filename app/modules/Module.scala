package modules

import akka.actor.ActorSystem
import akka.actor.typed.{Scheduler, ActorSystem => TypedActorSystem}
import akka.actor.typed.scaladsl.adapter.ClassicActorSystemOps
import com.google.inject.AbstractModule
import com.typesafe.config.ConfigFactory
import net.codingwell.scalaguice.ScalaModule
import play.api.ConfigLoader

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class Module() extends AbstractModule with ScalaModule {

  override def configure(): Unit = {

    val actorSystem = ActorSystem.apply("TestActorSystem", ConfigFactory.load())
    val typedSystem = actorSystem.toTyped
    val executionContext = typedSystem.executionContext
    val scheduler = actorSystem.scheduler

    bind[TypedActorSystem[_]].toInstance(typedSystem)
  }
}
