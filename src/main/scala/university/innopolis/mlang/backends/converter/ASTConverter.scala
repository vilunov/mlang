package university.innopolis.mlang.backends.converter

import university.innopolis.mlang.backends.fanuc._
import university.innopolis.mlang.parser.MlangParser._
import university.innopolis.mlang.program
import university.innopolis.mlang.program._

import scala.collection.mutable

class ASTConverter(ast: List[Statement]) {
  val positionRegisters: mutable.Map[String, Int] = mutable.Map[String, Int]()
  val defaultSpeed: Int = 100
  var positionRegistersCount: Int = 0

  def convertAST(): List[FanucInstruction] = {
    val fanucInstructions: mutable.MutableList[FanucInstruction] = mutable.MutableList[FanucInstruction]()

    ast.foreach(statement => {
      var instruction: FanucInstruction = null

      statement match {
        case _move if statement.isInstanceOf[MoveCommand] => // MoveCommand
          val move = statement.asInstanceOf[MoveCommand]
          val target: MoveTarget = move.moveTarget
          val params: Map[String, Operand] = move.parameters

          val moveRegister: MoveRegister = null

          if (params.isEmpty) {
            LinearInstruction(moveRegister, defaultSpeed, OtherMMSec, SmoothnessFine)
          } else {
            val trajectory: StringLiteral = params("trajectory").asInstanceOf[StringLiteral]
            val speed: Int = params("speed").asInstanceOf[IntLiteral].value
            val smoothness: Int = params("smoothness").asInstanceOf[IntLiteral].value
            val smoothnessType: SmoothnessType = convertSmoothness(smoothness)

            //todo: how to differentiate OtherMMSec, etc.???

            trajectory.value match {
              case "linear" => LinearInstruction(moveRegister, speed, OtherMMSec, smoothnessType)
              case "joint" => JointInstruction(moveRegister, speed, JointPercent, smoothnessType)
              case "arc" =>  ArcInstruction(moveRegister, secondPointRegister = ???, speed, OtherMMSec, smoothnessType) //todo: me
              case "" => CircularInstruction(moveRegister, secondPointRegister = ???, speed, OtherMMSec, smoothnessType) //todo: me
              case _ => ???
            }
          }
        case _assignment if statement.isInstanceOf[AssignmentStatement] =>
          val assignment = statement.asInstanceOf[AssignmentStatement]
      }

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

  private[this] def convertSmoothness(smoothess: Int): SmoothnessType = {
    smoothess match {
      case x == 100 => SmoothnessFine
      case x < 100 => SmoothnessCNT(x)
    }
  }

  private[this] def getPRIndex(name: String): Int = {
    positionRegisters.getOrElseUpdate(name, getFreePRIndex)
  }

  private[this] def getFreePRIndex: Int = {
    { positionRegistersCount += 1; positionRegistersCount }
  }
}
