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
```f
2 b * c + 'a =
```

See sample [scripts](/scripts). Note that MarkDown files are also executable *fifth-lang* files.

## Executable Markdown
See an example of Literate Programming with [fifth-lang using Markdown](scripts/TestMarkDown.md)


## Documentation
The [root](Doc/Readme.md) of the language documentation is fully fledged out and very coherent.

## Example [Script](scripts/TestContinuations.f)
This will all look like nonsense at first. But, it can actually be executed live from this Readme.md file.
See above section for details.

```f
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
2 d& d& 8 == assert

# Can also use locals in continuations.
# This stores whatever is on the stack to a local called 'a',
# then uses it to calculate 'a*2 + a'
{ 'a = a 2 * a + } 'e =
3 e& 9 == assert # 3*2 + 3 = 9

# Call continuations from within contiunuations
{ + }'a=
{ * }'b=
{ a& b& } 'c=
2 3 4 c& 14 == assert

# Nested continuations
{ { 1 + } 'a= 2 a& 3 b& } & 9 == assert

# ensure the data-stack is empty
depth 0 == assert
```

## Todo
- [ ] Better error reporting at each stage:
  - [ ] Lexer
  - [ ] Parser
  - [ ] Translator
  - [ ] Executor
- [ ] Replace operator
- [ ] Resume operator
- [ ] For loops
- [ ] While loops
- [ ] Language description other than tests and code
- [ ] Interaction with local file system: **Danger**
- [ ] Basic REST calls: **Danger**

## Known Bugs
* A final line in a script with a trailing comment fails to parse.
