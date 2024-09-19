const loadedDiv = document.getElementById('loadMessage');
const actionCache = new Map();
const debug = true;

function loadScript(src, onError = () => console.error('script loading failed: ', src)) {
  var script = document.createElement('script');
  script.src = src;
  script.onload = function() {
    script.classList.add("persistent");
    console.log('script loaded:', src);
    fetch("/loaded", {method: "post"}).catch(console.error);
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
      handleActions(event.data);      
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

function handleActions(instruction) {
  const splitPos = instruction.indexOf(":");
  const action = instruction.slice(0, splitPos);
  const base64Data = instruction.slice(splitPos + 1);
  const data = new TextDecoder("utf-8").decode(Uint8Array.from(atob(base64Data), c => c.charCodeAt(0)));

  // const data = atob(base64Data);
  if (debug) console.log(`Action: ${action}\nData: ${data}\n`);
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
    case "CACHE": {
      const splitPos = data.indexOf(":");
      const id = data.slice(0, splitPos);
      const instruction = data.slice(splitPos + 1)
      if (debug) console.log(`Saving ${instruction} with ID ${id}!`);
      
      if (actionCache.has(id)) actionCache.get(id).push(instruction);
      else actionCache.set(id, [instruction]);
      break;
    }
    case "EXECUTE": {
      const instructions = actionCache.get(data);
      if (debug) console.log(`Executing ${instruction}`);
      if (instructions) {
        for (const instruction of instructions)
          handleActions(instruction);
      }
      break;
    }
    default:
      console.log("Unknown Action");
      break;
  }
}

setUp();

// https://samthor.au/2020/understanding-load/