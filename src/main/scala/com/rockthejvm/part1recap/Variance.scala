package com.rockthejvm.part1recap

import java.util

object Variance {

  // OOP - substitution -> subtype polymorphism

  class Animal

  class Dog(name: String) extends Animal

  val lassie = new Dog("Lassie")
  val anAnimal: Animal = lassie

  // Variance question for the List[A]
  // if Dog <: Animal, then List[Dog] <: List[Animal] == true?
  // YES - Covariant data types

  val hachi = new Dog("Hachi")
  val laika = new Dog("Laika")

  // covariant
  val someAnimals: List[Animal] = List(lassie, hachi, laika)

  // + means List is covariant in type A
  class MyList[+A]

  val myAnimalList: MyList[Animal] = new MyList[Dog]

  // NO - then the type is INVARIANT

  // mathematical definition of semigroup
  // invariant
  trait Semigroup[A] {
    def combine(a1: A, a2: A): A
  }

  // all generics in Java
  //  val aJavaList: java.util.ArrayList[Animal] = new util.ArrayList[Dog]()

  // HELL NO - Contravariance
  // Dog <: Animal then List[Animal] <: List[Dog] = sounds unintuitive

  trait Vet[-A] {
    def heal(animal: A): Boolean
  }

  // Vet[Animal] is "better" than Vet[Dog] it can treat any animal, dogs too
  // Dog <: Animal, Vet[Dog] >: Vet[Animal]
  val myVet: Vet[Dog] = new Vet[Animal] {
    override def heal(animal: Animal): Boolean =
      println("Here u go")
      true
  }

  val healingLassie: Boolean = myVet heal lassie

  // How to pick variance annotation?

  /*
  * Rule of thumb:
    - if the type PRODUCES or RETRIEVES values of type A (e.g lists, options), then the type should be COVARIANT
    - if the type CONSUMES or ACTS ON values of type A (e.g a vet), then the type should be CONTRAVARIANT
    - otherwise, INVARIANT
  * */

  /**
   * Variance positions
   */

  // Assume the below code compiled fine...
  // class Cat extends Animal
  //  class Vet2[-A](val favoriteAnimal: A) { <-- the types of val fields are in COVARIANT position
  //
  //  }
  //  val garfield             = new Cat
  //  val theVet: Vet2[Animal] = new Vet2[Animal](garfield)
  //  val dogVet: Vet2[Dog]    = theVet
  //  val favAnimal: Dog       = dogVet.favoriteAnimal // type conflict, must be a Dog but is a Cat
  //

  // if the below compiled..
  // class MutableContainer[+A](var contents: A) <- types of vars are in a CONTRAVARIANT position

  // val containerAnimal: MutableContainer[Animal] = new MutableContainer[Dog](new Dog)
  // containerAnimal.contents = new Cat // type conflict

  // types of method arguments are in CONTRAVARIANT position
  //  class MyList2[+A] {
  //    def add(element: A): MyList[A]
  //  }
  // val animals: MyList2[Animal] = new MyList2[Cat]
  // val biggetListOfAnimals: MyList2[Animal] = animals add new Dog // type conflict

  // solution: WIDEN the type argument
  class MyList2[+A] {
    def add[B >: A](element: B): MyList[B] = ???
  }

  // method return types are in COVARIANT position
  //  abstract class Vet2[-A] {
  //    def rescueAnimal(): A
  //  }
  // val vet: Vet2[Animal] = new Vet2[Animal] {
  //  def rescueAnimal(): Animal = new Cat
  // }
  // val lassieVet: Vet2[Dog] = vet
  // val rescueDog: Dog = lassieVet.rescueAnimal() // must return a Dog but returns a Cat - type conflict
  //
  //

  abstract class Vet2[-A] {
    def rescueAnimal[B <: A](): B
  }


  def main(args: Array[String]): Unit = {

  }
}
