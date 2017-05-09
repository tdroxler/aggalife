package aggalife

import akka.actor.ActorRef
import akka.util._
import scala.util._
import akka.actor._
import akka.testkit._
import scala.concurrent.duration._
import scala.collection.immutable
import akka.testkit.TestActorRef
import scala.concurrent.Await
import akka.pattern.ask
import org.scalatest._

class WorldActorSpec
  extends TestKit(ActorSystem("atlasTestSystem"))
  with ImplicitSender
  with Matchers
  with WordSpecLike
  with DefaultTimeout {
  import WorldActor._

  val god = TestProbe()
  val world = TestActorRef(WorldActor.props(god.ref))

  "World should create the given cells " in {
    world ! WorldActor.CreateCells(Patterns.blinker)
    val Success(receivedCellCoordinates: CellCoordinates) = (world ? GetCellCoordinates).value.get
    receivedCellCoordinates should be(CellCoordinates(Patterns.blinker))
  }

  "Make step eventually finish " in {
    within(500 millis) {
      world ! MakeStep
      god.expectMsg(Done(Set(Coordinate(1,0), Coordinate(-1,0), Coordinate(0,0))))
    }
  }

  "Blinker pattern" in {
    within(500 millis) {
      world ! MakeStep
      god.expectMsg(Done(Patterns.blinker))
    }
  }

  "Toad pattern" in {
    val worldToad = TestActorRef(WorldActor.props(god.ref))
    worldToad ! WorldActor.CreateCells(Patterns.toad)

    within(500 millis) {
      worldToad ! MakeStep
      god.expectMsg(Done(Set(Coordinate(2,-1), Coordinate(1,-2), Coordinate(1,1), Coordinate(0,-2), Coordinate(-1,0), Coordinate(0,1))))
    }

    within(500 millis) {
      worldToad ! MakeStep
      god.expectMsg(Done(Patterns.toad))
    }
  }
}
