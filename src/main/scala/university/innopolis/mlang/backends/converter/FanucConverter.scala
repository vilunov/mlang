package university.innopolis.mlang.backends.converter

import university.innopolis.mlang.backends.fanuc._
import university.innopolis.mlang.program._

import scala.collection.mutable

class FanucConverter(program: Program) {
  val positionRegisters: mutable.Map[String, Int] = mutable.Map[String, Int]()
  val defaultSpeed: Int = 100
  val defaultSmoothness: Int = 0
  val defaultTrajectory: String = "linear"

  var positionRegistersCount: Int = 0
  var pointRegistersCount: Int = 0

  val fanucInstructions: mutable.MutableList[FanucInstruction] = mutable.MutableList[FanucInstruction]()
  val points: mutable.MutableList[CartesianPoint] = mutable.MutableList[CartesianPoint]()

  def convert(): (List[FanucInstruction], List[Position]) = {
    val statements = program.statements
    val memory = program.memory

    statements.foreach(statement => {
      var instruction: FanucInstruction = null

      statement match {
        case move: MoveCommand => // MoveCommand
          val target: MoveTarget = move.moveTarget
          val params: Map[String, Operand] = move.parameters

          var register: MoveRegister = null

          target match {
            case identifier: Identifier =>
              register = PositionRegister(getPRIndex(identifier.ident))
            case typeOperand: TypeOperand =>
              register = handleTypeOperand(typeOperand)
          }

          require(register != null) // todo: may be it is wrong tactic

          if (params.isEmpty) { // default movement
            instruction = LinearInstruction(register, defaultSpeed, OtherMMSec, SmoothnessFine)
          } else {
            val trajectory: StringLiteral = params.getOrElse("trajectory", StringLiteral(defaultTrajectory)).asInstanceOf[StringLiteral]
            val speed: Int = params.getOrElse("speed", IntLiteral(defaultSpeed)).asInstanceOf[IntLiteral].value
            val smoothness: Int = params.getOrElse("smoothness", IntLiteral(defaultSmoothness)).asInstanceOf[IntLiteral].value
            val smoothnessType: SmoothnessType = convertSmoothness(smoothness)

            //todo: how to differentiate OtherMMSec, etc.???

            // todo: sry, I know code below can be done better but I dont know how
            trajectory.value match {
              case "linear" => instruction = LinearInstruction(register, speed, OtherMMSec, smoothnessType)
              case "joint" =>  instruction = JointInstruction(register, speed, JointPercent, smoothnessType)
              case "arc" =>    instruction = ArcInstruction(register, secondPointRegister = ???, speed, OtherMMSec, smoothnessType) //todo: me
              case "circular" => instruction = CircularInstruction(register, secondPointRegister = ???, speed, OtherMMSec, smoothnessType) //todo: me
              case _ => ???
            }
          }
        case assignment: AssignmentStatement =>
          assignment.left match {
            case moveTarget: MoveTarget =>
              // Handles register = register
              // not handled pr[1, 1] = 150
              val targetName: String = assignment.left.asInstanceOf[Identifier].ident //todo: I am not sure, need to discuss
              val targetRegister: PositionRegister = PositionRegister(getPRIndex(targetName))
              var provider: MoveRegister = null

              assignment.right match {
                case identifier: Identifier =>
                  provider = PositionRegister(getPRIndex(identifier.ident))
                case typeOperand: TypeOperand =>
                  provider = handleTypeOperand(typeOperand)
                case _ => ???
              }

              instruction = PointAssignment(targetRegister, provider)
            case dataRegister: ExpressionOperand => //todo: here I wished to convert R[1] = R[2] + ..., but not sure what is it
              ???
          }
        case _ => ???
      }

      fanucInstructions += instruction
    })


    val positions: List[Position] = convertPoints(points.toList)
    (fanucInstructions.toList, positions)
  }

  private[this] def buildCartesianPoint(params: Map[String, Operand], uTool: Int = 0, uFrame: Int = 0): CartesianPoint = {
    // Expected that params contain x, y, z, p, w, r
    // todo: I am sure there is a better solution
    val x = params("x").asInstanceOf[FloatLiteral].value
    val y = params("y").asInstanceOf[FloatLiteral].value
    val z = params("z").asInstanceOf[FloatLiteral].value
    val w = params("w").asInstanceOf[FloatLiteral].value
    val p = params("p").asInstanceOf[FloatLiteral].value
    val r = params("r").asInstanceOf[FloatLiteral].value

    val cartesianCoordinates: CartesianCoordinates = CartesianCoordinates(x, y, z, w, p, r)
    val cartesianPoint: CartesianPoint = CartesianPoint(uTool, uFrame, cartesianCoordinates, null) //todo: what is expected to use as config?

    cartesianPoint
  }

  private[this] def savePoint(cartesianPoint: CartesianPoint) = {
    points += cartesianPoint
  }

  private[this] def handleTypeOperand(typeOperand: TypeOperand): PointRegister = {
//    val point: Point = typeOperand.typeLiteral.asInstanceOf[Point]
    val params: Map[String, Operand] = typeOperand.parameters

    val cartesianPoint: CartesianPoint = buildCartesianPoint(params, uFrame = 0, uTool = 0) //TODO: IT IS NOT CORRECT
    savePoint(cartesianPoint)

    PointRegister(getPointIndex())
  }

  private[this] def convertSmoothness(smoothess: Int): SmoothnessType = {
    require(smoothess >= 0 && smoothess <= 100)

    smoothess match {
      case 0 => SmoothnessFine
      case x if x > 0 =>  SmoothnessCNT(x)
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

  private[this] def convertPoints(points: List[CartesianPoint]): List[Position] = {
    points.map(Position(_))
  }

}
