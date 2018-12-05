package university.innopolis.mlang.backends.converter

import university.innopolis.mlang.backends.converter.ASTConverter.convertIf
import university.innopolis.mlang.backends.fanuc.{FanucInstruction, MoveInstruction, PointAssignment}
import university.innopolis.mlang.parser.MlangParser._
import university.innopolis.mlang.program.IfStatement

import scala.collection.mutable

object ASTConverter {
  def convertAST(ast: Seq[StatementContext]): List[FanucInstruction] = {
    val fanucInstructions: mutable.MutableList[FanucInstruction] = mutable.MutableList[FanucInstruction]()


    ast.foreach(context => {
      var instruction: FanucInstruction = null

      val assignment = context.assignStatement()
      val ifStatement = context.ifStatement()
      val commandStatement = context.command()
      val forStatement = context.forStatement()

      if (assignment != null) {
        instruction = convertAssignment(assignment)
      } else if (commandStatement != null) {
        instruction = convertCommand(commandStatement)
      } else if (forStatement != null) {
        instruction = convertFor(forStatement)
      }
//      else if (ifStatement != null) {
//        instruction = convertIf(ifStatement)

      fanucInstructions += instruction
    })

    (fanucInstructions.toList)
  }

  private[this] def convertAssignment(assignment: AssignStatementContext): PointAssignment = {

    (null)
  }

  private[this] def convertIf(ifStatement: IfStatementContext): IfStatement = {

    (null)
  }

  private[this] def convertCommand(command: CommandContext): MoveInstruction = {

    (null)
  }

  private[this] def convertFor(forStatement: ForStatementContext): FanucInstruction = ???
}
