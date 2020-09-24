# Test Conditionals

## Test basic assertions and negation
```f
true assert
false not assert
```

## Test if
```f
2 true if
2 == assert
depth 0 == assert

1 true if
1 == assert
depth 0 == assert

1 false if
depth 0 == assert
```

## Test if-else

```f
1 2 true ifElse
1 == assert
depth 0 == assert

1 2 false ifElse
2 == assert
depth 0 == assert
```

TODO: move tests to a better spot
```f
{ 2 + 3 * } 'body=
2 body true if & 12 == assert # test trailing comment
3 body true if & 15 == assert
{ 4 } body false if & 4 == assert

# let a = 4;
# if (a == 4) print(123); else print(456);
4 'a=
123 456 a 4 == ifElse print
123 456 a 3 == ifElse print
```