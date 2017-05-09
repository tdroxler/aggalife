package aggalife

import akka.actor._

object CellActor {
  sealed trait Commmand
  case object Live                      extends Commmand
  case object MakeStep                  extends Commmand
  case object GetStatus                 extends Commmand
  case object GetNeighbors              extends Commmand
  case object Continue                  extends Commmand
  case class AddNeighbor(ref: ActorRef) extends Commmand

  sealed trait Answer
  case class StepDone(
    coordinate: Coordinate,
    status: Status
  ) extends Answer

  sealed trait Status { def inverse: Status }
  case object Alive extends Status { def inverse = Dead  }
  case object Dead  extends Status { def inverse = Alive }

  def props(coordinate: Coordinate, parent: ActorRef): Props = Props(new CellActor(coordinate, parent))
}

class CellActor(coordinate: Coordinate, parent: ActorRef) extends Actor {
  import CellActor._

  def receive = statusReceive(Dead)(Set.empty)

  def statusReceive(status: Status)(neighbors: Set[ActorRef]): Receive = {
    case Live =>
      if (status == Dead) context.become(statusReceive(Alive)(neighbors)) else ()
    case AddNeighbor(neighbor) =>
      context.become(statusReceive(status)(neighbors + neighbor))
    case MakeStep =>
      neighbors.map(_ ! GetStatus)
      context.become(stepping(status, neighbors.map(cell => ((cell, None))).toMap))
    case GetStatus =>
      sender ! status
    case GetNeighbors =>
      sender ! neighbors
  }

  def stepping(status: Status, neighbors: Map[ActorRef, Option[Status]]): Receive = {
    case neighborStatus: Status =>
      val newMap = neighbors + ((sender, Option(neighborStatus)))
      //TODO Optimize this, to avoid flatening newMap at every Message, use a counter?
      val flattenNewMapValues = newMap.values.flatten
      if (flattenNewMapValues.size == neighbors.size) {
        status match {
          case Alive =>
            val aliveNeighbor = flattenNewMapValues.filter(_ == Alive).size
            if (aliveNeighbor == 2 || aliveNeighbor == 3) {
              parent ! StepDone(coordinate, Alive)
              context.become(done(Alive, Alive))
            } else {
              parent ! StepDone(coordinate, Dead)
              context.become(done(Alive, Dead))
            }
          case Dead =>
            if (flattenNewMapValues.filter(_ == Alive).size == 3) {
              parent ! StepDone(coordinate, Alive)
              context.become(done(Dead, Alive))
            } else {
              parent ! StepDone(coordinate, Dead)
              context.become(done(Dead, Dead))
            }
        }
      } else {
        context.become(stepping(status, newMap))
      }
    case GetStatus =>
      sender ! status
    case GetNeighbors =>
      sender ! neighbors.map(_._1)
  }

  def done(status: Status, nextStatus: Status): Receive = {
    case GetStatus =>
      sender ! status
    case Continue =>
      context.become(statusReceive(nextStatus)(Set.empty))
  }
}
