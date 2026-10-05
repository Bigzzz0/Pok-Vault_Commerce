// Immediate theme initialization to prevent flash of wrong theme
(function() {
    const savedTheme = localStorage.getItem('tcg-theme') || 'dark';
    document.documentElement.setAttribute('data-theme', savedTheme);
})();

document.addEventListener('DOMContentLoaded', () => {
    initTheme();
    init3DTilt();
    initInspectionFlipper();
    initAudioTriggers();
});

// --- Theme Management System (Pitch-Black Obsidian Vault / Clean Studio Gallery) ---
function initTheme() {
    const savedTheme = localStorage.getItem('tcg-theme') || 'dark';
    setTheme(savedTheme, false);
}

function setTheme(theme, playAudio = true) {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('tcg-theme', theme);

    const toggleBtns = document.querySelectorAll('.theme-toggle-btn');
    toggleBtns.forEach(btn => {
        btn.setAttribute('title', theme === 'dark' ? 'Switch to Clean Studio Light' : 'Switch to Pitch-Black Obsidian Vault');
        btn.setAttribute('aria-label', theme === 'dark' ? 'Switch to Clean Studio Light' : 'Switch to Pitch-Black Obsidian Vault');
        const sunIcon = btn.querySelector('.theme-icon-sun');
        const moonIcon = btn.querySelector('.theme-icon-moon');
        if (sunIcon && moonIcon) {
            if (theme === 'dark') {
                sunIcon.style.display = 'inline-flex';
                moonIcon.style.display = 'none';
            } else {
                sunIcon.style.display = 'none';
                moonIcon.style.display = 'inline-flex';
            }
        }
    });

    if (playAudio && window.soundFx) {
        window.soundFx.playClick();
    }
}

function toggleTheme() {
    const current = document.documentElement.getAttribute('data-theme') || 'dark';
    const nextTheme = current === 'dark' ? 'light' : 'dark';
    setTheme(nextTheme, true);
    showToast(`Switched to ${nextTheme === 'dark' ? 'Pitch-Black Obsidian Vault' : 'Clean Studio Gallery'}`, 'info');
}

