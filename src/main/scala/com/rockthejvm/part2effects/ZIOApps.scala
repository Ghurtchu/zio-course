package com.rockthejvm.part2effects

import zio._

object ZIOApps {

  val meaningOfLife: UIO[Int] = for {
    _ <- ZIO.succeed(println("let's go"))
  } yield 42

  def main(args: Array[String]): Unit = {
    val runtime = Runtime.default
    given trace: Trace = Trace.empty
    Unsafe.unsafeCompat { unsafe =>
      given u: Unsafe = unsafe

      runtime.unsafe.run(meaningOfLife)
    }
  }
}

object BetterApp extends ZIOAppDefault {

  // provides runtime, trace and all the other bits
  override def run = ZIOApps.meaningOfLife.debug
}
