function setUp() {    
    if (window.EventSource) {
        const source = new EventSource("/events");

        source.onmessage = function (event) {
            const splitPos = event.data.indexOf(':');
            const action = event.data.slice(0, splitPos);
            const data = event.data.slice(splitPos + 1).replaceAll("\\n", "\n");
            
            switch (action) {
                case "script":
                    script(data);
                    break;
                case "write":
                    write(data);
                    break;
                default:
                    console.log("Unknown Action");
                    break;
            }
        };

        source.onerror = function (error) {
            console.error("EventSource failed:", error);
            source.close();
        };
    } else {
        document.getElementById("events").innerHTML = "Your browser does not support Server-Sent Events.";
    }
}

function script(input) {
    eval(input);
    // new Function(input).apply(); // https://www.educative.io/answers/eval-vs-function-in-javascript
}

function write(input) {
    const newElement = document.createElement("p");
    newElement.innerHTML = input;
    document.getElementById("events").appendChild(newElement);
}

setUp();