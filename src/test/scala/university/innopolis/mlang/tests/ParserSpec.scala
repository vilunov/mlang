package university.innopolis.mlang.tests

import scala.collection.JavaConverters._

import org.scalatest._
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}

import university.innopolis.mlang.parser._
import university.innopolis.mlang.parser.MlangParser._

class ParserSpec extends FlatSpec with Matchers {

  "val kek = 1337;" should "be parsed" in {
    val input = CharStreams.fromString("val kek = 1337;")

    val lexer = new MlangLexer(input)
    val tokens = new CommonTokenStream(lexer)
    val parser = new MlangParser(tokens)
    val walker = new ParseTreeWalker()

    var tokensList = Seq[String]()

    walker.walk(new MlangBaseListener {
      override def enterProgram(ctx: ProgramContext): Unit = {
        ctx.topLevelDecl().asScala.foreach { topLevel: TopLevelDeclContext =>
          val valDecl = topLevel.valDecl()
          tokensList :+= valDecl.Identifier().getText + " = " + valDecl.Literal().getText + " " + topLevel.getRuleIndex
        }
      }
      override def exitProgram(ctx: ProgramContext): Unit =
        tokensList :+= "EOS"
    }, parser.program())

    assert(tokensList == Seq("kek = 1337 1", "EOS"))
  }
}
