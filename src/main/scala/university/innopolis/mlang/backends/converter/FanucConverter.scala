package university.innopolis.mlang.backends.converter

import university.innopolis.mlang.backends.fanuc._
import university.innopolis.mlang.parser.MlangParser._
import university.innopolis.mlang.program.Program._
import university.innopolis.mlang.program._

import scala.collection.mutable

class FanucConverter(program: Program) {
  val positionRegisters: mutable.Map[String, Int] = mutable.Map[String, Int]()
  val defaultSpeed: Int = 100
  var positionRegistersCount: Int = 0
  var pointRegistersCount: Int = 0

  def convert(): (List[FanucInstruction], List[Position]) = {
    val statements = program.statements
    val memory = program.memory

    val fanucInstructions: mutable.MutableList[FanucInstruction] = mutable.MutableList[FanucInstruction]()
    val points: mutable.MutableList[CartesianPoint] = mutable.MutableList[CartesianPoint]()

    statements.foreach(statement => {
      var instruction: FanucInstruction = null

      statement match {
        case _move if statement.isInstanceOf[MoveCommand] => // MoveCommand
          val move = statement.asInstanceOf[MoveCommand]
          val target: MoveTarget = move.moveTarget
          val params: Map[String, Operand] = move.parameters

          var register: MoveRegister = null

          target match {
            case _identifier if target.isInstanceOf[Identifier] =>
              val identifier: String = target.asInstanceOf[Identifier].ident
              register = PositionRegister(getPRIndex(identifier))
            case _typeOperand if target.isInstanceOf[TypeOperand] =>
              val typeOperand: TypeOperand = target.asInstanceOf[TypeOperand]
              val point: Point = typeOperand.typeLiteral.asInstanceOf[Point]
              val params: Map[String, Operand] = typeOperand.parameters

              val cartesianPoint: CartesianPoint = buildCartesianPoint(params, uFrame = 0, uTool = 0) //TODO: IT IS NOT CORRECT
              points += cartesianPoint

              register = PointRegister(getPointIndex())
          }

          require(register != null) // todo: may be it is wrong tactic

          if (params.isEmpty) {
            LinearInstruction(register, defaultSpeed, OtherMMSec, SmoothnessFine)
          } else {
            val trajectory: StringLiteral = params("trajectory").asInstanceOf[StringLiteral]
            val speed: Int = params("speed").asInstanceOf[IntLiteral].value
            val smoothness: Int = params("smoothness").asInstanceOf[IntLiteral].value
            val smoothnessType: SmoothnessType = convertSmoothness(smoothness)

            //todo: how to differentiate OtherMMSec, etc.???

            trajectory.value match {
              case "linear" => LinearInstruction(register, speed, OtherMMSec, smoothnessType)
              case "joint" => JointInstruction(register, speed, JointPercent, smoothnessType)
              case "arc" =>  ArcInstruction(register, secondPointRegister = ???, speed, OtherMMSec, smoothnessType) //todo: me
              case "" => CircularInstruction(register, secondPointRegister = ???, speed, OtherMMSec, smoothnessType) //todo: me
              case _ => ???
            }
          }
        case _assignment if statement.isInstanceOf[AssignmentStatement] =>
          val assignment = statement.asInstanceOf[AssignmentStatement]
      }

      fanucInstructions += instruction
    })


    (fanucInstructions.toList, positions)
  }

  private[this] def buildCartesianPoint(params: Map[String, Operand], uTool: Int = 0, uFrame: Int = 0): CartesianPoint = {
    // Expected that params contain x, y, z, p, w, r
    // todo: I am sure there is a better solution
    val x = params("x").asInstanceOf[FloatLiteral].value
    val y = params("y").asInstanceOf[FloatLiteral].value
    val z = params("z").asInstanceOf[FloatLiteral].value
    val w = params("w").asInstanceOf[FloatLiteral].value
    val r = params("r").asInstanceOf[FloatLiteral].value
    val p = params("p").asInstanceOf[FloatLiteral].value

    val cartesianCoordinates: CartesianCoordinates = CartesianCoordinates(x, y, z, w, p, r)
    val cartesianPoint: CartesianPoint = CartesianPoint(uTool, uFrame, cartesianCoordinates, null) //todo: what is expected to use as config?

    cartesianPoint
  }

  private[this] def convertSmoothness(smoothess: Int): SmoothnessType = {
    require(smoothess >= 0 && smoothess <= 100)

    smoothess match {
      case 100 => SmoothnessFine
      case x if x < 100 =>  SmoothnessCNT(x)
    }
  }

  private[this] def getPRIndex(name: String): Int = {
    positionRegisters.getOrElseUpdate(name, getFreePRIndex)
  }

  private[this] def getPointIndex(): Int = {
    { pointRegistersCount += 1; pointRegistersCount }
  }

  private[this] def getFreePRIndex: Int = {
    { positionRegistersCount += 1; positionRegistersCount }
  }
}
