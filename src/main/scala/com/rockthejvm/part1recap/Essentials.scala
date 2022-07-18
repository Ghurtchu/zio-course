package com.rockthejvm.part1recap

import scala.concurrent.ExecutionContext

object Essentials:

  def main(args: Array[String]): Unit =

    // values
    val aBoolean: Boolean = false
    val aString: String   = "Rock the JVM"

    // expressions
    type StringOrInt = String | Int
    val anIfExpression: StringOrInt = if 2 > 3 then 5 else "str"

    // instructions vs expressions
    // instruction is also expression which returns Unit
    // Unit is same as void in java
    val theUnit: Unit = println("Hello, Scala") // returns () which is an instance of Unit


    // Object Oriented Programming = program consists of objects that send messages to each other
    abstract class Animal(private val name: String)

    case class Dog(name: String, age: Int) extends Animal(name)
    case class Cat(name: String, favFood: String) extends Animal(name)

    trait Carnivore:
      def eat(animal: Animal): Unit

    // inheritance model
    // extend at most one class but several traits (called mixin composition)
    class Crocodile(name: String) extends Animal(name) with Carnivore:
      override def eat(animal: Animal): Unit =
        println(s"${this.name} is eating an $animal.. delicious!")

    // singleton
    object Singleton // single running instance

    // companion objects for classes = same name
    object Carnivore:
      def staticMethod(): Unit = println("Same for all instances of carnivore")

    // generics - parametric/type polymorphism
    class MyList[A]

    // method notation
    val three: Int = 1 + 2
    // OOP = everything is an object, baby!
    val methodNotationThree: Int = 1.+(2)

    // functional programming
    val incrementer: Int => Int = _ + 1

    // higher order functions
    def applyFunc(func: Int => Int)(value: Int): Int = func(value)
    applyFunc(_ + 1)(5) // 6
    applyFunc(_ * 2)(10) // 20

    def applyFuncAndGetFuncBack(f: Int => String)(value: Int): String => Array[Char] =
      val str: String = f(value)

      string => (str concat string).toCharArray

    println(applyFuncAndGetFuncBack(_.toString)(5)("10").mkString("Array(", ", ", ")")) // 5, 1, 0

    // map, flatMap, filter
    val processedList: List[Int] = (1 to 10).toList.map(incrementer)

    val aLongerList: List[Int] = (1 to 10).toList.flatMap(x => List(x, x * 2)) // List(1, 2, 2, 4, 3, 6 ...)

    // options and try
    val anOption      = Option(4) // Some(4) = Option.apply[T](value: T)
    val doubledOption = anOption.map(_ * 2) // if Some(5) then Some(10) else None

    val anAttempt        = scala.util.Try(5 / 0) // Failure(ArithmeticException) or Success(value)
    val aModifiedAttempt = anAttempt.map(_ + 10) // if Success(5) then Success(10) else Failure(some exception)

    // pattern matching
    val anUnknown: Any  = 45

    val ordinal: String = anUnknown match
      case 1 => "Found one."
      case 2 => "Found two."
      case _ => "Found some weirdness which is neither a one nor two."

    anOption match
      case Some(value) => println("anOption has some value")
      case _           => println("None baby")

    // Futures
    // implicit execution context needed - basically a thread pool

    import java.util.concurrent.Executors
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(8))

    import scala.concurrent.Future

    // by name param which means lazy/delayed evaluation
    val aFuture: Future[Int] = Future {

      42
    }

    // wait for completion (async)
    // it's a partial function because it's defined only for a certain amount of inputs
    aFuture.onComplete {
      case scala.util.Success(value) => println(s"The async meaning of life is $value")
      case scala.util.Failure(ex)    => ex.printStackTrace()
    }

    // map a Future
    // val anotherFuture: Future[Int] = aFuture.map(_ + 1)

    // for-comprehensions
    val checkerboard = List(1, 2, 3).flatMap(n => List('a', 'b', 'c').map(c => (n, c)))
    println(checkerboard)

    val checkerboardForCompr =
      for
        n <- List(1, 2, 3)
        c <- List('a', 'b', 'c')
      yield (n, c)

    println(checkerboardForCompr)

    // partial function - not defined for all cases
    val aPartialFunction: PartialFunction[Int, Int] = {
      case 1 => 42
      case 2 => 55
    }

    // some more advanced bits - higher kinded type
    // F[_] is a type constructor, realizations: Option[Int], List[String], Try[Unit]
    trait Higher[F[_]] {
      def isSequential: Boolean
    }

    val listChecker = new Higher[List] {
      override def isSequential: Boolean = true
    }
