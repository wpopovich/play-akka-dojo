package models

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.persistence.Persistence
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}

case class Student() {

}

sealed trait StudentState {
  def commandHandler(command: StudentCommand): Effect[StudentEvent, StudentState]
  def eventHandler(event: StudentEvent) : StudentState
}

case class EmptyStudent(id: String) extends StudentState {
  override def commandHandler(command: StudentCommand): Effect[StudentEvent, StudentState] = {
    command match {
      case CreateStudent(name, replyTo) =>
        Effect.persist(StudentCreated(name))
          .thenReply(replyTo)(_ => id)
    }
  }

  override def eventHandler(event: StudentEvent): StudentState = event match {
    case StudentCreated(name) => CreatedStudent(id, name)
  }
}
case class CreatedStudent(id: String, name: String) extends StudentState {
  override def commandHandler(command: StudentCommand): Effect[StudentEvent, StudentState] = ???

  override def eventHandler(event: StudentEvent): StudentState = ???
}

sealed trait StudentCommand
case class CreateStudent(name:String, replyTo: ActorRef[String]) extends StudentCommand
case class GetStudent(replyTo: ActorRef[String]) extends StudentCommand

sealed trait StudentEvent
case class StudentCreated(name: String) extends StudentEvent

object Student {

  private def persistenceId(id: String) = PersistenceId("Student", id)
  def apply(id: String): EventSourcedBehavior[StudentCommand, StudentEvent, StudentState] = {
    EventSourcedBehavior[StudentCommand, StudentEvent, StudentState](
      persistenceId = persistenceId(id),
      emptyState = EmptyStudent(id),
      commandHandler = (state, cmd) => state.commandHandler(cmd),
      eventHandler = (state, event) => state.eventHandler(event)
    )
  }
}
