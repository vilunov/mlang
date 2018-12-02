grammar Mlang;
program
    : topLevelDecl*
    ;

WS
    : [ \t\r\n]+ -> skip
    ;

Literal
    : DecimalNumeral
    ;

Identifier
    : Letter+
    ;

topLevelDecl
    : valDecl
    ;

type
    : Identifier
    ;

valDecl
    : 'val' Identifier '=' Literal ';'
    | 'var' Identifier '=' Literal ';'
    ;

fragment Val : 'val';
fragment Semicolon : ';';
fragment Keyword
    : 'val'
    | ';'
    ;

fragment DecimalNumeral
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

fragment Upper
    : 'A'  ..  'Z' | '$' | '_'
    ;
fragment Lower
    : 'a' .. 'z'
    ;
fragment Letter
    : Upper
    | Lower
    ;
