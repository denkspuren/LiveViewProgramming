# LVP Syntax
## Grammatik
- `[]` -> Optional
- `::=` -> Definiert als
- `''` -> Literal
```
INSTRUCTION ::=     COMMAND | REGISTER | PIPE
```
### Command
```
COMMAND ::=             COMMANDNAME['{'ID'}']':' CONTENT
COMMAND ::=             COMMANDNAME['{'ID'}']':' 
                        CONTENT
                        '~~~'

COMMANDNAME ::=         STRING
ID ::=                  STRING
CONTENT ::=             STRING
```

Grundidee:
Aufteilung in Service und Target oder Function und Consumer
- **Service:** String -> String
- **Target:** String -> {}
### Register
```
REGISTER ::=    'Register:' COMMANDNAME CALL

CALL ::=        STRING
```

### Pipe
```
PIPE ::= '|' COMMAND ['|' COMMAND '|' ...]
```


## Default Services

- Text
- Codeblock
- Turtle
- Button
- Input
- Checkbox
- Test

### Turtle
```
init XFROM XTO YFROM YTO STARTX STARTY STARTANGLE
init WIDTH HEIGHT

penup
pendown
forward DISTANCE
backward DISTANCE
right ANGLE
left ANGLE
color R G B [A]
text TEXT [FONT]
width WIDTH
push
pop
timeline
save
```

### Codeblock
```
Codeblock: PATH;LABEL

Codeblock:
PATH
LABEL
~~~
```

### Test
```
Test:
Send: SNIPPET
Expect: STRING
~~~
```

### Interaction Elements
```
Button:
Text: TEXT
[width: WIDTH]
[height: HEIGHT]
path: PATH
label: "LABEL"
replacement: REPLACEMENT
~~~

Input:
path: PATH
label: "LABEL"
placeholder: PLACEHOLDER
template: TEMPLATE (with Placeholder '$')
type: TYPE (Text, Email, Number, etc)
~~~

Checkbox:
path: PATH
label: "LABEL"
template: TEMPLATE (with Placeholder '$')
checked: BOOLEAN
~~~
```

## Targets

- Markdown
- Html
- JavaScript
- JavaScriptCall
- Clear
- Dot

### Dot
```
Dot:
[width: WIDTH]
[height: HEIGHT]
GRAPH
~~~
```