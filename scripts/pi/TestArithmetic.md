# Test Arithmetic
Test basic arithmetic like plus, minus, multiply, divide operations.

Also, uses dup, print, assert, logical not.  

Really should be broken down into separate tests.

```pi
1 drop # test trailing comments
2 2 * 4 == assert
6 2 / 3 == assert

1 2 + dup print 3 == assert
7 5 - 2 + dup print 4 == assert
1 1 1 + + 3 ==assert
1 2 3 + + 2 - 4 == assert
1 1 == assert
2 1 - 1 == assert

# logical negation
1 not not assert
0 not assert
1 3 == not assert

# ensure data stack is empty
depth 0 == assert
```

