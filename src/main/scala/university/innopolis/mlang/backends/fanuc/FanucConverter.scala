package university.innopolis.mlang.backends.fanuc

import scala.collection.mutable
import university.innopolis.mlang.program.ast

private[fanuc] class FanucConverter(program: ast.Program) {
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
      case move: ast.MoveCommand =>
        val target: ast.MoveTarget = move.moveTarget
        val params: Map[String, ast.Operand] = move.parameters
        val register: MoveRegister = handleMoveTarget(target)

        if (params.isEmpty) { // default movement
          storeInstruction(LinearInstruction(register, defaultSpeed, OtherMMSec, SmoothnessFine))
        } else {
          val trajectory: ast.StringLiteral = params.getOrElse("trajectory", ast.StringLiteral(defaultTrajectory)).asInstanceOf[ast.StringLiteral]
          val speed: Int = params.getOrElse("speed", ast.IntLiteral(defaultSpeed)).asInstanceOf[ast.IntLiteral].value
          val smoothness: Int = params.getOrElse("smoothness", ast.IntLiteral(defaultSmoothness)).asInstanceOf[ast.IntLiteral].value
          val secondary = params.get("secondary").map(_.asInstanceOf[ast.MoveTarget]).map(handleMoveTarget)
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
      case assignment: ast.AssignmentStatement =>
        assignment.left match {
          case ast.UnaryExpression(moveTarget: ast.MoveTarget, Nil) =>
            // Handles register = register
            // not handled pr[1, 1] = 150
            val targetName: String = moveTarget match {
              case ast.Identifier(ident) => ident
            } //todo: Никита привет, мне лень
            val targetRegister: PositionRegister = PositionRegister(getPRIndex(targetName))
            val provider: MoveRegister = assignment.right match {
              case ast.UnaryExpression(identifier: ast.Identifier, Nil) =>
                PositionRegister(getPRIndex(identifier.ident))
              case ast.UnaryExpression(typeOperand: ast.TypeOperand, Nil) =>
                handleTypeOperand(typeOperand)
              case _ => ???
            }

            storeInstruction(PointAssignment(targetRegister, provider))
          case ast.UnaryExpression(dataRegister: ast.ExpressionOperand, Nil) =>
            //todo: here I wished to convert R[1] = R[2] + ..., but not sure what is it
            ???
          case ast.DotExpression(exp: ast.UnaryExpression, varName: String) =>
            exp.operand match {
              case typeOperand: ast.TypeOperand =>
                throw new RuntimeException("It should not be possible") // Point(4,2,1...).x=10 ???
              case identifier: ast.Identifier =>
                val pr: PositionCoordinateRegister =
                  PositionCoordinateRegister(getPRIndex(identifier.ident), axisToIndex(varName))
                val value: Expression = convertExpression(assignment.right)
                storeInstruction(IntegerAssignment(pr, expression = value))
            }
        }
      case _ => ???
    }


    val positions: List[Position] = convertPoints(points.toList)
    (fanucInstructions.toList, positions)
  }

  private[this] def convertExpression(expr: ast.Expression): Expression = {
    expr match {
      case binExpr: ast.BinaryExpression =>
        val op: Operator = convertOperator(binExpr.binOp)
        BinaryExpression(op, convertExpression(binExpr.left), convertExpression(binExpr.right))
      case unary: ast.UnaryExpression =>
        if (unary.unaryOp.isEmpty) {
          convertOperand(unary.operand)
        } else {
          ??? // Lots of binary expressions or what?
        }
    }
  }

  private[this] def convertOperand(operand: ast.Operand): Expression = {
    operand match {
      case eOp: ast.ExpressionOperand => convertExpression(eOp.expression)
      case iLit: ast.IntLiteral => IntegerExpression(iLit.value)
      case fLit: ast.FloatLiteral => FloatExpression(fLit.value.toFloat) // from double to float
      case _ => ??? // TODO: val is R[i] ???
    }
  }

  private[this] def convertOperator(operator: ast.BinOp): Operator = {
    operator match {
      case ast.Addition => Plus
      case ast.Minus => Minus
      case ast.Multiplication => Multiplication
      case ast.Division => Division
      case _ => ??? //todo: not implemented DIV & MOD on MLANG
    }
  }

  private[this] def unloadPositions(memory: Map[String, ast.Expression]) = {
    memory.foreach[Unit]{case (key:String, value: ast.Expression) => {
      val operand = value.asInstanceOf[ast.UnaryExpression].operand
      operand match {
        case typeOperand: ast.TypeOperand =>
          val point: PointRegister = handleTypeOperand(typeOperand)
          val pr: PositionRegister = PositionRegister(getPRIndex(key))
          storeInstruction(PointAssignment(pr, point))
        case _ => ???
      }
    }}
  }

  private[this] def axisToIndex(varName: String): Int = {
    varName match {
      case "x" => 1
      case "y" => 2
      case "z" => 3
      case "w" => 4
      case "p" => 5
      case "r" => 6
      case "j1" => 1
      case "j2" => 2
      case "j3" => 3
      case "j4" => 4
      case "j5" => 5
      case "j6" => 6
    }
  }


  private[this] def storeInstruction(instruction: FanucInstruction): Unit = {
    fanucInstructions += instruction
  }

  private[this] def buildPoint(params: Map[String, ast.Operand], uTool: Int = 1, uFrame: Int = 1): Point = {
    {
      List(
        params.get("x"), params.get("y"), params.get("z"),
        params.get("w"), params.get("p"), params.get("r")
      ).collect { case Some(ast.FloatLiteral(i)) => i } match {
        case x :: y :: z :: w :: p :: r :: Nil =>
          val coords = CartesianCoordinates(x, y, z, w, p, r)
          Some(CartesianPoint(uFrame, uTool, coords))
        case _ => None
      }
    }.orElse {
      List(
        params.get("j1"), params.get("j2"), params.get("j3"),
        params.get("j4"), params.get("j5"), params.get("j6")
      ).collect { case Some(ast.FloatLiteral(i)) => i } match {
        case joints =>
          val coords = JointCoordinates(joints)
          Some(JointPoint(uFrame, uTool, coords))
      }
    }.get
  }

  private[this] def handleMoveTarget(target: ast.MoveTarget): MoveRegister = target match {
    case identifier: ast.Identifier =>
      PositionRegister(getPRIndex(identifier.ident))
    case typeOperand: ast.TypeOperand =>
      handleTypeOperand(typeOperand)
  }

  // сохраняет point-литерал в /POS блок
  private[this] def handleTypeOperand(typeOperand: ast.TypeOperand): PointRegister = {
    require(typeOperand.typeLiteral == ast.Point)

    val params: Map[String, ast.Operand] = typeOperand.parameters

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
