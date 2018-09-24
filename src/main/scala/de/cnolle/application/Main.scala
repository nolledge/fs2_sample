package de.cnolle.application
import cats.effect.IO
import de.cnolle.Module
import de.cnolle.typeclasses.Console._



object Main extends App {

  private val module = new Module[IO]

//  module.ticketService.expandSystemTicketsFromSource().unsafeRunSync()

  module.ticketService.evaluateDraw().unsafeRunSync()
}
