/**
 * Bitly URL Shortener — Frontend Application
 */
const API_BASE = '/api/urls';
const AUTH_BASE = '/api/auth';

// DOM Elements - Original
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
const resultMeta = document.getElementById('result-meta');
const resultAddedAt = document.getElementById('result-added-at');
const resultExpiryItem = document.getElementById('result-expiry-item');
const resultExpiresAt = document.getElementById('result-expires-at');
const resultPasswordItem = document.getElementById('result-password-item');
const copyBtn = document.getElementById('copy-btn');
const qrBtn = document.getElementById('qr-btn');
const refreshBtn = document.getElementById('refresh-btn');
const linksGrid = document.getElementById('links-grid');
const emptyState = document.getElementById('empty-state');
const emptyTitle = document.getElementById('empty-title');
const emptyDesc = document.getElementById('empty-desc');
const qrModal = document.getElementById('qr-modal');
const qrModalClose = document.getElementById('qr-modal-close');
const qrImage = document.getElementById('qr-image');
const qrUrl = document.getElementById('qr-url');
const qrDownload = document.getElementById('qr-download');
const toast = document.getElementById('toast');
const toastMessage = document.getElementById('toast-message');

// DOM Elements - Auth & Controls
const authSection = document.getElementById('auth-section');
const userSection = document.getElementById('user-section');
const userDisplay = document.getElementById('user-display');
const loginNavBtn = document.getElementById('login-nav-btn');
const signupNavBtn = document.getElementById('signup-nav-btn');
const logoutNavBtn = document.getElementById('logout-nav-btn');

const authModal = document.getElementById('auth-modal');
const authModalClose = document.getElementById('auth-modal-close');
const authModalTitle = document.getElementById('auth-modal-title');
const authForm = document.getElementById('auth-form');
const authUsername = document.getElementById('auth-username');
const authEmailContainer = document.getElementById('email-field-container');
const authEmail = document.getElementById('auth-email');
const authPassword = document.getElementById('auth-password');
const authErrorBox = document.getElementById('auth-error-box');
const authSubmitBtn = document.getElementById('auth-submit-btn');
const authSwitchBtn = document.getElementById('auth-switch-btn');
const authSwitchText = document.getElementById('auth-switch-text');

const linkPassword = document.getElementById('link-password');
const enablePasswordCheckbox = document.getElementById('enable-password-checkbox');
const passwordInputContainer = document.getElementById('password-input-container');
const searchInput = document.getElementById('search-input');
const sortBySelect = document.getElementById('sort-by-select');
const sortDirSelect = document.getElementById('sort-dir-select');
const dashboardControls = document.getElementById('dashboard-controls');

// DOM Elements - Analytics
const analyticsModal = document.getElementById('analytics-modal');
const analyticsModalClose = document.getElementById('analytics-modal-close');
const analyticsOriginalUrl = document.getElementById('analytics-original-url');
const analyticsClickCount = document.getElementById('analytics-click-count');
const analyticsTableBody = document.getElementById('analytics-table-body');

let currentResult = null;
let currentUser = null;
let isSignUpMode = false;

// ===== Authentication state checks =====

async function checkAuth() {
    try {
        const res = await fetch(`${AUTH_BASE}/me`);
        if (res.ok) {
            currentUser = await res.json();
            showUserSession(currentUser.username);
            dashboardControls.classList.remove('hidden');
            loadLinks();
        } else {
            clearSession();
        }
    } catch (err) {
        clearSession();
    }
}

function showUserSession(username) {
    authSection.classList.add('hidden');
    userSection.classList.remove('hidden');
    userDisplay.textContent = username;
    emptyTitle.textContent = "No links yet";
    emptyDesc.textContent = "Shorten your first URL above to get started!";
}

function clearSession() {
    currentUser = null;
    authSection.classList.remove('hidden');
    userSection.classList.add('hidden');
    dashboardControls.classList.add('hidden');
    linksGrid.innerHTML = '';
    emptyState.classList.remove('hidden');
    emptyTitle.textContent = "Log in to get started";
    emptyDesc.textContent = "Create an account or log in to manage, search, and secure your links!";
}

// ===== Auth Modal Controls =====

loginNavBtn.addEventListener('click', () => openAuthModal(false));
signupNavBtn.addEventListener('click', () => openAuthModal(true));

