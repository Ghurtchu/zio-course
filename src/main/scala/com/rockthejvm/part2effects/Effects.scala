package com.rockthejvm.part2effects

object Effects {

  // functional programming
  // EXPRESSIONS

  // mini functional program, easily testable, locally reasonable, no mutable state
  // type signature describes the entire computation to be performed
  def combine(a: Int, b: Int): Int = a + b

  // local reasoning

  // referential transparency = replace expression with value and the meaning of program won't change
  val five: Int   = combine(2, 3)
  val fiveV2: Int = 2 + 3
  val fiveV3: Int = 5

  // not all expressions are RT
  // they are not the same
  val resultOfPrinting: Unit   = println("Learning ZIO")
  val resultOfPrintingV2: Unit = ()

  // example 2: changing a variable
  var anInt: Int = 0
  // reassignment returns Unit but it also does a side effect (changing a value stored in a variable)
  val changingInt: Unit = anInt = 42 // side effect
  val changingIntV2: Unit = () // not the same program

  // side effects are inevitable

  /*
  * Effect properties:
    - the type signature describes what kind of computation it will perform
    - the type signature describes the type of VALUE that it will produce
    - if side effects are required, construction must be separate from the EXECUTION
  * */

  // Example: Option = possibly absent values
  // 1 => check => possibly absent value
  // 2 => check => Some[Int] or None
  // 3 => check => no side effects are needed
  // Option is an effect
  val anOption: Option[Int] = Option(42)

  // Example: Future
  // 1 => check          => describes async computation which will execute on some thread
  // 2 => check          => produces a value of type Int if it finishes and it's successful
  // 3 => does not check => it's not lazy, it's eager baby! :)
  // Future is not an effect
  import scala.concurrent.Future
  import scala.concurrent.ExecutionContext.Implicits.global
  val aFuture: Future[Int] = Future(42)

  // Example: MyIO
  // 1 => check => describes a computation which might perform side effects
  // 2 => check => produces values of type A if the computation is successful
  // 3 => check => side effects are required, yes, because it's lazy = () => A
  case class MyIO[A](unsafeRun: () => A) {
    def map[B](f: A => B): MyIO[B] = MyIO(() => f(unsafeRun()))
    def flatMap[B](f: A => MyIO[B]): MyIO[B] = MyIO(() => f(unsafeRun()).unsafeRun())
  }

  val ioWithSideEffects: MyIO[Int] = MyIO(() => {
    println("producing effect")

    42
  }).flatMap { num =>
    MyIO(() => num + 1)
  }

  def main(args: Array[String]): Unit = {
    val value = ioWithSideEffects.unsafeRun()
    println(value)
  }
}
