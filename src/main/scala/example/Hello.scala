package example

import java.util.Scanner

import cats.effect._
import cats.implicits._

import scala.concurrent.ExecutionContext.Implicits.global

object Hello extends Greeting {
  def main(args: Array[String]): Unit = {
    implicit val timer: Timer[IO] = IO.timer(global)
    implicit val contextShift: ContextShift[IO] = IO.contextShift(global)
    val fiber0 = fib(Int.MaxValue, 1, 1).start
    val fiber = fiber0.unsafeRunSync()
    println("Press Enter to Cancel")
    val scanner = new Scanner(System.in)
    scanner.nextLine()
    fiber.cancel.unsafeRunSync()
  }

  def fib(n: Int, a: Long, b: Long)(implicit cs: ContextShift[IO]): IO[Long] =
    IO.suspend {
      if (n == 0) IO.pure(a) else {
        val next = fib(n - 1, b, a + b)
        // Every 100-th cycle, check cancellation status
        if (n % 100 == 0) {
          println(n)
          IO.cancelBoundary *> next
        } else next
      }
    }
}

trait Greeting {
  lazy val greeting: String = "hello"
}
