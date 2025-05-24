function convertCodeBlock (renderer) {
    return function wrapRenderer(tokens, idx, options, env, slf) {
        const original = renderer(tokens, idx, options, env, slf)
            .replace(/src-info:(.*?)\|\|\|/g, '');
        const match = tokens[idx].content.match(/src-info:(.*?)(\|\|\|)/s);
        const content = tokens[idx].content.replace(/src-info:(.*?)\|\|\|/g, ' ');    
        
        return match != null ? `<div>` + original +
            `<textarea style="display: none; width: 700px; height: 150px">${content}</textarea>` +
            `<button id=${match[1]} onclick="editCodeBlock(this)">Edit Code</button>` + `</div>` : original;
    }
    
}

function editCodeBlock(element) {
    if (!element || !element.parentElement || !element.parentElement.parentElement) return;
    const codeBlock = element.parentElement.firstChild;
    const codeEditor = codeBlock.nextElementSibling
    if (codeEditor.style.display === 'none') {
        // Switch to edit mode
        codeEditor.value = codeBlock.textContent;
        codeBlock.style.display = 'none';
        codeEditor.style.display = 'block';
        element.textContent = 'Save Code';
    } else {
        fetch("interact", { method: "post", body: `${element.id}:${btoa(String.fromCharCode(...new TextEncoder().encode(codeEditor.value.trim())))}` }).catch(console.error);
    }
}


