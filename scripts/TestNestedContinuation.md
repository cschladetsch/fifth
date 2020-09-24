# Test Nested Continuations
A [continuation](https://en.wikipedia.org/wiki/Continuation) is just a block of code that can be interrupted and perhaps returned to.

## Example
```f
{+}'a=          # make a continuatoin that just adds the two things on the stack, and call it 'a'
{*}'b=          # same for multiplication
{ a& b& } 'c=   # combine the two can call it 'c'
2 3 4 c& 14 == assert                   # assert that (3+4)*2 == 14
{ { 1+ } 'a= 2 a& 3 b& } & 9 == assert  # assert that (2+1)*3 == 9. Note that the local 'a' resolves first
```

