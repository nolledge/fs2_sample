package de.cnolle.models
import cats.Show
import de.cnolle.validations.{
  DrawValidation,
  EuroMillionSystemTicketValidation,
  EuroMillionsNormalTicketValidation,
  TicketValidationResult
}

object EuroMillions
    extends EuroMillionSystemTicketValidation
    with EuroMillionsNormalTicketValidation
    with DrawValidation {

  /**
   * Basic data structure for EuroMillionsTickets and EuroMillionsSystemTickets
   *
   * @param numbers *number* fields, containing Ints between 1-50 (included)
   * @param starNumbers star fields, containing Integers between 1-11 (included)
   */
  case class TicketFields(numbers: Set[Int], starNumbers: Set[Int])

  /**
   * A draw result also contains numbers and  stars
   */
  type DrawResult = TicketFields

  /**
   * Sum type for lottery tickets
   */
  sealed trait Ticket {
    def fields: TicketFields
  }

  object Ticket {
    implicit val show: Show[Ticket] = {
      case EuroMillionsNormalTicket(fields) =>
        s"EuroMillionsNormalTicket with numbers ${fields.numbers} and stars ${fields.starNumbers}"
      case EuroMillionsSystemTicket(fields) =>
        s"EuroMillionsSystemTicket with numbers ${fields.numbers} and stars ${fields.starNumbers}"
    }

  }

  /**
   * The normal ticket with 5 number and 2 star fields
   *
   * @param fields
   */
  case class EuroMillionsNormalTicket(fields: TicketFields) extends Ticket

  /**
   * The system ticket with up to 10 number and up to 5 star fields
   *
   * @param fields
   */
  case class EuroMillionsSystemTicket(fields: TicketFields) extends Ticket

  /**
   * Smart constructor to create validated normal ticket
   *
   * @param numbers
   * @param stars
   * @return
   */
  def normalTicket(
      numbers: Set[Int],
      stars: Set[Int]
  ): TicketValidationResult[EuroMillionsNormalTicket] =
    validateEuroMillionNormalTicket(numbers, stars)

  /**
   * Smart constructor to create a validated system ticket and avoid illegal states
   *
   * @param numbers
   * @param stars
   * @return
   */
  def systemTicket(
      numbers: Set[Int],
      stars: Set[Int]
  ): TicketValidationResult[EuroMillionsSystemTicket] =
    validateEuroMillionSystemTicket(numbers, stars)

  /**
   * Smart constructor to create a validated ticket of unspecified type
   *
   * @param numbers
   * @param stars
   * @return
   */
  def ticket(
      numbers: Set[Int],
      stars: Set[Int]
  ): TicketValidationResult[Ticket] = {
    normalTicket(numbers, stars) orElse systemTicket(numbers, stars)
  }

  /**
   * Smart constructor to create a validated draw result
   *
   * @param numbers
   * @param stars
   * @return
   */
  def drawResult(
      numbers: Set[Int],
      stars: Set[Int]
  ): TicketValidationResult[DrawResult] = validateDraw(numbers, stars)

  sealed trait PrizeLevel
  case object WinningClass1 extends PrizeLevel
  case object WinningClass2 extends PrizeLevel
  case object WinningClass3 extends PrizeLevel
  case object WinningClass4 extends PrizeLevel
  case object WinningClass5 extends PrizeLevel
  case object WinningClass6 extends PrizeLevel
  case object WinningClass7 extends PrizeLevel
  case object WinningClass8 extends PrizeLevel
  case object WinningClass9 extends PrizeLevel
  case object WinningClass10 extends PrizeLevel
  case object WinningClass11 extends PrizeLevel
  case object WinningClass12 extends PrizeLevel
  case object WinningClass13 extends PrizeLevel

  def prizeLevelByCorrectHits(correctNumbers: Int, correctStarNumbers: Int): Option[PrizeLevel] =
    (correctNumbers, correctStarNumbers) match {
      case (5, 2) => Some(WinningClass1)
      case (5, 1) => Some(WinningClass2)
      case (5, 0) => Some(WinningClass3)
      case (4, 2) => Some(WinningClass4)
      case (4, 1) => Some(WinningClass5)
      case (4, 0) => Some(WinningClass6)
      case (3, 2) => Some(WinningClass7)
      case (2, 2) => Some(WinningClass8)
      case (3, 1) => Some(WinningClass9)
      case (3, 0) => Some(WinningClass10)
      case (1, 2) => Some(WinningClass11)
      case (2, 1) => Some(WinningClass12)
      case (2, 0) => Some(WinningClass13)
      case _      => None
    }
}
