package de.cnolle
import cats.effect.Effect
import de.cnolle.typeclasses.Console
import de.cnolle.repositories.{DrawRepository, MixedTicketsRepository, StreamedJsonFileSource, SystemTicketRepository}
import de.cnolle.services.TicketService

// Custom DI module
class Module[F[_] : Effect: Console] {

  lazy val systemTicketRepository: SystemTicketRepository[F] = new SystemTicketRepository[F]()
  lazy val mixedTicketRepository: MixedTicketsRepository[F] = new MixedTicketsRepository[F]()
  lazy val drawRepository: DrawRepository[F] = new DrawRepository[F]()

  lazy val ticketService: TicketService[F] = new TicketService[F](systemTicketRepository, drawRepository, mixedTicketRepository)

}
