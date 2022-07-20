package com.rockthejvm.part2effects

import Effects.MyIO

import java.time.LocalTime
import scala.io.StdIn

object EffectsExercises {

  // exec 1 - measure the current time of the system
  def curTime: MyIO[Long] = MyIO(() => System.currentTimeMillis())
  val currentTime = curTime
  println(currentTime.unsafeRun())

  // exec 2 - measure the duration of a computation
  val dur = for {
    start      <- curTime
    _          <- MyIO(() => {
      println("computing...")
      Thread.sleep(1000)
    })
    stop      <- curTime
  } yield stop - start

  // exec 2
  def measure[A](computation: MyIO[A]): MyIO[(Long, A)] =
    for {
      start  <- curTime
      result <- computation
      end    <- curTime
    } yield (end - start, result)

  def measureAlt[A](computation: MyIO[A]): MyIO[(Long, A)] =
    curTime.flatMap { startTime =>
      computation.flatMap { result =>
        curTime.map { endTime =>
          (endTime - startTime, result)
        }
      }
    }

  // exec 3 & 4 together
  def readFromConsole: MyIO[String] = for {
    _        <- MyIO(() => println("Hello there, what's your name?"))
    userName <- MyIO(() => StdIn.readLine())
    _        <- MyIO(() => print(s"Hello there $userName, I hope you'll have a fantastic time learning ZIO!"))
  } yield userName

  def putStrLn(line: String): MyIO[Unit] = MyIO(() => println(line))
  def putStrLine: String => MyIO[Unit] = line => MyIO(() => println(line))

  def readLine: MyIO[String] = MyIO(() => StdIn.readLine())

  def main(args: Array[String]): Unit = {

  }


}
