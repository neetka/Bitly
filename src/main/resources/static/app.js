/**
 * Bitly URL Shortener — Frontend Application
 */
const API_BASE = '/api/urls';

// DOM Elements
const form = document.getElementById('shorten-form');
const urlInput = document.getElementById('url-input');
const shortenBtn = document.getElementById('shorten-btn');
const toggleAdvancedBtn = document.getElementById('toggle-advanced-btn');
const advancedOptions = document.getElementById('advanced-options');
const customAliasInput = document.getElementById('custom-alias');
const expiresAtInput = document.getElementById('expires-at');
const resultCard = document.getElementById('result-card');
const errorCard = document.getElementById('error-card');
const errorMessage = document.getElementById('error-message');
const resultShortUrl = document.getElementById('result-short-url');
const resultOriginalUrl = document.getElementById('result-original-url');
const copyBtn = document.getElementById('copy-btn');
const qrBtn = document.getElementById('qr-btn');
const refreshBtn = document.getElementById('refresh-btn');
const linksGrid = document.getElementById('links-grid');
const emptyState = document.getElementById('empty-state');
const qrModal = document.getElementById('qr-modal');
const qrModalClose = document.getElementById('qr-modal-close');
const qrImage = document.getElementById('qr-image');
const qrUrl = document.getElementById('qr-url');
const qrDownload = document.getElementById('qr-download');
const toast = document.getElementById('toast');
const toastMessage = document.getElementById('toast-message');

let currentResult = null;

// ===== Form Submit =====
form.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideMessages();

    const url = urlInput.value.trim();
    if (!url) return;

    const body = { url };
    const alias = customAliasInput.value.trim();
    const expires = expiresAtInput.value;
    if (alias) body.customAlias = alias;
    if (expires) body.expiresAt = expires;

    shortenBtn.disabled = true;
    shortenBtn.innerHTML = '<div class="spinner"></div><span>Shortening...</span>';

    try {
        const res = await fetch(API_BASE, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body),
        });

        const data = await res.json();

        if (!res.ok) {
            const msg = data.validationErrors
                ? Object.values(data.validationErrors).join(', ')
                : data.message || 'Something went wrong';
            showError(msg);
            return;
        }

        currentResult = data;
        showResult(data);
        urlInput.value = '';
        customAliasInput.value = '';
        expiresAtInput.value = '';
        loadLinks();
    } catch (err) {
        showError('Network error. Is the server running?');
    } finally {
        shortenBtn.disabled = false;
        shortenBtn.innerHTML = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></svg><span>Shorten</span>';
    }
});

// ===== Toggle Advanced =====
toggleAdvancedBtn.addEventListener('click', () => {
    advancedOptions.classList.toggle('show');
    toggleAdvancedBtn.classList.toggle('active');
});

// ===== Show/Hide Messages =====
function showResult(data) {
    resultShortUrl.href = data.shortUrl;
    resultShortUrl.textContent = data.shortUrl;
    resultOriginalUrl.textContent = data.originalUrl;
    resultCard.classList.remove('hidden');
    errorCard.classList.add('hidden');
}

function showError(msg) {
    errorMessage.textContent = msg;
    errorCard.classList.remove('hidden');
    resultCard.classList.add('hidden');
}

function hideMessages() {
    resultCard.classList.add('hidden');
    errorCard.classList.add('hidden');
}

// ===== Copy =====
copyBtn.addEventListener('click', () => {
    if (!currentResult) return;
    navigator.clipboard.writeText(currentResult.shortUrl).then(() => showToast('Copied to clipboard!'));
});

// ===== QR Code =====
qrBtn.addEventListener('click', () => {
    if (!currentResult) return;
    openQrModal(currentResult.shortCode, currentResult.shortUrl);
});

function openQrModal(shortCode, shortUrl) {
    qrImage.src = `${API_BASE}/${shortCode}/qr?width=300&height=300`;
    qrUrl.textContent = shortUrl;
    qrDownload.href = `${API_BASE}/${shortCode}/qr?width=600&height=600`;
    qrModal.classList.remove('hidden');
}

