grammar Mlang;

// PARSER
// Main language structures

program
    : memoryBlock programBlock
    ;

memoryBlock
    : 'memory' WS? '{' ( valDecl eos )* '}'
    ;

programBlock
    : 'program' WS? statementBlock
    ;

// Statements

statement
    : command
    | assignStatement
    | ifStatement
    | forStatement
    ;

statementBlock
    :'{' ( statement )* '}'
    ;

// Used only in program block
assignStatement
    : expression '=' expression eos
    ;

ifStatement
    : 'if' expression statementBlock ('else' statementBlock)?
    ;

forStatement
    : 'for' forClause statementBlock
    ;

forClause
    : range
    | IDENTIFIER 'in' range
    ;

range
    : expression 'to' expression
    ;

// used only in memory block
valDecl
    : IDENTIFIER '=' expression
    ;

// Expressions

expression
    : unaryExpr
    | expression BINARY_OP expression
    | expression '.' IDENTIFIER
    ;

unaryExpr
    : UNARY_OP* operand
    ;

operand
    : IDENTIFIER
    | literal
    | '(' expression ')'
    | typeExpression
    ;

typeExpression
    : TYPE '(' parameterList ')'
    ;

// Commands

command
    : moveCommand
    ;

moveCommand
    : 'move' moveTarget eos
    | 'move' moveTarget ':' '{' parameterList '}'
    ;

moveTarget
    : IDENTIFIER
    | typeExpression
    ;

parameterList
    : parameterDecl ( ',' parameterDecl )*
    ;

parameterDecl
    : IDENTIFIER ':' operand
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

literal
    : INT_LIT
    | STRING_LIT
    | FLOAT_LIT
    | BOOLEAN_LIT
    ;

STRING_LIT
    : '\'' (LETTER | DIGIT)* '\''
    ;

INT_LIT
    : '0'
    | MINUS_SIGN? NON_ZERO_DIGIT DIGIT*
    ;

FLOAT_LIT
    : MINUS_SIGN? DIGIT* '.' DIGIT+
    ;

BOOLEAN_LIT
    : 'true'
    | 'false'
    ;

TYPE
    : 'Point'
    ;

fragment DIGIT
    : '0'
    | NON_ZERO_DIGIT
    ;

fragment MINUS_SIGN
    : '-'
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

// Operators

BINARY_OP
    : LOGIC_OP | REL_OP | ADD_OP | MUL_OP
    ;

fragment LOGIC_OP
    : '||'
    | '&&'
    ;

//rel_op     = "==" | "!=" | "<" | "<=" | ">" | ">=" .
fragment REL_OP
    : '=='
    | '!='
    | '<'
    | '<='
    | '>'
    | '>='
    ;

//add_op     = "+" | "-"
fragment ADD_OP
    : '+'
    | '-'
    ;

//mul_op     = "*" | "/" .
fragment MUL_OP
    : '*'
    | '/'
    ;

UNARY_OP
    : '!'
    ;

// Utility

KEYWORDS
    : 'memory'
    | 'program'
    | 'if'
    | 'for'
    | 'to'
    | 'in'
    | 'else'
    ;

WS
    : [ \t\r\n]+ -> skip
    ;

TERMINATOR
	: [\r\n]+ -> channel(HIDDEN)
	;
