package aggalife

import akka.actor._

import scala.collection.immutable.Map

object GodActor {
  sealed trait Command
  case class Start(rate: Long)                  extends Command
  case class AddCell(coordinate: Coordinate)    extends Command
  case class AddCells(coordinates: Set[Coordinate])    extends Command
  case class RemoveCell(coordinate: Coordinate) extends Command

  def props(): Props = Props(new GodActor())
}

class GodActor() extends Actor {
  import GodActor._

  val world = context.actorOf(WorldActor.props(self), name = "world-actor")

  def receive = init(Set.empty)

  def init(cells: Set[Coordinate]): Receive = {
    case AddCell(cell)   => context.become(init(cells + cell))
    case AddCells(cs)   => context.become(init(cells ++ cs))
    case RemoveCell(cell) => context.become(init(cells - cell))
    case Start(rate) =>
      world ! WorldActor.CreateCells(cells)
      world ! WorldActor.MakeStep
      context.become(running(rate, true))

  }

  def running(rate: Long, isRunning: Boolean): Receive = {
    case WorldActor.Done(livingCells) =>
      Thread.sleep(rate)
      world ! WorldActor.MakeStep
  }
}
