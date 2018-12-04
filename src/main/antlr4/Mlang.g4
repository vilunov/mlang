grammar Mlang;

// PARSER
// Main language structures

program
    : memoryBlock eos programBlock
    ;

memoryBlock
    : 'memory' WS? '{' (valDecl eos)* '}'
    ;

programBlock
    : 'program' WS? statementBlock
    ;

// Statements

statement
    : command
    | assignStatement
    ;

statementBlock
    :'{' (statement eos)* '}'
    ;

assignStatement
    : IDENTIFIER '=' ( LITERAL | IDENTIFIER )
    ;

valDecl
    : IDENTIFIER '=' LITERAL
    ;

// Commands

command
    : moveCommand
    ;

moveCommand
    : 'move' IDENTIFIER eos
    | 'move' IDENTIFIER parameterList
    ;

parameterList
    :
    ;

eos
    : ';'
    | TERMINATOR
    | EOF
    ;

// LEXER

IDENTIFIER
    : LETTER ( LETTER | DIGIT )*
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

// Utility

KEYWORDS
    : 'memory'
    | 'program'
    | 'move'
    ;

WS
    : [ \t\r\n]+ -> skip
    ;

TERMINATOR
	: [\r\n]+ -> channel(HIDDEN)
	;