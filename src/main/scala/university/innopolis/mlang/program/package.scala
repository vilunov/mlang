package university.innopolis.mlang

package object program {
  type BuildFunc = BuildingContext => Unit

  def move(i: Int)(implicit builder: BuildingContext): Unit =
    builder.add(InstructionMoveConst(i))

  def move(i: String)(implicit builder: BuildingContext): Unit =
    builder.add(InstructionMoveVar(i))

  implicit class point(val ident: String) {
    def :=(i: Int)(implicit builder: BuildingContext): Unit =
      builder.add(InstructionAssign(ident, i))
  }

  def cond(p: point)(first: BuildFunc)(second: BuildFunc)(implicit builder: BuildingContext): Unit = {
    val leftBuilder = new BuildingContext
    val rightBuilder = new BuildingContext
    first(leftBuilder)
    second(rightBuilder)

    builder.add(InstructionCondition(
      ExpressionIdent(p.ident),
      leftBuilder.instructions.toList,
      rightBuilder.instructions.toList))
  }

}
