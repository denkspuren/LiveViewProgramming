const debug = true;

const loadedDiv = document.getElementById('loadMessage');

let commandCache = [];  //{action: 0, data: ""};
let locks = [];
let commandStore = new Map(); //k = ID; v = {action, data}

function loadScript(src, onError = () => console.error('script loading failed: ', src)) {
  const script = document.createElement('script');
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

function receive(commands) {
  const commandList = commands.split("#");

  for (const command of commandList) {
    if (!command) return;
    if (debug) console.log(`Processing: ${command}`);

    const splitPos = command.indexOf(":");
    const action = command.slice(0, splitPos !== -1 ? splitPos : command.length);

    switch (action) {
      case "CALL": {
        const data = decode(command.slice(splitPos+1));
        commandCache.push({ action: 0, data: data});
        break;
      }
      case "HTML": {
        const data = decode(command.slice(splitPos+1));
        commandCache.push({ action: 1, data: data});
        break;
      }
      case "SCRIPT":  {
        const data = decode(command.slice(splitPos+1));
        commandCache.push({ action: 2, data: data});
        break;
      }        
      case "LOAD": {
        const data = command.slice(splitPos+1);
        loadedDiv.style.display = 'block';
        setTimeout(() => {
          loadedDiv.style.display = 'none';
        }, 300);
        let srcs = data.split(',');
        srcs = srcs.map(src => src.trim());
        if (srcs.length >= 2) loadScriptWithFallback(srcs[0], srcs[1]);
        else loadScript(data);
        break;
      }
      case "CLEAR": {
        const toRemove = [];
        for (const node of document.body.children) {
          if (node.classList?.contains("persistent") === false) {
            toRemove.push(node);
          }
        }
        toRemove.forEach(x => document.body.removeChild(x));
        commandCache = [];
        commandStore.clear();
        break;
      }
      case "RELEASE": {
        const data = command.slice(splitPos+1);
        locks = locks.filter(lock => lock !== data);
        break;
      }
      case "EXECUTE":
        interpret(commandCache);
        commandCache = [];
        break;
      case "STORE": {
        const data = command.slice(splitPos+1);
        commandStore.set(data, commandCache);
        commandCache = [];
        break;
      }
      case "RESTORE": {
        const data = command.slice(splitPos+1);
        const commands = commandStore.get(data);

        if (commands) commandCache = commandCache.concat(commands);
        else console.warn(`CommandStore Entry with ID: ${data} not found!`);
        break;
      }
      default:
        console.error("Unknown Action");
        break;
    }
  }
  
}

function interpret(commandCache) {
  if (commandCache === undefined) return;

  for (const command of commandCache) {
    if (debug) console.log(`Command: ${command.action} - ${command.data}`);

    switch (command.action) {
      case 0:
        Function(command.data).apply(); // https://www.educative.io/answers/eval-vs-function-in-javascript
        break;
      case 1: {
        const container = document.createElement("div");
        container.innerHTML = command.data;
        document.body.appendChild(container);
        break;
      }
      case 2: {
        const scriptTag = document.createElement("script");
        scriptTag.innerHTML = command.data;
        document.body.appendChild(scriptTag);
        break;
      }
      default:
        console.error("Unknown Action");
        break;
    }
  }
}

setUp();

// https://samthor.au/2020/understanding-load/
