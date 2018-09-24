package de

import de.cnolle.models.EuroMillions.{
  EuroMillionsSystemTicket,
  EuroMillionsNormalTicket,
  Ticket,
  TicketFields
}
import de.cnolle.models.FieldRecord
import org.scalacheck.Gen

trait TicketGenerators {

  val validNumberFields = 1 to 50
  val validStarFields = 1 to 12

  /**
   * Generates valid ticket fields (for draw results or normal tickets)
   */
  val validTicketFieldsGenerator: Gen[TicketFields] = for {
    numbers <- Gen.pick(5, validNumberFields).map(_.toSet)
    stars <- Gen.pick(2, validStarFields).map(_.toSet)
  } yield TicketFields(numbers, stars)

  /**
   * Generates normal tickets
   */
  val validEuroMillionsNormalTicketGenerator: Gen[EuroMillionsNormalTicket] =
    validTicketFieldsGenerator.map(EuroMillionsNormalTicket)

  /**
   * Generates valid system tickets
   */
  val validEuroMillionsSystemTicketGenerator: Gen[EuroMillionsSystemTicket] = for {
    numbersSize <- Gen.chooseNum(5, 10)
    numbers <- Gen.pick(numbersSize, validNumberFields).map(_.toSet)
    starsSizeMin = if (numbersSize == 5) 3 else 2
    starsSize <- Gen.chooseNum(starsSizeMin, 5)
    stars <- Gen.pick(starsSize, validStarFields).map(_.toSet)
  } yield
    EuroMillionsSystemTicket(
      TicketFields(
        numbers,
        stars
      )
    )

  /**
   * Generates tickets of SumType tickets
   */
  val validTicketGenerator: Gen[Ticket] = for {
    ticket <- Gen.oneOf(
      validEuroMillionsSystemTicketGenerator,
      validEuroMillionsNormalTicketGenerator
    )
  } yield ticket

  val invalidFieldsGenerator: Gen[TicketFields] = for {
    length <- Gen.choose(-100, 100)
    starLength <- Gen.choose(-100, 100)
    randomInt = Gen.chooseNum(Integer.MIN_VALUE, Integer.MAX_VALUE) suchThat (
        e => !validNumberFields.contains(e)
    )
    numbers <- Gen.listOfN(length, randomInt)
    stars <- Gen.listOfN(length, starLength)
  } yield TicketFields(numbers.toSet, stars.toSet)

  val invalidNormalTicketGenerator: Gen[EuroMillionsNormalTicket] =
    invalidFieldsGenerator.map(EuroMillionsNormalTicket)

  val invalidSystemTicketGenerator: Gen[EuroMillionsSystemTicket] =
    invalidFieldsGenerator.map(EuroMillionsSystemTicket)

  val invalidTicketGenerator: Gen[Ticket] = for {
    ticket <- Gen.oneOf(invalidNormalTicketGenerator, invalidSystemTicketGenerator)
  } yield ticket

  val validSystemTicketRecordGenerator =
    validTicketFieldsGenerator.map(f => FieldRecord(f.numbers, f.starNumbers))
}
