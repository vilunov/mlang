package university.innopolis.mlang.program

import scala.collection.JavaConverters._
import org.antlr.v4.runtime._
import university.innopolis.mlang.backends.fanuc.PointAssignment
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
    Option(program.exception).foreach(throw _)

    val memory: Map[String, Expression] = program
      .memoryBlock().valDecl().asScala
      .map { i => (i.IDENTIFIER().toString, i.expression()) }.toMap.mapValues {
        _ => IntLiteral(0)
      }
    val instructions: Seq[StatementContext] = program
      .programBlock().statementBlock().statement().asScala

    Program(memory, List())
  }
}
