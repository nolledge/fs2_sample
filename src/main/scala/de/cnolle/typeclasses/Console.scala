package de.cnolle.typeclasses
import cats.Show
import cats.effect.IO
import cats.implicits._
import de.cnolle.models.EuroMillions.{ PrizeLevel, Ticket }

/**
 * Typeclass for console interaction
 *
 * @tparam F context
 */
trait Console[F[_]] {
  def printTicket(ticket: Ticket)(implicit s: Show[Ticket]): F[Unit]
  def printDrawResult(r: Map[PrizeLevel, Int]): F[Unit]
}

object Console {

  def apply[F[_]](implicit c: Console[F]): Console[F] = c

  // Instances
  implicit val ioConsole: Console[IO] = new Console[IO] {
    override def printTicket(ticket: Ticket)(implicit s: Show[Ticket]): IO[Unit] = IO.pure(println(ticket.show))

    override def printDrawResult(r: Map[PrizeLevel, Int]): IO[Unit] = {
      IO.pure(r.foreach {
        case (level, winningTickets) =>
          println(s"Prizelevel $level has $winningTickets winning tickets")
      })
  }}
}
