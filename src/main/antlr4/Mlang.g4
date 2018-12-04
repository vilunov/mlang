grammar Mlang;
program
    : memoryBlock programBlock
    ;

memoryBlock
    : 'memory' WS? '{' valDecl* eos '}' eos
    ;

programBlock
    : 'program' WS? '{' statement* eos '}' eos
    ;

statement
    : Command eos
    ;

valDecl
    : Identifier '=' Literal eos
    ;

eos
    : ';'
    | EOF
    ;

WS
    : [ \t\r\n]+ -> skip
    ;

Literal
    : DecimalLiteral
    | StringLiteral
    ;

Identifier
    : Letter+
    ;

Command
    : 'move'
    ;

fragment StringLiteral
    : Letter+
    ;

fragment DecimalLiteral
    : '0'
    | NonZeroDigit Digit*
    ;

fragment Digit
    : '0'
    | NonZeroDigit
    ;
fragment NonZeroDigit
    : '1' .. '9'
    ;

fragment Letter
    : Upper
    | Lower
    ;

fragment Upper
    : 'A'  ..  'Z' | '$' | '_'
    ;

fragment Lower
    : 'a' .. 'z'
    ;