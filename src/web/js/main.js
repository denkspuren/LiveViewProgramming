
const socket = new WebSocket("ws://localhost:8083");
const turtles = [];

socket.onopen = function() {
    console.log("Die Verbindung wurde erfolgreich aufgebaut.");
};

socket.onmessage = function (event) {
    const data = JSON.parse(event.data);
    switch (data.command) {
        case 'update': {
            const turtle = turtles[data.id];
            turtle.update(data.data);
        } break;
        case 'await': {
            const turtle = turtles[data.id];
            turtle.await(function(turtle) {
               socket.send("{ \"id\": " + turtle.id + " }");
            });
        } break;
        case 'create': {
            const turtle = new Turtle(turtles.length);
            turtles.push(turtle);
            socket.send("{ \"id\": " + turtle.id + " }");
        } break;
    }
};

socket.onerror = function (event) {
    console.error("Die Verbindung wurde unerwartet geschlossen.");
    console.error(event);
};

socket.onclose = function (event) {
    console.log("Die Verbindung wurde geschlossen.");
    console.log(event);
};

const container = document.getElementById("container");
const canvas = document.getElementById("canvas");
const context = canvas.getContext("2d");

function resize() {
    const { width, height } = container.getBoundingClientRect();
    canvas.width = width;
    canvas.height = height;
}

function render() {
    requestAnimationFrame(render);
    context.clearRect( 0, 0, canvas.width, canvas.height);
    context.translate(canvas.width / 2, canvas.height / 2);
    for (let i = 0; i < turtles.length; ++i) {
        context.save();
        turtles[i].render(context);
        context.restore();
    }
    context.setTransform(1, 0, 0, 1, 0, 0);
}

window.addEventListener('resize', function(event) {
    resize();
});

resize();
render();
