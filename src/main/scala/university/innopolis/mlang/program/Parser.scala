package university.innopolis.mlang.program

import scala.collection.JavaConverters._

import org.antlr.v4.runtime._

import university.innopolis.mlang.program.ast._
import university.innopolis.mlang.parser._
import university.innopolis.mlang.parser.MlangParser._

object Parser {

  private object ThrowErrorListener extends BaseErrorListener {

    override def syntaxError(recognizer: Recognizer[_, _],
                             offendingSymbol: Any,
                             line: Int,
                             charPositionInLine: Int,
                             msg: String,
                             e: RecognitionException): Unit =
      throw e
  }

  /**
    * Parses the mlang source code
    * @param input contents of the source file
    * @return inner AST representation
    */
  def parse(input: String): Program = {
    val stream = CharStreams.fromString(input)
    val lexer = new MlangLexer(stream)
    val tokens = new CommonTokenStream(lexer)
    val parser = new MlangParser(tokens)
    parser.addErrorListener(ThrowErrorListener)

    val program = parser.program()

    val memory: Map[String, Expression] = program
      .memoryBlock().valDecl().asScala
      .map { i => (i.IDENTIFIER().toString, i.expression()) }.toMap
      .mapValues { _ => UnaryExpression(IntLiteral(0)) }
    val instructions: List[Statement] = program
      .programBlock().statementBlock().statement().asScala
      .map {
        case i if i.command() != null =>
          val command = i.command().moveCommand()
          val params: Map[String, Operand] = Option(command.parameterList()).iterator
            .flatMap(_.parameterDecl().asScala)
            .map { i => i.IDENTIFIER().toString -> operand(i.operand()) }.toMap
          val target = command.moveTarget()
          MoveCommand(Identifier(target.IDENTIFIER().toString), params)
        case i if i.assignStatement() != null =>
          val assignment = i.assignStatement()
          val left = assignment.expression(0)
          val right = assignment.expression(1)
          AssignmentStatement(expression(left), expression(right))
        case i => println(i.getRuleIndex); ???
      }.toList

    Program(memory, instructions)
  }

  def operand(input: OperandContext): Operand = input match {
    case i if i.IDENTIFIER() != null =>
      Identifier(i.IDENTIFIER().toString)
    case i => i.literal() match {
      case j if j.INT_LIT() != null =>
        IntLiteral(j.INT_LIT().toString.toInt)
      case j if j.FLOAT_LIT() != null =>
        FloatLiteral(j.FLOAT_LIT().toString.toDouble)
    }
  }

  def expression(input: ExpressionContext): Expression = input match {
    case i if i.unaryExpr() != null =>
      val unaryExpr = i.unaryExpr()
      val unaryOps: List[UnaryOp] = unaryExpr.UNARY_OP().asScala.map(_.toString).map {
        case "!" => Not
      }.toList

      UnaryExpression(operand(unaryExpr.operand()), unaryOps)

  }
}
