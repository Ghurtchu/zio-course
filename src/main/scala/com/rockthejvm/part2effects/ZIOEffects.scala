package com.rockthejvm.part2effects

import zio._

object ZIOEffects {

  // success
  val meaningOfLife: ZIO[Any, Nothing, Int]      = ZIO.succeed(42)
  // failure
  val aFailure     : ZIO[Any, String, Nothing]   = ZIO.fail("Something wen't wrong")
  // suspension/delay
  val aSuspendedZIO: ZIO[Any, Throwable, Int]    = ZIO.suspend(meaningOfLife)

  // map + flatMap
  val improvedMOL: ZIO[Any, Nothing, Int] = meaningOfLife.map(_ * 2)
  val printingMOL: ZIO[Any, Nothing, Unit] = meaningOfLife.flatMap(res => ZIO.succeed(println(res)))

  // for comprehension
  val smallProgram: ZIO[Any, Throwable, Unit] = for {
    _    <- ZIO.succeed(println("what's your name?"))
    name <- ZIO.succeed(scala.io.StdIn.readLine())
    _    <- ZIO.succeed(println(s"Welcome to ZIO $name"))
  } yield ()

  // A Lot of combinators
  // zip

  val anotherMol: ZIO[Any, Nothing, Int] = ZIO.succeed(100)
  val tupledZIO: ZIO[Any, Nothing, (Int, Int)] = meaningOfLife.zip(anotherMol)
  val combinedZIO: ZIO[Any, Nothing, Int] = meaningOfLife.zipWith(anotherMol)(_ * _)

  // type aliases

  // UIO = ZIO[Any, Nothing, A] - no requirements, cannot fail, produces A (Universal IO)
  val aUIO: UIO[Int] = ZIO.succeed(99)
  // URIO = ZIO[R, Nothing, A] - requirement, cannot fail, produces A
  val aURIO: URIO[Int, Int] = ZIO.succeed(65)
  // RIO = ZIO[R, Throwable, A] - requirement, fail with Throwable, produces A
  val anRIO: RIO[Int, Int] = ZIO.succeed(42)
  val aFailedRIO: RIO[Int, Int] = ZIO.fail(new RuntimeException("RIO failed"))

  // Task[A] = ZIO[Any, Throwable, A] - no requirements, can fail with a Throwable, produces A
  val aSuccessfulTask: Task[Int] = ZIO.succeed(150)
  val aFailedTask: Task[Int] = ZIO.fail(new RuntimeException("Something bad"))

  // IO[E, A] = ZIO[Any, E, A] - no requirements
  val aSuccessfulZIO: IO[String, Int] = ZIO.succeed(42)
  val aFailedIO: IO[String, Int] = ZIO.fail("Something bad happened")

  /**
   * Exercises
   */

  // 1 - sequence two ZIOs and take the value of the last one
  def sequenceTakeLast[R, E, A, B](zioa: ZIO[R, E, A], ziob: ZIO[R, E, B]): ZIO[R, E, B] = for
    _ <- zioa
    b <- ziob
  yield b

  def sequenceTakeLastV2[R, E, A, B](zioa: ZIO[R, E, A], ziob: ZIO[R, E, B]): ZIO[R, E, B] =
    zioa.flatMap(_ => ziob.map(identity))

  def sequenceTakeLastV3[R, E, A, B](zioa: ZIO[R, E, A], ziob: ZIO[R, E, B]): ZIO[R, E, B] =
    zioa *> ziob

  def sequenceTakeLastV4[R, E, A, B](zioa: ZIO[R, E, A], ziob: ZIO[R, E, B]): ZIO[R, E, B] =
    zioa zipRight ziob


  // 2 - sequence two ZIOs and take the value of the first one
  def sequenceTakeFirst[R, E, A, B](zioa: ZIO[R, E, A], ziob: ZIO[R, E, B]): ZIO[R, E, A] = for {
    a <- zioa
    _ <- ziob
  } yield a

  def sequenceTakeFirstV2[R, E, A, B](zioa: ZIO[R, E, A], ziob: ZIO[R, E, B]): ZIO[R, E, A] =
    zioa.zipLeft(ziob)

  def sequenceTakeFirstV3[R, E, A, B](zioa: ZIO[R, E, A], ziob: ZIO[R, E, B]): ZIO[R, E, A] =
    zioa <* ziob

  // 3 - run a ZIO forever
  def runForever[R, E, A](zio: ZIO[R, E, A]): ZIO[R, E, A] = zio.flatMap(_ => runForever(zio))

  def runForeverV2[R, E, A](zio: ZIO[R, E, A]): ZIO[R, E, A] = zio *> runForeverV2(zio)

  // 4 convert the value of a ZIO to something else
  def convert[R, E, A, B](zio: ZIO[R, E, A], value: B): ZIO[R, E, B] = for {
    _ <- zio
    b <- ZIO.succeed(value)
  } yield b

  def convertV2[R, E, A, B](zio: ZIO[R, E, A], value: B): ZIO[R, E, B] =
    zio.map(_ => value)

  def convertV3[R, E, A, B](zio: ZIO[R, E, A], value: B): ZIO[R, E, B] =
    zio as value

  // 5 - discard the value of a ZIO to Unit
  def asUnit[R, E, A](zio: ZIO[R, E, A]): ZIO[R, E, Unit] = zio.as(ZIO.succeed(()))

  def asUnitV2[R, E, A](zio: ZIO[R, E, A]): ZIO[R, E, Unit] = zio.unit

  // 6 - recursion
  // this will crash at sum(20000)
  def sum(n: Int): Int = if n == 0 then 0 else n + sum(n - 1)

  def sumZIO(n: Int): UIO[Int] = if n == 0 then ZIO.succeed(0) else for {
    current <- ZIO.succeed(n)
    prevSum <- sumZIO(n - 1)
  } yield current + prevSum

  // 7 - fibonacci on ZIO
  // hint: use ZIO.suspend
  def fibo(n: Int): BigInt =
    if n <= 2 then 1
    else fibo(n - 1) + fibo(n - 2)

  def fibZio(n: Int): UIO[BigInt] = if n <= 2 then ZIO.succeed(1) else for {
    last <- fibZio(n - 1)
    prev <- fibZio(n - 2)
  } yield last + prev

  def main(args: Array[String]): Unit = {

  }

}

object SimpleZioImpl {
  // design a simple front facing API for ZIO (mocking)
  // contravariant in env type and covariant in error & success channels
  case class MockZIO[-R, +E, +A](unsafeRun: R => Either[E, A]) {

    def map[B](f: A => B): MockZIO[R, E, B] =
      MockZIO(r => unsafeRun(r) match
        case Left(e)  => Left(e)
        case Right(v) => Right(f(v)))

    def flatMap[R1 <: R, E1 >: E, B](f: A => MockZIO[R1, E1, B]): MockZIO[R1, E1, B] =
      MockZIO(r => unsafeRun(r) match
        case Left(value)  => Left(value)
        case Right(value) => f(value).unsafeRun(r))
  }
}
