# Test Conditionals

This file shows the use of the `if` and `ifElse` operations in *pi*.

This file can be executed by the Fifth environment.

## Test `assert`
### Stack Diagram
```
X assert # program continues if X evaluates to true
X assert # program ends if X evaluates to false
```

### Executable Example

Note that `true` and `false` are keywords.

```pi
true assert
false not assert
1 1 == assert
1 2 == not assert
```

## Test `if`
### Stack Diagram
```
X test if -> X # if test is true
X test if ->   # if test is not true
```

### Executable Example

```pi
1 true if 1 == assert
1 false if depth 0 == assert
```

## Test `ifElse`
### Stack Diagram
```X test if -> X # if test is true
X Y test ifElse -> X # if test is true
X Y test ifElse -> Y # if test is not true
```
### Executable Example

```pi
1 2 true ifElse
1 == assert
depth 0 == assert

1 2 false ifElse
2 == assert
depth 0 == assert
```

## More elaborate example
```pi
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