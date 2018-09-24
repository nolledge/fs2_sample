package de.cnolle.repositories
import de.cnolle.models.EuroMillions.{DrawResult, EuroMillionsSystemTicket, Ticket}

object algebra {

  trait SystemTicketRepository[F[_]] {
    def all(): fs2.Stream[F, EuroMillionsSystemTicket]
  }

  trait MixedTicketRepository[F[_]] {
    def all(): fs2.Stream[F, Ticket]
  }


  trait DrawRepository[F[_]] {
    def first(): fs2.Stream[F, DrawResult]
  }

}
