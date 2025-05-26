const loadedDiv = document.getElementById('loadMessage');

let debug = false;
let scrollPosition = 0;

const clerk = {}; // Scope for declarations



// Logs debug messages to both server and browser console when debug mode is active
function debugLog(message) {
  if(!debug) return;
  
  console.debug(message);
  fetch('log', { method: 'post', body: `debug:${message}` })
    .catch(console.error);
}

// Send error logs to the server and log to browser console
function errorLog(message) {
  console.error(message);
  fetch('log', { method: 'post', body: `error:${message}` })
    .catch(console.error);
}

function setUp() {

  if (window.EventSource) {
    const source = new EventSource(`/events`);

    source.onmessage = function (event) {
      const splitPos = event.data.indexOf(":");
      const action = event.data.slice(0, splitPos);
      const base64Data = event.data.slice(splitPos + 1);
      const data = new TextDecoder("utf-8").decode(Uint8Array.from(atob(base64Data), c => c.charCodeAt(0)));
      
      debugLog(`Action: ${action}\nData: ${data}`);

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
        case "CLEAR": {
          scrollPosition = window.scrollY;
          const element = document.getElementById("events");
          while (element.firstChild) {
            element.removeChild(element.firstChild);
          }

          const errors = document.getElementById("errors");
          errors.style.display = "none";
          while (errors.firstChild) {
            errors.removeChild(errors.firstChild);
          }

          const toRemove = [];
          for (const node of document.body.children) {
            if (node.classList == null || !node.classList.contains("persistent")) {
              toRemove.push(node);
            }
          }
          toRemove.forEach(x => document.body.removeChild(x));
          
          for (const prop of Object.getOwnPropertyNames(clerk)) {
            delete clerk[prop];
          }
          
          break;
        }
        case "DEBUG":
          debug = true;
          break;
        case "LOG":
          const newElement = document.createElement("div");
          newElement.innerText = data;
          const errors = document.getElementById("errors");
          errors.appendChild(newElement);
          errors.style.display = "block";
          scrollPosition = 0;
          window.scrollTo(0, 0);
          break;
        default:
          errorLog("Unknown Action");
          break;
      }

      if (scrollPosition > 0) {
        window.scrollTo(0, scrollPosition);
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

document.addEventListener("DOMContentLoaded", () => {
  window.md = markdownit({
      highlight: function (str, lang) {
          if (lang && hljs.getLanguage(lang)) {
              try {
                  return hljs.highlight(str, { language: lang }).value;
              } catch (__) {}
          }
          return ''; // use external default escaping
      },
      html: true,
      linkify: true,
      typographer: true
  });
  window.md.use(window.mathjax3);

  window.md.renderer.rules.code_block = convertCodeBlock(window.md.renderer.rules.code_block);
  window.md.renderer.rules.code_inline = convertCodeBlock(window.md.renderer.rules.code_inline);
  window.md.renderer.rules.fence = convertCodeBlock(window.md.renderer.rules.fence);  

  setUp();
});

// https://samthor.au/2020/understanding-load/