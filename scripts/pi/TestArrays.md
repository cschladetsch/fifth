# Test Arrays
Arrays are simply sequences of objects. They can be indexed to obtain an element, or an element can be erased.

```pi
[] size 0 == assert

[1 2 3] size 3 == assert

[2 3 4] expand 3 == assert
3 depth == assert

[1 2] [1 2] == assert
[1 2] [2 1] == not assert

2 3 4 3 toArray [2 3 4] == assert

[6 7 8] 'a=
a size 3 == assert
a 0 get 6 == assert
a 1 get 7 == assert
a 2 get 8 == assert
```

