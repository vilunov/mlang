package university.innopolis.mlang.io

import scala.io.Source
import java.io._

object ReadWrite {
  def read(filePath: String): Seq[String] =
    Source.fromFile(filePath).mkString.split('\n')

  def write(lines: Seq[String], filePath: String): Unit = {
    val file = new File(filePath)
    val writer = new BufferedWriter(new FileWriter(file))
    lines.foreach(writer.write)
    writer.close()
  }
}
