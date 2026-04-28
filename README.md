# Lox Java Interpreter

A simple interpreter for the Lox programming language, implemented in Java.  
Based on the language and design from 
[Crafting Interpreters](https://craftinginterpreters.com) by Robert Nystrom.

## Lox program

```lox
print "Hello, world!";

var x = 10;
for (var i = 0; i < x; i = i + 1) {
  print i;
}
```

>[!TIP]
> You can find more lox code snippets under [the test folder](./src/test/resources).
