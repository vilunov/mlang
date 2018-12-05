package university.innopolis.mlang

import university.innopolis.mlang.program._
import university.innopolis.mlang.program.dsl._
import university.innopolis.mlang.backends.fanuc.FanucBackend
import university.innopolis.mlang.io.ReadWrite

object Main extends App {
  implicit val builder: BuildingContext = new BuildingContext

  move(1.0, 1.0, 1.0, 1.0, 1.0, 1.0)

  ReadWrite.write(FanucBackend.translate(), "output.ls")
}
