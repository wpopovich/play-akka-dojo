package controllers

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, ActorSystem, Props, Scheduler, SpawnProtocol}
import akka.util.Timeout
import models.{CreateStudent, GetStudent, Student, StudentCommand, StudentState}
import play.api.libs.json.Json
import play.api.mvc
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

@Singleton
class AkkaController @Inject()(val controllerComponents: ControllerComponents,
                               actorSystem: ActorSystem[_],
                               )(implicit val ec: ExecutionContextExecutor, implicit val sc: Scheduler) extends BaseController {

  implicit val timeout = Timeout(30 seconds)
  var actorMap: Map[String, ActorRef[StudentCommand]] = Map.empty

  case class CreateStudentRequest(name: String)
  implicit val r = Json.reads[CreateStudentRequest]
  def createStudent(): mvc.Action[CreateStudentRequest] = Action.async(parse.json[CreateStudentRequest]) { request =>
    val parsed = request.body
    val studentId = UUID.randomUUID().toString

    val actorRef = getStudentActor(studentId)
    actorRef.ask(replyTo => CreateStudent(parsed.name, replyTo))
      .map(Ok(_))
  }

  def getStudent(id: String): Action[AnyContent] = Action.async {
    val actOf = getStudentActor(id)
    (actOf ? GetStudent).map(name => Ok(name))
  }

  def getStudentActor(id: String): ActorRef[StudentCommand] = {
    val actorRef = actorMap.getOrElse(id, actorSystem.systemActorOf(Student(id), s"Student-${id}"))
    addStudentActor(id, actorRef)
    actorRef
  }

  def addStudentActor(id: String, actor: ActorRef[StudentCommand]): Unit = {
    actorMap = actorMap + (id -> actor)
  }
}
