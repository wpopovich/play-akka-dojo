package models

import akka.actor.ActorSystem
import akka.actor.testkit.typed.scaladsl.{ActorTestKit, ScalaTestWithActorTestKit}
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.adapter.ClassicActorSystemOps
import akka.pattern.StatusReply
import akka.persistence.testkit.PersistenceTestKitPlugin
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class StudentSpec extends AnyWordSpecLike with Matchers with BeforeAndAfterAll {

  val actorSystem = ActorSystem("TestActorSystem", PersistenceTestKitPlugin.config.withFallback(ConfigFactory.load()))
  val typed = actorSystem.toTyped
  val testKit = ActorTestKit(typed)
  def ignoredRef[T]: ActorRef[T] = testKit.createTestProbe[T].ref

  override protected def afterAll(): Unit = {
    testKit.shutdownTestKit()
  }

  override protected def beforeAll(): Unit = super.beforeAll()

  "Student" should {
    "Ser creado" in {
      val studentId = "123"
      val studentActor = testKit.spawn(Student(studentId))
      val probe = testKit.createTestProbe[StatusReply[CreatedStudent]]
      studentActor ! CreateStudent("Carlos Aguirre", ignoredRef)
      studentActor ! GetStudent(probe.ref)
      val student = probe.expectMessageType[StatusReply[CreatedStudent]].getValue
      student.id shouldBe "123"
      student.name shouldBe "Carlos Aguirre"
    }

    "Agregar Cursos" in {
      val studentId = "1234"
      //Genera EmptyStudent
      val studentActor = testKit.spawn(Student(studentId))

      //EmptyStudent -> CreatedStudent(JuanPerez, sinCursos)
      studentActor ! CreateStudent("Juan Perez", ignoredRef)

      //AgregarCursos a JuanPerez
      studentActor ! EnrollStudent(Set("Scala", "akka"), ignoredRef)

      val probe = testKit.createTestProbe[StatusReply[CreatedStudent]]
      studentActor ! GetStudent(probe.ref)
      val student = probe.expectMessageType[StatusReply[CreatedStudent]].getValue
      println(student)

//      student.courses should contain allElementsOf(Set("akka", "Scala"))
      student.courses shouldBe Set("akka", "Scala")
      //Listo chango!
    }

    //TODO: Agregar caso de uso para obtener cursos del estudiante
    "Obtener Cursos" in {
      val studentId = "1234"
      //Genera EmptyStudent
      //Cliente -> Dame referencia Student(1234)
      val studentActor = testKit.spawn(Student(studentId))

      //EmptyStudent -> CreatedStudent(JuanPerez, sinCursos)
      val probe2 = testKit.createTestProbe[Int]
      studentActor ! CreateStudent("Juana Fernandez", ignoredRef)

      //AgregarCursos a JuanaFernandez
      studentActor ! EnrollStudent(Set("Scala", "akka"), ignoredRef)

      val probe = testKit.createTestProbe[Set[String]]
      studentActor ! GetCourses(probe.ref)
      val courses = probe.expectMessageType[Set[String]]

      courses shouldBe Set("Scala", "akka")

      //Cliente -> StudentRef(1234) pedile Cursos
      //Student(1234) Dar Cursos -> Cliente
//      studentActor ! GetCourses(probe.ref)
//      studentActor.ask(actorRef => GetCourses(actorRef))
//
//      new CommandHandler(dameCursos) {
//        def dameCursos = {
//
//        }
//      }
//
//      new StudentService(studentId) {
//        def getCourses = {
//
//        }
//      }
//      new CreatedStudent()

//      studentActor.ask()



      //Controller -> ActorStudent(Dame Tus Cursos)

    }
  }
}
