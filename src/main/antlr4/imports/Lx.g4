/*
 * Lexer (define the tokens)
 */

lexer grammar Lx;

LINE: 'line' | 'ln';
POINT: 'point' | 'pt';
FROM: 'from';
TO: 'to';
AT: 'at';
COMMA: ',';
INT: [0-9]+;

WHITESPACE: ' ' -> skip;
NEWLINE: ('\r'? '\n' | '\r')+ ;