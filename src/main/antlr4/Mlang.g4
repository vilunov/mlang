grammar Mlang;
program
    : memoryBlock eos programBlock
    ;

memoryBlock
    : 'memory' WS? '{' (valDecl eos)* '}'
    ;

programBlock
    : 'program' WS? '{' (statement eos)* '}'
    ;

statement
    : Command eos
    ;

valDecl
    : IDENTIFIER '=' LITERAL
    ;

eos
    : ';'
    | EOF
    ;

WS
    : [ \t\r\n]+ -> skip
    ;

IDENTIFIER
    : LETTER ( LETTER | DIGIT )*
    ;

Command
    : 'move'
    ;

// Literals

LITERAL
    : INT_LIT
    | STRING_LIT
    | INT_LIT
    ;

STRING_LIT
    : '\'' LETTER+ '\''
    ;

INT_LIT
    : '0'
    | NON_ZERO_DIGIT DIGIT*
    ;

FLOAT_LIT
    : DIGIT* '.' DIGIT+
    ;

fragment DIGIT
    : '0'
    | NON_ZERO_DIGIT
    ;

fragment NON_ZERO_DIGIT
    : '1' .. '9'
    ;

fragment LETTER
    : UPPER
    | LOWER
    ;

fragment UPPER
    : 'A'  ..  'Z' | '$' | '_'
    ;

fragment LOWER
    : 'a' .. 'z'
    ;