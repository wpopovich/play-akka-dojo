package controllers

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, ActorSystem, Props, Scheduler, SpawnProtocol}
import akka.pattern.StatusReply
import akka.util.Timeout
import models.{CreateStudent, EnrollStudent, GetStudent, Student, StudentCommand, StudentState}
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, Reads}
import play.api.mvc
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}

import java.util.UUID
import java.util.concurrent.TimeoutException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

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

  case class EnrollStudentRequest(courses: Set[String])
  implicit val t = Json.reads[EnrollStudentRequest]
  def enrollStudent(id: String): Action[EnrollStudentRequest] = Action.async(parse.json[EnrollStudentRequest]) { request =>
    val parsed : EnrollStudentRequest = request.body

    val actorRef = getStudentActor(id)
    actorRef.ask(replyTo => EnrollStudent(parsed.courses, replyTo))
      .map(_ => NoContent)
  }

  def getStudent(id: String): Action[AnyContent] = Action.async {
    /*
    $student = Student::find(id)
    return $student.name
    
    $student = Student::find(id)
    $student.enrollCourse($curso)
     */
    val actOf = getStudentActor(id)
    actOf.ask[StatusReply[String]](GetStudent)
      .map(student => Ok(student.getValue))
      .recover {
        case StatusReply.ErrorMessage(error) => NotFound(s"Stuent $id not found, ${error}")
      }

//      .onComplete {
//        case Failure(ex) =>
//          println(s"ex $ex")
//          NotFound
//        case Success(value) =>
//          println("Hello")
//          Ok(value)
//      }

//      .map(reply =>

//      reply match {
//        case a: Stat
//      }
//      if (reply.isSuccess)
//        Ok(reply.getValue)
//      else
//        NotFound(reply.getValue)
//      reply match {
//      case StatusReply.Success(name) => Ok(name)
//      case StatusReply.Error(error) => NotFound(error)
//    }
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
