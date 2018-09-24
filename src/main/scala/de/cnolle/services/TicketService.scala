package de.cnolle.services

import cats.Monad
import cats.effect.Sync
import cats.implicits._
import de.cnolle.models.EuroMillions
import de.cnolle.models.EuroMillions._
import de.cnolle.repositories.algebra.{
  DrawRepository,
  MixedTicketRepository,
  SystemTicketRepository
}
import de.cnolle.typeclasses.Console
class TicketService[F[_]: Sync: Console: Monad](
    systemTicketRepository: SystemTicketRepository[F],
    drawRepository: DrawRepository[F],
    ticketRepository: MixedTicketRepository[F]
) extends algebra.TicketService[Ticket, EuroMillionsNormalTicket, EuroMillionsSystemTicket, F] {

  /**
   * Combines fields from system tickets to all possible normal tickets
   * (Task 1)
   *
   * @param s
   * @return
   */
  override def expand(
      s: EuroMillionsSystemTicket
  ): Seq[EuroMillionsNormalTicket] =
    for {
      numbers <- s.fields.numbers.subsets(5).toList
      stars <- s.fields.starNumbers.subsets(2).toList
    } yield EuroMillionsNormalTicket(TicketFields(numbers, stars))

  /**
   * Expand system tickets read from file
   * (Task 2)
   *
   * @return
   */
  override def expandSystemTicketsFromSource(): F[Unit] = {
    systemTicketRepository
      .all()
      .map(expand)
      .flatMap(group => fs2.Stream.emits(group))
      .evalMap(printTicket)
      .compile
      .drain
  }


  /**
   * Given a draw result, a ticket gets mapped on its prize level (if it is a winning ticket).
   * Provides functionality for ticketsByWinningClass in underlying algebra
   *
   * @param d draw result
   * @param l the ticket (system or normal ticket)
   * @return
   */
  override def ticketToPrizeClass(
      d: DrawResult,
      l: Ticket
  ): Option[(EuroMillions.PrizeLevel, Ticket)] =
    EuroMillions
      .prizeLevelByCorrectHits(
        d.numbers.intersect(l.fields.numbers).size,
        d.starNumbers.intersect(l.fields.starNumbers).size
      )
      .map(p => p -> l)

  /**
   *  Reads mixed tickets (system tickets as well as normal tickets) from a source and combines them with
   *  the draw result of another source.
   *  Combines them to a map of prize level to winning tickets to determine the number
   *  System tickets will be expanded to normal tickets.
   * @return
   */
  def evaluateDraw(): F[Unit] =
    ticketRepository
      .all()
      .flatMap[EuroMillionsNormalTicket] {
        case t: EuroMillionsSystemTicket => fs2.Stream.emits(expand(t))
        case t: EuroMillionsNormalTicket => fs2.Stream.emits(Seq(t))
      }
      .zip(drawRepository.first().repeat)
      .map { case (t, r) => ticketToPrizeClass(r, t) }
      .collect { case Some((r, _)) => (r, 1) }
      .fold(Map.empty[PrizeLevel, Int]) { case (acc, elem) => acc |+| Map(elem) }
      .evalMap(printDrawResult)
      .compile
      .drain


  /**
   * Uses Console to write a Ticket
   *
   * @param out the ticket to write
   * @return
   */
  private def printTicket(out: Ticket): F[Unit] = Console[F].printTicket(out)


  /**
   * Uses Console to write a Draw result
   *
   * @param out the draw result to write
   * @return
   */
  private def printDrawResult(r: Map[PrizeLevel, Int]): F[Unit] = Console[F].printDrawResult(r)
}
