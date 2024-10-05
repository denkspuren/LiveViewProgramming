const loadedDiv = document.getElementById('loadMessage');
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
      if(debug) console.log(`Message: ${event.data}`);
      receive(event.data);      
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

function decode(data) {
  return new TextDecoder("utf-8").decode(Uint8Array.from(atob(data), c => c.charCodeAt(0)));
}

function currentInstruction() {};

function compose(...functions) {
  return (input) => {
    return functions.reduce((acc, fn) => {
      return fn(acc);
    }, input);
  };
}

const commandCache = [];
//{action: 0, data: ""};

function receive(commands) {
  const commandList = commands.split("\n");
  for (let i = 0; i < commandList.length; i++) {
    const [command, data] = commandList[i].split(":");
    switch (command) {
      case "CALL":
      case "HTML":
        commandCache.push({(command === "CALL" ? 0 : 1), decode(data)});
        break;
      case "EXECUTE":
        interpret(commandCache);
        break;
      default:
        break;
    }
  }
  
}

function interpret(commandCache) {
  if (actions === undefined || actions.length === 0) return;
  if (debug) console.log(`Action: ${actions[0]}`);
  switch (actions[0]) {
    case "CALL": {
      if (debug) console.log(`Data: ${decode(actions[1])}`);
      const fn = () => Function(decode(actions[1])).apply(); // https://www.educative.io/answers/eval-vs-function-in-javascript
      currentInstruction = compose(currentInstruction, fn);
      handleActions(actions.slice(2, actions.length));
      break;
    }
    case "HTML": {
      if (debug) console.log(`Data: ${decode(actions[1])}`);
      const fn = () => document.body.innerHTML += decode(actions[1]);
      currentInstruction = compose(currentInstruction, fn);
      handleActions(actions.slice(2, actions.length));
      break;
    }
    case "LOAD": {
      loadedDiv.style.display = 'block';
      setTimeout(() => {
        loadedDiv.style.display = 'none';
      }, 300);
      let srcs = actions[1].split(',');
      srcs = srcs.map(src => src.trim());
      if (srcs.length >= 2) loadScriptWithFallback(srcs[0], srcs[1]);
      else loadScript(actions);
      break;
    }
    case "CLEAR": {
      const fn = () => {
        const toRemove = [];
        for (const node of document.body.children) {
          if (node.classList == null || !node.classList.contains("persistent")) {
            toRemove.push(node);
          }
        }
        toRemove.forEach(x => document.body.removeChild(x));
      }
      currentInstruction = compose(currentInstruction, fn);
      handleActions(actions.slice(1, actions.length));      
      break;
    }
    case "EXECUTE": {
      this.currentInstruction();
      this.currentInstruction = () => {};
      handleActions(actions.slice(1, actions.length));
      break;
    }
    default:
      console.log("Unknown Action");
      break;
  }
}

setUp();

// https://samthor.au/2020/understanding-load/
