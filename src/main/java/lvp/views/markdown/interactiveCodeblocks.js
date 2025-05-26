function convertCodeBlock (renderer) {
    return function wrapRenderer(tokens, idx, options, env, slf) {
        const original = renderer(tokens, idx, options, env, slf)
            .replace(/src-info:(.*?)\|\|\|/g, '');
        const match = tokens[idx].content.match(/src-info:(.*?)(\|\|\|)/s);
        const content = tokens[idx].content.replace(/src-info:(.*?)\|\|\|/g, ' ');    
        
        return match != null ? `<div>` + original +
            `<textarea style="display: none; width: 100%; min-height: 30px; resize: none; max-height: 50vh" oninput="autoResize(this)">${content}</textarea>` +
            `<button id=${match[1]} onclick="editCodeBlock(this)" style="padding: 5px 10px; margin: 5px 10px 0px 0px">Edit Code</button>` +
            `<button onclick="cancelEdit(this)" style="padding: 5px 10px; display: none; margin: 5px 10px 0px 0px">Cancel</button>` +
            `</div>` : original;
    }
    
}

function editCodeBlock(element) {
    if (!element || !element.parentElement || !element.parentElement.parentElement) return;
    const codeBlock = element.parentElement.firstChild;
    const codeEditor = codeBlock.nextElementSibling;
    const cancelButton = element.nextElementSibling;
    if (codeEditor.style.display === 'none') {
        // Switch to edit mode
        codeEditor.value = codeBlock.textContent;
        codeBlock.style.display = 'none';
        codeEditor.style.display = 'block';
        autoResize(codeEditor);
        element.textContent = 'Save Code';
        cancelButton.style.display = 'inline';
    } else {
        fetch("interact", { method: "post", body: `${element.id}:${btoa(String.fromCharCode(...new TextEncoder().encode(codeEditor.value.trim())))}` }).catch(console.error);
    }
}

function cancelEdit(element) {
    if (!element || !element.parentElement || !element.parentElement.parentElement) return;
    const codeBlock = element.parentElement.firstChild;
    const codeEditor = codeBlock.nextElementSibling;
    const editButton = element.previousElementSibling;
    if (codeBlock.style.display === 'none') {
        codeEditor.style.display = 'none';
        codeBlock.style.display = 'block';
        editButton.textContent = 'Edit Code';
        element.style.display = 'none';
    }
}

function autoResize(textarea) {
    textarea.style.height = 'auto';
    textarea.style.height = textarea.scrollHeight + 'px';
}


