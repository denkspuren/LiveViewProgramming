"use strict";
class ClerkAddon {
}
var Commands;
(function (Commands) {
    Commands["Markdown"] = "Markdown";
    Commands["Script"] = "Script";
    Commands["Write"] = "Write";
})(Commands || (Commands = {}));
var TurtleCommands;
(function (TurtleCommands) {
    TurtleCommands["Init"] = "Init";
    TurtleCommands["PenUp"] = "PenUp";
    TurtleCommands["PenDown"] = "PenDown";
    TurtleCommands["Forward"] = "Forward";
    TurtleCommands["Backward"] = "Backward";
    TurtleCommands["Left"] = "Left";
    TurtleCommands["Right"] = "Right";
})(TurtleCommands || (TurtleCommands = {}));
class Turtle {
    canvas;
    ctx;
    x;
    y;
    angle;
    isPenDown;
    constructor(canvas) {
        this.canvas = canvas;
        this.ctx = canvas.getContext('2d');
        this.x = this.canvas.width / 2;
        this.y = this.canvas.height / 2;
        this.angle = 0;
        this.isPenDown = true;
        this.ctx?.clearRect(0, 0, this.canvas.width, this.canvas.height);
    }
    penDown() {
        this.isPenDown = true;
    }
    penUp() {
        this.isPenDown = false;
    }
    forward(distance) {
        const radians = (this.angle * Math.PI) / 180;
        const newX = this.x + distance * Math.cos(radians);
        const newY = this.y + distance * Math.sin(radians);
        if (this.isPenDown) {
            this.ctx?.beginPath();
            this.ctx?.moveTo(this.x, this.y);
            this.ctx?.lineTo(newX, newY);
            this.ctx?.stroke();
        }
        this.x = newX;
        this.y = newY;
    }
    backward(distance) {
        this.forward(-distance);
    }
    right(degrees) {
        this.angle += degrees;
    }
    left(degrees) {
        this.angle -= degrees;
    }
}
class TurtleAddon extends ClerkAddon {
    turtles = new Map();
    handle(msg) {
        const content = JSON.parse(msg.content);
        switch (msg.command) {
            case Commands.Script:
                this.execute(content);
                break;
            case Commands.Write:
            default:
                Clerk.render(msg.content.replaceAll("\\n", "\n"));
                break;
        }
    }
    execute(content) {
        switch (content.command.toLowerCase()) {
            case TurtleCommands.Init.toLowerCase():
                {
                    let c = document.getElementById(`turtleCanvas${content.value}`);
                    if (c instanceof HTMLCanvasElement)
                        this.turtles.set(content.id, new Turtle(c));
                    break;
                }
            case TurtleCommands.PenUp.toLowerCase():
                this.turtles.get(content.id)?.penUp();
                break;
            case TurtleCommands.PenDown.toLowerCase():
                this.turtles.get(content.id)?.penDown();
                break;
            case TurtleCommands.Forward.toLowerCase():
                this.turtles.get(content.id)?.forward(+content.value);
                break;
            case TurtleCommands.Backward.toLowerCase():
                this.turtles.get(content.id)?.backward(+content.value);
                break;
            case TurtleCommands.Left.toLowerCase():
                this.turtles.get(content.id)?.left(+content.value);
                break;
            case TurtleCommands.Right.toLowerCase():
                this.turtles.get(content.id)?.right(+content.value);
                break;
        }
    }
}
class Clerk {
    static addons = new Map([
        ["Turtle", new TurtleAddon()],
    ]);
    static setUp() {
        if (window.EventSource) {
            const source = new EventSource("/events");
            source.onmessage = function (event) {
                const msg = JSON.parse(event.data);
                if (!msg.addonId)
                    Clerk.handleClerk(msg);
                const addon = Clerk.addons.get(msg.addonId);
                if (addon)
                    addon.handle(msg);
            };
            source.onerror = function (error) {
                console.error("EventSource failed:", error);
                source.close();
            };
        }
        else {
            document.getElementById("events").innerHTML =
                "Your browser does not support Server-Sent Events.";
        }
    }
    static handleClerk(msg) {
        switch (msg.command) {
            case Commands.Markdown:
                Clerk.toMarkdown(msg.content.replaceAll("\\n", "\n"));
                break;
            case Commands.Script:
            case Commands.Write:
            default:
                Clerk.render(msg.content.replaceAll("\\n", "\n"));
                break;
        }
    }
    static toMarkdown(input) {
        const renderedHTML = marked.parse(input);
        const newElement = document.createElement("div");
        newElement.innerHTML = renderedHTML;
        document.getElementById("events").appendChild(newElement);
    }
    static render(input) {
        const newElement = document.createElement("div");
        newElement.innerHTML = input;
        document.getElementById("events").appendChild(newElement);
    }
}
Clerk.setUp();
