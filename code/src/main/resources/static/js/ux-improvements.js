// Presentation behaviour only. Requests and business rules remain in app.js.
function formatOrderStatus(status) {
    return { PENDING: 'รอชำระเงิน', PAID: 'ชำระแล้ว', SHIPPING: 'กำลังเทรด', COMPLETED: 'เสร็จสิ้น', CANCELLED: 'ยกเลิกแล้ว' }[status] || status;
}

function formatRarityLabel(rarity) {
    return { COMMON: 'ทั่วไป', UNCOMMON: 'ไม่ทั่วไป', RARE: 'หายาก', DOUBLE_RARE: 'ดับเบิลแรร์', ART_RARE: 'อาร์ตแรร์', SUPER_RARE: 'ซูเปอร์แรร์', SPECIAL_ART_RARE: 'สเปเชียลอาร์ตแรร์', IMMERSIVE_RARE: 'อิมเมอร์ซีฟแรร์', CROWN_RARE: 'คราวน์แรร์' }[rarity] || formatEnumLabel(rarity);
}

function formatConditionLabel(condition) {
    return { MINT: 'สภาพสมบูรณ์', NEAR_MINT: 'เกือบสมบูรณ์', PLAYED: 'ผ่านการใช้งาน' }[condition] || formatEnumLabel(condition);
}

function openOrderCardImage() {
    const preview = document.getElementById('orderCardPreview');
    const dialog = document.getElementById('orderCardImageDialog');
    if (!preview?.getAttribute('src') || !dialog) return;
    const image = document.getElementById('orderCardLargeImage');
    image.src = preview.src;
    image.alt = preview.alt;
    document.getElementById('orderCardImageTitle').textContent = preview.alt;
    dialog.showModal();
}

document.addEventListener('DOMContentLoaded', () => {
    initCompactNavigation();
    initAccessibleDialogs();
    initInventoryFilters();
    initCardKeyboardAccess();
    initCustomerFriendFeedback();
    initOrderFilters();
    initGalleryHistory();
    initOrderSecondaryDetails();
    document.querySelectorAll('[data-label="สภาพ"] .rarity-pill').forEach(label => {
        label.textContent = formatConditionLabel(label.textContent.trim());
    });
    restoreSelectedCard();
});

function initOrderSecondaryDetails() {
    const breakpoint = window.matchMedia('(max-width: 768px)');
    const details = [...document.querySelectorAll('.order-secondary-details')];
    const update = () => details.forEach(element => element.open = !breakpoint.matches);
    breakpoint.addEventListener('change', update);
    update();
}

const bookingControlStates = new WeakMap();

function contactStoreAboutOrder(button) {
    const message = `สวัสดีครับ ต้องการติดต่อเรื่องออเดอร์ #${button.dataset.orderCode} ยอด ${formatBaht(button.dataset.orderAmount)} ครับ`;
    const url = buildMessengerUrl(message);
    copyToClipboard(message, 'คัดลอกรหัสและยอดออเดอร์แล้ว วางข้อความในแชทได้เลย').then(() => {
        if (!openMessengerDeepLink(url)) window.location.href = url;
    });
}

function setCustomerBookingBusy(busy) {
    const modal = document.getElementById('customerOrderModal');
    const button = document.getElementById('customerOrderSubmit');
    const label = button.querySelector('span');
    if (busy) button.dataset.idleLabel = label.textContent;
    label.textContent = busy ? 'กำลังบันทึก...' : button.dataset.idleLabel || 'ยืนยันคำสั่งซื้อ';
    modal.setAttribute('aria-busy', String(busy));
    modal.querySelectorAll('button, input').forEach(control => {
        if (busy) {
            bookingControlStates.set(control, control.disabled);
            control.disabled = true;
        } else {
            control.disabled = bookingControlStates.get(control) || false;
            bookingControlStates.delete(control);
        }
    });
}

function showCustomerBookingError(message) {
    const error = document.getElementById('customerOrderError');
    if (!error) return;
    error.textContent = message;
    error.hidden = !message;
}

function showTransitionError(message) {
    const error = document.getElementById('transitionError');
    if (error && document.getElementById('transitionModal').classList.contains('active')) {
        error.textContent = message;
        error.hidden = false;
    } else {
        showToast(message, 'danger');
    }
}

function initOrderFilters() {
    const search = document.getElementById('orderSearch');
    const filter = document.getElementById('orderStatusFilter');
    if (!search || !filter) return;
    const rows = [...document.querySelectorAll('.orders-table tr[data-order-status]')];
    const update = () => {
        const keyword = search.value.trim().toLocaleLowerCase();
        let count = 0;
        rows.forEach(row => {
            const match = (filter.value === 'all' || row.dataset.orderStatus === filter.value) &&
                [...row.cells].slice(0, 3).some(cell => cell.textContent.toLocaleLowerCase().includes(keyword));
            row.hidden = !match;
            if (match) count++;
        });
        document.getElementById('orderFilterCount').textContent = `${count} จาก ${rows.length} ออเดอร์`;
        document.getElementById('orderFilterEmpty').hidden = count > 0 || rows.length === 0;
    };
    search.addEventListener('input', update);
    filter.addEventListener('change', update);
    document.getElementById('orderFilterReset').addEventListener('click', () => {
        search.value = ''; filter.value = 'all'; update(); search.focus();
    });
    update();
}

