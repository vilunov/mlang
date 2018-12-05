package university.innopolis.mlang.program

import university.innopolis.mlang.program.ast._

package object dsl extends TypesMixin with StatementsMixin {

  type BuildingContext = university.innopolis.mlang.program.BuildingContext

  implicit class identifier(val ident: String) {
    def :=(i: Double)(implicit builder: BuildingContext): Unit =
      builder.add(AssignmentStatement(
        UnaryExpression(Identifier(ident)),
        UnaryExpression(FloatLiteral(i))
      ))
  }

  def emit()(implicit builder: BuildingContext): List[Statement] =
    builder.emit()

  def compile(instructions: List[Statement]) =
    Program(Map(), instructions)

}
