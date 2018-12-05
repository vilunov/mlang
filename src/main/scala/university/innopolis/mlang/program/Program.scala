package university.innopolis.mlang.program

/**
  * Internal representation of 'mlang' program
  */
case class Program (memory: Map[String, Any],
                    instructions: List[Instruction])
