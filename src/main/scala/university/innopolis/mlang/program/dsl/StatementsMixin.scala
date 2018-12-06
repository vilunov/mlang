package university.innopolis.mlang.program.dsl

import university.innopolis.mlang.program.ast._

private[dsl] trait StatementsMixin {

  def move(point: MoveTargetExpression,
           velocity: Velocity = Undefined, trajectory: Trajectory = Undefined,
           smoothness: Smoothness = Undefined)
          (implicit builder: BuildingContext): Unit = {

    builder.add(MoveCommand(
      point.typeOperand,
      Map(
        "speed" -> velocity.toVelocityProperty,
        "smoothness" -> smoothness.toSmoothnessProperty,
      ).collect { case (a, Some(b)) => (a, b) } ++ trajectory.toTrajectoryProperty,
    ))
  }

  def move(i: String)(implicit builder: BuildingContext): Unit =
    builder.add(MoveCommand(Identifier(i)))

  def cond(p: identifier)(first: => Unit)(second: => Unit)
          (implicit builder: BuildingContext): Unit = {
    builder.delve()
    first
    builder.delve()
    second
    builder.blockCond(UnaryExpression(Identifier(p.ident)))
  }

  def loop(varName: String, from: Int, to: Int)(body: => Unit)
          (implicit builder: BuildingContext): Unit = {
    builder.delve()
    body
    builder.blockLoop(Identifier(varName), from, to)
  }

}
