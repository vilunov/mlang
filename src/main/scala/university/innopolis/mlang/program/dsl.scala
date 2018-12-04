package university.innopolis.mlang.program

import scala.language.experimental.macros

object dsl {

  def move(i: Int)(implicit builder: BuildingContext): Unit =
    builder.add(InstructionMoveConst(i))

  def move(i: String)(implicit builder: BuildingContext): Unit =
    builder.add(InstructionMoveVar(i))

  implicit class point(val ident: String) {
    def :=(i: Int)(implicit builder: BuildingContext): Unit =
      builder.add(InstructionAssign(ident, i))
  }

  def cond(p: point)(first: => Unit)(second: => Unit)(implicit builder: BuildingContext): Unit = {
    builder.add(ExpressionIdent(p.ident))
    builder.delve()
    first
    builder.delve()
    second
    builder.blockCond()
  }

  def emit()(implicit builder: BuildingContext): List[Instruction] =
    builder.emit()

  def compile(instructions: List[Instruction]) =
    Program(instructions)

}
