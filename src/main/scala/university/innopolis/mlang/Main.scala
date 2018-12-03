package university.innopolis.mlang

import program._

object Main extends App {
  val program1 = Program {
    move(1)
    move(10)
    "kek" := 1
    move("kek")
    cond ("kek") {
      move(1)
      move(2)
    } {
      move(3)
    }
  } compile()

  assert(program1.instructions == List(
    InstructionMoveConst(1),
    InstructionMoveConst(10),
    InstructionAssign("kek", 1),
    InstructionMoveVar("kek"),
    InstructionCondition(ExpressionIdent("kek"),
      List(
        InstructionMoveConst(1),
        InstructionMoveConst(2),
      ),
      List(InstructionMoveConst(3))
    )
  ), program1.instructions)

  val program2 = Program {
    move(1)
  } compile()
  assert(program2.instructions == List(
    InstructionMoveConst(1),
  ))
}