function initGalleryHistory() {
    const recentKey = 'pokevault-gallery-location';
    const positionsKey = 'pokevault-gallery-scroll';
    const route = location.pathname + location.search;
    try {
        const recent = JSON.parse(sessionStorage.getItem(recentKey) || 'null');
        if (recent && Date.now() - recent.timestamp < 30 * 60 * 1000) {
            const url = new URL(recent.route, location.origin);
            if (url.origin === location.origin && url.pathname === '/cards') {
                document.querySelectorAll('[data-gallery-return]').forEach(link => link.href = url.pathname + url.search);
            }
        }
        if (location.pathname !== '/cards') return;
        sessionStorage.setItem(recentKey, JSON.stringify({ route, timestamp: Date.now() }));
        const positions = JSON.parse(sessionStorage.getItem(positionsKey) || '{}');
        const saved = positions[route];
        if (saved && Number.isFinite(saved.y) && Date.now() - saved.timestamp < 30 * 60 * 1000) {
            requestAnimationFrame(() => {
                if (!document.querySelector('.modal-overlay.active')) window.scrollTo({ top: saved.y, behavior: 'instant' });
            });
        }
        window.addEventListener('pagehide', () => {
            try {
                positions[route] = { y: window.scrollY, timestamp: Date.now() };
                const limited = Object.fromEntries(Object.entries(positions).sort((a, b) => b[1].timestamp - a[1].timestamp).slice(0, 12));
                sessionStorage.setItem(positionsKey, JSON.stringify(limited));
            } catch (_) { /* Navigation works without storage. */ }
        });
    } catch (_) { /* Private browsing may disable session storage. */ }
}

function initCompactNavigation() {
    const button = document.querySelector('.nav-menu-toggle');
    if (!button) return;
    button.addEventListener('click', () => {
        button.setAttribute('aria-expanded', String(button.getAttribute('aria-expanded') !== 'true'));
    });
    document.addEventListener('click', event => {
        if (!event.target.closest('.navbar')) button.setAttribute('aria-expanded', 'false');
    });
    document.addEventListener('keydown', event => {
        if (event.key === 'Escape' && button.getAttribute('aria-expanded') === 'true') {
            button.setAttribute('aria-expanded', 'false');
            button.focus();
        }
    });
}

function initAccessibleDialogs() {
    const dialogs = [...document.querySelectorAll('.modal-overlay')];
    if (!dialogs.length) return;
    const backgrounds = [...document.querySelectorAll('body > nav, body > main')];
    const openers = new WeakMap();
    let lastTrigger = null;
    let previous = [];
    let previousOverflow = '';
    document.addEventListener('click', event => {
        lastTrigger = event.target.closest('button, a, [tabindex]') || document.activeElement;
    }, true);
    const focusables = modal => [...modal.querySelectorAll('button, a[href], input, select, textarea, [tabindex]')]
        .filter(element => !element.disabled && element.tabIndex >= 0 && !element.closest('[hidden], [inert]') && element.getClientRects().length);
    const sync = () => {
        const active = dialogs.filter(modal => modal.classList.contains('active'));
        dialogs.forEach((modal, index) => {
            const visible = active.includes(modal);
            modal.inert = !visible;
            modal.setAttribute('aria-hidden', String(!visible));
            const heading = modal.querySelector('.modal-title, h2, h3');
            if (heading) {
                if (!heading.id) heading.id = `dialogHeading${index}`;
                modal.setAttribute('aria-labelledby', heading.id);
            }
            if (visible && !previous.includes(modal)) {
                // When moving between dialogs, return to the original page control.
                const trigger = lastTrigger?.closest('.modal-overlay');
                openers.set(modal, trigger ? openers.get(trigger) : lastTrigger || document.activeElement);
                if (!modal.contains(document.activeElement)) {
                    modal.tabIndex = -1;
                    (focusables(modal)[0] || modal).focus();
                }
            }
        });
        if (active.length && !previous.length) {
            previousOverflow = document.body.style.overflow;
            document.body.style.overflow = 'hidden';
        }
        backgrounds.forEach(element => element.inert = active.length > 0);
        if (!active.length && previous.length) {
            document.body.style.overflow = previousOverflow;
            const opener = openers.get(previous[previous.length - 1]);
            if (opener?.isConnected && !opener.closest('[inert]')) opener.focus();
        }
        previous = active;
    };
    const observer = new MutationObserver(sync);
    dialogs.forEach(modal => observer.observe(modal, { attributes: true, attributeFilter: ['class'] }));
    sync();
    document.addEventListener('keydown', event => {
        if (document.querySelector('dialog[open]')) return;
        const modal = dialogs.filter(item => item.classList.contains('active')).pop();
        if (!modal) return;
        if (event.key === 'Escape') {
            const closeButton = modal.querySelector('.modal-close-btn');
            if (closeButton && !closeButton.disabled) {
                event.preventDefault();
                closeButton.click();
            }
        } else if (event.key === 'Tab') {
            const available = focusables(modal);
            const first = available[0];
            const last = available[available.length - 1];
            if (!first) { event.preventDefault(); modal.focus(); return; }
            if (event.shiftKey && (document.activeElement === first || !modal.contains(document.activeElement))) {
                event.preventDefault(); last.focus();
            } else if (!event.shiftKey && (document.activeElement === last || !modal.contains(document.activeElement))) {
                event.preventDefault(); first.focus();
            }
        }
    });
}

