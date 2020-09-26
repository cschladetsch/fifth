# Test Continuation Resumption
In typical computer languages, there is but one way to change flow to another routine: the *subroutine* call.  
This has been drilled into us for a long time, but it's not the only way that flow control can happen in a computer language.

There are three main ways to change flow in *pi*:
* Suspend: `&`
* Replace: `!`
* Resume: `...`

## Suspend
This is your basic sub-routine call. Execution will return to the caller when the sub-routine has completed:

```pi
{1} 'a=             # make a continuation that just pushes 1 onto the stack
a&                  # invoke the contination as a subroutine (*suspend* to it)
2 + 3 == assert     # when control returns here, we have 1 on the stack, then push 2, add it to 1 and assert the result is 3
```

*Suspend* is exactly like your typical "call this other thing and return here when it's finished". *Suspend* is a sub-routine call.

## Replace
See [TestContinuationReplace](TestContinuationReplace.md). Basically, the current flow is *replaced* by another continuation, and we do not expect execution to return to the caller.

Replace is like `goto` in other languages. We go from where to are to somewhere else, and leave no direct way to get back.

## Resume
Resuming just means ending what we're currently doing, and replacing it with what is on the [context stack](../TwoStacks.md).

```pi
{ 1 ... 2 3 4 } 'a=   # at the `replace` operation, ..., the current continuation is replaced by whatever is on the context stack
a&
1 depth == assert     # execution of 'a' was ended with the ... resume operation
1 == assert           # because of this, everything that happens after ... in 'a' doesn't happen: the stack just contains 1
```

There is no direct equivalent operation to `resume` in most other languages. *Resume* just continues whatever is on the context stack.
