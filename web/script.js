const loadedDiv = document.getElementById('loadMessage');  

function setUp() {
  if (window.EventSource) {
    const source = new EventSource("/events");

    source.onmessage = function (event) {
      const splitPos = event.data.indexOf(":");
      const action = event.data.slice(0, splitPos);
      const data = event.data.slice(splitPos + 1).replaceAll("\\n", "\n");

      switch (action) {
        case "CALL": {
          Function(data).apply(); // https://www.educative.io/answers/eval-vs-function-in-javascript
          break;
        }
        case "SCRIPT": {
          const newElement = document.createElement("script");
          newElement.innerHTML = data;
          document.body.appendChild(newElement);
          break;
        }
        case "WRITE": {
          const newElement = document.createElement("p");
          newElement.innerHTML = data;
          document.getElementById("events").appendChild(newElement);
          break;
        }
        case "LOAD": {
          loadedDiv.style.display = 'block';
          const newElement = document.createElement("script");
          newElement.classList.add("persistent");
          newElement.src = data;
          newElement.onload = function (_) {
            fetch("/loaded", {method: "post"}).catch(console.log);
          }
          document.body.appendChild(newElement);
          setTimeout(() => {
            loadedDiv.style.display = 'none';
          }, 100);
          break;
        }
        case "CLEAR": {
          const element = document.getElementById("events");
          while (element.firstChild) {
            element.removeChild(element.firstChild);
          }

          const toRemove = [];
          for (const node of document.body.children) {
            if (node.classList == null || !node.classList.contains("persistent")) {
              toRemove.push(node);
            }
          }
          toRemove.forEach(x => document.body.removeChild(x));
          
          break;
        }
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
    document.getElementById("events").innerHTML =
      "Your browser does not support Server-Sent Events.";
  }
}

const Clerk = {}; // not used, yet
setUp();

// https://samthor.au/2020/understanding-load/