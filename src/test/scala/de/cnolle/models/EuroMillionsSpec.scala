package de.cnolle.models
import cats.data.Validated.{ Invalid, Valid }
import cats.data.{ NonEmptyList, Validated }
import de.TicketGenerators
import de.cnolle.CaseStudySpec
import de.cnolle.models.EuroMillions.{
  EuroMillionsNormalTicket,
  EuroMillionsSystemTicket,
  TicketFields
}
import de.cnolle.validations._
import org.scalatest.prop.PropertyChecks

class EuroMillionsSpec extends CaseStudySpec with PropertyChecks with TicketGenerators {

  "The EuroMillionsNormalTicket" must {
    "be valid for every generated valid combination" in {
      forAll(validEuroMillionsNormalTicketGenerator) { t =>
        EuroMillions.normalTicket(t.fields.numbers, t.fields.starNumbers).isValid mustBe true
      }
    }
    "fail on a number out of bounds (over 50)" in {
      EuroMillions.normalTicket(Set(51, 1, 2, 3, 4), Set(1, 2)) mustBe Invalid(
        NonEmptyList(NumberFieldOutOfBounds, List.empty)
      )
    }
    "fail on a number out of bounds (less 1)" in {
      EuroMillions.normalTicket(Set(0, 1, 2, 3, 4), Set(1, 2)) mustBe Invalid(
        NonEmptyList(NumberFieldOutOfBounds, List.empty)
      )
    }
    "fail on a star out of bounds (over 12)" in {
      EuroMillions.normalTicket(Set(50, 1, 2, 3, 4), Set(13, 2)) mustBe Invalid(
        NonEmptyList(StarFieldOutOfBounds, List.empty)
      )
    }
    "fail on a starField out of bounds (less 1)" in {
      EuroMillions.normalTicket(Set(5, 1, 2, 3, 4), Set(0, 2)) mustBe Invalid(
        NonEmptyList(StarFieldOutOfBounds, List.empty)
      )
    }
    "be invalid when not enough numbers are checked" in {
      EuroMillions.normalTicket(Set(1, 2), Set(1, 2)) mustBe Invalid(
        NonEmptyList(NotEnoughNumberHits, List.empty)
      )
    }
    "be invalid when too many numbers are checked" in {
      EuroMillions.normalTicket(Set(1, 2, 3, 4, 5, 6), Set(1, 2)) mustBe Invalid(
        NonEmptyList(TooManyNumberHits, List.empty)
      )
    }
    "be invalid when not enough stars are checked" in {
      EuroMillions.normalTicket(Set(1, 2, 3, 4, 5), Set(2)) mustBe Invalid(
        NonEmptyList(NotEnoughStarHits, List.empty)
      )
    }
    "be invalid when too many stars are checked" in {
      EuroMillions.normalTicket(Set(1, 2, 3, 4, 5), Set(1, 2, 3)) mustBe Invalid(
        NonEmptyList(TooManyStarHits, List.empty)
      )
    }
  }
  "The EuroMillionsSystemTicket" must {
    "be valid for every generated valid combination" in {
      forAll(validEuroMillionsSystemTicketGenerator) { t =>
        EuroMillions.systemTicket(t.fields.numbers, t.fields.starNumbers).isValid mustBe true
      }
    }
    "be invalid for values of a normal ticket" in {
      EuroMillions.systemTicket(Set(1, 2, 3, 4, 5), Set(1, 2)) mustBe Invalid(
        NonEmptyList(NotEnoughNumberHits, List(NotEnoughStarHits))
      )
    }
    "be invalid for not enough numbers chosen" in {
      EuroMillions.systemTicket(Set(1, 2), Set(1, 2)) mustBe Invalid(
        NonEmptyList(NotEnoughNumberHits, List.empty)
      )
    }
    "ba valid for 10 tips" in {
      EuroMillions.systemTicket((1 to 10).toSet, Set(1, 2)) mustBe Valid(
        EuroMillionsSystemTicket(TicketFields((1 to 10).toSet, Set(1, 2)))
      )
    }
  }

  "The smart constructor for tickets" must {
    "validate as a normal ticket" in {
      EuroMillions.ticket(Set(1, 2, 3, 4, 5), Set(1, 2)).getOrElse(fail()) mustBe a[EuroMillionsNormalTicket]
    }
    "validate as a system ticket" in {
      EuroMillions.ticket(Set(1, 2, 3, 4, 5, 6), Set(1, 2)).getOrElse(fail()) mustBe a[EuroMillionsSystemTicket]
    }
  }
}
