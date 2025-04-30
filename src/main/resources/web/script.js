const loadedDiv = document.getElementById('loadMessage');
const debug = false;

let locks = [];


function loadScript(src, onError = () => {
  errorLog(`script loading failed: ${src}`);
  loadedDiv.style.display = 'none';
}) {
  loadedDiv.style.display = 'block';
  
  var script = document.createElement('script');
  script.src = src;
  script.onload = function() {
    script.classList.add("persistent");
    fetch("/loaded", {method: "post"}).catch(console.error);
    debugLog(`script loaded: ${src}`);
    loadedDiv.style.display = 'none';
  };
  script.onerror = onError;
  document.body.appendChild(script);
}

function loadScriptWithFallback(mainSrc, alternativeSrc) {
  loadScript(mainSrc, function() {
    debugLog('loading', mainSrc, 'failed, trying', alternativeSrc);
    loadScript(alternativeSrc);
  });
}

/**
 * Attempts to lock an event listener for the given ID until the associated callback 
 * is executed on the server. Prevents duplicate event handling.
 * 
 * @param {string} id - Unique identifier for the event listener.
 * @returns {boolean} - false if the listener is already locked;
 *                      true if the lock was successfully acquired.
 */

function lockAndCheck(id) {
  if (locks.includes(id)) return false;
  locks.push(id);
  return true;
}

// Always send debug logs to the server; log to browser console only if debug mode is enabled
function debugLog(message) {
  if(debug) console.debug(message);
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
        case "LOAD": {
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
          errorLog("Unknown Action");
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

setUp();

// https://samthor.au/2020/understanding-load/