// --- 1. 3D Holographic Parallax Tilt & Specular Light Engine ---
function init3DTilt() {
    const cards = document.querySelectorAll('.holo-card:not(#inspectFlipperBox), .tcg-card-3d:not(#inspectFlipperBox)');

    cards.forEach(card => {
        let hoverSoundTriggered = false;
        let rafId = null;

        card.addEventListener('mouseenter', () => {
            card.classList.add('interacting');
            card.style.transition = 'transform 0.12s ease-out, box-shadow 0.25s ease';
            if (!hoverSoundTriggered && window.soundFx) {
                window.soundFx.playHover();
                hoverSoundTriggered = true;
            }
        });

        card.addEventListener('pointermove', (e) => {
            const rect = card.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;

            const centerX = rect.width / 2;
            const centerY = rect.height / 2;

            const percentX = Math.max(0, Math.min(100, (x / rect.width) * 100));
            const percentY = Math.max(0, Math.min(100, (y / rect.height) * 100));

            // Dynamic rotation angles: max 14 degrees for responsive 3D tilt
            const rotateX = (((y - centerY) / centerY) * -13).toFixed(2);
            const rotateY = (((x - centerX) / centerX) * 13).toFixed(2);

            const dx = (percentX - 50);
            const dy = (percentY - 50);
            const fromCenter = Math.min(1, Math.sqrt(dx * dx + dy * dy) / 50);

            // Shifting holographic texture coordinates
            const bgX = (35 + percentX * 0.3).toFixed(1);
            const bgY = (35 + percentY * 0.3).toFixed(1);

            if (rafId) cancelAnimationFrame(rafId);
            rafId = requestAnimationFrame(() => {
                // Apply 3D perspective tilt & slight zoom
                card.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) scale3d(1.04, 1.04, 1.04)`;

                // Simeydotme shader coordinates
                card.style.setProperty('--pointer-x', `${percentX.toFixed(1)}%`);
                card.style.setProperty('--pointer-y', `${percentY.toFixed(1)}%`);
                card.style.setProperty('--pointer-from-center', fromCenter.toFixed(2));
                card.style.setProperty('--rotate-x', `${rotateY}deg`);
                card.style.setProperty('--rotate-y', `${rotateX}deg`);
                card.style.setProperty('--background-x', `${bgX}%`);
                card.style.setProperty('--background-y', `${bgY}%`);
                card.style.setProperty('--card-opacity', '0.92');

                // Specular spotlight glare
                card.style.setProperty('--glare-x', `${percentX.toFixed(1)}%`);
                card.style.setProperty('--glare-y', `${percentY.toFixed(1)}%`);
                card.style.setProperty('--glare-opacity', '0.7');
            });
        });

        card.addEventListener('pointerleave', () => {
            if (rafId) cancelAnimationFrame(rafId);
            card.classList.remove('interacting');
            card.style.transition = 'transform 0.5s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.4s ease';
            card.style.transform = 'perspective(1000px) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1)';
            card.style.setProperty('--card-opacity', '0');
            card.style.setProperty('--glare-opacity', '0');
            hoverSoundTriggered = false;
            setTimeout(() => {
                if (!card.classList.contains('interacting')) {
                    card.style.transition = '';
                }
            }, 500);
        });
    });
}

function initAudioTriggers() {
    document.querySelectorAll('.btn, .nav-link, .stepper-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            if (window.soundFx) window.soundFx.playClick();
        });
    });
}

// --- 2. 3D Card Inspection Stage Modal with Tilt & Flip ---
let activeCardData = null;
let isCardFlipped = false;

function initInspectionFlipper() {
    const flipperBox = document.getElementById('inspectFlipperBox');
    if (!flipperBox) return;

    let hoverSoundTriggered = false;

    flipperBox.addEventListener('mouseenter', () => {
        if (!hoverSoundTriggered && window.soundFx) {
            window.soundFx.playHover();
            hoverSoundTriggered = true;
        }
    });

    flipperBox.addEventListener('mousemove', (e) => {
        const rect = flipperBox.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;

        const centerX = rect.width / 2;
        const centerY = rect.height / 2;

        const percentX = ((x / rect.width) * 100).toFixed(1);
        const percentY = ((y / rect.height) * 100).toFixed(1);

        // Max tilt angle in modal: 16 degrees for a dramatic 3D inspection
        const rotateX = (((y - centerY) / centerY) * -15).toFixed(2);
        const rotateY = (((x - centerX) / centerX) * 15).toFixed(2);

        // If card is flipped to back, inverse the horizontal tilt so it mirrors perspective naturally
        const effectiveY = isCardFlipped ? -rotateY : rotateY;

        flipperBox.style.transform = `perspective(1200px) rotateX(${rotateX}deg) rotateY(${effectiveY}deg) scale3d(1.02, 1.02, 1.02)`;

        // Update Simeydotme Shimmer & Diffraction variables on modal card
        flipperBox.style.setProperty('--pointer-x', `${percentX}%`);
        flipperBox.style.setProperty('--pointer-y', `${percentY}%`);
        flipperBox.style.setProperty('--background-x', `${(35 + percentX * 0.3).toFixed(1)}%`);
        flipperBox.style.setProperty('--background-y', `${(35 + percentY * 0.3).toFixed(1)}%`);
        flipperBox.style.setProperty('--card-opacity', '0.95');

        const frontGlare = document.getElementById('inspectGlareFront');
        const backGlare = document.getElementById('inspectGlareBack');

        if (!isCardFlipped && frontGlare) {
            frontGlare.style.setProperty('--glare-x', `${percentX}%`);
            frontGlare.style.setProperty('--glare-y', `${percentY}%`);
            frontGlare.style.setProperty('--glare-opacity', '0.65');
        } else if (isCardFlipped && backGlare) {
            backGlare.style.setProperty('--glare-x', `${(100 - percentX).toFixed(1)}%`);
            backGlare.style.setProperty('--glare-y', `${percentY}%`);
            backGlare.style.setProperty('--glare-opacity', '0.65');
        }
    });

    flipperBox.addEventListener('mouseleave', () => {
        flipperBox.style.transform = 'perspective(1200px) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1)';
        flipperBox.style.setProperty('--card-opacity', '0');
        const frontGlare = document.getElementById('inspectGlareFront');
        const backGlare = document.getElementById('inspectGlareBack');
        if (frontGlare) frontGlare.style.setProperty('--glare-opacity', '0');
        if (backGlare) backGlare.style.setProperty('--glare-opacity', '0');
        hoverSoundTriggered = false;
    });

    // Keyboard shortcuts for modal
    document.addEventListener('keydown', (e) => {
        const modal = document.getElementById('inspectionModal');
        if (modal && modal.classList.contains('active')) {
            if (e.code === 'Space' || e.key === 'f' || e.key === 'F') {
                e.preventDefault();
                toggleCardFlip();
            } else if (e.key === 'Escape') {
                closeInspection();
            }
        }
    });
}

function toggleCardFlip() {
    isCardFlipped = !isCardFlipped;
    const flipperBox = document.getElementById('inspectFlipperBox');
    const label = document.getElementById('inspectFlipLabel');

    if (flipperBox) {
        if (isCardFlipped) {
            flipperBox.classList.add('is-flipped');
        } else {
            flipperBox.classList.remove('is-flipped');
        }
        // Smoothly reset box tilt during flip
        flipperBox.style.transform = 'perspective(1200px) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1)';
    }

    if (label) {
        label.textContent = isCardFlipped ? 'Flip Card (Front)' : 'Flip Card (Back)';
    }

    if (window.soundFx) {
        window.soundFx.playInspect();
    }
}

function openInspection(card) {
    activeCardData = card;
    if (window.soundFx) window.soundFx.playInspect();

    // Reset card flip to front side
    isCardFlipped = false;
    const flipperBox = document.getElementById('inspectFlipperBox');
    if (flipperBox) {
        flipperBox.classList.remove('is-flipped');
        flipperBox.style.transform = 'perspective(1200px) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1)';
        if (card.rarity) {
            flipperBox.setAttribute('data-rarity', card.rarity);
        }
    }
    const label = document.getElementById('inspectFlipLabel');
    if (label) {
        label.textContent = 'Flip Card (Back)';
    }

    // 3D Entry Flip Animation (Smooth 360-degree spin on arrival)
    const flipperInner = document.getElementById('inspectFlipperInner');
    if (flipperInner) {
        flipperInner.style.transition = 'none';
        flipperInner.style.transform = 'rotateY(-360deg)';
        setTimeout(() => {
            flipperInner.style.transition = 'transform 0.8s cubic-bezier(0.16, 1, 0.3, 1)';
            flipperInner.style.transform = 'rotateY(0deg)';
        }, 50);
    }

    const imgElem = document.getElementById('inspectCardImg');
    if (imgElem && card.imageUrl) {
        imgElem.src = card.imageUrl;
    }

    document.getElementById('inspectCardNumber').textContent = `${card.expansionCode || 'A1'} #${card.cardNumber}`;
    document.getElementById('inspectCardName').textContent = card.name;
    document.getElementById('inspectCardHp').textContent = card.hp > 0 ? `HP ${card.hp}` : '';
    document.getElementById('inspectCardDesc').textContent = card.description || 'Rare Pokémon TCG Pocket collectible card.';
    document.getElementById('inspectCardRarity').textContent = card.rarityDescription || card.rarity;
    document.getElementById('inspectCardStock').textContent = `In Stock: ${card.totalStock || 0} copies`;

    // Dynamic Element Theme for Modal Glow
    const aura = document.getElementById('inspectAura');
    if (aura) {
        aura.className = 'inspect-aura ' + getElementGlowClass(card.elementType);
    }

    const modal = document.getElementById('inspectionModal');
    modal.classList.add('active');
}

function handleInspectClick(elem) {
    const card = {
        name: elem.getAttribute('data-name'),
        cardNumber: elem.getAttribute('data-number'),
        expansionCode: elem.getAttribute('data-expansion'),
        hp: parseInt(elem.getAttribute('data-hp')) || 0,
        rarity: elem.getAttribute('data-rarity') || elem.getAttribute('data-rarity-desc'),
        rarityDescription: elem.getAttribute('data-rarity-desc') || elem.getAttribute('data-rarity'),
        description: elem.getAttribute('data-desc'),
        totalStock: parseInt(elem.getAttribute('data-stock')) || 0,
        elementType: elem.getAttribute('data-element'),
        imageUrl: elem.getAttribute('data-image')
    };
    openInspection(card);
}

function handleOrderClick(btn) {
    const id = btn.getAttribute('data-id');
    const name = btn.getAttribute('data-name');
    const number = btn.getAttribute('data-number');
    const condition = btn.getAttribute('data-condition');
    const price = btn.getAttribute('data-price');
    const stock = btn.getAttribute('data-stock');
    openOrderModal(id, name, number, condition, price, stock);
}

function closeInspection() {
    const modal = document.getElementById('inspectionModal');
    modal.classList.remove('active');
    if (window.soundFx) window.soundFx.playClick();
}

function getElementGlowClass(element) {
    switch (element) {
        case 'FIRE': return 'aura-fire';
        case 'WATER': return 'aura-water';
        case 'LIGHTNING': return 'aura-volt';
        case 'PSYCHIC': return 'aura-psy';
        case 'GRASS': return 'aura-grass';
        default: return 'aura-neutral';
    }
}

// --- 3. Live Strategy Pattern Order Drawer & Calculator ---
let currentOrderTarget = null;
const TIER_DISCOUNTS = {
    'REGULAR': 0.00,
    'VIP': 0.10,
    'ELITE': 0.15
};

function openOrderModal(inventoryId, cardName, cardNumber, condition, unitPrice, availableStock) {
    if (availableStock <= 0) {
        showToast('This item is currently out of stock!', 'danger');
        return;
    }

    currentOrderTarget = {
        inventoryId: inventoryId,
        cardName: cardName,
        cardNumber: cardNumber,
        condition: condition,
        unitPrice: parseFloat(unitPrice),
        availableStock: parseInt(availableStock),
        quantity: 1
    };

    document.getElementById('orderCardTitle').textContent = `${cardName} (${condition})`;
    document.getElementById('orderCardNumber').textContent = cardNumber;
    document.getElementById('orderAvailableStock').textContent = `Available: ${availableStock} in store`;
    document.getElementById('orderQtyInput').value = 1;
    document.getElementById('orderQtyInput').max = availableStock;

    updateOrderCalculation();

    const modal = document.getElementById('orderModal');
    modal.classList.add('active');
    if (window.soundFx) window.soundFx.playInspect();
}

function closeOrderModal() {
    const modal = document.getElementById('orderModal');
    modal.classList.remove('active');
    if (window.soundFx) window.soundFx.playClick();
}

function adjustOrderQty(delta) {
    if (!currentOrderTarget) return;
    const input = document.getElementById('orderQtyInput');
    let currentVal = parseInt(input.value) || 1;
    let newVal = currentVal + delta;

    if (newVal >= 1 && newVal <= currentOrderTarget.availableStock) {
        input.value = newVal;
        currentOrderTarget.quantity = newVal;
        updateOrderCalculation();
        if (window.soundFx) window.soundFx.playClick();
    }
}

function updateOrderCalculation() {
    if (!currentOrderTarget) return;

    const userSelect = document.getElementById('orderUserSelect');
    const selectedOption = userSelect.options[userSelect.selectedIndex];
    const tier = selectedOption.getAttribute('data-tier') || 'REGULAR';
    const discountRate = TIER_DISCOUNTS[tier] || 0.00;

    const subtotal = currentOrderTarget.unitPrice * currentOrderTarget.quantity;
    const discountAmount = subtotal * discountRate;
    const finalAmount = Math.max(0, subtotal - discountAmount);

    document.getElementById('orderCalcSubtotal').textContent = `฿${subtotal.toFixed(2)}`;
    document.getElementById('orderCalcDiscountRate').textContent = `${tier} (-${(discountRate * 100).toFixed(0)}%)`;
    document.getElementById('orderCalcDiscountAmt').textContent = `-฿${discountAmount.toFixed(2)}`;
    document.getElementById('orderCalcFinal').textContent = `฿${finalAmount.toFixed(2)}`;
}

async function submitOrder() {
    if (!currentOrderTarget) return;

    const userSelect = document.getElementById('orderUserSelect');
    const userId = parseInt(userSelect.value);
    const notes = document.getElementById('orderNotesInput') ? document.getElementById('orderNotesInput').value : '';

    const friendIdInput = document.getElementById('orderFriendIdInput');
    const ignInput = document.getElementById('orderInGameNameInput');
    const customerFriendId = friendIdInput && friendIdInput.value.trim() ? friendIdInput.value.trim() : "1111-2222-3333-4444";
    const customerInGameName = ignInput && ignInput.value.trim() ? ignInput.value.trim() : "AshMaster";

    const payload = {
        userId: userId,
        customerFriendId: customerFriendId,
        customerInGameName: customerInGameName,
        items: [
            {
                inventoryId: currentOrderTarget.inventoryId,
                quantity: currentOrderTarget.quantity
            }
        ],
        notes: notes || "Online Vault Order"
    };

    const submitBtn = document.getElementById('orderSubmitBtn');
    submitBtn.disabled = true;
    submitBtn.textContent = "Processing...";

    try {
        const response = await fetch('/api/v1/orders', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const result = await response.json();

        if (response.ok && result.success) {
            if (window.soundFx) window.soundFx.playOrderChime();
            showToast(`Order ${result.data.orderCode} created! Strategy Discount: -฿${result.data.discountAmount}`, 'success');
            closeOrderModal();
            
            // Trigger Chat Commerce Handshake Modal
            showChatCommerceModal(result.data, currentOrderTarget, customerFriendId, customerInGameName);
        } else {
            showToast(result.message || 'Order failed. Please check stock.', 'danger');
        }
    } catch (err) {
        showToast('Network error while placing order: ' + err.message, 'danger');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = "Confirm Order & Pay";
    }
}

// --- Chat Commerce Handshake Helpers ---
let lastChatSummaryText = '';

function showChatCommerceModal(orderData, targetCard, friendId, ign) {
    const modal = document.getElementById('chatCommerceModal');
    if (!modal) {
        setTimeout(() => window.location.reload(), 1200);
        return;
    }

    const codeElem = document.getElementById('chatOrderCode');
    const cardElem = document.getElementById('chatOrderCardName');
    const friendElem = document.getElementById('chatOrderFriendId');
    const finalElem = document.getElementById('chatOrderFinalAmt');
    const messengerBtn = document.getElementById('chatMessengerBtn');

    const cardTitle = targetCard ? `${targetCard.cardName} (${targetCard.condition})` : 'Pokémon TCG Card';
    const finalAmountStr = `฿${orderData.finalAmount.toFixed(2)}`;

    if (codeElem) codeElem.textContent = `#${orderData.orderCode}`;
    if (cardElem) cardElem.textContent = cardTitle;
    if (friendElem) friendElem.textContent = friendId;
    if (finalElem) finalElem.textContent = finalAmountStr;

    // Compose prefilled message for Facebook Messenger
    const summaryMsg = `สวัสดีครับ สั่งจองการ์ดผ่านเว็บเรียบร้อยแล้วครับ!\n• รหัสคำสั่งซื้อ: #${orderData.orderCode}\n• รายการการ์ด: ${cardTitle}\n• ยอดชำระ: ${finalAmountStr} (ส่วนลด Strategy: -฿${orderData.discountAmount.toFixed(2)})\n• รหัสเพื่อนในเกม (Friend ID): ${friendId}\n• ชื่อเทรนเนอร์ (IGN): ${ign}\nขอส่งหลักฐานการโอนเงินและนัดส่งการ์ดเทรดในเกมครับ`;
    lastChatSummaryText = summaryMsg;

    if (messengerBtn) {
        // Facebook m.me link with prefilled text parameter
        messengerBtn.href = `https://m.me/poketcgpocketstore?text=${encodeURIComponent(summaryMsg)}`;
    }

    modal.classList.add('active');
}

function closeChatCommerceModal() {
    const modal = document.getElementById('chatCommerceModal');
    if (modal) modal.classList.remove('active');
    setTimeout(() => window.location.reload(), 300);
}

function copyChatOrderSummary(btn) {
    if (!lastChatSummaryText) return;
    navigator.clipboard.writeText(lastChatSummaryText).then(() => {
        showToast('คัดลอกข้อความสรุปออเดอร์แล้ว! สามารถนำไปวางส่งในแชทได้ทันที', 'success');
        if (btn) {
            const span = btn.querySelector('span');
            if (span) {
                const orig = span.textContent;
                span.textContent = '✓ คัดลอกสำเร็จแล้ว!';
                setTimeout(() => span.textContent = orig, 2000);
            }
        }
    }).catch(() => {
        showToast('ไม่สามารถคัดลอกข้อความอัตโนมัติได้', 'danger');
    });
}

// --- 4. Quick Stock Stepper (Inventory Page) ---
async function quickAdjustStock(inventoryId, delta) {
    const qtyElem = document.getElementById(`stockQty_${inventoryId}`);
    if (!qtyElem) return;

    let currentQty = parseInt(qtyElem.textContent) || 0;
    let newQty = currentQty + delta;
    if (newQty < 0) return;

    qtyElem.textContent = newQty;

    try {
        const response = await fetch(`/api/v1/inventories/${inventoryId}/stock?quantity=${newQty}`, {
            method: 'PATCH'
        });
        const result = await response.json();

        if (response.ok) {
            showToast(`Stock updated for item #${inventoryId}: ${newQty} in vault`, 'success');
            if (newQty <= 3) {
                qtyElem.classList.add('low-stock-alert');
            } else {
                qtyElem.classList.remove('low-stock-alert');
            }
        } else {
            qtyElem.textContent = currentQty;
            showToast(result.message || 'Failed to update stock', 'danger');
        }
    } catch (e) {
        qtyElem.textContent = currentQty;
        showToast('Error communicating with server', 'danger');
    }
}

// --- 5. Order State Machine Transition Action ---
async function executeOrderTransition(orderId, action) {
    if (!confirm(`Are you sure you want to transition Order #${orderId} with action: '${action}'?`)) {
        return;
    }

    try {
        const response = await fetch(`/api/v1/orders/${orderId}/transition?action=${action}`, {
            method: 'PATCH'
        });
        const result = await response.json();

        if (response.ok && result.success) {
            if (window.soundFx) window.soundFx.playOrderChime();
            showToast(`Order status successfully transitioned to: ${result.data.orderStatus}`, 'success');
            setTimeout(() => window.location.reload(), 1000);
        } else {
            showToast(result.message || 'State transition rejected by State Pattern rules!', 'danger');
        }
    } catch (e) {
        showToast('Network error during state transition: ' + e.message, 'danger');
    }
}

// --- 6. Futuristic Toast Notifications ---
function showToast(message, type = 'info') {
    let container = document.getElementById('toastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toastContainer';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
        <div class="toast-indicator"></div>
        <div class="toast-message">${message}</div>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.classList.add('show');
    }, 10);

    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 400);
    }, 3800);
}

// --- 7. Clipboard Copy Helper ---
// --- 7. Clipboard Copy Utilities ---
function copyToClipboard(text, label = 'Copied to clipboard!') {
    if (!text) return;
    if (navigator.clipboard) {
        navigator.clipboard.writeText(text).then(() => {
            showToast(label, 'success');
            if (window.soundFx) window.soundFx.playClick();
        }).catch(() => {
            legacyCopy(text, label);
        });
    } else {
        legacyCopy(text, label);
    }
}

function legacyCopy(text, label) {
    const textarea = document.createElement('textarea');
    textarea.value = text;
    textarea.style.position = 'fixed';
    textarea.style.opacity = '0';
    document.body.appendChild(textarea);
    textarea.select();
    try {
        document.execCommand('copy');
        showToast(label, 'success');
        if (window.soundFx) window.soundFx.playClick();
    } catch (err) {
        showToast('Failed to copy', 'danger');
    }
    document.body.removeChild(textarea);
}

function copyFriendCode(code, btn) {
    copyToClipboard(code, `Friend Code ${code} copied!`);
    if (btn) {
        const origColor = btn.style.color;
        btn.style.color = '#10b981';
        setTimeout(() => { btn.style.color = origColor; }, 1200);
    }
}

function copyCustomerModalFriendCode(btn) {
    const codeElem = document.getElementById('tradeCustomerFriendCode');
    if (codeElem) {
        copyFriendCode(codeElem.textContent.trim(), btn);
    }
}

// --- 8. In-Game Trade Matching & Fulfillment Manager Modal ---
let currentTradeOrderId = null;
let currentTradeCustomerFriendId = null;

async function openTradeModal(orderId, orderCode, customerFriendId) {
    currentTradeOrderId = orderId;
    currentTradeCustomerFriendId = customerFriendId || '1111-2222-3333-4444';

    const modal = document.getElementById('tradeFulfillmentModal');
    const title = document.getElementById('tradeModalOrderTitle');
    const sub = document.getElementById('tradeModalCustomerSub');
    const fcElem = document.getElementById('tradeCustomerFriendCode');
    const tbody = document.getElementById('tradeItemsTableBody');
    const banner = document.getElementById('tradeRecommendationBanner');

    if (!modal || !tbody) return;

    if (title) title.textContent = `In-Game Trade Manager - Order #${orderCode || orderId}`;
    if (sub) sub.textContent = `Customer Friend ID: ${currentTradeCustomerFriendId}`;
    if (fcElem) fcElem.textContent = currentTradeCustomerFriendId;
    if (banner) banner.style.display = 'none';

    tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; padding: 2rem; color: var(--text-muted);">Calculating trade allocations from Game Accounts Vault...</td></tr>`;
    modal.classList.add('active');

    try {
        const response = await fetch(`/api/v1/orders/${orderId}/trade-recommendations`);
        const result = await response.json();

        if (response.ok && result.success) {
            renderTradeModalContent(result.data);
        } else {
            tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: #ef4444; padding: 2rem;">Error: ${result.message}</td></tr>`;
        }
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: #ef4444; padding: 2rem;">Network Error: ${e.message}</td></tr>`;
    }
}

function closeTradeModal() {
    const modal = document.getElementById('tradeFulfillmentModal');
    if (modal) modal.classList.remove('active');
    currentTradeOrderId = null;
}

function renderTradeModalContent(data) {
    const banner = document.getElementById('tradeRecommendationBanner');
    const tbody = document.getElementById('tradeItemsTableBody');

    // Display recommendation insight
    if (banner) {
        if (data.singleAccountMatchPossible && data.bestSingleAccount) {
            banner.style.display = 'flex';
            banner.style.justifyContent = 'space-between';
            banner.style.alignItems = 'center';
            banner.style.background = 'rgba(16, 185, 129, 0.12)';
            banner.style.border = '1px solid rgba(16, 185, 129, 0.35)';
            banner.innerHTML = `
                <div>
                    <div style="color: #10b981; font-weight: 800; font-size: 0.95rem; margin-bottom: 0.2rem;">
                        ✨ Perfect Single-Account Trade Found!
                    </div>
                    <div style="font-size: 0.85rem; color: var(--text-secondary);">
                        Account <strong>${data.bestSingleAccount.accountCode} (${data.bestSingleAccount.inGameName})</strong> owns ALL cards needed. Fulfill entire order with 1 friend trade!
                    </div>
                </div>
                <button type="button" class="btn btn-primary" onclick="triggerAutoMatch()" style="font-size: 0.8rem; padding: 0.4rem 0.85rem;">
                    Auto-Assign Single Account
                </button>
            `;
        } else if (data.recommendationMessage) {
            banner.style.display = 'block';
            banner.style.background = 'rgba(59, 130, 246, 0.1)';
            banner.style.border = '1px solid rgba(59, 130, 246, 0.3)';
            banner.innerHTML = `
                <div style="color: var(--accent-cyan); font-weight: 700; font-size: 0.88rem;">
                    💡 Trade Recommendation: ${data.recommendationMessage}
                </div>
            `;
        } else {
            banner.style.display = 'none';
        }
    }

    if (!data.itemOptions || data.itemOptions.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: var(--text-muted); padding: 2rem;">No items found in this order.</td></tr>`;
        return;
    }

    let rowsHtml = '';
    data.itemOptions.forEach(item => {
        const isAssigned = !!item.currentAssignedAccountId;
        const status = item.tradeStatus || 'UNASSIGNED';

        let statusBadge = `<span style="font-size: 0.75rem; padding: 2px 8px; border-radius: 4px; background: rgba(239, 68, 68, 0.15); color: #ef4444; font-weight: 700;">UNASSIGNED</span>`;
        if (status === 'FRIEND_PENDING') {
            statusBadge = `<span style="font-size: 0.75rem; padding: 2px 8px; border-radius: 4px; background: rgba(245, 158, 11, 0.15); color: #f59e0b; font-weight: 700;">🤝 FRIEND PENDING</span>`;
        } else if (status === 'TRADE_SENT') {
            statusBadge = `<span style="font-size: 0.75rem; padding: 2px 8px; border-radius: 4px; background: rgba(59, 130, 246, 0.15); color: #3b82f6; font-weight: 700;">📤 TRADE SENT</span>`;
        } else if (status === 'COMPLETED') {
            statusBadge = `<span style="font-size: 0.75rem; padding: 2px 8px; border-radius: 4px; background: rgba(16, 185, 129, 0.15); color: #10b981; font-weight: 700;">✓ COMPLETED</span>`;
        }

        // Account options dropdown
        let selectHtml = `<select onchange="handleAccountSelectChange(${item.orderItemId}, this.value)" class="gallery-search-input" style="font-size: 0.8rem; padding: 0.3rem 0.5rem; border-radius: 6px; background: var(--bg-surface-elevated); color: var(--text-primary); max-width: 220px;">`;
        selectHtml += `<option value="" ${!isAssigned ? 'selected' : ''}>-- Select Game Account --</option>`;

        if (item.candidates) {
            item.candidates.forEach(cand => {
                const sel = (item.currentAssignedAccountId === cand.accountId) ? 'selected' : '';
                selectHtml += `<option value="${cand.accountId}" ${sel}>${cand.accountCode} - ${cand.inGameName} (${cand.availableStock} in stock)</option>`;
            });
        }
        selectHtml += `</select>`;

        // Action buttons based on status
        let actionButtons = '';
        if (isAssigned) {
            if (status === 'UNASSIGNED' || status === 'FRIEND_PENDING') {
                actionButtons += `
                    <button class="btn btn-secondary" style="padding: 0.25rem 0.6rem; font-size: 0.75rem;" onclick="advanceItemTradeStatus(${item.orderItemId}, 'TRADE_SENT')">
                        Mark Trade Sent
                    </button>
                `;
            } else if (status === 'TRADE_SENT') {
                actionButtons += `
                    <button class="btn btn-primary" style="padding: 0.25rem 0.6rem; font-size: 0.75rem;" onclick="advanceItemTradeStatus(${item.orderItemId}, 'COMPLETED')">
                        Complete In-Game Trade
                    </button>
                `;
            } else if (status === 'COMPLETED') {
                actionButtons += `<span style="color: #10b981; font-size: 0.8rem; font-weight: 700;">Traded In-Game</span>`;
            }
        }

        rowsHtml += `
            <tr>
                <td>
                    <div style="display: flex; align-items: center; gap: 0.6rem;">
                        <img src="${item.imageUrl}" alt="${item.cardName}" style="width: 36px; height: 50px; object-fit: cover; border-radius: 4px;">
                        <div>
                            <div style="font-weight: 700; font-size: 0.88rem;">${item.cardName}</div>
                            <div style="font-family: 'JetBrains Mono', monospace; font-size: 0.75rem; color: var(--accent-cyan);">${item.cardNumber}</div>
                        </div>
                    </div>
                </td>
                <td style="font-weight: 700;">${item.quantityNeeded}x</td>
                <td>${selectHtml}</td>
                <td>
                    ${item.currentAssignedAccountFriendCode ? `
                        <div style="display: inline-flex; align-items: center; gap: 0.4rem; background: var(--bg-surface-elevated); padding: 0.2rem 0.5rem; border-radius: 4px; border: 1px solid var(--border-color);">
                            <span style="font-family: 'JetBrains Mono', monospace; font-size: 0.78rem; color: var(--accent-gold);">${item.currentAssignedAccountFriendCode}</span>
                            <button type="button" class="icon-btn" onclick="copyFriendCode('${item.currentAssignedAccountFriendCode}', this)" title="Copy Friend Code" style="background: none; border: none; cursor: pointer; color: var(--text-muted); display: inline-flex; align-items: center; padding: 0;">
                                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><rect width="14" height="14" x="8" y="8" rx="2"/><path d="M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2"/></svg>
                            </button>
                        </div>
                    ` : '<span style="color: var(--text-muted); font-size: 0.8rem;">-</span>'}
                </td>
                <td>${statusBadge}</td>
                <td>${actionButtons}</td>
            </tr>
        `;
    });

    tbody.innerHTML = rowsHtml;
}

async function triggerAutoMatch() {
    if (!currentTradeOrderId) return;
    try {
        const response = await fetch(`/api/v1/orders/${currentTradeOrderId}/auto-match`, {
            method: 'POST'
        });
        const result = await response.json();

        if (response.ok && result.success) {
            showToast('Order cards auto-matched with optimal Game Accounts!', 'success');
            if (window.soundFx) window.soundFx.playOrderChime();
            openTradeModal(currentTradeOrderId, currentTradeOrderId, currentTradeCustomerFriendId);
        } else {
            showToast(result.message || 'Auto-matching failed. Some cards may be out of stock in accounts.', 'danger');
        }
    } catch (e) {
        showToast('Network error: ' + e.message, 'danger');
    }
}

async function handleAccountSelectChange(orderItemId, accountId) {
    if (!accountId) return;
    try {
        const response = await fetch('/api/v1/orders/assign-trade-account', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                orderItemId: parseInt(orderItemId),
                gameAccountId: parseInt(accountId)
            })
        });
        const result = await response.json();

        if (response.ok && result.success) {
            showToast('Assigned Game Account for this card trade!', 'success');
            if (window.soundFx) window.soundFx.playClick();
            openTradeModal(currentTradeOrderId, currentTradeOrderId, currentTradeCustomerFriendId);
        } else {
            showToast(result.message || 'Failed to assign account', 'danger');
        }
    } catch (e) {
        showToast('Error: ' + e.message, 'danger');
    }
}

