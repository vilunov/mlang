package university.innopolis.mlang.tests

import org.scalatest._

import university.innopolis.mlang.program._

class DslTest extends FlatSpec {

  "Complex program" should "evaluate correctly" in {
    val program = Program {
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

    assertResult(List(
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
    ))(program.instructions)
  }

  "Oneliner" should "evaluate correctly" in {
    val program = Program {
      move(1)
    } compile()

    assertResult(List(
      InstructionMoveConst(1),
    ))(program.instructions)
  }

}
