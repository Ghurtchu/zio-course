package com.rockthejvm.part2effects

import zio._

object ErrorsVsDefects extends ZIOAppDefault {

  /**
   * Errors  = failures present in the ZIO type signature ("Checked" exceptions)
   * Defects = failures that are unrecoverable, unforeseen, NOT present in the ZIO type signature
   */

  /**
   * ZIO[R, E, A] can finish with Exit[E, A]
   * - Success containing A
   * - Cause[E]
   *   - Fail[E]
   *   - Die(t: Throwable) a throwable which was unforeseen
   */

  // defect = ArithmeticException
  val divisionByZero: UIO[Int] = ZIO.succeed(1 / 0)


  val failedInt: ZIO[Any, String, Int] = ZIO.fail("I failed :(")
  val failureCauseExposed: ZIO[Any, Cause[String], Int] = failedInt.sandbox // wrap error over Cause ds
  val failureCauseHidden: ZIO[Any, String, Int] = failureCauseExposed.unsandbox // get string error message

  // fold with casue
  val foldedWithCause = failedInt.foldCause(
    cause => s"This failed with ${cause.defects}",
    value => s"Value succeeded with $value")

  val foldedWithCauseV2 = failedInt.foldCauseZIO(
    cause => ZIO.succeed(s"This failed with ${cause.defects}"),
    value => ZIO.succeed(s"Value succeeded with $value"))

  /**
   * Good practice:
   * - at a lower level, your "errors" should be treated
   * - at a higher level you should hide "errors" and assume they are unrecoverable
   */

  // turning error into defect
  import java.io.IOException

  def callHTTPEndpoint(url: String): ZIO[Any, IOException, String] =
    ZIO.fail(new IOException("no internet, dummy"))

  // swallow exception and make all errors defects which can not be recovered, not in our control
  val endpointCallWithDefects: ZIO[Any, Nothing, String] = callHTTPEndpoint("rockthejvm.com").orDie

  // refining the error channel
  def callHTTPEndpointWideError(url: String): ZIO[Any, Exception, String] =
    ZIO.fail(new IOException("no internet, dummy"))

  def callHTTPEndpointV2(url: String): ZIO[Any, IOException, String] =
    callHTTPEndpointWideError(url).refineOrDie[IOException] {
      case ioe: IOException => ioe
      case _                => new IOException(s"No route to host to $url, can't fetch page")
    }

  // reverse: turn defects into the error channel
  // throwable to String
  val endpointCallWithError: ZIO[Any, String, String] = endpointCallWithDefects.unrefine {
    case e => e.getMessage
  }

  /**
   * Combine effects with different errors
   */

  sealed trait AppError

  case class IndexError(message: String) extends AppError
  case class DbError(message: String)    extends AppError

  val callApi: ZIO[Any, IndexError, String] = ZIO.succeed("page: <html></html>")
  val queryDb: ZIO[Any, DbError, Int]       = ZIO.succeed(1)

  val combined: ZIO[Any, IndexError | DbError, (String, Int)] = for {
    page         <- callApi
    rowsAffected <- queryDb
  } yield (page, rowsAffected) // lost type safety, because errors are case classes

  /**
   * Solutions:
   *  - design an error model
   *  - use Scala 3 union types (IndexError | DbError) => run pattern matches
   *  - .mapError to some common error type
   */

  /**
   * Exercises
   */

  // 1 - an effect that fails
  // make this effect fail with a TYPED error
  val aBadFailure: ZIO[Any, Nothing, Int] = ZIO.succeed[Int](throw new RuntimeException("this is bad!"))
  val aBetterFailure: ZIO[Any, Cause[Nothing], Int] = aBadFailure.sandbox // exposes the defect in the Cause
  val aBetterFailure2: ZIO[Any, Throwable, Int] = aBadFailure.unrefine {
    case e => e
  }

  // 2 - transform a zio into another zio with a narrower exception type
  def ioException[R, A](zio: ZIO[R, Throwable, A]): ZIO[R, IOException, A] =
    zio.refineOrDie {
      case ioe: IOException => ioe
    }

  // 3
  def left[R, E, A, B](zio: ZIO[R, E, Either[A, B]]): ZIO[R, Either[E, A], B] =
    zio.foldZIO(
      e      => ZIO.fail(Left(e)),
      either => either match
        case Left(value)  => ZIO.fail(Right(value))
        case Right(value) => ZIO.succeed(value)
    )

  // 4
  val database: Map[String, Int] = Map (
    "daniel" -> 123,
    "alice"  -> 789
  )

  case class QueryError(reason: String)
  case class UserProfile(name: String, phone: Int)

  def lookupProfile(userId: String): ZIO[Any, QueryError, Option[UserProfile]] =
    if userId != userId.toLowerCase then ZIO.fail(QueryError("user ID format is invalid"))
    else ZIO.succeed(database.get(userId).map(phone => UserProfile(userId, phone)))

  // surface out all the failed cases of this API
  def betterLookupProfile(userId: String): ZIO[Any, Option[QueryError], UserProfile] =
    lookupProfile(userId).foldZIO(
      queryError => ZIO.fail(Some(queryError)),
      profOption => profOption.fold(ZIO.fail(None))(ZIO.succeed)
    )

  def betterLookupProfileV2(userId: String): ZIO[Any, Option[QueryError], UserProfile] =
    lookupProfile(userId).some


  override def run = ???

}
