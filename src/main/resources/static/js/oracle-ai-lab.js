(() => {
    'use strict';

    const apiBase = document.body.dataset.apiBase || '/api';
    const result = document.querySelector('#api-result');
    const status = document.querySelector('#request-status');
    const conversationId = document.querySelector('#conversation-id');

    const render = (label, payload) => {
        if (result) {
            result.textContent = `${label}\n\n${JSON.stringify(payload, null, 2)}`;
        }
    };

    const setStatus = (message, isError = false) => {
        if (status) {
            status.textContent = message;
            status.classList.toggle('is-error', isError);
        }
    };

    const request = async (path, options = {}) => {
        setStatus('Working…');
        const response = await fetch(`${apiBase}${path}`, {
            ...options,
            headers: { Accept: 'application/json', ...options.headers }
        });
        const responseText = await response.text();
        let payload = responseText;
        if ((response.headers.get('content-type') || '').includes('application/json') && responseText) {
            try {
                payload = JSON.parse(responseText);
            } catch (_) {
                // The architecture endpoint remains a plain String for API compatibility.
            }
        }
        if (!response.ok) {
            const message = typeof payload === 'object' && payload.message ? payload.message : response.statusText;
            throw new Error(`${response.status}: ${message}`);
        }
        return payload;
    };

    const postJson = (path, body) => request(path, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });

    const submit = (formId, action) => {
        const form = document.querySelector(formId);
        if (!form) {
            return;
        }
        form.addEventListener('submit', async (event) => {
            event.preventDefault();
            if (!form.reportValidity()) {
                return;
            }
            try {
                render('Success', await action(form));
                setStatus('Completed successfully.');
            } catch (error) {
                render('Request failed', { message: error.message });
                setStatus('The request did not complete. Review the response details.', true);
            }
        });
    };

    const seedDemoCorpus = async () => {
        try {
            render('Demo RAG corpus indexed', await postJson('/knowledge/demo-corpus', {}));
            setStatus('Demo RAG corpus indexed successfully.');
        } catch (error) {
            render('Request failed', { message: error.message });
            setStatus('The demo corpus could not be indexed.', true);
        }
    };

    const seedButton = document.querySelector('#seed-button-secondary');
    if (seedButton) {
        seedButton.addEventListener('click', seedDemoCorpus);
    }

    submit('#start-conversation-form', async (form) => {
        const payload = await postJson('/conversations', { message: form.elements.message.value.trim() });
        if (conversationId) {
            conversationId.value = payload.conversationId;
        }
        return payload;
    });

    submit('#continue-conversation-form', (form) => postJson(
        `/conversations/${encodeURIComponent(form.elements.conversationId.value.trim())}/messages`,
        { message: form.elements.message.value.trim() }
    ));

    submit('#index-document-form', (form) => postJson('/knowledge/documents', {
        title: form.elements.title.value.trim(),
        content: form.elements.content.value.trim()
    }));

    submit('#agent-form', (form) => postJson('/agent/ask', { message: form.elements.message.value.trim() }));
})();
