package university.innopolis.mlang

import program._

object Main extends App {
  val program = Program { implicit b =>
    move(1)
    move(10)
    "kek" := 1
    move("kek")
    cond("kek") { implicit b =>
      move(1)
    } { implicit b =>
      move(2)
    }
  } { implicit b =>
    move(10)
  } compile()

  println(program)
  assert(true, "asd")
}
