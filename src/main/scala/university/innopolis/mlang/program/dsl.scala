package university.innopolis.mlang.program

object dsl {

  def move(x: Double, y: Double, z: Double, w: Double, p: Double, r: Double,
           speed: Option[Int] = None, trajectory: Option[String] = None,
           smoothness: Option[Int] = None)
          (implicit builder: BuildingContext): Unit = {
    val params = Map("x" -> x, "y" -> y, "z" -> z, "w" -> w, "r" -> r, "p" -> p).mapValues(FloatLiteral)
    builder.add(MoveCommand(
      TypeOperand(Point, params),
      List(
        speed.map("speed" -> IntLiteral(_)),
        trajectory.map("trajectory" -> StringLiteral(_)),
        smoothness.map("smoothness" -> IntLiteral(_)),
      ).flatten.toMap,
    ))
  }

  def move(i: String)(implicit builder: BuildingContext): Unit =
    builder.add(MoveCommand(Identifier(i)))

  implicit class identifier(val ident: String) {
    def :=(i: Double)(implicit builder: BuildingContext): Unit =
      builder.add(AssignmentStatement(Identifier(ident.ident), FloatLiteral(i)))
  }

  def cond(p: identifier)(first: => Unit)(second: => Unit)
          (implicit builder: BuildingContext): Unit = {
    builder.add(Identifier(p.ident))
    builder.delve()
    first
    builder.delve()
    second
    builder.blockCond()
  }

  def emit()(implicit builder: BuildingContext): List[Statement] =
    builder.emit()

  def compile(instructions: List[Statement]) =
    Program(Map(), instructions)

}
