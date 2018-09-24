package de.cnolle.services
import de.cnolle.models.EuroMillions.{DrawResult, PrizeLevel}

object algebra {

  trait TicketService[LotteryTicket, NormalTicket, SystemTicket, F[_]] {

    def expand(s: SystemTicket):Seq[NormalTicket]

    def expandSystemTicketsFromSource(): F[_]

    def ticketToPrizeClass(d: DrawResult, l: LotteryTicket): Option[(PrizeLevel, LotteryTicket)]

    /**
     *  Actually not implemented (due to the use of streaming)
     *  but it shows how functions can compose on an abstract level, without knowledge of the actual
     *  structure of the data.
     * @param d result of the draw
     * @param t the tickets
     * @return
     */
    def normalTicketsByWinningClass(d: DrawResult, t: Seq[LotteryTicket]): Map[PrizeLevel, Seq[LotteryTicket]] = {
      t.foldLeft(Seq.empty[(PrizeLevel, LotteryTicket)]){
        case (acc, l) => acc ++ ticketToPrizeClass(d, l)
      }.groupBy(_._1)
       .mapValues(_.map(_._2))
    }

    def winsByClass(d: DrawResult, t: Seq[LotteryTicket]): Map[PrizeLevel, Int] = normalTicketsByWinningClass(d, t)
      .mapValues(_.length)
  }
}
