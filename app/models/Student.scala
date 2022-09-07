package models

import akka.Done
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.pattern.StatusReply
import akka.persistence.Persistence
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, ReplyEffect}

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
      case GetStudent(replyTo) =>
        Effect.reply(replyTo)(StatusReply.Error("Id no existe capooo"))

    }
  }

  override def eventHandler(event: StudentEvent): StudentState = event match {
    case StudentCreated(name) => CreatedStudent(id, name)
  }
}
case class CreatedStudent(id: String, name: String, courses: Set[String] = Set.empty) extends StudentState {
  override def commandHandler(command: StudentCommand): Effect[StudentEvent, StudentState] = {
    command match {
      case GetStudent(replyTo) =>
        Effect.reply(replyTo)(StatusReply.Success(name))
      case EnrollStudent(courses, replyTo) =>
        Effect.persist(StudentEnrolled(courses))
          .thenReply(replyTo)(_ => StatusReply.Ack)
    }
  }

  //TODO: Handle StudentEnrolled
  override def eventHandler(event: StudentEvent): StudentState = event match {
    case StudentEnrolled(newCourses) => 
      copy(courses = courses ++ newCourses)
  }
}

sealed trait StudentCommand
case class CreateStudent(name:String, replyTo: ActorRef[String]) extends StudentCommand
case class GetStudent(whatever: ActorRef[StatusReply[String]]) extends StudentCommand
case class EnrollStudent(courses: Set[String], replyTo: ActorRef[StatusReply[Done]]) extends StudentCommand

sealed trait StudentEvent
case class StudentCreated(name: String) extends StudentEvent
case class StudentEnrolled(courses: Set[String]) extends StudentEvent


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
