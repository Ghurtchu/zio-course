package com.rockthejvm.part2effects

import zio._

object ZIOErrorHandling extends ZIOAppDefault {

  // ZIOs can fail
  val aFailedZIO            = ZIO.fail("Something went wrong")
  val failedWithThrowable   = ZIO.fail(new RuntimeException("splash")) // exception is not thrown, it's just constructed
  val failedWithDescription = failedWithThrowable.mapError(_.getMessage) // fails with string after mapping it

  // attempt: try to run an effect that might throw an exception
  val badZIO                = ZIO.succeed {
    println("Trying something...")
    val string: String = null

    string.length // null pointer exception
  } // this is bad, "succeed" must never fail, instead use "attempt"

  val betterZIO: ZIO[Any, Throwable, Int] = ZIO.attempt {
    println("Trying something...")
    val string: String = null

    string.length // null pointer exception
  } // might throw an exception, "attempt"

  // effectfully catch errors
  val catchError           = betterZIO.catchAll(err => ZIO.succeed(s"Returning a different value because $err"))

  val catchSelectiveErrors = betterZIO.catchSome {
    case e: RuntimeException => ZIO.succeed(s"Ignoring runtime exceptions $e")
    case _                   => ZIO.succeed("Ignoring everything else")
  }

  // chain effects
  val aBetterAttempt       = betterZIO.orElse(ZIO.succeed(56))

  // fold
  val handleBoth           = betterZIO.fold(ex => s"Something went wrong $ex", suc => s"Length of the string was $suc")

  // effectfu fold
  val handleBothV2         = betterZIO.foldZIO(ex => ZIO.succeed(s"Something went wrong $ex"), suc => ZIO.succeed(s"Length of the string was $suc"))

  /*
  * Conversions between scala stdlib(Try, Option, Either) types to ZIO
  * */

  import scala.util.{Try, Success, Failure}

  val aTryToZIO: Task[Int] = ZIO.fromTry(Try(42 / 0)) // can fail with Throwable => Task[Int]

  val anEither: Either[Int, String] = Right("Success!")
  val anEitherToZIO: IO[Int, String] = ZIO.fromEither(anEither)

  // option -> ZIO
  val anOption: IO[Option[Nothing], Int] = ZIO.fromOption(Some(42))

  /**
   * Exercise: implement a version of fromTry, fromOption, fromEither, either, absolve
   * using fold and foldZIO
   */

  def tryToZIO[A](aTry: Try[A]): Task[A] = aTry.fold(ZIO.fail, ZIO.succeed)

  def eitherToZIO[E, A](anEither: Either[E, A]): ZIO[Any, E, A] = anEither.fold(ZIO.fail, ZIO.succeed)

  def optionToZIO[A](anOption: Option[A]): ZIO[Any, Option[Nothing], A] = anOption.fold(ZIO.fail(None))(ZIO.succeed)

  def zioToZIOEither[R, A, B](zio: ZIO[R, A, B]): ZIO[R, Nothing, Either[A, B]] = zio.foldZIO(
    f => ZIO.succeed(Left(f)),
    s => ZIO.succeed(Right(s))
  )

  def absolveZIO[R, A, B](zio: ZIO[R, Nothing, Either[A, B]]): ZIO[R, A, B] = zio.flatMap {
    case Left(error)  => ZIO.fail(error)
    case Right(value) => ZIO.succeed(value)
  }

  override def run = ???
}