qrModalClose.addEventListener('click', () => qrModal.classList.add('hidden'));
qrModal.addEventListener('click', (e) => { if (e.target === qrModal) qrModal.classList.add('hidden'); });
document.addEventListener('keydown', (e) => { if (e.key === 'Escape') qrModal.classList.add('hidden'); });

// ===== Toast =====
function showToast(msg) {
    toastMessage.textContent = msg;
    toast.classList.remove('hidden');
    setTimeout(() => toast.classList.add('hidden'), 2500);
}

// ===== Load Links =====
async function loadLinks() {
    try {
        const res = await fetch(API_BASE);
        const links = await res.json();

        if (!links.length) {
            linksGrid.innerHTML = '';
            emptyState.classList.remove('hidden');
            return;
        }

        emptyState.classList.add('hidden');
        linksGrid.innerHTML = links.map((link, i) => createLinkCard(link, i)).join('');
    } catch (err) {
        console.error('Failed to load links:', err);
    }
}

function createLinkCard(link, index) {
    const isExpired = link.expiresAt && new Date(link.expiresAt) < new Date();
    const clicks = link.clickCount || 0;
    const created = formatDate(link.createdAt);
    const lastAccessed = link.lastAccessedAt ? formatDate(link.lastAccessedAt) : 'Never';
    const expiry = link.expiresAt ? formatDate(link.expiresAt) : null;

    return `
    <div class="link-card" style="animation-delay: ${index * 0.05}s">
        <div class="link-card-top">
            <div class="link-card-url">
                <a href="${link.shortUrl}" target="_blank" class="link-card-short">${link.shortUrl}</a>
                <div class="link-card-original">${link.originalUrl}</div>
            </div>
            <div class="link-card-actions">
                ${isExpired ? '<span class="expired-badge">Expired</span>' : ''}
                <button class="btn btn-icon" title="Copy" onclick="copyLink('${link.shortUrl}')">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>
                </button>
                <button class="btn btn-icon" title="QR Code" onclick="openQrModal('${link.shortCode}', '${link.shortUrl}')">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="2" width="8" height="8" rx="1"/><rect x="14" y="2" width="8" height="8" rx="1"/><rect x="2" y="14" width="8" height="8" rx="1"/></svg>
                </button>
                <button class="btn btn-danger-ghost" title="Delete" onclick="deleteLink('${link.shortCode}')">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                </button>
            </div>
        </div>
        <div class="link-card-meta">
            <div class="meta-item">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                <span class="meta-value">${clicks}</span> clicks
            </div>
            <div class="meta-item">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                ${created}
            </div>
            ${expiry ? `<div class="meta-item">${isExpired ? '⛔' : '⏳'} ${expiry}</div>` : ''}
        </div>
    </div>`;
}

// ===== Delete Link =====
async function deleteLink(shortCode) {
    if (!confirm('Delete this link permanently?')) return;
    try {
        await fetch(`${API_BASE}/${shortCode}`, { method: 'DELETE' });
        showToast('Link deleted');
        loadLinks();
    } catch (err) {
        showToast('Failed to delete');
    }
}

// ===== Copy Link =====
function copyLink(url) {
    navigator.clipboard.writeText(url).then(() => showToast('Copied to clipboard!'));
}

// ===== Format Date =====
function formatDate(dateStr) {
    const d = new Date(dateStr);
    const now = new Date();
    const diff = now - d;

    if (diff < 60000) return 'Just now';
    if (diff < 3600000) return `${Math.floor(diff / 60000)}m ago`;
    if (diff < 86400000) return `${Math.floor(diff / 3600000)}h ago`;
    if (diff < 604800000) return `${Math.floor(diff / 86400000)}d ago`;

    return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

// ===== Refresh =====
refreshBtn.addEventListener('click', () => {
    refreshBtn.querySelector('svg').style.animation = 'spin 0.5s linear';
    setTimeout(() => refreshBtn.querySelector('svg').style.animation = '', 500);
    loadLinks();
});

// ===== Init =====
loadLinks();
