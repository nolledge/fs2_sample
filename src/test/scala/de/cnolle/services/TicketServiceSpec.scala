package de.cnolle.services
import cats.data.StateT
import cats.effect.IO
import cats.effect.Sync._
import cats.implicits._
import cats.{ Monoid, Show }
import de.TicketGenerators
import de.cnolle.CaseStudySpec
import de.cnolle.models.EuroMillions._
import de.cnolle.repositories.algebra.{
  DrawRepository,
  MixedTicketRepository,
  SystemTicketRepository
}
import de.cnolle.typeclasses.Console
import org.scalatest.prop.PropertyChecks

class TicketServiceSpec extends CaseStudySpec with PropertyChecks with TicketGenerators {

  case class TestData(ticketOuts: Seq[Ticket], drawResultOut: Map[PrizeLevel, Int])

  implicit val testDataMonoid: Monoid[TestData] = new Monoid[TestData] {
    override def empty: TestData = TestData(Seq.empty, Map.empty)
    override def combine(x: TestData, y: TestData): TestData = {
      TestData(x.ticketOuts ++ y.ticketOuts, x.drawResultOut |+| y.drawResultOut)
    }
  }

  /**
   * Using state monad for capturing console interaction
   *
   * @tparam A
   */
  type TestIO[A] = StateT[IO, TestData, A]

//  implicit val testConsole: Console[TestIO] = (line: String) => {
//    StateT.modify[IO, TestData] { s => s |+| TestData(List(line))
//    }
//  }

  implicit val testConsole: Console[TestIO] = new Console[TestIO] {
    override def printTicket(ticket: Ticket)(implicit s: Show[Ticket]): TestIO[Unit] =
      StateT.modify[IO, TestData] { s => s |+| TestData(List(ticket), Map.empty)
      }
    override def printDrawResult(
        r: Map[PrizeLevel, Int]
    ): TicketServiceSpec.this.TestIO[Unit] = StateT.modify[IO, TestData] { s =>
      s |+| TestData(List.empty, r)
    }
  }

  trait DefaultTestSetup {
    val systemTicketFromStream = validEuroMillionsSystemTicketGenerator.sample.get
    val systemTicketRepository = new SystemTicketRepository[TestIO] {
      override def all(): fs2.Stream[TestIO, EuroMillionsSystemTicket] = {
        fs2.Stream.emit(systemTicketFromStream)
      }
    }
    val drawRepository = new DrawRepository[TestIO] {
      override def first(): fs2.Stream[TestIO, DrawResult] = {
        fs2.Stream.emit(TicketFields(Set(1, 2, 3, 4, 5, 6), Set(1, 2)))
      }
    }
    val mixedTicketRepository = new MixedTicketRepository[TestIO] {
      override def all(): fs2.Stream[TestIO, Ticket] = {
        fs2.Stream.emit(validTicketGenerator.sample.get)
      }
    }
    val service =
      new TicketService[TestIO](systemTicketRepository, drawRepository, mixedTicketRepository)
  }
  object CustomTestSetup {
    def apply(
        systemTicket: EuroMillionsSystemTicket = validEuroMillionsSystemTicketGenerator.sample.get,
        drawResult: DrawResult = validTicketFieldsGenerator.sample.get,
        ticket: Ticket = validTicketGenerator.sample.get
    ): TicketService[TestIO] = {

      val systemTicketRepository = new SystemTicketRepository[TestIO] {
        override def all(): fs2.Stream[TestIO, EuroMillionsSystemTicket] = {
          fs2.Stream.emit(systemTicket)
        }
      }
      val drawRepository = new DrawRepository[TestIO] {
        override def first(): fs2.Stream[TestIO, DrawResult] = {
          fs2.Stream.emit(drawResult)
        }
      }
      val mixedTicketRepository = new MixedTicketRepository[TestIO] {
        override def all(): fs2.Stream[TestIO, Ticket] = {
          fs2.Stream.emit(ticket)
        }
      }
        new TicketService[TestIO](systemTicketRepository, drawRepository, mixedTicketRepository)
    }
  }

