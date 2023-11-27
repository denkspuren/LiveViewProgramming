class Turtle {
    constructor(canvas) {
        this.canvas = canvas;
        this.ctx = canvas.getContext('2d');
        this.reset();
    }

    reset() {
        this.x = this.canvas.width / 2;
        this.y = this.canvas.height / 2;
        this.angle = 0;
        this.isPenDown = true;
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
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
}
