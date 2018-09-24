package de.cnolle.validations

import cats.data.Validated
import cats.implicits._
import de.cnolle.models.EuroMillions.{ EuroMillionsSystemTicket, TicketFields }

/**
 * Validations for the 'normal' EuroMillion ticket
 */
trait EuroMillionSystemTicketValidation extends TicketFieldBoundsValidation {

  /**
   * The maximum of number fields to check on a ticket
   */
  private val MaxSizeNumberHits: Int = 10

  /**
   * The minimum of number fields to check on a ticket
   */
  private val MinSizeNumberHits: Int = 5

  /**
   * Max size of star fields to check on a ticket
   */
  private val MaxSizeStarHits: Int = 5

  /**
   * Min size of star fields to check on a ticket
   */
  private val MinSizeStarHits: Int = 2

  def validateEuroMillionSystemTicket(
      n: Set[Int],
      s: Set[Int]
  ): TicketValidationResult[EuroMillionsSystemTicket] =
    (
      validateTicketsLowerNumberBounds(n, s),
      validateTicketsLowerStarBounds(n, s),
      validateTicketsUpperNumberBounds(n),
      validateTicketsUpperStarBounds(s)
    ).mapN {
      case _ => EuroMillionsSystemTicket(TicketFields(n, s))
    }

  private def validateTicketsLowerNumberBounds(
      n: Set[Int],
      s: Set[Int]
  ): TicketValidationResult[(Set[Int], Set[Int])] = (n.size, s.size) match {
    case (numbers, _) if numbers < MinSizeNumberHits => NotEnoughNumberHits.invalidNel
    case (numbers, MinSizeStarHits) if numbers < MinSizeNumberHits + 1 =>
      NotEnoughNumberHits.invalidNel
    case _ => (n, s).validNel
  }

  private def validateTicketsLowerStarBounds(
      n: Set[Int],
      s: Set[Int]
  ): TicketValidationResult[(Set[Int], Set[Int])] = (n.size, s.size) match {
    case (_, stars) if stars < MinSizeStarHits => NotEnoughStarHits.invalidNel
    case (MinSizeNumberHits, starNumbers) if starNumbers < MinSizeStarHits + 1 =>
      NotEnoughStarHits.invalidNel
    case _ => (n, s).validNel
  }

  private def validateTicketsUpperNumberBounds(
      n: Set[Int]
  ): TicketValidationResult[Set[Int]] =
    if (n.size > MaxSizeNumberHits) { TooManyNumberHits.invalidNel } else { n.validNel }

  private def validateTicketsUpperStarBounds(s: Set[Int]): TicketValidationResult[Set[Int]] =
    if (s.size > MaxSizeStarHits) { TooManyStarHits.invalidNel } else { s.validNel }

}
