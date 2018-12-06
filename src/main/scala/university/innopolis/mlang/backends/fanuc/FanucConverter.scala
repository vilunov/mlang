package university.innopolis.mlang.backends.fanuc

import university.innopolis.mlang.program.ast

import scala.collection.mutable
import university.innopolis.mlang.program.ast._

private[fanuc] class FanucConverter(program: Program) {
  val positionRegisters: mutable.Map[String, Int] = mutable.Map[String, Int]()
  val defaultSpeed: Int = 100
  val defaultSmoothness: Int = 0
  val defaultTrajectory: String = "linear"

  var positionRegistersCount: Int = 0
  var pointRegistersCount: Int = 0

  val fanucInstructions: mutable.MutableList[FanucInstruction] = mutable.MutableList[FanucInstruction]()
  val points: mutable.MutableList[Point] = mutable.MutableList[Point]()

  def convert(): (List[FanucInstruction], List[Position]) = {
    val statements = program.statements
    val memory = program.memory

    unloadPositions(memory)

    statements.foreach {
      case move: MoveCommand =>
        val target: MoveTarget = move.moveTarget
        val params: Map[String, Operand] = move.parameters
        val register: MoveRegister = handleMoveTarget(target)

        if (params.isEmpty) { // default movement
          storeInstruction(LinearInstruction(register, defaultSpeed, OtherMMSec, SmoothnessFine))
        } else {
          val trajectory: StringLiteral = params.getOrElse("trajectory", StringLiteral(defaultTrajectory)).asInstanceOf[StringLiteral]
          val speed: Int = params.getOrElse("speed", IntLiteral(defaultSpeed)).asInstanceOf[IntLiteral].value
          val smoothness: Int = params.getOrElse("smoothness", IntLiteral(defaultSmoothness)).asInstanceOf[IntLiteral].value
          val secondary = params.get("secondary").map(_.asInstanceOf[MoveTarget]).map(handleMoveTarget)
          val smoothnessType: SmoothnessType = convertSmoothness(smoothness)

          //todo: how to differentiate OtherMMSec, etc.???

          // todo: sry, I know code below can be done better but I dont know how
          storeInstruction(trajectory.value.toLowerCase() match {
            case "linear" =>
              LinearInstruction(register, speed, OtherMMSec, smoothnessType)
            case "joint" =>
              JointInstruction(register, speed, JointPercent, smoothnessType)
            case "arc" =>
              ArcInstruction(register, secondPointRegister = secondary.get, speed, OtherMMSec, smoothnessType)
            case "circular" =>
              CircularInstruction(register, secondPointRegister = secondary.get, speed, OtherMMSec, smoothnessType)
            case _ => ???
          })
        }
      case assignment: AssignmentStatement =>
        assignment.left match {
          case UnaryExpression(moveTarget: MoveTarget, Nil) =>
            // Handles register = register
            // not handled pr[1, 1] = 150
            val targetName: String = moveTarget match {
              case Identifier(ident) => ident
            } //todo: I am not sure, need to discuss
            val targetRegister: PositionRegister = PositionRegister(getPRIndex(targetName))
            val provider: MoveRegister = assignment.right match {
              case UnaryExpression(identifier: Identifier, Nil) =>
                PositionRegister(getPRIndex(identifier.ident))
              case UnaryExpression(typeOperand: TypeOperand, Nil) =>
                handleTypeOperand(typeOperand)
              case _ => ???
            }

            storeInstruction(PointAssignment(targetRegister, provider))
          case UnaryExpression(dataRegister: ExpressionOperand, Nil) =>
            //todo: here I wished to convert R[1] = R[2] + ..., but not sure what is it
            ???
        }
      case _ => ???
    }


    val positions: List[Position] = convertPoints(points.toList)
    (fanucInstructions.toList, positions)
  }

  private[this] def unloadPositions(memory: Map[String, ast.Expression]) = {
    memory.foreach[Unit]{case (key:String, value: ast.Expression) => {
      val operand = value.asInstanceOf[UnaryExpression].operand
      operand match {
        case typeOperand: TypeOperand =>
          val point: PointRegister = handleTypeOperand(typeOperand)
          val pr: PositionRegister = PositionRegister(getPRIndex(key))
          storeInstruction(PointAssignment(pr, point))
        case _ => ???
      }
    }}
  }

  private[this] def storeInstruction(instruction: FanucInstruction): Unit = {
    fanucInstructions += instruction
  }

  private[this] def buildPoint(params: Map[String, Operand], uTool: Int = 1, uFrame: Int = 1): Point = {
    {
      List(
        params.get("x"), params.get("y"), params.get("z"),
        params.get("w"), params.get("p"), params.get("r")
      ).collect { case Some(FloatLiteral(i)) => i } match {
        case x :: y :: z :: w :: p :: r :: Nil =>
          val coords = CartesianCoordinates(x, y, z, w, p, r)
          Some(CartesianPoint(uFrame, uTool, coords))
        case _ => None
      }
    }.orElse {
      List(
        params.get("j1"), params.get("j2"), params.get("j3"),
        params.get("j4"), params.get("j5"), params.get("j6")
      ).collect { case Some(FloatLiteral(i)) => i } match {
        case joints =>
          val coords = JointCoordinates(joints)
          Some(JointPoint(uFrame, uTool, coords))
      }
    }.get
  }

  private[this] def handleMoveTarget(target: MoveTarget): MoveRegister = target match {
    case identifier: Identifier =>
      PositionRegister(getPRIndex(identifier.ident))
    case typeOperand: TypeOperand =>
      handleTypeOperand(typeOperand)
  }

  // сохраняет point-литерал в /POS блок
  private[this] def handleTypeOperand(typeOperand: TypeOperand): PointRegister = {
    require(typeOperand.typeLiteral == Point)

    val params: Map[String, Operand] = typeOperand.parameters

    points += buildPoint(params)
    PointRegister(points.length) // не добавлять -1, отсчет начинается с единицы
  }

  private[this] def convertSmoothness(smoothess: Int): SmoothnessType = {
    require(smoothess >= 0 && smoothess <= 100)

    smoothess match {
      case 0 => SmoothnessFine
      case x if x > 0 =>  SmoothnessCNT(x)
    }
  }

  // получение айдишника для идентифиера
  private[this] def getPRIndex(name: String): Int = {
    positionRegisters.getOrElseUpdate(name, getFreePRIndex)
  }

  // резервирование айдишника для PR-регистра
  private[this] def getFreePRIndex: Int = {
    positionRegistersCount += 1
    positionRegistersCount
  }

  private[this] def convertPoints(points: List[Point]): List[Position] =
    points.map(Position(_))

}
