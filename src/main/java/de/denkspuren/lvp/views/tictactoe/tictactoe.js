class TicTacToe {
    constructor(canvas, endpoint) {
        this.canvas = canvas;
        this.ctx = canvas.getContext("2d");
        this.marginX = this.canvas.width / 20;
        this.marginY = this.canvas.height / 20;
        this.fieldWidth = this.canvas.width / 3;
        this.fieldHeight = this.canvas.height / 3;

        this.isOver = false;

        this.ctx.lineWidth = 6
        this.drawBoard();

        this.canvas.addEventListener("click", (event) => {
            if (this.isOver) return;
            const indexY = Math.floor(event.offsetY / this.fieldHeight);
            const indexX = Math.floor(event.offsetX / this.fieldWidth);
            const index = indexX + indexY * 3;
            console.log(`Clicked at X: ${indexX} Y: ${indexY} => ${index}`);
            fetch(endpoint, {method: "post", body: index.toString()}).catch(console.log);
        });
    }

    drawToken(isX, index) {
        const x = (index % 3) * this.fieldWidth;
        const y = Math.floor(index / 3) * this.fieldHeight;

        if (isX) {
            this.drawX(x, y);
        } else {
            this.drawO(x, y);
        }
    }

    drawX(x, y) {
        this.ctx.beginPath();

        this.ctx.moveTo(x + this.marginX, y + this.marginY);
        this.ctx.lineTo(x + this.fieldWidth - this.marginX, y + this.fieldHeight - this.marginY);

        this.ctx.moveTo(x + this.marginX, y + this.fieldHeight - this.marginY);
        this.ctx.lineTo(x + this.fieldWidth - this.marginX, y + this.marginY);
        this.ctx.stroke();
    }

    drawO(x, y) {
        this.ctx.beginPath();

        this.ctx.arc(x + this.fieldWidth / 2, y+ this.fieldHeight / 2, Math.min(this.fieldHeight - this.marginY, this.fieldWidth - this.marginX) / 4, Math.PI * 2, 0, false);
        this.ctx.stroke();
    }

    showWinner(start, end) {
        this.isOver = true;
        const startX = (start % 3) * this.fieldWidth + this.fieldWidth / 2;
        const startY = Math.floor(start / 3) * this.fieldHeight + this.fieldHeight / 2;

        
        const endX = (end % 3) * this.fieldWidth + this.fieldWidth / 2;
        const endY = Math.floor(end / 3) * this.fieldHeight + this.fieldHeight / 2;


        this.ctx.beginPath();
        this.ctx.strokeStyle = "red";
        this.ctx.moveTo(startX, startY);
        this.ctx.lineTo(endX, endY);
        this.ctx.stroke();

    }


    drawBoard() {
        this.ctx.beginPath();

        this.ctx.moveTo(0, this.fieldHeight);
        this.ctx.lineTo(this.canvas.width, this.fieldHeight);

        this.ctx.moveTo(0, 2 * this.fieldHeight);
        this.ctx.lineTo(this.canvas.width, 2 * this.fieldHeight);

        this.ctx.moveTo(this.fieldWidth, 0);
        this.ctx.lineTo(this.fieldWidth, this.canvas.height);

        this.ctx.moveTo(2 * this.fieldWidth, 0);
        this.ctx.lineTo(2 * this.fieldWidth, this.canvas.height);
        
        this.ctx.stroke();
    }
}