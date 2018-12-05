package university.innopolis.mlang.tests

import org.scalatest._

import university.innopolis.mlang.program._

trait ParserSpecData {
  val programSimple: String =
    """memory {
      |}
      |
      |program {
      |  start = end;
      |  move start;
      |}
      |""".stripMargin

  val expectedSimple: Program = Program(
    memory = Map(),
    instructions = List()
  )
}

class ParserSpec extends FlatSpec with ParserSpecData {
  "Parser" should "process simple program" in {
    assertResult(expectedSimple)(Parser.parse(programSimple))
  }
}