function openAuthModal(signup = false) {
    isSignUpMode = signup;
    authUsername.value = '';
    authEmail.value = '';
    authPassword.value = '';
    authErrorBox.classList.add('hidden');
    
    if (isSignUpMode) {
        authModalTitle.textContent = 'Create Account';
        authEmailContainer.classList.remove('hidden');
        authSubmitBtn.querySelector('span').textContent = 'Sign Up';
        authSwitchText.textContent = 'Already have an account?';
        authSwitchBtn.textContent = 'Log In';
    } else {
        authModalTitle.textContent = 'Sign In';
        authEmailContainer.classList.add('hidden');
        authSubmitBtn.querySelector('span').textContent = 'Sign In';
        authSwitchText.textContent = "Don't have an account?";
        authSwitchBtn.textContent = 'Sign Up';
    }
    
    authModal.classList.remove('hidden');
}

authModalClose.addEventListener('click', () => authModal.classList.add('hidden'));
authModal.addEventListener('click', (e) => { if (e.target === authModal) authModal.classList.add('hidden'); });

authSwitchBtn.addEventListener('click', () => {
    openAuthModal(!isSignUpMode);
});

// ===== Form Submits =====

authForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    authErrorBox.classList.add('hidden');

    const username = authUsername.value.trim();
    const password = authPassword.value;
    
    if (!username || !password) return;

    authSubmitBtn.disabled = true;
    const originalText = authSubmitBtn.querySelector('span').textContent;
    authSubmitBtn.querySelector('span').textContent = isSignUpMode ? 'Signing up...' : 'Signing in...';

    try {
        if (isSignUpMode) {
            const email = authEmail.value.trim();
            const res = await fetch(`${AUTH_BASE}/signup`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, email, password })
            });
            const data = await res.json();
            if (!res.ok) {
                authErrorBox.textContent = data.message || 'Signup failed';
                authErrorBox.classList.remove('hidden');
                return;
            }
            // Auto login after signup
            isSignUpMode = false;
            authPassword.value = password;
            authForm.dispatchEvent(new Event('submit'));
        } else {
            const res = await fetch(`${AUTH_BASE}/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });
            const data = await res.json();
            if (!res.ok) {
                authErrorBox.textContent = data.message || 'Invalid username or password';
                authErrorBox.classList.remove('hidden');
                return;
            }
            authModal.classList.add('hidden');
            showToast('Welcome back!');
            checkAuth();
        }
    } catch (err) {
        authErrorBox.textContent = 'Network error. Please try again.';
        authErrorBox.classList.remove('hidden');
    } finally {
        authSubmitBtn.disabled = false;
        authSubmitBtn.querySelector('span').textContent = originalText;
    }
});

logoutNavBtn.addEventListener('click', async () => {
    try {
        await fetch(`${AUTH_BASE}/logout`, { method: 'POST' });
        showToast('Logged out successfully');
        clearSession();
    } catch (err) {
        showToast('Logout failed');
    }
});

// ===== Form Shorten Submit =====
form.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideMessages();

    if (!currentUser) {
        openAuthModal(false);
        showToast('Please log in first!');
        return;
    }

    const url = urlInput.value.trim();
    if (!url) return;

    const body = { url };
    const alias = customAliasInput.value.trim();
    const expires = expiresAtInput.value;
    
    if (alias) body.customAlias = alias;
    if (expires) body.expiresAt = expires;
    
    if (enablePasswordCheckbox && enablePasswordCheckbox.checked) {
        const password = linkPassword.value.trim();
        if (password) {
            body.password = password;
        }
    }

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
        linkPassword.value = '';
        if (enablePasswordCheckbox) {
            enablePasswordCheckbox.checked = false;
        }
        if (passwordInputContainer) {
            passwordInputContainer.classList.add('hidden');
        }
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

// ===== Enable/Disable Password Fields =====
if (enablePasswordCheckbox && passwordInputContainer) {
    enablePasswordCheckbox.addEventListener('change', () => {
        if (enablePasswordCheckbox.checked) {
            passwordInputContainer.classList.remove('hidden');
            linkPassword.focus();
        } else {
            passwordInputContainer.classList.add('hidden');
            linkPassword.value = '';
        }
    });
}

// ===== Show/Hide Messages =====
function showResult(data) {
    resultShortUrl.href = data.shortUrl;
    resultShortUrl.textContent = data.shortUrl;
    resultOriginalUrl.textContent = data.originalUrl;
    
    resultAddedAt.textContent = formatDate(data.createdAt);
    if (data.expiresAt) {
        resultExpiresAt.textContent = formatDate(data.expiresAt);
        resultExpiryItem.classList.remove('hidden');
    } else {
        resultExpiryItem.classList.add('hidden');
    }
    
    if (data.passwordProtected) {
        resultPasswordItem.classList.remove('hidden');
    } else {
        resultPasswordItem.classList.add('hidden');
    }
    
    resultMeta.classList.remove('hidden');
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

// ===== Toast =====
function showToast(msg) {
    toastMessage.textContent = msg;
    toast.classList.remove('hidden');
    setTimeout(() => toast.classList.add('hidden'), 2500);
}

// ===== Load & Search Links =====

async function loadLinks() {
    if (!currentUser) return;
    
    const q = searchInput.value.trim();
    const sortBy = sortBySelect.value;
    const sortDir = sortDirSelect.value;

    try {
        const url = `${API_BASE}/search?q=${encodeURIComponent(q)}&sortBy=${sortBy}&sortDir=${sortDir}`;
        const res = await fetch(url);
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

// Attach Search & Sort Event Listeners for Instant Results
searchInput.addEventListener('input', loadLinks);
sortBySelect.addEventListener('change', loadLinks);
sortDirSelect.addEventListener('change', loadLinks);

function createLinkCard(link, index) {
    const isExpired = link.expiresAt && new Date(link.expiresAt) < new Date();
    const clicks = link.clickCount || 0;
    const created = formatDate(link.createdAt);
    const expiry = link.expiresAt ? formatDate(link.expiresAt) : null;
    const isProtected = link.passwordProtected;

    return `
    <div class="link-card" style="animation-delay: ${index * 0.05}s">
        <div class="link-card-top">
            <div class="link-card-url">
                <a href="${link.shortUrl}" target="_blank" class="link-card-short">${link.shortUrl}</a>
                <div class="link-card-original">${link.originalUrl}</div>
            </div>
            <div class="link-card-actions">
                ${isExpired ? '<span class="expired-badge">Expired</span>' : ''}
                ${isProtected ? `
                <span class="lock-badge" title="Password Protected">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                </span>` : ''}
                <button class="btn btn-icon" title="Copy" onclick="copyLink('${link.shortUrl}')">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>
                </button>
                <button class="btn btn-icon" title="QR Code" onclick="openQrModal('${link.shortCode}', '${link.shortUrl}')">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="2" width="8" height="8" rx="1"/><rect x="14" y="2" width="8" height="8" rx="1"/><rect x="2" y="14" width="8" height="8" rx="1"/></svg>
                </button>
                <button class="btn btn-icon" title="Analytics Log" onclick="openAnalyticsModal('${link.shortCode}', '${link.shortUrl}')">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>
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
                <span>Added:</span>
                <span class="meta-value">${created}</span>
            </div>
            ${expiry ? `
            <div class="meta-item">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z"/><path d="m9 12 2 2 4-4"/></svg>
                <span>Expires:</span>
                <span class="meta-value ${isExpired ? 'text-danger' : ''}">${expiry}</span>
            </div>` : ''}
        </div>
    </div>`;
}

// ===== Click Analytics Modal =====

async function openAnalyticsModal(shortCode, shortUrl) {
    try {
        const res = await fetch(`${API_BASE}/${shortCode}/analytics`);
        if (!res.ok) {
            showToast('Could not fetch analytics');
            return;
        }
        
        const data = await res.json();
        analyticsOriginalUrl.textContent = data.originalUrl;
        analyticsClickCount.textContent = data.totalClicks;
        
        if (!data.recentClicks || !data.recentClicks.length) {
            analyticsTableBody.innerHTML = `<tr><td colspan="4" class="text-center text-muted py-4">No click visitor data logged yet.</td></tr>`;
        } else {
            analyticsTableBody.innerHTML = data.recentClicks.map(click => {
                const formattedTime = new Date(click.clickedAt).toLocaleString('en-US', {
                    month: 'short',
                    day: 'numeric',
                    hour: 'numeric',
                    minute: '2-digit',
                    second: '2-digit'
                });
                return `
                <tr>
                    <td class="font-mono text-xs">${formattedTime}</td>
                    <td class="truncate" style="max-width: 140px;" title="${click.referrer}">${click.referrer}</td>
                    <td>${click.deviceType}</td>
                    <td class="font-mono text-xs">${click.ipAddress}</td>
                </tr>`;
            }).join('');
        }
        
        analyticsModal.classList.remove('hidden');
    } catch (err) {
        showToast('Error loading analytics');
    }
}

analyticsModalClose.addEventListener('click', () => analyticsModal.classList.add('hidden'));
analyticsModal.addEventListener('click', (e) => { if (e.target === analyticsModal) analyticsModal.classList.add('hidden'); });

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

// Escape key closes modals
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        qrModal.classList.add('hidden');
        authModal.classList.add('hidden');
        analyticsModal.classList.add('hidden');
    }
});

// ===== Init =====
checkAuth();
