package example

import java.util.Scanner

import cats.effect._
import cats.implicits._
import scala.concurrent.duration._

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

  def fib(n: Int, a: Long, b: Long)(implicit cs: ContextShift[IO], timer: Timer[IO]): IO[Long] =
    IO.cancelBoundary *> IO.sleep(1.milliseconds) *> IO.cancelBoundary *> IO(()).flatMap(_ =>
      if (n == 0) IO.pure(a) else {
        println(n)
        // Every 100-th cycle, check cancellation status
        if (n % 100 == 0) {
          //          IO.cancelBoundary *>
          fib(n - 1, b, a + b)
        } else fib(n - 1, b, a + b)
      })
}

trait Greeting {
  lazy val greeting: String = "hello"
}