async function advanceItemTradeStatus(orderItemId, newStatus) {
    if (!currentTradeOrderId) return;
    try {
        const response = await fetch(`/api/v1/orders/${currentTradeOrderId}/items/${orderItemId}/trade-status?status=${newStatus}`, {
            method: 'PATCH'
        });
        const result = await response.json();

        if (response.ok && result.success) {
            showToast(`Trade status updated to: ${newStatus}`, 'success');
            if (window.soundFx) window.soundFx.playClick();
            openTradeModal(currentTradeOrderId, currentTradeOrderId, currentTradeCustomerFriendId);
        } else {
            showToast(result.message || 'Failed to update status', 'danger');
        }
    } catch (e) {
        showToast('Error: ' + e.message, 'danger');
    }
}

// --- 9. Game Accounts Vault (accounts.html) Modal Handlers ---

// Modal 1: Register New Game Account
function openCreateAccountModal() {
    const modal = document.getElementById('createAccountModal');
    if (modal) modal.classList.add('active');
    if (window.soundFx) window.soundFx.playInspect();
}

function closeCreateAccountModal() {
    const modal = document.getElementById('createAccountModal');
    if (modal) modal.classList.remove('active');
}

async function handleCreateAccount(event) {
    event.preventDefault();
    const code = document.getElementById('accCode').value.trim();
    const ign = document.getElementById('accInGameName').value.trim();
    const friendCode = document.getElementById('accFriendCode').value.trim();
    const level = parseInt(document.getElementById('accLevel').value) || 20;
    const trades = parseInt(document.getElementById('accTrades').value) || 5;
    const notes = document.getElementById('accNotes').value.trim();

    const payload = {
        accountCode: code,
        inGameName: ign,
        friendCode: friendCode,
        accountLevel: level,
        dailyTradesRemaining: trades,
        notes: notes
    };

    try {
        const response = await fetch('/api/v1/accounts', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const result = await response.json();

        if (response.ok && result.success) {
            showToast(`Account ${result.data.accountCode} registered in vault!`, 'success');
            if (window.soundFx) window.soundFx.playOrderChime();
            closeCreateAccountModal();
            setTimeout(() => window.location.reload(), 1000);
        } else {
            showToast(result.message || 'Failed to register account', 'danger');
        }
    } catch (e) {
        showToast('Network error: ' + e.message, 'danger');
    }
}

// Modal 2: Record Pulled Card / Open Pack
let cachedCardsList = null;

async function ensureCardsListLoaded() {
    const select = document.getElementById('pullCardSelect');
    if (!select || select.options.length > 1) return;

    try {
        const response = await fetch('/api/v1/cards');
        const result = await response.json();
        if (response.ok && result.success) {
            cachedCardsList = result.data;
            let options = '<option value="" disabled selected>Select Card</option>';
            result.data.forEach(c => {
                options += `<option value="${c.id}">${c.expansionCode} #${c.cardNumber} - ${c.name} (${c.rarity})</option>`;
            });
            select.innerHTML = options;
        }
    } catch (e) {
        console.error('Failed to load cards for pull select:', e);
    }
}

async function openAddCardModal() {
    await ensureCardsListLoaded();
    const modal = document.getElementById('addCardModal');
    if (modal) modal.classList.add('active');
    if (window.soundFx) window.soundFx.playInspect();
}

async function openAddCardForAccount(accountId) {
    await ensureCardsListLoaded();
    const accSelect = document.getElementById('pullAccountSelect');
    if (accSelect) accSelect.value = accountId;

    const modal = document.getElementById('addCardModal');
    if (modal) modal.classList.add('active');
    if (window.soundFx) window.soundFx.playInspect();
}

function closeAddCardModal() {
    const modal = document.getElementById('addCardModal');
    if (modal) modal.classList.remove('active');
}

async function handleAddCardSubmit(event) {
    event.preventDefault();
    const accountId = document.getElementById('pullAccountSelect').value;
    const cardId = document.getElementById('pullCardSelect').value;
    const quantity = parseInt(document.getElementById('pullQuantity').value) || 1;
    const pack = document.getElementById('pullPack').value.trim();

    if (!accountId || !cardId) {
        showToast('Please select both an account and a card', 'danger');
        return;
    }

    const payload = {
        cardId: parseInt(cardId),
        quantity: quantity,
        condition: 'MINT',
        sellingPrice: 100.0,
        notes: pack || 'Booster Pack Pull'
    };

    try {
        const response = await fetch(`/api/v1/accounts/${accountId}/pull-card`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const result = await response.json();

        if (response.ok && result.success) {
            showToast(`Pulled card recorded in account stock!`, 'success');
            if (window.soundFx) window.soundFx.playOrderChime();
            closeAddCardModal();
            setTimeout(() => window.location.reload(), 1000);
        } else {
            showToast(result.message || 'Failed to record pulled card', 'danger');
        }
    } catch (e) {
        showToast('Error: ' + e.message, 'danger');
    }
}

// Modal 3: Account Cards Vault Inspector (Drawer with 3D Holo Cards)
async function inspectAccountCards(accountId, accountCode, inGameName) {
    const modal = document.getElementById('accountCardsModal');
    const title = document.getElementById('drawerAccountTitle');
    const sub = document.getElementById('drawerAccountSub');
    const grid = document.getElementById('accountCardsGrid');
    const summary = document.getElementById('drawerSummaryText');

    if (!modal || !grid) return;

    if (title) title.textContent = `Account ${accountCode} (${inGameName}) Inventory`;
    if (sub) sub.textContent = `Pulled cards stored in this game account, ready for trading`;
    grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 2rem; color: var(--text-muted);">Opening account card vault...</div>';
    modal.classList.add('active');
    if (window.soundFx) window.soundFx.playInspect();

    try {
        const response = await fetch(`/api/v1/accounts/${accountId}/cards`);
        const result = await response.json();

        if (response.ok && result.success) {
            const cards = result.data;
            if (summary) summary.textContent = `Total Unique Cards: ${cards.length}`;

            if (cards.length === 0) {
                grid.innerHTML = `
                    <div style="grid-column: 1/-1; text-align: center; padding: 3rem; color: var(--text-muted);">
                        <p style="font-size: 1.1rem; margin-bottom: 1rem;">No cards pulled in this account yet.</p>
                        <button class="btn btn-primary" onclick="closeAccountCardsModal(); openAddCardForAccount(${accountId});">
                            Open Pack / Pull First Card
                        </button>
                    </div>
                `;
                return;
            }

            let cardsHtml = '';
            cards.forEach(c => {
                cardsHtml += `
                    <div class="gallery-card-unit">
                        <!-- 3D Parallax Card Unit with Simeydotme Shaders -->
                        <div class="tcg-card-3d holo-card ${c.rarity === 'CROWN_RARE' ? 'crown-rare' : (c.rarity === 'STAR_3' ? 'holo-immersive' : '')}"
                             data-rarity="${c.rarity}"
                             data-name="${c.cardName}"
                             data-number="${c.cardNumber}"
                             data-expansion="${c.expansionCode}"
                             data-hp="${c.hp || 0}"
                             data-rarity-desc="${c.rarity}"
                             data-desc=""
                             data-stock="${c.quantity}"
                             data-element="${c.elementType}"
                             data-image="${c.imageUrl}"
                             onclick="handleInspectClick(this)">
                            
                            <div class="holo-card__shine"></div>
                            <div class="holo-card__glare"></div>
                            <div class="card-glare"></div>

                            <div class="card-top-bar">
                                <span class="card-num-badge">${c.expansionCode} #${c.cardNumber}</span>
                                <div class="element-badge-pill">
                                    <span>${c.elementType}</span>
                                </div>
                            </div>

                            <img src="${c.imageUrl}" alt="${c.cardName}" class="card-real-img" loading="lazy">
                        </div>

                        <div class="card-gallery-sub">
                            <div class="card-gallery-title">${c.cardName}</div>
                            <div class="card-gallery-meta">
                                <span class="stock-bullet" style="color: #10b981; font-weight: 700;">${c.quantity}x in account</span>
                                <span class="rarity-pill">${c.rarity}</span>
                            </div>
                        </div>
                    </div>
                `;
            });

            grid.innerHTML = cardsHtml;
            init3DTilt();
        } else {
            grid.innerHTML = `<div style="grid-column: 1/-1; color: #ef4444; padding: 2rem;">Error: ${result.message}</div>`;
        }
    } catch (e) {
        grid.innerHTML = `<div style="grid-column: 1/-1; color: #ef4444; padding: 2rem;">Network Error: ${e.message}</div>`;
    }
}

function closeAccountCardsModal() {
    const modal = document.getElementById('accountCardsModal');
    if (modal) modal.classList.remove('active');
}
