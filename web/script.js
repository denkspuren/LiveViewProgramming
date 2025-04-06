const loadedDiv = document.getElementById('loadMessage');

function loadScript(src, onError = () => console.log('script loading failed: ', src)) {
  var script = document.createElement('script');
  script.src = src;
  script.onload = function() {
    script.classList.add("persistent");
    console.log('script loaded:', src);
    fetch("/loaded", {method: "post"}).catch(console.log);
  };
  script.onerror = onError;
  document.body.appendChild(script);
}

function loadScriptWithFallback(onlineSrc, offlineSrc) {
  loadScript(onlineSrc, function() {
    console.log('loading', onlineSrc, 'failed, trying', offlineSrc);
    loadScript(offlineSrc);
  });
}

function setUp() {
  if (window.EventSource) {
    const source = new EventSource("/events");

    source.onmessage = function (event) {
      const splitPos = event.data.indexOf(":");
      const action = event.data.slice(0, splitPos);
      const base64Data = event.data.slice(splitPos + 1);
      const data = new TextDecoder("utf-8").decode(Uint8Array.from(atob(base64Data), c => c.charCodeAt(0)));
      // const data = atob(base64Data);
      // console.log(`Action: ${action}\n`);
      // console.log(`Data: ${data}\n`);

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
          const newElement = document.createElement("div");
          newElement.innerHTML = data;
          document.getElementById("events").appendChild(newElement);
          break;
        }
        case "LOAD": {
          loadedDiv.style.display = 'block';
          setTimeout(() => {
            loadedDiv.style.display = 'none';
          }, 300);
          var srcs = data.split(',');
          srcs = srcs.map(src => src.trim());
          if (srcs.length >= 2) loadScriptWithFallback(srcs[0], srcs[1]);
          else loadScript(data);
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
        case "RELEASE":
          locks = locks.filter(lock => lock !== data);
          break;
        default:
          console.log("Unknown Action");
          break;
      }
    };

    source.onerror = function (error) {
      console.error("EventSource failed:", error);
      // source.close(); // uncommented to enable recovery
    };

  } else {
    document.getElementById("events").innerHTML =
      "Your browser does not support Server-Sent Events.";
  }
}

const Clerk = {}; // not used, yet
let locks = [];
setUp();

// https://samthor.au/2020/understanding-load/