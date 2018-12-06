package university.innopolis.mlang.tests

import org.scalatest.FlatSpec
import university.innopolis.mlang.program.BuildingContext
import university.innopolis.mlang.program.dsl._
import university.innopolis.mlang.backends.fanuc._
import university.innopolis.mlang.program.ast._

import scala.collection.mutable


class FanucConverterTest extends FlatSpec {
  "Converter" should "convert correctly" in {
    implicit val builder: BuildingContext = new BuildingContext

    move(Cartesian(1.0, 1.0, 1.0, 1.0, 1.0, 1.0))
    move(Joints(2.0, 2.0, 2.0, 2.0, 2.0, 2.0), velocity = 50, trajectory = Joint)
    "a" := "b"
    "a" := Cartesian(1.0, 1.0, 1.0, 1.0, 1.0, 1.0)


    val program = compile(emit())

    val statements = mutable.MutableList[Statement]()
    program.statements.foreach(statements.+=)

    statements += AssignmentStatement(
      DotExpression(
        UnaryExpression(Identifier("val")),
        "x"
      ),
      UnaryExpression(IntLiteral(100))
    )

    val fanucProgram = FanucBackend(Program(program.memory, statements.toList))

    assertResult(3)(fanucProgram.positions.positions.length)

    val expected = List(
      "LinearInstruction",
      "JointInstruction",
      "PointAssignment",
      "PointAssignment",
      "IntegerAssignment"
    )
    assertResult(expected)(fanucProgram.instructions.instructions.map(_.getClass.getSimpleName))

    val fourthInstruction: FanucInstruction = fanucProgram.instructions.instructions(4)
    assertResult(true)(fourthInstruction.isInstanceOf[IntegerAssignment])

    val integerAssignment = fourthInstruction.asInstanceOf[IntegerAssignment]
    val value = integerAssignment.expression.asInstanceOf[IntegerExpression].value
    assertResult(value)(100)

    assertResult(true)(integerAssignment.register.isInstanceOf[PositionCoordinateRegister])
    val posCoordReg: PositionCoordinateRegister = integerAssignment.register.asInstanceOf[PositionCoordinateRegister]
    // X should be converted into 1
    assertResult(1)(posCoordReg.j)
  }
}
