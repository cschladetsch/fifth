#
# Test continuation replacement.
#
# When we use '&' to invoke a continuation, it is
# like calling a subroutine: control will return to the
# calling context after the subroutine ends. This is
# called `suspending` a continuation.
#
# Rather than only suspension, we can also `replace` one
# continuation with another. In this case, when the continuation
# we replaced with ends, control is NOT returned to the calling
# continuation. In effect, we have *replaced* the current
# continuation with another.
#
# This is useful for a number of reasons and purposes, including
# infinite tail recursion.
#

0 'n=

{ { exit } { n 1 + 'n= } n 10000 == ifelse & } 'e= # exit the app if n equals 1000 otherwise increment it

{ e& b! } 'a= # suspend to e, then replace with b
{ e& a! } 'b= # suspend to e, then replace with a

a&

10000 n == assert