function initCardKeyboardAccess() {
    document.querySelectorAll('.tcg-card-3d[role="button"]').forEach(card => {
        card.addEventListener('keydown', event => {
            if (event.key === 'Enter' || event.key === ' ') {
                event.preventDefault(); card.click();
            }
        });
    });
    const flipper = document.getElementById('inspectFlipperBox');
    if (flipper) {
        flipper.tabIndex = 0;
        flipper.setAttribute('role', 'button');
        flipper.setAttribute('aria-label', 'พลิกดูด้านหน้าและด้านหลังการ์ด');
        flipper.addEventListener('keydown', event => {
            if (event.key === 'Enter') { event.preventDefault(); toggleCardFlip(); }
        });
    }
}

function initInventoryFilters() {
    const search = document.getElementById('inventorySearch');
    const filter = document.getElementById('inventoryStockFilter');
    if (!search || !filter) return;
    const rows = [...document.querySelectorAll('tr[data-stock]')];
    const normalized = value => value.toLocaleLowerCase().replace(/\s+/g, ' ').trim();
    const update = () => {
        const keyword = normalized(search.value);
        let count = 0;
        rows.forEach(row => {
            const stock = Number(row.dataset.stock);
            const state = filter.value;
            const stockMatches = state === 'all' || (state === 'low' && row.dataset.lowStock === 'true') ||
                (state === 'available' && stock > 0) || (state === 'empty' && stock <= 0);
            const content = [...row.cells].slice(0, 2).map(cell => cell.textContent).join(' ');
            row.hidden = !(stockMatches && normalized(content).includes(keyword));
            if (!row.hidden) count++;
        });
        document.getElementById('inventoryResultCount').textContent = `${count} จาก ${rows.length} รายการ`;
        document.getElementById('inventoryNoResults').hidden = count > 0;
    };
    search.addEventListener('input', update);
    filter.addEventListener('change', update);
    update();
}

function updateCustomerFriendFeedback(showError = false) {
    const input = document.getElementById('customerOrderFriendId');
    if (!input) return;
    const digits = input.value.replace(/\D/g, '').length;
    const invalid = showError && digits !== 16;
    document.getElementById('customerFriendCount').textContent = `${digits}/16`;
    input.setAttribute('aria-invalid', String(invalid));
    const error = document.getElementById('customerFriendError');
    error.hidden = !invalid;
    error.textContent = invalid ? 'กรุณากรอก Friend ID ให้ครบ 16 หลัก' : '';
}

function initCustomerFriendFeedback() {
    const input = document.getElementById('customerOrderFriendId');
    if (!input) return;
    input.addEventListener('input', () => updateCustomerFriendFeedback(input.getAttribute('aria-invalid') === 'true'));
    input.addEventListener('blur', () => updateCustomerFriendFeedback(input.value.length > 0));
}

function restoreSelectedCard() {
    const storageKey = 'pokevault-selected-card';
    document.querySelectorAll('[data-remember-card]').forEach(link => {
        link.addEventListener('click', () => {
            if (!currentInspectCard) return;
            try {
                sessionStorage.setItem(storageKey, JSON.stringify({
                    name: currentInspectCard.name, number: currentInspectCard.cardNumber,
                    expansion: currentInspectCard.expansionCode, timestamp: Date.now()
                }));
            } catch (_) { /* Browsing still works when storage is disabled. */ }
        });
    });
    if (!document.getElementById('customerOrderUserId')) return;
    try {
        const selected = JSON.parse(sessionStorage.getItem(storageKey) || 'null');
        if (!selected) return;
        if (Date.now() - selected.timestamp > 30 * 60 * 1000) { sessionStorage.removeItem(storageKey); return; }
        const card = [...document.querySelectorAll('.tcg-card-3d[data-number]')].find(element =>
            element.dataset.number === selected.number && element.dataset.expansion === selected.expansion);
        if (card) {
            sessionStorage.removeItem(storageKey);
            card.scrollIntoView({ block: 'center' });
            card.focus();
            handleInspectClick(card);
        } else if (location.pathname !== '/cards') {
            location.assign('/cards?search=' + encodeURIComponent(selected.name));
        } else {
            sessionStorage.removeItem(storageKey);
            showToast('การ์ดที่เลือกไม่มีในรายการล่าสุด กรุณาเลือกการ์ดอีกครั้ง', 'info');
        }
    } catch (_) { /* Ignore malformed or unavailable session storage. */ }
}
