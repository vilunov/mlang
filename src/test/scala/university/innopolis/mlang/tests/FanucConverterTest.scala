package university.innopolis.mlang.tests

import org.scalatest.FlatSpec

import university.innopolis.mlang.program.BuildingContext
import university.innopolis.mlang.program.dsl._
import university.innopolis.mlang.backends.fanuc.FanucConverter


class FanucConverterTest extends FlatSpec {
  "Converter" should "convert correctly" in {
    implicit val builder: BuildingContext = new BuildingContext

    move(1.0, 1.0, 1.0, 1.0, 1.0, 1.0)
    move(2.0, 2.0, 2.0, 2.0, 2.0, 2.0, velocity = 50, trajectory = Joint)
    "a" := "b"
    "a" := Cartesian(1.0, 1.0, 1.0, 1.0, 1.0, 1.0)

    val program = compile(emit())

    val converter: FanucConverter = new FanucConverter(program)
    val (fanucInstructions, positions) = converter.convert()

    val positionsAmount: Int = 3

    assertResult(positionsAmount)(positions.size)

    val expected = List(
      "LinearInstruction",
      "JointInstruction",
      "PointAssignment",
      "PointAssignment",
    )
    assertResult(expected)(fanucInstructions.map(_.getClass.getSimpleName))
  }
}
