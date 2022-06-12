package com.example

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.example.TwoSumSolver.{TwoSumRequest, TwoSumResponse}
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.prop.TableDrivenPropertyChecks

class TwoSumSolverSpec extends ScalaTestWithActorTestKit with AnyFunSuiteLike with TableDrivenPropertyChecks {
  test("should return correct answer") {
    val testData = Table(
      ("target number", "input array", "expected result"),
      (7, List(3, 5, 2, -4, 8, 11), List(List(2, 5), List(11, -4))),
      (9, List(2, 7, 11, 15, 3, -2), List(List(7, 2), List(-2, 11))),
      (0, List(2, -2, 11, -11, 3, -3), List(List(-2, 2), List(-11, 11), List(-3, 3))),
      (10, List(2, 5, 5, 3, 0, 1), List(List(5, 5))),
      (10, List(5, 5, 5, 5, 5, 5), List(List(5, 5))),
      (7, List(2, 5, 2, 5, 2, 5), List(List(5, 2))),
      (1, List(), List()),
      (10, List(3, 5, 9, 12), List()),
    )

    val probe = createTestProbe[TwoSumResponse]()
    val actor = spawn(TwoSumSolver())

    forAll(testData) {
      (targetNumber, inputArray, expectedResult) => {
        actor ! TwoSumRequest(targetNumber, inputArray, probe.ref)
        val response = probe.receiveMessage()

        response.arrayOfPairs should contain theSameElementsAs (expectedResult)
      }
    }
  }
}
