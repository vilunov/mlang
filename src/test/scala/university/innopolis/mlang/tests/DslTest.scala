package university.innopolis.mlang.tests

import org.scalatest._

import university.innopolis.mlang.program.dsl._
import university.innopolis.mlang.program.ast._

trait DslSpecData {
  lazy val programOneliner: Program = {
    implicit val builder: BuildingContext = new BuildingContext

    move(Cartesian(1.0, 1.0, 1.0, 1.0, 1.0, 1.0))

    compile(emit())
  }

  val expectedOneliner: List[MoveCommand] = List(
    MoveCommand(TypeOperand(
      Point,
      Map("x" -> 1.0, "y" -> 1.0, "z" -> 1.0, "w" -> 1.0, "r" -> 1.0, "p" -> 1.0)
        .mapValues(FloatLiteral),
    )),
  )
}

class DslTest extends FlatSpec with DslSpecData {
  /*
  "Complex program" should "evaluate correctly" in {
    implicit val builder: BuildingContext = new BuildingContext

    val k = 1

    move(k, speed = Some(10))
    move(10)
    "kek" := 1
    move("kek")
    cond ("kek") {
      move(k)
      move(2)
    } {
      move(3)
    }

    val first = emit()

    move(33)
    cond ("shrek") {
      move(13)
      move(23)
    } {
      cond ("inception layer") {
        move(228)
      } {
        move(1488)
      }
    }

    val second = emit()

    val program: Program = compile(first ++ second)

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
        List(
          InstructionMoveConst(3),
        )
      ),
      InstructionMoveConst(33),
      InstructionCondition(ExpressionIdent("shrek"),
        List(
          InstructionMoveConst(13),
          InstructionMoveConst(23),
        ),
        List(
          InstructionCondition(ExpressionIdent("inception layer"),
            List(
              InstructionMoveConst(228),
            ),
            List(
              InstructionMoveConst(1488),
            )
          ),
        )
      ),
    ))(program.statements)
  }
  */

  "Oneliner" should "evaluate correctly" in {
    assertResult(expectedOneliner)(programOneliner.statements)
  }

}
