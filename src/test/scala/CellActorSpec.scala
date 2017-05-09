package aggalife

import akka.actor.ActorRef
import akka.util._
import scala.util._
import akka.actor._
import akka.testkit._
import scala.concurrent.duration._
import scala.collection.immutable
import akka.testkit.TestActorRef
import scala.concurrent.duration._
import scala.concurrent.Await
import akka.pattern.ask
import org.scalatest._

class CellActorSpec
  extends TestKit(ActorSystem("atlasTestSystem"))
  with ImplicitSender
  with Matchers
  with WordSpecLike
  with DefaultTimeout {
  import CellActor._

  val parent = TestProbe()
  val cellRef = TestActorRef(CellActor.props(Coordinate(0, 0), parent.ref))

  "Fresh cell should be dead" in {
    val Success(deadStatus: Status) = (cellRef ? GetStatus).value.get
    deadStatus should be(CellActor.Dead)
  }

  "Make it alive" in {
    cellRef ! Live
    val Success(aliveStatus: Status) = (cellRef ? GetStatus).value.get
    aliveStatus should be(CellActor.Alive)
  }

  "Add neighbors" in {
    val neighbors = Coordinate.neighbors(Coordinate(0,0)).map { coord =>
      TestActorRef(CellActor.props(coord, parent.ref))
    }
    neighbors.map { neighbor =>
      cellRef ! AddNeighbor(neighbor)
    }
    val Success(neighborsRef) = (cellRef ? GetNeighbors).value.get
    neighborsRef should be(neighbors)
  }

  "`MakeStep` eventually finish and cell send `Done` to parent" in {
    within(500 millis) {
      cellRef ! MakeStep
      parent.expectMsg(StepDone(Coordinate(0,0), Dead))
    }
  }

  "Done status should not change yet" in {
    val Success(doneStatus: Status) = (cellRef ? GetStatus).value.get
    doneStatus should be(CellActor.Alive)
  }

  "Sending `Continue` change to the new status" in {
    cellRef ! Continue
    val Success(continueStatus: Status) = (cellRef ? GetStatus).value.get
    continueStatus should be(CellActor.Dead)
  }

  "Neighbor list is reseted" in {
    val Success(emptyNeighborsRef) = (cellRef ? GetNeighbors).value.get
    emptyNeighborsRef should be(Set.empty)
  }
}
