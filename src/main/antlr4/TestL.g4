grammar TestL;
import Lx;

file : commands EOF;

commands : command
         | command NEWLINE commands
         ;

command : LINE FROM fro=point TO to=point # cline
        | POINT AT at=point # cpoint
        ;

point : INT COMMA INT;