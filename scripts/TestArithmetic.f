# Test arithmetic, dup, print, assert, logical not

1 2 + dup print 3 == assert # end of line comment
7 5 - 2 + dup print 4 == assert
1 1 1 + + 3 ==assert
1 2 3 + + 2 - 4 dup 4== assert
1 1 == assert
2 1 - 1 == assert

# logical negation
1 not not assert
0 not assert
1 3 == not assert
6 2 / 3 == assert

# ensure data stack is empty
depth 0 == assert

