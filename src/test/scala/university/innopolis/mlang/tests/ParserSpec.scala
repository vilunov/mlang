package university.innopolis.mlang.tests

import org.scalatest._
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}

import university.innopolis.mlang.parser._

class ParserSpec extends FlatSpec with Matchers {

  "hello world" should "be parsed" in {
    val input = CharStreams.fromString("hello world")

    val lexer = new HelloLexer(input)
    val tokens = new CommonTokenStream(lexer)
    val parser = new HelloParser(tokens)
    val walker = new ParseTreeWalker()

    var tokensList = Seq[String]()

    walker.walk(new HelloBaseListener {
      override def enterR(ctx: HelloParser.RContext): Unit =
        tokensList :+= ctx.ID.getText
      override def exitR(ctx: HelloParser.RContext): Unit =
        tokensList :+= "EOS"
    }, parser.r())

    assert(tokensList == Seq("world", "EOS"))
  }
}