  "The TicketService" must {
    "expand to the expected size of normal tickets" in new DefaultTestSetup {
      forAll(validEuroMillionsSystemTicketGenerator) { t =>
        val expectedSize = binomialCoefficient(t.fields.numbers.size, 5) * binomialCoefficient(
          t.fields.starNumbers.size,
          2
        )
        service
          .expand(t)
          .size mustBe expectedSize
      }
    }
    "expand a system ticket as in the case study document" in new DefaultTestSetup {
      service.expand(EuroMillionsSystemTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2, 3)))) mustBe Seq(
        EuroMillionsNormalTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2))),
        EuroMillionsNormalTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(1, 3))),
        EuroMillionsNormalTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(2, 3)))
      )
    }
  }
  "The TicketService#ticketsByWinningClass" must {
    "not map invalid tickets at all" in new DefaultTestSetup {
      forAll(invalidTicketGenerator) { i =>
        val winningTicketFields = TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2))
        service.normalTicketsByWinningClass(winningTicketFields, Seq(i)) mustBe Map.empty
      }
    }
    "map class1 win as expected" in new DefaultTestSetup {
      val winningTicketFields = TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2))
      val winningTicket = EuroMillionsNormalTicket(winningTicketFields)
      service.normalTicketsByWinningClass(winningTicketFields, Seq(winningTicket)) mustBe Map(
        WinningClass1 -> List(winningTicket)
      )
    }
    "map class2 win as expected" in new DefaultTestSetup {
      val winningTicketFields = TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2))
      val winningTicket = EuroMillionsNormalTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(1, 3)))
      service.normalTicketsByWinningClass(winningTicketFields, Seq(winningTicket)) mustBe Map(
        WinningClass2 -> List(winningTicket)
      )
    }
    "map class3 win as expected" in new DefaultTestSetup {
      val winningTicketFields = TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2))
      val winningTicket = EuroMillionsNormalTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(4, 3)))
      service.normalTicketsByWinningClass(winningTicketFields, Seq(winningTicket)) mustBe Map(
        WinningClass3 -> List(winningTicket)
      )
    }
    "map class4 win as expected" in new DefaultTestSetup {
      val winningTicketFields = TicketFields(Set(1, 2, 3, 4, 50), Set(1, 2))
      val winningTicket = EuroMillionsNormalTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2)))
      service.normalTicketsByWinningClass(winningTicketFields, Seq(winningTicket)) mustBe Map(
        WinningClass4 -> List(winningTicket)
      )
    }
    "map class5 win as expected" in new DefaultTestSetup {
      val winningTicketFields = TicketFields(Set(1, 2, 3, 4, 50), Set(1, 3))
      val winningTicket = EuroMillionsNormalTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2)))
      service.normalTicketsByWinningClass(winningTicketFields, Seq(winningTicket)) mustBe Map(
        WinningClass5 -> List(winningTicket)
      )
    }
    "map class6 win as expected" in new DefaultTestSetup {
      val winningTicketFields = TicketFields(Set(1, 2, 3, 4, 50), Set(4, 3))
      val winningTicket = EuroMillionsNormalTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2)))
      service.normalTicketsByWinningClass(winningTicketFields, Seq(winningTicket)) mustBe Map(
        WinningClass6 -> List(winningTicket)
      )
    }
    "map class7 win as expected" in new DefaultTestSetup {
      val winningTicketFields = TicketFields(Set(1, 2, 3, 40, 50), Set(1, 2))
      val winningTicket = EuroMillionsNormalTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2)))
      service.normalTicketsByWinningClass(winningTicketFields, Seq(winningTicket)) mustBe Map(
        WinningClass7 -> List(winningTicket)
      )
    }
    "map class8 win as expected" in new DefaultTestSetup {
      val winningTicketFields = TicketFields(Set(1, 2, 30, 40, 50), Set(1, 2))
      val winningTicket = EuroMillionsNormalTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2)))
      service.normalTicketsByWinningClass(winningTicketFields, Seq(winningTicket)) mustBe Map(
        WinningClass8 -> List(winningTicket)
      )
    }
    "map class9 win as expected" in new DefaultTestSetup {
      val winningTicketFields = TicketFields(Set(1, 2, 3, 40, 50), Set(1, 3))
      val winningTicket = EuroMillionsNormalTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2)))
      service.normalTicketsByWinningClass(winningTicketFields, Seq(winningTicket)) mustBe Map(
        WinningClass9 -> List(winningTicket)
      )
    }
    "map class10 win as expected" in new DefaultTestSetup {
      val winningTicketFields = TicketFields(Set(1, 2, 3, 40, 50), Set(4, 3))
      val winningTicket = EuroMillionsNormalTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2)))
      service.normalTicketsByWinningClass(winningTicketFields, Seq(winningTicket)) mustBe Map(
        WinningClass10 -> List(winningTicket)
      )
    }
    "map class11 win as expected" in new DefaultTestSetup {
      val winningTicketFields = TicketFields(Set(1, 20, 30, 40, 50), Set(1, 2))
      val winningTicket = EuroMillionsNormalTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2)))
      service.normalTicketsByWinningClass(winningTicketFields, Seq(winningTicket)) mustBe Map(
        WinningClass11 -> List(winningTicket)
      )
    }
    "map class12 win as expected" in new DefaultTestSetup {
      val winningTicketFields = TicketFields(Set(1, 2, 30, 40, 50), Set(1, 3))
      val winningTicket = EuroMillionsNormalTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2)))
      service.normalTicketsByWinningClass(winningTicketFields, Seq(winningTicket)) mustBe Map(
        WinningClass12 -> List(winningTicket)
      )
    }
    "map class13 win as expected" in new DefaultTestSetup {
      val winningTicketFields = TicketFields(Set(1, 2, 30, 40, 50), Set(4, 3))
      val winningTicket = EuroMillionsNormalTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2)))
      service.normalTicketsByWinningClass(winningTicketFields, Seq(winningTicket)) mustBe Map(
        WinningClass13 -> List(winningTicket)
      )
    }
    "map two results as expected" in new DefaultTestSetup {
      val winningTicketFields = TicketFields(Set(1, 2, 30, 40, 50), Set(4, 3))
      val winningTicket = EuroMillionsNormalTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2)))
      val winningTicket2 = EuroMillionsNormalTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2)))
      service.normalTicketsByWinningClass(winningTicketFields, Seq(winningTicket, winningTicket2)) mustBe Map(
        WinningClass13 -> List(winningTicket, winningTicket2)
      )
    }
  }

  "The TicketService#printNormalTicketsFromFile" must {
    "log as much lines as expected" in new DefaultTestSetup {
      val expectedSize = binomialCoefficient(systemTicketFromStream.fields.numbers.size, 5) * binomialCoefficient(
        systemTicketFromStream.fields.starNumbers.size,
        2
      )
      service
        .expandSystemTicketsFromSource()
        .runS(testDataMonoid.empty)
        .unsafeRunSync()
        .ticketOuts
        .size mustBe expectedSize
    }
  }

  "The TicketService#evaluateDraw" must {
    "not map invalid tickets at all" in new DefaultTestSetup {
      forAll(invalidTicketGenerator) { i =>
      CustomTestSetup(drawResult = TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2)), ticket = i)
          .evaluateDraw().runS(testDataMonoid.empty).unsafeRunSync() mustBe testDataMonoid.empty
      }
    }
    "print class1 win as expected" in new DefaultTestSetup {
      val winningTicketsField = TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2))
      val winningTicket = EuroMillionsNormalTicket(winningTicketsField)
      CustomTestSetup(drawResult = winningTicketsField, ticket = winningTicket)
        .evaluateDraw().runS(testDataMonoid.empty).unsafeRunSync().drawResultOut mustBe Map(
        WinningClass1 -> 1
      )
    }
    "print class2 win as expected" in new DefaultTestSetup {
      val winningTicketFields = TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2))
      val winningTicket = EuroMillionsNormalTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(1, 3)))
      CustomTestSetup(drawResult = winningTicketFields, ticket = winningTicket)
        .evaluateDraw().runS(testDataMonoid.empty).unsafeRunSync().drawResultOut mustBe Map(
        WinningClass2 -> 1
      )
    }
    "print class3 win as expected" in new DefaultTestSetup {
      val winningTicketFields = TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2))
      val winningTicket = EuroMillionsNormalTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(4, 3)))
      CustomTestSetup(drawResult = winningTicketFields, ticket = winningTicket)
        .evaluateDraw().runS(testDataMonoid.empty).unsafeRunSync().drawResultOut mustBe Map(
        WinningClass3 -> 1
      )
    }
    "print class4 win as expected" in new DefaultTestSetup {
      val winningTicketFields = TicketFields(Set(1, 2, 3, 4, 50), Set(1, 2))
      val winningTicket = EuroMillionsNormalTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2)))
      CustomTestSetup(drawResult = winningTicketFields, ticket = winningTicket)
        .evaluateDraw().runS(testDataMonoid.empty).unsafeRunSync().drawResultOut mustBe Map(
        WinningClass4 -> 1
      )
    }
    "print class5 win as expected" in new DefaultTestSetup {
      val winningTicketFields = TicketFields(Set(1, 2, 3, 4, 50), Set(1, 3))
      val winningTicket = EuroMillionsNormalTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2)))
      CustomTestSetup(drawResult = winningTicketFields, ticket = winningTicket)
        .evaluateDraw().runS(testDataMonoid.empty).unsafeRunSync().drawResultOut mustBe Map(
        WinningClass5 -> 1
      )
    }
    "print class6 win as expected" in new DefaultTestSetup {
      val winningTicketFields = TicketFields(Set(1, 2, 3, 4, 50), Set(4, 3))
      val winningTicket = EuroMillionsNormalTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2)))
      CustomTestSetup(drawResult = winningTicketFields, ticket = winningTicket)
        .evaluateDraw().runS(testDataMonoid.empty).unsafeRunSync().drawResultOut mustBe Map(
        WinningClass6 -> 1
      )
    }
    "print class7 win as expected" in new DefaultTestSetup {
      val winningTicketFields = TicketFields(Set(1, 2, 3, 40, 50), Set(1, 2))
      val winningTicket = EuroMillionsNormalTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2)))
      CustomTestSetup(drawResult = winningTicketFields, ticket = winningTicket)
        .evaluateDraw().runS(testDataMonoid.empty).unsafeRunSync().drawResultOut mustBe Map(
        WinningClass7 -> 1
      )
    }
    "print class8 win as expected" in new DefaultTestSetup {
      val winningTicketFields = TicketFields(Set(1, 2, 30, 40, 50), Set(1, 2))
      val winningTicket = EuroMillionsNormalTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2)))
      CustomTestSetup(drawResult = winningTicketFields, ticket = winningTicket)
        .evaluateDraw().runS(testDataMonoid.empty).unsafeRunSync().drawResultOut mustBe Map(
        WinningClass8 -> 1
      )
    }
    "print class9 win as expected" in new DefaultTestSetup {
      val winningTicketFields = TicketFields(Set(1, 2, 3, 40, 50), Set(1, 3))
      val winningTicket = EuroMillionsNormalTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2)))
      CustomTestSetup(drawResult = winningTicketFields, ticket = winningTicket)
        .evaluateDraw().runS(testDataMonoid.empty).unsafeRunSync().drawResultOut mustBe Map(
        WinningClass9 -> 1
      )
    }
    "print class10 win as expected" in new DefaultTestSetup {
      val winningTicketFields = TicketFields(Set(1, 2, 3, 40, 50), Set(4, 3))
      val winningTicket = EuroMillionsNormalTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2)))
      CustomTestSetup(drawResult = winningTicketFields, ticket = winningTicket)
        .evaluateDraw().runS(testDataMonoid.empty).unsafeRunSync().drawResultOut mustBe Map(
        WinningClass10 -> 1
      )
    }
    "print class11 win as expected" in new DefaultTestSetup {
      val winningTicketFields = TicketFields(Set(1, 20, 30, 40, 50), Set(1, 2))
      val winningTicket = EuroMillionsNormalTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2)))
      CustomTestSetup(drawResult = winningTicketFields, ticket = winningTicket)
        .evaluateDraw().runS(testDataMonoid.empty).unsafeRunSync().drawResultOut mustBe Map(
        WinningClass11 -> 1
      )
    }
    "print class12 win as expected" in new DefaultTestSetup {
      val winningTicketFields = TicketFields(Set(1, 2, 30, 40, 50), Set(1, 3))
      val winningTicket = EuroMillionsNormalTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2)))
      CustomTestSetup(drawResult = winningTicketFields, ticket = winningTicket)
        .evaluateDraw().runS(testDataMonoid.empty).unsafeRunSync().drawResultOut mustBe Map(
        WinningClass12 -> 1
      )
    }
    "print class13 win as expected" in new DefaultTestSetup {
      val winningTicketFields = TicketFields(Set(1, 2, 30, 40, 50), Set(4, 3))
      val winningTicket = EuroMillionsNormalTicket(TicketFields(Set(1, 2, 3, 4, 5), Set(1, 2)))
      CustomTestSetup(drawResult = winningTicketFields, ticket = winningTicket)
        .evaluateDraw().runS(testDataMonoid.empty).unsafeRunSync().drawResultOut mustBe Map(
        WinningClass13 -> 1
      )
    }
  }
}
