package university.innopolis.mlang.backends.fanuc

import java.time.LocalDateTime

import university.innopolis.mlang.backends.Backend
import university.innopolis.mlang.program.ast.{Expression => ProgramExpression, _}

object FanucBackend extends Backend {
  override type Output = FanucProgram

  override def apply(p: Program): FanucProgram = {
    val converter = new FanucConverter(p)
    val (fanucInstructions, positions) = converter.convert()

    FanucProgram(
      "GENERATED",
      Attributes(
        owner = "",
        comment = "MLANG_GENERATED",
        progSize = 0,
        create = LocalDateTime.now(),
        modified = LocalDateTime.now(),
        fileName = "UNDEFINED",
        version = 1,
        lineCount = fanucInstructions.length,
        memorySize = 0,
      ),
      Instructions(fanucInstructions: _*), Positions(positions: _*),
    )
  }
}
