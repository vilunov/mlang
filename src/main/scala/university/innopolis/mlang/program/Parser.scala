package university.innopolis.mlang.program

import scala.collection.JavaConverters._
import org.antlr.v4.runtime._
import org.antlr.v4.runtime.tree.TerminalNode
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

    val memory: Map[String, Expression] = this.memory(program.memoryBlock())
    val instructions: List[Statement] =

    Program(memory, instructions)
  }

  def memory(input: MemoryBlockContext): Map[String, Expression] = input.valDecl().asScala
    .map { i => i.IDENTIFIER().getText -> expression(i.expression()) }
    .toMap

  def command(input: CommandContext): Command = {
    val command = input.moveCommand()
    val target = command.moveTarget() match {
      case j if j.IDENTIFIER() != null =>
        Identifier(j.IDENTIFIER().getText)
      case j if j.typeExpression() != null =>
        typeOperand(j.typeExpression())
    }

    var parameters: Map[String, Operand] = Map.empty
    if (command.parameterList() != null) {
      parameters = this.parameters(command.parameterList())
    }

    MoveCommand(target, parameters)
  }

  def statements(input: StatementBlockContext): StatementBlock = StatementBlock(
    input.statement().asScala
      .map {
        case i if i.command() != null =>
          command(i.command())
        case i if i.assignStatement() != null =>
          val assignExpressions = i.assignStatement().expression()
          AssignmentStatement(expression(assignExpressions(0)), expression(assignExpressions(0)))
        case i if i.ifStatement() != null =>
          val ifStatement = i.ifStatement()
          var elseStatements: Option[StatementBlock] = None

          if (ifStatement.statementBlock().asScala.size > 1) {
            elseStatements = Some(statements(ifStatement.statementBlock(1)))
          }

          IfStatement(
            expression(ifStatement.expression()),
            statements(ifStatement.statementBlock(0)),
            elseStatements
          )
        case i if i.forStatement() != null =>
          val forStatment = i.forStatement()

          val forClause = forStatment.forClause()
          val forClauseExpressions = forClause.range().expression()
          var clauseID: Option[Identifier] = None
          if (forClause.IDENTIFIER() != null) {
            clauseID = Some(Identifier(forClause.IDENTIFIER().getText))
          }
          val clause = ForClause(
            clauseID,
            expression(forClauseExpressions(0)),
            expression(forClauseExpressions(1))
          )

          ForStatement(clause, statements(forStatment.statementBlock()))
      }
      .toList
  )

  def parameters(input: ParameterListContext): Map[String, Operand] = input.parameterDecl().asScala
    .map { i => i.IDENTIFIER().getText -> operand(i.operand()) }
    .toMap

  def typeOperand(input: TypeExpressionContext): TypeOperand = TypeOperand(Point, parameters(input.parameterList()))

  def operand(input: OperandContext): Operand = input match {
    case i if i.IDENTIFIER() != null =>
      // simple identifier operands
      Identifier(i.IDENTIFIER().toString)
    case i if i.literal() != null => i.literal() match {
      // literal operands
      case j if j.INT_LIT() != null =>
        // integer literals
        IntLiteral(j.INT_LIT().toString.toInt)
      case j if j.FLOAT_LIT() != null =>
        // float literals
        FloatLiteral(j.FLOAT_LIT().toString.toDouble)
      case j if j.BOOLEAN_LIT() != null => j.BOOLEAN_LIT().getText match {
        // bool literals
        case "true" => BooleanLiteral(true)
        case "false" => BooleanLiteral(false)
      }
      // string literals
      case j if j.STRING_LIT() != null => StringLiteral(j.STRING_LIT().getText)
    }
    case i if i.expression() != null =>
      // operands that are themselves expressions, i.e. (5 + 3)
      ExpressionOperand(expression(i.expression()))
    case i if i.typeExpression() != null =>
      // type expressions
      typeOperand(i.typeExpression())
  }

  /**
    * Retrieves an [[Expression]] from a given [[ExpressionContext]] ANTLR node.
    *
    * @param input ANTLR's [[ExpressionContext]] node to map to [[Expression]].
    * @return parsed [[Expression]] subclass.
    */
  def expression(input: ExpressionContext): Expression = input match {
    case i if i.unaryExpr() != null =>
      // unary
      val unaryExpr = i.unaryExpr()
      val unaryOps: List[UnaryOp] = unaryExpr.UNARY_OP().asScala.map(_.toString).map {
        case "!" => Not
      }.toList

      UnaryExpression(operand(unaryExpr.operand()), unaryOps)
    case i if i.BINARY_OP() != null =>
      // binary
      val left = i.expression(0)
      val right = i.expression(1)
      val operator: BinOp = binaryOperator(i.BINARY_OP())

      BinaryExpression(operator, expression(left), expression(right))
    case i if i.IDENTIFIER() != null =>
      // dot
      val identifier = i.IDENTIFIER().getText
      val left = i.expression(0)

      DotExpression(expression(left), identifier)
  }

  /**
    * Retrieves a [[BinOp]] from a given [[BINARY_OP]] terminal node.
    *
    * @param input terminal node from ANTLR, should be [[BINARY_OP]].
    * @return parsed [[BinOp]] subclass.
    */
  def binaryOperator(input: TerminalNode): BinOp = input.getText match {
    case "||" => Or
    case "&&" => And
    case "==" => Equal
    case "!=" => NotEqual
    case "<" => Less
    case "<=" => LessOrEqual
    case ">" => Greater
    case ">=" => GreaterOrEqual
    case "+" => Addition
    case "-" => Minus
    case "*" => Multiplication
    case "/" => Division
  }
}
