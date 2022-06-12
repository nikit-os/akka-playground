package com.example

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{RejectionHandler, Route, ValidationRejection}
import akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers
import akka.util.Timeout
import com.example.TwoSumSolver.TwoSumRequest

import java.io.StringReader
import scala.concurrent.Future


class TwoSumRoutes(twoSumSolver: ActorRef[TwoSumSolver.Command])(implicit val system: ActorSystem[_]) {

  import JsonFormats._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import com.github.tototoshi.csv._
  import spray.json._
  import DefaultJsonProtocol._
  import system.executionContext

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def uploadNumber(targetNum: Int, listOfLists: List[List[Int]]): Future[List[List[List[Int]]]] = {
    val results = listOfLists.map(inputList => {
      twoSumSolver.ask(TwoSumRequest(targetNum, inputList, _)).map(_.arrayOfPairs)
    })
    Future.sequence(results)
  }

  implicit val csvUnmarshaller = PredefinedFromEntityUnmarshallers.stringUnmarshaller.map(csvString => {
    if (csvString.trim.isEmpty) {
      throw new IllegalArgumentException("Payload should not be empty")
    }
    CSVReader.open(new StringReader(csvString)).all().map(listOfStrings => listOfStrings.map(_.trim.toInt))
  })

  implicit def myRejectionHandler = RejectionHandler.newBuilder().handle {
    case ValidationRejection(msg, cause) => {
      complete(HttpResponse(
        status = StatusCodes.BadRequest,
        entity = HttpEntity(
          ContentType(MediaTypes.`application/json`),
          ErrorResponse("payload.validation.fail", msg).toJson.prettyPrint
        ),
      ))
    }
  }.result()

  val twoSumRoutes: Route = Route.seal(
    pathPrefix("upload") {
      concat(
        path(IntNumber) { targetNumber =>
          post {
            entity(as[List[List[Int]]]) { listOfLists =>
              onSuccess(uploadNumber(targetNumber, listOfLists)) { result: List[List[List[Int]]] =>
                complete((StatusCodes.OK, result))
              }
            }
          }
        }
      )
    }
  )

}
