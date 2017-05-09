package aggalife

import akka.actor._

import scala.collection.immutable.Map

object PrintActor {
  case class PrintCells(cells: Set[Coordinate])

  def props(): Props = Props(new PrintActor())
}

class PrintActor() extends Actor {
  import PrintActor._

 val gridX: Int = 100
 val gridY: Int = 40
  def receive = {
    case PrintCells(cells) =>
    val gx = gridX/2
    val gy = gridY/2
    (-gy to gy).reverseMap { y =>
      println()
      (-gx to gx).map { x =>
        if(cells.contains(Coordinate(x, y))) {
          print("*")
        } else {
          print("-")
        }
      }
    }
    println()
  }
}
