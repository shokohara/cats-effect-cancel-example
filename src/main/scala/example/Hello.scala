package example

import cats.effect._
import cats.effect.concurrent.MVar
import cats.implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Hello extends Greeting {
  def main(args: Array[String]): Unit = {
    implicit val timer: Timer[IO] = IO.timer(global)
    implicit val contextShift: ContextShift[IO] = IO.contextShift(global)
    val mVar = MVar.empty[IO, Boolean].unsafeRunSync()
    val fiber = (fib(mVar).guaranteeCase(a => IO(println(a)))).start.unsafeRunSync()
    println("started thread")
    fiber.cancel.unsafeRunSync()
    println("requested cancel")
    fiber.join.unsafeRunTimed(2000.millisecond)
    println(mVar.tryTake.unsafeRunSync())
  }

  def fib(mVar: MVar[IO, Boolean])(implicit timer: Timer[IO], cs: ContextShift[IO]): IO[Unit] =
    IO(Thread.sleep(1000)) *> IO.cancelBoundary *> mVar.put(true)
}

trait Greeting {
  lazy val greeting: String = "hello"
}
