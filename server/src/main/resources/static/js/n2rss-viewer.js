/*
 * Copyright (c) 2025 Nicolas PICON
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

const $ = (s) => document.querySelector(s);
const statusEl = $('#status');
const itemsEl = $('#items');
const titleEl = $('#title');
const descEl = $('#desc');
const tpl = $('#tplItem');

async function load(url) {
    if (!url) return;
    statusEl.textContent = 'Loading…';
    try {
        const res = await fetch(url, {headers: {'Accept': 'application/xml,text/xml'}});
        if (!res.ok) throw new Error(res.status + ' ' + res.statusText);
        const xmlText = await res.text();
        const doc = new DOMParser().parseFromString(xmlText, 'application/xml');

        const parseErr = doc.querySelector('parsererror');
        if (parseErr) throw new Error('Invalid XML');

        renderRSS(doc)

        statusEl.textContent = '';
    } catch (e) {
        statusEl.textContent = 'Error: ' + e.message;
    }
}

function text(el) {
    return el?.textContent?.trim() ?? '';
}

function first(el, sel) {
    return el.querySelector(sel);
}

function renderRSS(doc) {
    const channel = doc.querySelector('channel');
    titleEl.textContent = text(first(channel, 'title'));
    descEl.textContent = text(first(channel, 'description'));
    const items = [...doc.querySelectorAll('channel > item')].slice(0, 50);
    renderItems(items.map(item => ({
        title: text(first(item, 'title')),
        link: text(first(item, 'link')),
        date: text(first(item, 'pubDate')) || text(first(item, 'dc\\:date')),
        summary: text(first(item, 'description')) || text(first(item, 'content\\:encoded')),
    })));
}

function renderItems(items) {
    const frag = document.createDocumentFragment();
    for (const it of items) {
        const node = tpl.content.cloneNode(true);
        const a = node.querySelector('a');
        a.textContent = it.title || it.link || '(sans titre)';
        if (it.link) a.href = it.link;
        node.querySelector('time').textContent = it.date;
        node.querySelector('.summary').innerHTML = sanitizeHTML(it.summary);
        frag.appendChild(node);
    }
    itemsEl.appendChild(frag);
}

function sanitizeHTML(html) {
    const tmp = document.createElement('div');
    tmp.innerHTML = html || '';
    tmp.querySelectorAll('script, iframe').forEach(n => n.remove());
    tmp.querySelectorAll('*').forEach(n => {
        [...n.attributes].forEach(a => {
            if (a.name.startsWith('on')) n.removeAttribute(a.name);
        });
    });
    return tmp.innerHTML;
}
