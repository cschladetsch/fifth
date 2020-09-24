# Executable Markdown
Markdown is useful. Wouldn't it be nice to be able to use it as actual source code for
writing programs?

## Motivation
Documenting code is hard, especially new code, and especially code for new languages. By using
code embedded in markdown text, we can have all the benefits of markdown and inject code into it.

By allowing the embedding of executable code into mark down, we can produce what Donald Knuth called [Literate Programming](https://en.wikipedia.org/wiki/Literate_programming).

It is important to note that it is not necessary to write fifth-lang code using markdown.
* If a source file has the extension `.f`, it is treated as containing pure script.
* If a source file as the extension `.md`, all code-blocks marked with **f** code type are stripped from the mark-down and are executed normally.

In either case, a new file with the same name but with an appended `txt` extension is created containing the output of the program.

## Live Example
The following is a code-block with type *f* (e.g. **\`\`\`f**)
```f
# Trace a string to the info log stream
"Hello, Markdown!" print

# add 1+2+3, duplicate the result, print it, then assert that the result is 6
1 2 3 + + 6 dup print == assert 
```

Running this file through the *fifth-lang processor*, a new file called `TestMarkDown.md.txt` is created, containing:
```
00:00:077:    Info: String=Hello, Markdown!
00:00:077:    Info: Integer=6
00:00:078:   Debug: Passed
```

Note the format of the output is:
```
min:sec:millis:level: output
```