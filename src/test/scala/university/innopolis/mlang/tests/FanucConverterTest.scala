package university.innopolis.mlang.tests

import org.scalatest.FlatSpec
import university.innopolis.mlang.program.dsl.{compile, emit, move}
import university.innopolis.mlang.program._
import university.innopolis.mlang.backends.converter._
import university.innopolis.mlang.backends.fanuc.{JointInstruction, LinearInstruction, PointAssignment}
import university.innopolis.mlang.program

import scala.collection.mutable

class FanucConverterTest extends FlatSpec {
  "Converter" should "convert correctly" in {
    implicit val builder: BuildingContext = new BuildingContext

    move(1.0, 1.0, 1.0, 1.0, 1.0, 1.0)
    move(2.0, 2.0, 2.0, 2.0, 2.0, 2.0, speed = Option(50), trajectory = Option("joint"))
    val program = compile(emit())

    val statements = mutable.MutableList[Statement]()
    program.statements.foreach(statements.+=)
    statements += AssignmentStatement(Identifier("a"), Identifier("b"))
    statements += AssignmentStatement(Identifier("a"), TypeOperand(
      Point,
      Map("x" -> 1.0, "y" -> 1.0, "z" -> 1.0, "w" -> 1.0, "r" -> 1.0, "p" -> 1.0)
        .mapValues(FloatLiteral),
    ))

    val converter: FanucConverter = new FanucConverter(Program(program.memory, statements.toList))
    val result = converter.convert()
    val fanucInstructions = result._1
    val positions = result._2

    val positionsAmount: Int = 3

    assertResult(positionsAmount)(positions.size)

    assertResult(List(
      LinearInstruction.getClass.getSimpleName,
      JointInstruction.getClass.getSimpleName,
      PointAssignment.getClass.getSimpleName,
      PointAssignment.getClass.getSimpleName
    ))(fanucInstructions.map(_.getClass.getSimpleName))
  }
}