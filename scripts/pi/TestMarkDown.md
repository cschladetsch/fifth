# Executable Markdown
Markdown is useful. Wouldn't it be nice to be able to use it as actual source code for
writing programs?

## Motivation
Documenting code is hard, especially new code, and especially code for new languages. By using
code embedded in markdown text, we can have all the benefits of markdown and inject code into it.

By allowing the embedding of executable code into mark down, we can produce what Donald Knuth called [Literate Programming](https://en.wikipedia.org/wiki/Literate_programming).

It is important to note that it is not necessary to write fifth-lang code using markdown.
* If a source file has the extension `.pi`, it is treated as containing pure script.
* If a source file as the extension `.md`, all code-blocks marked with **f** code type are stripped from the mark-down and are executed normally.

In either case, a new file with the same name but with an appended `.out.md` extension is created containing the output of the program.

## Live Example
The following is a code-block with type *pi* (e.g. **\`\`\`pi**)
```pi
# Trace a string to the info log stream
"Hello, Markdown!" print

# add 1+2+3, duplicate the result, print it, then assert that the result is 6
1 2 3 + + 6 dup print == assert 
```

Running this file through the *fifth-lang processor*, a new file called `TestMarkDown.md.out.md` is created, containing:

00:00:291:  `Info`: **Started [TestMarkDown.md](TestMarkDown.md) on Sat Sep 26 21:17:07 AEST 2020**  
00:00:292:  `Info`: **Hello, Markdown!**  
00:00:292:  `Info`: **6**  
00:00:292: `Debug`: **Passed**  

Note the format of the output is:
```
min:sec:millis:level: output
```

## Future Work
Of course, the same approach can be taken for all other code-blocks in Markdown files. I just thought it was a good way to document the test cases for my little toy language.

