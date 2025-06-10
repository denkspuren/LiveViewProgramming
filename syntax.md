# LVP Syntax
## Grammatik
```
INSTRUCTION ::= COMMAND | REGISTER
```
### Command
```
COMMAND ::=       SERVICE | TARGET

COMMANDNAME ::=   STRING
CONTENT ::=       STRING
```

```
SERVICE ::= SERVICENAME':' CONTENT

SERVICE ::= SERVICENAME':'
            CONTENT
            ~~~

TARGET ::=  TARGETNAME':' CONTENT

TARGET ::=  TARGETNAME':'
            CONTENT
            ~~~
```

Grundidee:
Service: String -> String
Target: String -> {}
### Register
```
NAME ::= STRING
CALL ::= STRING
```

```
REGISTER ::= 'Register:' NAME CALL
```

### Pipe
```
COMMAND
'|' COMMAND ['|' COMMAND '|' ...]
```


## Default Services
```
Cutout:
PATH
LABEL
~~~

Test:
Send: SNIPPET
Expect: STRING
~~~

Test:
Send: SNIPPET
Expect:
STRING1
STRING2
~~~

Turtle:
COMMANDS
~~~
```


## Targets
```
Markdown
Html
JavaScript
JavaScriptCall
Clear
```