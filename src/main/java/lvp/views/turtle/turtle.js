class Turtle {
    constructor(canvas) {
        this.canvas = canvas;
        this.ctx = canvas.getContext('2d');
        this.reset();
    }

    reset() {
        this.ctx.reset();
        this.x = this.canvas.width / 2;
        this.y = this.canvas.height / 2;
        this.angle = 0;
        this.penDown();
        this.color("black");
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
            this.ctx.beginPath();
            this.ctx.moveTo(this.x, this.y);
            this.ctx.lineTo(newX, newY);
            this.ctx.stroke();
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

    color(color) {
        this.ctx.strokeStyle = color;
    }

    lineWidth(width) {
        this.ctx.lineWidth = width;
    }

    text(text, font = '10px sans-serif', align = 'center') {
        const radians = (this.angle * Math.PI) / 180 + Math.PI / 2.0;
        this.ctx.save();
        this.ctx.translate(this.x, this.y);
        this.ctx.rotate(radians);
        this.ctx.font = font;
        this.ctx.fillStyle = this.ctx.strokeStyle;
        this.ctx.textAlign = align;
        this.ctx.fillText(text, 0, 0);
        this.ctx.restore();
    }
    moveTo(x, y) {
        this.x = x;
        this.y = y;
    }

    lineTo(x, y) {
        const originalPenState = this.isPenDown;
        this.isPenDown = true;

        this.ctx.beginPath();
        this.ctx.moveTo(this.x, this.y);
        this.ctx.lineTo(x, y);
        this.ctx.stroke();

        this.x = x;
        this.y = y;

        this.isPenDown = originalPenState;
    }
}
