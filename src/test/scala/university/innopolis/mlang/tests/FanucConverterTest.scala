package university.innopolis.mlang.tests

import org.scalatest.FlatSpec
import university.innopolis.mlang.program.BuildingContext
import university.innopolis.mlang.program.dsl._
import university.innopolis.mlang.backends.fanuc.{FanucBackend, FanucConverter}


class FanucConverterTest extends FlatSpec {
  "Converter" should "convert correctly" in {
    implicit val builder: BuildingContext = new BuildingContext

    move(Cartesian(1.0, 1.0, 1.0, 1.0, 1.0, 1.0))
    move(Joints(2.0, 2.0, 2.0, 2.0, 2.0, 2.0), velocity = 50, trajectory = Joint)
    "a" := "b"
    "a" := Cartesian(1.0, 1.0, 1.0, 1.0, 1.0, 1.0)

    val program = compile(emit())

    val fanucProgram = FanucBackend(program)

    assertResult(3)(fanucProgram.positions.positions.length)

    val expected = List(
      "LinearInstruction",
      "JointInstruction",
      "PointAssignment",
      "PointAssignment",
    )
    assertResult(expected)(fanucProgram.instructions.instructions.map(_.getClass.getSimpleName))
  }
}
