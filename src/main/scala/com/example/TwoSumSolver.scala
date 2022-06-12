package com.example

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object TwoSumSolver {
  sealed trait Command
  final case class TwoSumRequest(target: Int, array: List[Int], replyTo: ActorRef[TwoSumResponse]) extends Command
  final case class TwoSumResponse(arrayOfPairs: List[List[Int]]) extends Command

  def apply(): Behavior[Command] = {
    Behaviors.setup(ctx => new TwoSumSolver(ctx))
  }
}

class TwoSumSolver(ctx: ActorContext[TwoSumSolver.Command]) extends AbstractBehavior[TwoSumSolver.Command](ctx){
  import TwoSumSolver._

  def findTwoSums(target: Int, arr: List[Int]): List[List[Int]] = {
    // val result = ArrayBuffer[List[Int]]() // in case we could have duplicates
    val result = mutable.Set.empty[List[Int]]
    val numSet = mutable.Set.empty[Int]

    arr.foreach(number => {
      val complement = target - number
      if (numSet.contains(complement)) {
        result.addOne(List(number, complement))
      } else {
        numSet.addOne(number)
      }
    })

    result.toList
  }

  override def onMessage(msg: Command): Behavior[TwoSumSolver.Command] = {
    msg match {
      case TwoSumRequest(target, array, replyTo) => {
        val result = findTwoSums(target, array)
        replyTo ! TwoSumResponse(result)
        this
      }
    }
  }
}
