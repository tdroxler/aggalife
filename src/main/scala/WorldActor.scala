package aggalife

import akka.actor._

object WorldActor {
  sealed trait Command
  case object MakeStep                                     extends Command
  case object GetCellCoordinates                           extends Command
  case class CreateCells(cellCoordinates: Set[Coordinate]) extends Command

  sealed trait Answer
  case class Done(livingCells: Set[Coordinate]) extends Answer
  case class CellCoordinates(coordinates: Set[Coordinate])

  def props(god: ActorRef): Props = Props(new WorldActor(god))
}

class WorldActor(god: ActorRef) extends Actor {
  import WorldActor._

  def receive = init

  def init(): Receive = {
    case CreateCells(cellCoordinates) =>
      context.become(
        idle(
          cellCoordinates.map { cellCoordinate =>
            val cellRef = context.actorOf(CellActor.props(cellCoordinate, self))
            cellRef ! CellActor.Live
            (cellCoordinate, cellRef)
          }.toMap
        )
      )
  }

  def idle(cells: Map[Coordinate, ActorRef]): Receive = {
    case MakeStep =>
      val newCells = cells ++ createMissingNeighbors(cells)
      newCells.values.map(_ ! CellActor.MakeStep)
      context.become(running(newCells.map { case (coord, ref) => (ref, None) }))
    case GetCellCoordinates =>
      sender ! CellCoordinates(cells.keys.toSet)
  }

  def running(cells: Map[ActorRef, Option[(Coordinate, CellActor.Status)]]): Receive = {
    case CellActor.StepDone(coord, status) =>
      val newCells = cells + ((sender, Some((coord, status))))
      if (newCells.values.toList.contains(None)) {
        //Some cells are not done yet, we wait for their answers
        context.become(running(newCells))
      } else {
        //All cells are done, we can tell it to God
        val livingCells = newCells.flatMap {
          case (ref, opt) =>
            opt.flatMap {
              case ((coord, status)) =>
                if (status == CellActor.Alive) {
                  //Make living cells to continue
                  ref ! CellActor.Continue
                  Option(coord, ref)
                } else {
                  //And stop dead cells
                  ref ! PoisonPill
                  None
                }
            }
        }
        god ! Done(livingCells.map { case (coord, _) => coord }.toSet)
        context.become(
          idle(
            livingCells
          )
        )
      }
  }

  def createMissingNeighbors(cells: Map[Coordinate, ActorRef]): Map[Coordinate, ActorRef] =
    cells.foldLeft(Map.empty: Map[Coordinate, ActorRef]) {
      case (freshCells, (coordinate, cellRef)) =>
        freshCells ++ Coordinate.neighbors(coordinate).flatMap { neighborCoord =>
          (freshCells ++ cells)
            .get(neighborCoord)
            .map { neighborRef =>
              // Neighbor exists, we send it to the cell
              cellRef ! CellActor.AddNeighbor(neighborRef)
              // And send to the neighbor his neighbor
              neighborRef ! CellActor.AddNeighbor(cellRef)
              //Don't need to add it to the world's cells (since already exists)
              None
            }
            .getOrElse {
              // Neighbor doesn't exist, we create and send it to the cell
              val neighborRef = context.actorOf(CellActor.props(neighborCoord, self))
              cellRef ! CellActor.AddNeighbor(neighborRef)
              // And send to the new cell his neighbor
              neighborRef ! CellActor.AddNeighbor(cellRef)
              //Add it to the world's cells
              Option((neighborCoord, neighborRef))
            }
        }
    }
}
