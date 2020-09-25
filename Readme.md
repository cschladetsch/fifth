# ![logo](res/fifth-logo.png) Fifth
[![Build status](https://ci.appveyor.com/api/projects/status/github/cschladetsch/fifth-lang?svg=true)](https://ci.appveyor.com/project/cschladetsch/fifth-lang)
[![CodeFactor](https://www.codefactor.io/repository/github/cschladetsch/fifth-lang/badge)](https://www.codefactor.io/repository/github/cschladetsch/fifth-lang)
[![License](https://img.shields.io/github/license/cschladetsch/fifth-lang.svg?label=License&maxAge=86400)](LICENSE)

A Forth-like interpreter written in Java, with three supported languages: **pi**, **rho** and **tau**.

Supports concept of a *continuation*, also known as a *fibre*.

Note that, like Forth, *pi* uses *reverse polish notation*. If you've only used *in-fix* notation, it will all look weird. For example, the expression:

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

## Todo
Re-organise project structure into multiple modules:
* fifth-common
    * Contains common functionality for all dependant modules, such as file access, logging etc.
* fifth-pi
    * The **pi** RPN language
* fifth-rho
    * the **rho** in-fix, python-like language that translates to *pi*
* fifth-tau
    * Idl for generating networked Dto' and Rpc's

## Documentation
The [root](doc/Readme.md) of the language documentation is fully fledged out and very coherent.

## Examples
See [scripts](scripts) folder for test scripts.

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
