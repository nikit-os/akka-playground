package com.example

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers


class TwoSumRoutesSpec extends AnyFunSuiteLike with Matchers with ScalaFutures with ScalatestRouteTest {

  import spray.json.DefaultJsonProtocol._

  lazy val testKit = ActorTestKit()

  implicit def typedSystem = testKit.system

  override def createActorSystem(): akka.actor.ActorSystem =
    testKit.system.classicSystem

  val twoSumSolver = testKit.spawn(TwoSumSolver())
  lazy val routes = new TwoSumRoutes(twoSumSolver).twoSumRoutes

  import JsonFormats._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._


  test("return success result") {
    val inputList =
      """|11, -4, 3, 4, 3, 2
         |2, 5, 5, 3, 0, 1
         |""".stripMargin
    val inputListEntity = Marshal(inputList).to[MessageEntity].futureValue
    val request = Post("/upload/7").withEntity(inputListEntity)

    request ~> routes ~> check {
      status should ===(StatusCodes.OK)

      contentType should ===(ContentTypes.`application/json`)

      entityAs[List[List[List[Int]]]] should contain theSameElementsAs (List(List(List(-4, 11), List(4, 3)), List(List(5, 2))))
    }
  }

  test("handle bad payload") {
    val inputList =
      """|D11, -4, 3, 4, 3, 2
         |2, 5, 5, 3, 0, 1
         |""".stripMargin
    val inputListEntity = Marshal(inputList).to[MessageEntity].futureValue
    val request = Post("/upload/7").withEntity(inputListEntity)

    request ~> routes ~> check {
      status should ===(StatusCodes.BadRequest)

      contentType should ===(ContentTypes.`application/json`)

      entityAs[ErrorResponse] shouldEqual ErrorResponse("payload.validation.fail", "For input string: \"D11\"")
    }
  }

  test("handle empty payload") {
    val inputList = ""
    val inputListEntity = Marshal(inputList).to[MessageEntity].futureValue
    val request = Post("/upload/7").withEntity(inputListEntity)

    request ~> routes ~> check {
      status should ===(StatusCodes.BadRequest)

      contentType should ===(ContentTypes.`application/json`)

      entityAs[ErrorResponse] shouldEqual ErrorResponse("payload.validation.fail", "Payload should not be empty")
    }
  }

}

