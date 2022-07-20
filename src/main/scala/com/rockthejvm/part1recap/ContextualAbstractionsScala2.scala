package com.rockthejvm.part1recap

object ContextualAbstractionsScala2 {

  // implicit classes

  case class Person(name: String):
    def greet(): String = s"Hi my name is $name"

  // a way to add additional methods withoud needing to modify internal code - extension methods
  implicit class ImpersonableString(name: String) {
    def greet(): String = Person(name).greet()
  }

  // example: scala.concurrent.duration
  import scala.concurrent.duration._

  val oneSecond: FiniteDuration = 1.second

  // implicit arguments and values
  def increment(x: Int)(implicit amount: Int): Int = x + amount

  def multiply(x: Int)(implicit factor: Int): Int = x * factor

  def main(args: Array[String]): Unit = {
    val greeting = "Peter".greet() // new ImpersonableString("Peter").greet()
    println(greeting)

    implicit val defaultAmount: Int = 10
    assert(increment(50) == 60)

    assert(multiply(1) == 10)

    // more complex example
    trait JsonSerializer[T] {
      def toJson(value: T): String
    }

    def convert2Json[T](value: T)(implicit jsonSerializer: JsonSerializer[T]): String =
      jsonSerializer toJson value

    implicit val personSerializer: JsonSerializer[Person] = (value: Person) => ???

    // implicit defs
    implicit def createListSerializer[T](implicit serializer: JsonSerializer[T]): JsonSerializer[List[T]] =
      new JsonSerializer[List[T]]:
        override def toJson(list: List[T]): String = s"[${list.map(serializer.toJson).mkString(",")}]"

    val personsJson = convert2Json(List(Person("Alice"), Person("Bob")))

    // implicit conversions (not recommended)
    case class Cat(name: String):
      def meow: String = s"$name is meowing"

    implicit def string2Cat(name: String): Cat = Cat(name)
    val aCat: Cat = "Garfield" // dangerous

    def main(args: Array[String]): Unit = {

    }
  }
}
