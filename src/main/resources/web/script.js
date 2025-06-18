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

function clear(sourceId, global) {
  const element = !global ? document.getElementById(sourceId) : document.getElementById("events");
  while (element.firstChild) {
    element.removeChild(element.firstChild);
  }

  const styleElements = document.querySelectorAll(global ? 'style' : `style.${sourceId}`);
  styleElements.forEach(el => el.parentNode.removeChild(el));
  const scriptElements = document.body.querySelectorAll(global ? 'script' : `script.${sourceId}`);
  scriptElements.forEach(el => el.parentNode.removeChild(el));

  const errors = document.getElementById("errors");
  
  
  if (!global) {
    errors?.querySelectorAll(`.${sourceId}`).forEach(el => el.parentNode.removeChild(el));
    for (const prop of Object.getOwnPropertyNames(clerk[sourceId])) {
      delete clerk[sourceId][prop];
    }
  } else {
    while (errors.firstChild) {
      errors.removeChild(errors.firstChild);
    }
    for (const prop of Object.getOwnPropertyNames(clerk)) {
      delete clerk[prop];
    }
  }
    
  if (!errors.hasChildNodes()) errors.parentNode.style.display = "none";
    
}

function splitEventMessage(message) {
  const parts = message.split(':');
  if (parts.length <= 4) return parts;

  const firstThree = parts.slice(0, 3);
  const rest = parts.slice(3).join(':');
  return [...firstThree, rest];
}

function setUp() {
  if (window.EventSource) {
    const source = new EventSource(`/events`);

    source.onmessage = function (event) {
      const [action, sourceId, id, base64Data] = splitEventMessage(event.data);
      const data = new TextDecoder("utf-8").decode(Uint8Array.from(atob(base64Data), c => c.charCodeAt(0)));
      
      debugLog(`Action: ${action}\nSourceId: ${sourceId}\nId: ${id}\nData: ${data}`);

      let subView = document.getElementById(sourceId);
      if (!subView) {
        const subViewContainer = document.createElement("div");
        subViewContainer.innerHTML = `<span class="section-marker">${new TextDecoder("utf-8").decode(Uint8Array.from(atob(sourceId), c => c.charCodeAt(0)))}</span>`;
        subViewContainer.classList.add("section");
        subViewContainer.id = `subViewContainer-${sourceId}`;
        subView = document.createElement("div");
        subView.id = sourceId;
        subViewContainer.appendChild(subView);
        clerk[sourceId] = {};
        document.getElementById("events").appendChild(subViewContainer);
      }

      switch (action) {
        case "CALL": {
          Function(data).apply(); // https://www.educative.io/answers/eval-vs-function-in-javascript
          break;
        }
        case "SCRIPT": {
          const newElement = document.createElement("script");
          newElement.innerHTML = data;
          newElement.id = id;
          newElement.classList.add(sourceId);
          document.body.appendChild(newElement);
          break;
        }
        case "WRITE": {
          const newElement = document.createElement("div");
          newElement.innerHTML = data;
          newElement.id = id;
          subView.appendChild(newElement);
          break;
        }
        case "CSS": {
          const newElement = document.createElement("style");
          newElement.innerHTML = data;
          newElement.id = id;
          newElement.classList.add(sourceId);
          document.head.appendChild(newElement);
          break;
        }
        case "CLEAR": {
          scrollPosition = window.scrollY;
          clear(sourceId, id === "-1" || id === "all" || id === "global");
          break;
        }
        case "LOG": {
          const newElement = document.createElement("div");
          newElement.innerText = data;
          newElement.classList.add(sourceId);
          const errors = document.getElementById("errors");
          errors.appendChild(newElement);
          errors.parentNode.style.display = "";
          break;
        }
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
  const errorContainer = document.getElementsByClassName("error-container")[0];
    errorContainer.addEventListener("click", (event) => {
    const errors = document.getElementById("errors");
    if (!errors.contains(event.target))
      errorContainer.style.display = "none";
  });
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