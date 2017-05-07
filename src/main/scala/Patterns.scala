package aggalife

object Patterns {

  val blinker = Set(
    Coordinate(0, -1), Coordinate(0, 0), Coordinate(0, 1)
  )

  val toad = Set(
    Coordinate(0, 0),  Coordinate(1, 0), Coordinate(0, 1),
    Coordinate(0, -1), Coordinate(1, -1), Coordinate(1, -2)
  )

  val glider = Set(
    Coordinate(0, 1), Coordinate(1, 0), Coordinate(-1, -1),
    Coordinate(0, -1), Coordinate(1, -1)
  )

  val diehard = Set(
    Coordinate(-3, 0), Coordinate(-2, 0), Coordinate(-2, -1),
    Coordinate(2, -1), Coordinate(3, -1), Coordinate(4, -1),
    Coordinate(3, 1)
  )

  val acorn = Set(
    Coordinate(-3, -1), Coordinate(-2, -1), Coordinate(-2, 1),
    Coordinate(0, 0), Coordinate(1, -1), Coordinate(2, -1),
    Coordinate(3, -1)
  )
}
