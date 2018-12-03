package university.innopolis.mlang

import scala.language.experimental.macros

package object program {
  type BuildFunc = BuildingContext => Unit

  def move(i: Int): BuildFunc = builder =>
    builder.add(InstructionMoveConst(i))

  def move(i: String): BuildFunc = builder =>
    builder.add(InstructionMoveVar(i))

  implicit class point(val ident: String) {
    def :=(i: Int): BuildFunc = builder =>
      builder.add(InstructionAssign(ident, i))
  }

  def cond(p: point)(first: BuildFunc)(second: BuildFunc): BuildFunc =
    macro Macros.condImpl

  def condImpl(p: point)(first: BuildFunc)(second: BuildFunc): BuildFunc = {
    val leftBuilder = new BuildingContext
    val rightBuilder = new BuildingContext
    first(leftBuilder)
    second(rightBuilder)

    _.add(InstructionCondition(
      ExpressionIdent(p.ident),
      leftBuilder.instructions.toList,
      rightBuilder.instructions.toList))
  }

  def applyImpl(program: BuildFunc): Precompiled = {
    val builder = new BuildingContext
    program(builder)
    new Precompiled(builder.instructions.toList)
  }

}
