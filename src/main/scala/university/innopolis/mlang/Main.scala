package university.innopolis.mlang

import university.innopolis.mlang.program.dsl._
import university.innopolis.mlang.backends.fanuc.FanucBackend
import university.innopolis.mlang.io.ReadWrite

object Main extends App {
  implicit val builder: BuildingContext = new BuildingContext

  move(x = 1.0, y = 1.0, z = 1.0, w = 1.0, p = 1.0, r = 1.0)
  move(x = 1.0, y = 1.0, z = 1.0, w = 1.0, p = 1.0, r = 1.0, smoothness = Cnt(10))
  move(x = 1.0, y = 1.0, z = 1.0, w = 1.0, p = 1.0, r = 1.0, smoothness = Fine, velocity = 3)
  cond("a") {} {}
  loop(varName = "i", from = 1, to = 100) {
    move("a")
  }

  ReadWrite.write(FanucBackend.translate(), "output.ls")
}
