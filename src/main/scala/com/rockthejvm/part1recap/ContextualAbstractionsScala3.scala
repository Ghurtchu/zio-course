package com.rockthejvm.part1recap

object ContextualAbstractionsScala3 {

  // given/using combo

  def increment(x: Int)(amount: Int): Int = x + amount
  val twelve: Int = increment(2)(10) // if the number 10 can be passed default we can use "given" and "using"

  given ten: Int = 10
  def inc(x: Int)(using amount: Int): Int= x + amount

  inc(5) // returns 15

  def multiply(x: Int)(using factor: Int): Int = x * factor

  val thousand: Int = multiply(100) // 1000

  // more complex use case

  trait Monoid[A] {
    def combine(x: A, y: A): A
    def empty: A
  }

  def combineAll[A](values: List[A])(using monoid: Monoid[A]): A =
    values.foldLeft(monoid.empty)(monoid.combine)

  given intMonoid: Monoid[Int] = new Monoid[Int]:
    override def combine(x: Int, y: Int): Int = x + y
    override def empty: Int = 0

  val summedList: Int = combineAll(List(1, 2, 3, 4, 5))
  //  val error: String = combineAll(List("str1", "str2", "str3"))

  // synthesize given instances
  given optionCombiner[T](using monoid: Monoid[T]): Monoid[Option[T]] with {
    override def empty: Option[T] = Some(monoid.empty)
    override def combine(x: Option[T], y: Option[T]): Option[T] = for {
      vx <- x
      vy <- y
    } yield monoid.combine(vx, vy)
  }

  val sumOptions: Option[Int] = combineAll(List(Some(1), None, Some(2)))

  // extension methods
  case class Person(name: String) {
    def greet(): String = s"Hi, my name is $name"
  }

  extension(name: String)
    def greet(): String = Person(name).greet()

  val alicesGreeting = "Alice".greet()

  extension [T](list: List[T])
    def reduceAll(using monoid: Monoid[T]): T = list.foldLeft(monoid.empty)(monoid.combine)

  val six: Int = List(1, 2, 3).reduceAll

  def main(args: Array[String]): Unit = {

  }
}
