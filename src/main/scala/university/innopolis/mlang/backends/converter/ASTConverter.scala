package university.innopolis.mlang.backends.converter

import university.innopolis.mlang.backends.fanuc._
import university.innopolis.mlang.parser.MlangParser._
import university.innopolis.mlang.program.IfStatement

import scala.collection.mutable

class ASTConverter(ast: Seq[StatementContext]) {
  val positionRegisters: mutable.Map[String, Int] = mutable.Map[String, Int]()
  var positionRegistersCount: Int = 0

  def convertAST(): List[FanucInstruction] = {
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

    fanucInstructions.toList
  }

  /**
    * Convert AssignmentContext into Fanuc PointAssignment
    * Right now it works only for command like `a = b`
    * @param assignment
    * @return
    */
  private[this] def convertAssignment(assignment: AssignStatementContext): PointAssignment = {
    val left = assignment.expression(0).unaryExpr().operand().IDENTIFIER().toString
    val right = assignment.expression(1).unaryExpr().operand().IDENTIFIER().toString
    val leftIndex: Int = getPRIndex(left.toString)
    val rightIndex: Int = getPRIndex(right.toString)


    val positionRegister: PositionRegister = PositionRegister(leftIndex)
    val moveRegister: MoveRegister = PositionRegister(rightIndex)

    PointAssignment(positionRegister, moveRegister)
  }

  private[this] def convertIf(ifStatement: IfStatementContext): IfStatement = {

    (null)
  }

  private[this] def convertCommand(command: CommandContext): MoveInstruction = {
    val targetObject = command.moveCommand().moveTarget().IDENTIFIER().toString

    MoveCommand
  }

  private[this] def convertFor(forStatement: ForStatementContext): FanucInstruction = ???

  private[this] def getPRIndex(name: String): Int = {
    positionRegisters.getOrElseUpdate(name, getFreePRIndex)
  }

  private[this] def getFreePRIndex: Int = {
    { positionRegistersCount += 1; positionRegistersCount }
  }
}
