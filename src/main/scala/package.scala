package aggalife {

  case class Coordinate(x: Int, y: Int)
  object Coordinate {

    val originNeighbors = Set(
      Coordinate(-1, 1),
      Coordinate(0, 1),
      Coordinate(1, 1),
      Coordinate(-1, 0),
      Coordinate(1, 0),
      Coordinate(-1, -1),
      Coordinate(0, -1),
      Coordinate(1, -1)
    )

    def neighbors(coordinate: Coordinate) =
      originNeighbors.map(neighbor => Coordinate(neighbor.x + coordinate.x, neighbor.y + coordinate.y))
  }
}
