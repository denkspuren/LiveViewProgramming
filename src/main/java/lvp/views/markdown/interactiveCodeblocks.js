function convertCodeBlock (renderer) {
    return function wrapRenderer(tokens, idx, options, env, slf) {
        const original = renderer(tokens, idx, options, env, slf)
            .replace(/src-info:(.*?)\|\|\|/g, '');
        const match = tokens[idx].content.match(/src-info:(.*?)(\|\|\|)/s);
        const content = tokens[idx].content.replace(/src-info:(.*?)\|\|\|/g, '');    
        
        return match != null ? `<div>` + original +
            `<textarea style="display: none; width: 100%; min-height: 30px; resize: none; max-height: 50vh; margin: 0 0 5px 0" oninput="autoResize(this)">${content}</textarea>` +
            `<button id=${match[1]} data-action="edit" onclick="editCodeBlock(this)" style="padding: 5px 10px; margin: 0 10px 0 0">Edit Code</button>` +
            `<button data-action="cancel" onclick="cancelEdit(this)" style="padding: 5px 10px; display: none;">Cancel</button>` +
            `</div>` : original;
    }
    
}

function editCodeBlock(element) {
    if (!element?.parentElement) return;

    const container = element.parentElement;
    const codeBlock = container.querySelector('pre, code');
    const codeEditor = container.querySelector('textarea');
    const cancelButton = container.querySelector('button[data-action="cancel"]');

    if (!codeBlock || !codeEditor || !cancelButton) return;

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
    if (!element?.parentElement) return;

    const container = element.parentElement;
    const codeBlock = container.querySelector('pre, code');
    const codeEditor = container.querySelector('textarea');
    const editButton = container.querySelector('button[data-action="edit"]');

    if (!codeBlock || !codeEditor || !editButton) return;

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


