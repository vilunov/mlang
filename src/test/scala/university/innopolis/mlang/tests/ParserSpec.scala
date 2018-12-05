package university.innopolis.mlang.tests

import org.scalatest._

import university.innopolis.mlang.program.Parser
import university.innopolis.mlang.program.ast._

trait ParserSpecData {
  val programSimple: String =
    """memory {
      |}
      |
      |program {
      |  start = end;
      |  move start;
      |  move start: {
      |    trajectory: Fine,
      |    speed: 20
      |  }
      |}
      |""".stripMargin

  val expectedSimple: Program = Program(
    memory = Map(),
    statements = List(
      AssignmentStatement(UnaryExpression(Identifier("start")), UnaryExpression(Identifier("end"))),
      MoveCommand(Identifier("start")),
      MoveCommand(Identifier("start"), Map(
        "trajectory" -> Identifier("Fine"),
        "speed" -> IntLiteral(20),
      )),
    )
  )
}

class ParserSpec extends FlatSpec with ParserSpecData {
  "Parser" should "process simple program" in {
    assertResult(expectedSimple)(Parser.parse(programSimple))
  }
}
