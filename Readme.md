# ![logo](res/fifth-logo.png) Fifth
[![Build status](https://ci.appveyor.com/api/projects/status/github/cschladetsch/fifth-lang?svg=true)](https://ci.appveyor.com/project/cschladetsch/fifth-lang)
[![CodeFactor](https://www.codefactor.io/repository/github/cschladetsch/fifth-lang/badge)](https://www.codefactor.io/repository/github/cschladetsch/fifth-lang)
[![License](https://img.shields.io/github/license/cschladetsch/fifth-lang.svg?label=License&maxAge=86400)](LICENSE.txt)

A Forth-like interpreter written in Java.

Supports concept of a *continuation*, also known as a *fibre*.

Note that, like Forth, Fifth uses *reverse polish notation*. If you've only used *in-fix* notation, it will all look weird. For example, the expression:

```
a = b*2 + c
```
would be expressed as:
```
2 b * c + 'a =
```

See sample [scripts](/scripts).

## Example `scripts/TestContinuations.f`
```
#
# Test basic continuations
#

# Store a continuation that simply executes 'plus'
# Note the quote character ' before the c. Otherwise, an object
# named c will attempted to be resolved (and fail)
{ + } 'c =

# Use it to add 2 numbers together
1 2 c& 3 == assert

# Make a continuation that doubles what's on the stack
{ 2 * } 'd =
2 d& 4 == assert
3 d& 6 == assert
2 2 d& d& 8 == assert

# Can also use locals in continuations.
# This stores whatever is on the stack to a local called 'a',
# then uses it to calculate 'a*2 + a'
{ 'a = a 2 * a + } 'e =
3 e& 9 == assert # 3*2 + 3 = 9

# ensure the data-stack is empty
depth 0 == assert
```
