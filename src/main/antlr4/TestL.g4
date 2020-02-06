grammar TestL;
import Lx;

file : commands EOF;

commands : command
         | command NEWLINE commands
         ;

command: LINE FROM point TO point
       | POINT AT point
       ;

point: INT COMMA INT;