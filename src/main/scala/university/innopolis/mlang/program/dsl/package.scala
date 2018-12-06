package university.innopolis.mlang.program

import university.innopolis.mlang.program.ast._

package object dsl extends TypesMixin with StatementsMixin {

  type BuildingContext = university.innopolis.mlang.program.BuildingContext

  implicit class identifier(val ident: String) {
    def :=(i: Double)(implicit builder: BuildingContext): Unit =
      builder.add(AssignmentStatement(
        UnaryExpression(Identifier(ident)),
        UnaryExpression(FloatLiteral(i)),
      ))
    def :=(ident2: String)(implicit builder: BuildingContext): Unit =
      builder.add(AssignmentStatement(
        UnaryExpression(Identifier(ident)),
        UnaryExpression(Identifier(ident2)),
      ))
    def :=(point: Cartesian)(implicit builder: BuildingContext): Unit =
      builder.add(AssignmentStatement(
        UnaryExpression(Identifier(ident)),
        UnaryExpression(point.typeOperand),
      ))
  }

  case class Cartesian(x: Double, y: Double, z: Double,
                       w: Double, p: Double, r: Double) {
    lazy val typeOperand: MoveTarget = TypeOperand(
      Point,
      Map("x" -> 1.0, "y" -> 1.0, "z" -> 1.0, "w" -> 1.0, "r" -> 1.0, "p" -> 1.0)
        .mapValues(FloatLiteral),
    )
  }

  def emit()(implicit builder: BuildingContext): List[Statement] =
    builder.emit()

  def compile(instructions: List[Statement]) =
    Program(Map(), instructions)

}