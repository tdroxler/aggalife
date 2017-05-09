package aggalife

import akka.actor.ActorSystem
import akka.actor.Props

object GameOfLife {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("aggalife")

    val god = system.actorOf(GodActor.props(), name = "godActor")

    god ! GodActor.AddCells(Patterns.glider)
    god ! GodActor.Start(300)
  }
}
