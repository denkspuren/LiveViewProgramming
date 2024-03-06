declare namespace marked {
  function parse(s: string): string;
}

interface Message {
  command: Commands;
  addonId: string;
  content: string;
}

interface TurtleContent {
  command: TurtleCommands;
  id: string;
  value: number;
}

abstract class ClerkAddon {
  abstract handle(msg: Message): void;
}

enum Commands {
  Markdown = "Markdown",
  Script = "Script",
  Write = "Write",
}

enum TurtleCommands {
  Init = "Init",
  PenUp = "PenUp",
  PenDown = "PenDown",
  Forward = "Forward",
  Backward = "Backward",
  Left = "Left",
  Right = "Right",
}

class Turtle {
  canvas: HTMLCanvasElement;
  ctx: CanvasRenderingContext2D | null;

  x: number;
  y: number;
  angle: number;
  isPenDown: boolean;


  constructor(canvas: HTMLCanvasElement) {
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

  forward(distance: number) {
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

  backward(distance: number) {
      this.forward(-distance);
  }

  right(degrees: number) {
      this.angle += degrees;
  }

  left(degrees: number) {
      this.angle -= degrees;
  }
}

class TurtleAddon extends ClerkAddon {
  turtles: Map<string, Turtle> = new Map();

  handle(msg: Message): void {
    const content: TurtleContent = JSON.parse(msg.content);
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

  execute(content: TurtleContent) {    
    switch (content.command.toLowerCase()) {
      case TurtleCommands.Init.toLowerCase():
        {
          let c = document.getElementById(`turtleCanvas${content.value}`);
          if(c instanceof HTMLCanvasElement)  
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
  static readonly addons: Map<string, ClerkAddon> = new Map([
    ["Turtle", new TurtleAddon()],
  ]);
  static setUp(): void {    
    if (window.EventSource) {
      const source = new EventSource("/events");

      source.onmessage = function (event) {
        const msg: Message = JSON.parse(event.data);
        if (!msg.addonId) Clerk.handleClerk(msg);

        const addon = Clerk.addons.get(msg.addonId);
        if (addon) addon.handle(msg);
      };

      source.onerror = function (error) {
        console.error("EventSource failed:", error);
        source.close();
      };
    } else {
      document.getElementById("events")!.innerHTML =
        "Your browser does not support Server-Sent Events.";
    }
  }

  static handleClerk(msg: Message): void {
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

  static toMarkdown(input: string): void {
    const renderedHTML = marked.parse(input);
    const newElement = document.createElement("div");
    newElement.innerHTML = renderedHTML;
    document.getElementById("events")!.appendChild(newElement);
  }

  static render(input: string) {
    const newElement = document.createElement("div");
    newElement.innerHTML = input;
    document.getElementById("events")!.appendChild(newElement);
  }
}

Clerk.setUp();
