package university.innopolis.mlang.backends.fanuc

import java.time.LocalDateTime

import university.innopolis.mlang.backends.Backend
import university.innopolis.mlang.program.ast.{Expression => ProgramExpression, _}

object FanucBackend extends Backend {
  override type Output = FanucProgram

  private def validateConsts(definitions: Map[String, ProgramExpression]): Map[String, Position] =
    definitions.mapValues {
      case UnaryExpression(TypeOperand(Point, parameters), List()) =>
        val params = parameters.mapValues {
          case FloatLiteral(value) => value
        }
        Position(CartesianPoint(
          1, 1,
          CartesianCoordinates(
            params("x"), params("y"), params("z"),
            params("w"), params("p"), params("r"),
          ),
          config = "N U T, 0, 0, 1",
        ))
    }

  private def validateInstructions(literalMap: Map[String, Any], program: Seq[Statement]): Instructions = {
    Instructions()
  }

  override def apply(p: Program): FanucProgram = {
    val positions = validateConsts(p.memory)

    val fanucInstructions = validateInstructions(Map.empty, p.statements)

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
        lineCount = fanucInstructions.instructions.length,
        memorySize = 0,
      ),
      fanucInstructions, Positions(positions.values.toSeq: _*),
    )
  }
}
