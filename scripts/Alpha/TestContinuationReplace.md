# Test Continuation Replacement

When we use the `suspend` operator *&* to invoke a continuation, it is
like calling a subroutine: control will return to the
calling context after the subroutine ends. This is
called *suspending* a continuation.  

Rather than only suspension, we can also `replace` one
continuation with another. In this case, when the continuation
we replaced with ends, control is NOT returned to the calling
continuation. In effect, we have *replaced* the current
continuation with another.  

This is useful for a number of reasons and purposes, including
infinite tail recursion.  

## Example 1
This example shows that the current continuation is *replaced* by the *!* operator, as the assertion is never met:
```pi
{ 123 } 'a=
a!
false assert # never reached! current continuation was *replaced* by the contination stored in 'a'

```

## Example 2
Tail recursion is trivial with the `replace` operator:
```pi
"Start" print
0 'n=

{ { exit } { n 1 + 'n= } n 10000 == ifElse & } 'e= # exit the prpgram if n equals 1000 otherwise increment it

{ e& b! } 'a= # suspend to e, then replace with b
{ e& a! } 'b= # suspend to e, then replace with a

a&

10000 n == assert
"Done" print
```
