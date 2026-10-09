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
        btn.setAttribute('title', theme === 'dark' ? 'เปลี่ยนเป็นธีมสว่าง' : 'เปลี่ยนเป็นธีมมืด');
        btn.setAttribute('aria-label', theme === 'dark' ? 'เปลี่ยนเป็นธีมสว่าง' : 'เปลี่ยนเป็นธีมมืด');
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
    showToast(`เปลี่ยนเป็น${nextTheme === 'dark' ? 'ธีมมืด' : 'ธีมสว่าง'}แล้ว`, 'info');
}

// --- 1. 3D Holographic Parallax Tilt & Specular Light Engine ---
function init3DTilt() {
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;
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
                // Unitless copy: CSS calc() cannot turn a percentage into a gradient angle
                card.style.setProperty('--pointer-n', percentX.toFixed(1));
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
        flipperBox.style.setProperty('--pointer-n', `${percentX}`);
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
            if ((e.code === 'Space' || e.key === 'f' || e.key === 'F') && e.target === document.getElementById('inspectFlipperBox')) {
                e.preventDefault();
                toggleCardFlip();
            }
        }
    });
}

function toggleCardFlip() {
    isCardFlipped = !isCardFlipped;
    const flipperBox = document.getElementById('inspectFlipperBox');

    // The entry spin leaves an inline rotateY(0deg) behind, which outranks the .is-flipped rule
    const flipperInner = document.getElementById('inspectFlipperInner');
    if (flipperInner) {
        flipperInner.style.transform = '';
        flipperInner.style.transition = '';
    }

    if (flipperBox) {
        if (isCardFlipped) {
            flipperBox.classList.add('is-flipped');
        } else {
            flipperBox.classList.remove('is-flipped');
        }
        // Smoothly reset box tilt during flip
        flipperBox.style.transform = 'perspective(1200px) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1)';
    }


    if (window.soundFx) {
        window.soundFx.playInspect();
    }
}

let currentInspectCard = null;

function openInspection(card) {
    currentInspectCard = card;
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

    // Start on the front; the existing flip interaction stays available.
    const flipperInner = document.getElementById('inspectFlipperInner');
    if (flipperInner) {
        flipperInner.style.transition = '';
        flipperInner.style.transform = '';
    }

    const imgElem = document.getElementById('inspectCardImg');
    if (imgElem && card.imageUrl) {
        imgElem.src = card.imageUrl;
        imgElem.alt = card.name;
    }

    document.getElementById('inspectCardNumber').textContent = `${card.expansionCode || 'A1'} #${card.cardNumber}`;
    document.getElementById('inspectCardName').textContent = card.name;
    // Pokémon cards show HP; Trainer cards have none, so show their type instead (TRAINER_SUPPORTER -> Trainer Supporter)
    const kindElem = document.getElementById('inspectCardHp');
    const isPokemon = card.hp > 0;
    kindElem.textContent = isPokemon ? `HP ${card.hp}` : (CARD_TYPE_LABELS[card.cardType] || formatEnumLabel(card.cardType));
    kindElem.classList.toggle('is-trainer', !isPokemon);
    document.getElementById('inspectCardDesc').textContent = card.description || 'การ์ดสะสม Pokémon TCG Pocket';
    document.getElementById('inspectCardRarity').textContent = formatRarityLabel(card.rarityDescription || card.rarity);
    document.getElementById('inspectCardStock').textContent = card.totalStock == null ? 'มีสินค้า' : `มีสินค้า: ${card.totalStock} ใบ`;
    const priceRow = document.getElementById('inspectCardPriceRow');
    if (priceRow) {
        priceRow.style.display = card.price > 0 ? '' : 'none';
        document.getElementById('inspectCardPrice').textContent = formatBaht(card.price);
    }

    // Dynamic Element Theme for Modal Glow
    const aura = document.getElementById('inspectAura');
    if (aura) {
        aura.className = 'inspect-aura ' + getElementGlowClass(card.elementType);
    }

    const modal = document.getElementById('inspectionModal');
    modal.classList.add('active');
}

function formatBaht(amount) {
    return '฿' + (Number(amount) || 0).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

const CARD_TYPE_LABELS = { POKEMON: 'โปเกมอน', TRAINER_SUPPORTER: 'เทรนเนอร์ ซัพพอร์ต', TRAINER_ITEM: 'เทรนเนอร์ ไอเท็ม' };

function formatEnumLabel(value) {
    return (value || '').split('_').filter(Boolean)
        .map(w => w.charAt(0) + w.slice(1).toLowerCase()).join(' ');
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
        totalStock: elem.hasAttribute('data-stock') ? (parseInt(elem.getAttribute('data-stock')) || 0) : null,
        elementType: elem.getAttribute('data-element'),
        imageUrl: elem.getAttribute('data-image'),
        cardType: elem.getAttribute('data-card-type'),
        inventoryId: parseInt(elem.getAttribute('data-inventory-id')) || null,
        price: parseFloat(elem.getAttribute('data-price')) || 0
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
    const imageUrl = btn.getAttribute('data-image');
    openOrderModal(id, name, number, condition, price, stock, imageUrl);
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
    'WHOLESALE': 0.15
};
const FRIEND_ID_PATTERN = /^\d{4}-\d{4}-\d{4}-\d{4}$|^\d{16}$/;

function openOrderModal(inventoryId, cardName, cardNumber, condition, unitPrice, availableStock, imageUrl = '') {
    if (availableStock <= 0) {
        showToast('การ์ดใบนี้หมดสต็อกแล้ว', 'danger');
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

    document.getElementById('orderCardTitle').textContent = `${cardName} (${formatConditionLabel(condition)})`;
    document.getElementById('orderCardNumber').textContent = cardNumber;
    const preview = document.getElementById('orderCardPreview');
    if (preview) {
        const previewButton = document.getElementById('orderCardPreviewButton');
        previewButton.hidden = !imageUrl;
        previewButton.setAttribute('aria-label', `ขยายภาพ ${cardName} ${cardNumber}`);
        preview.alt = `${cardName} (${cardNumber})`;
        preview.onerror = () => { previewButton.hidden = true; };
        if (imageUrl) preview.src = imageUrl;
        else preview.removeAttribute('src');
    }
    document.getElementById('orderAvailableStock').textContent = `มีในร้าน: ${availableStock} ใบ`;
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

// --- Back office: edit the retail price of one inventory item ---
let currentPriceTarget = null;

function openPriceModal(btn) {
    currentPriceTarget = { id: btn.getAttribute('data-id') };
    document.getElementById('priceCardTitle').textContent = btn.getAttribute('data-name') || 'แก้ไขราคาขาย';
    document.getElementById('priceCardNumber').textContent = btn.getAttribute('data-number') || '';
    const input = document.getElementById('priceInput');
    input.value = (parseFloat(btn.getAttribute('data-price')) || 0).toFixed(2);
    document.getElementById('priceCostHint').textContent = `ต้นทุน: ${formatBaht(btn.getAttribute('data-cost'))}`;
    document.getElementById('priceModal').classList.add('active');
    input.focus();
    input.select();
}

function closePriceModal() {
    document.getElementById('priceModal').classList.remove('active');
}

async function submitPriceUpdate(event) {
    event.preventDefault();
    if (!currentPriceTarget) return;
    const price = parseFloat(document.getElementById('priceInput').value);
    if (isNaN(price) || price < 0) {
        showToast('กรุณากรอกราคาตั้งแต่ 0 ขึ้นไป', 'danger');
        return;
    }

    const submitBtn = document.getElementById('priceSubmitBtn');
    submitBtn.disabled = true;
    try {
        const response = await fetch(`/api/v1/admin/inventories/${currentPriceTarget.id}/price?price=${encodeURIComponent(price.toFixed(2))}`, { method: 'PATCH' });
        const result = await response.json();
        if (response.ok && result.success) {
            showToast(`อัปเดตราคาขายเป็น ${formatBaht(result.data.sellingPrice)} แล้ว`, 'success');
            closePriceModal();
            setTimeout(() => window.location.reload(), 700);
        } else {
            showToast(apiErrorMessage(result, 'อัปเดตราคาไม่สำเร็จ'), 'danger');
        }
    } catch (err) {
        showToast('เชื่อมต่อเซิร์ฟเวอร์ไม่ได้: ' + err.message, 'danger');
    } finally {
        submitBtn.disabled = false;
    }
}

// --- Back office: set a customer's membership tier (drives the Strategy discount) ---
async function updateCustomerTier(select) {
    const userId = select.getAttribute('data-user-id');
    const previous = select.getAttribute('data-current');
    const tier = select.value;
    select.disabled = true;
    try {
        const response = await fetch(`/api/v1/admin/customers/${userId}/membership-tier?tier=${encodeURIComponent(tier)}`, { method: 'PATCH' });
        const result = await response.json();
        if (response.ok && result.success) {
            select.setAttribute('data-current', tier);
            showToast(`${select.getAttribute('data-name')} เป็นสมาชิกระดับ ${tier} แล้ว`, 'success');
        } else {
            select.value = previous;
            showToast(apiErrorMessage(result, 'อัปเดตระดับสมาชิกไม่สำเร็จ'), 'danger');
        }
    } catch (err) {
        select.value = previous;
        showToast('เชื่อมต่อเซิร์ฟเวอร์ไม่ได้: ' + err.message, 'danger');
    } finally {
        select.disabled = false;
    }
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

    document.getElementById('orderCalcSubtotal').textContent = formatBaht(subtotal);
    document.getElementById('orderCalcDiscountRate').textContent = `ส่วนลด ${tier} (-${(discountRate * 100).toFixed(0)}%)`;
    document.getElementById('orderCalcDiscountAmt').textContent = '-' + formatBaht(discountAmount);
    document.getElementById('orderCalcFinal').textContent = formatBaht(finalAmount);
}

async function submitOrder() {
    if (!currentOrderTarget) return;

    const userSelect = document.getElementById('orderUserSelect');
    const userId = parseInt(userSelect.value);
    const notes = document.getElementById('orderNotesInput') ? document.getElementById('orderNotesInput').value : '';

    const friendIdInput = document.getElementById('orderFriendIdInput');
    const ignInput = document.getElementById('orderInGameNameInput');
    const customerFriendId = friendIdInput ? friendIdInput.value.trim() : '';
    const customerInGameName = ignInput ? ignInput.value.trim() : '';

    if (!FRIEND_ID_PATTERN.test(customerFriendId)) {
        showToast('กรุณากรอก Friend ID ของลูกค้าให้ครบ 16 หลัก (เช่น 1234-5678-9012-3456)', 'danger');
        if (friendIdInput) friendIdInput.focus();
        return;
    }

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
    const submitLabel = submitBtn.querySelector('span') || submitBtn;
    const submitLabelText = submitLabel.textContent;
    submitBtn.disabled = true;
    submitLabel.textContent = "กำลังบันทึก...";

    try {
        const response = await fetch('/api/v1/orders', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const result = await response.json();

        if (response.ok && result.success) {
            if (window.soundFx) window.soundFx.playOrderChime();
            showToast(`สร้างคำสั่งซื้อ ${result.data.orderCode} แล้ว! ส่วนลด: -฿${result.data.discountAmount}`, 'success');
            closeOrderModal();
            
            // Trigger Chat Commerce Handshake Modal
            showChatCommerceModal(result.data, currentOrderTarget, customerFriendId, customerInGameName);
        } else {
            showToast(result.message || 'สั่งซื้อไม่สำเร็จ กรุณาตรวจสอบสต็อก', 'danger');
        }
    } catch (err) {
        showToast('เชื่อมต่อเซิร์ฟเวอร์ไม่ได้: ' + err.message, 'danger');
    } finally {
        submitBtn.disabled = false;
        submitLabel.textContent = submitLabelText;
    }
}

// --- Chat Commerce Handshake Helpers ---
// Primary: the store's Facebook Page (username or Page ID) -> m.me deep link with the summary prefilled.
// Leave empty until the Page exists; the handshake then falls back to the personal chat link below.
const MESSENGER_PAGE = '';
const MESSENGER_FALLBACK_LINK = 'https://www.facebook.com/messages/t/sapphanyu.khamtum';

function buildMessengerUrl(summaryMsg) {
    if (MESSENGER_PAGE) {
        return `https://m.me/${MESSENGER_PAGE}?text=${encodeURIComponent(summaryMsg)}`;
    }
    // The personal chat link ignores ?text=, the clipboard copy covers it
    return MESSENGER_FALLBACK_LINK;
}
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

    const cardTitle = targetCard ? `${targetCard.cardName} (${targetCard.condition}) x${targetCard.quantity}` : 'Pokémon TCG Card';
    const finalAmountStr = formatBaht(orderData.finalAmount);

    if (codeElem) codeElem.textContent = `#${orderData.orderCode}`;
    if (cardElem) cardElem.textContent = cardTitle;
    if (friendElem) friendElem.textContent = friendId;
    if (finalElem) finalElem.textContent = finalAmountStr;

    // Compose the order summary staff send to the customer
    const summaryMsg = `สวัสดีครับ สั่งจองการ์ดผ่านเว็บเรียบร้อยแล้วครับ!\n• รหัสคำสั่งซื้อ: #${orderData.orderCode}\n• รายการการ์ด: ${cardTitle}\n• ยอดชำระ: ${finalAmountStr} (ส่วนลดสมาชิก: -฿${orderData.discountAmount.toFixed(2)})\n• รหัสเพื่อนในเกม (Friend ID): ${friendId}\n• ชื่อเทรนเนอร์ (IGN): ${ign || '-'}\nขอส่งหลักฐานการโอนเงินและนัดส่งการ์ดเทรดในเกมครับ`;
    lastChatSummaryText = summaryMsg;

    modal.classList.add('active');

    // Handshake: copy the order summary and stay on this page (staff paste it into the chat themselves)
    const hint = document.getElementById('chatHandshakeHint');
    if (hint) hint.textContent = 'คัดลอกข้อความสรุปออเดอร์แล้ว — วางส่งให้ลูกค้าในแชทได้เลย';
    copyToClipboard(summaryMsg, 'คัดลอกข้อความสรุปออเดอร์แล้ว! วางส่งในแชทได้ทันที');
}

// Opens the Messenger link in a new tab (used by the customer "Inbox FB" button).
// Returns false when the browser blocks the popup so the caller can fall back.
function openMessengerDeepLink(url) {
    const hint = document.getElementById('chatHandshakeHint');
    let opened = null;
    try {
        // No 'noopener' feature here: it makes window.open return null even on success
        opened = window.open(url, '_blank');
        if (opened) opened.opener = null;
    } catch (e) {
        opened = null;
    }
    const launched = !!opened;
    if (hint) {
        hint.textContent = launched
            ? 'เปิด Messenger ให้แล้ว — วางข้อความ (Ctrl+V) แล้วกดส่งได้เลย'
            : 'คัดลอกข้อความแล้ว — กดปุ่มด้านล่างเพื่อเปิด Messenger แล้ววาง (Ctrl+V)';
    }
    return launched;
}

function closeChatCommerceModal() {
    const modal = document.getElementById('chatCommerceModal');
    if (modal) modal.classList.remove('active');
    setTimeout(() => window.location.reload(), 300);
}

function copyChatOrderSummary(btn) {
    if (!lastChatSummaryText) return;
    copyToClipboard(lastChatSummaryText, 'คัดลอกข้อความสรุปออเดอร์แล้ว! สามารถนำไปวางส่งในแชทได้ทันที');
    const span = btn ? btn.querySelector('span') : null;
    if (span) {
        const orig = span.textContent;
        span.textContent = '✓ คัดลอกสำเร็จแล้ว!';
        setTimeout(() => span.textContent = orig, 2000);
    }
}

// --- Card gallery: collapsible filter panel behind the Filter button ---
const FILTER_PANEL_KEY = 'tcg-filter-panel-open';

function setFilterPanelOpen(open) {
    const panel = document.getElementById('filterPanel');
    const btn = document.getElementById('filterToggleBtn');
    if (!panel || !btn) return;
    panel.classList.toggle('open', open);
    btn.setAttribute('aria-expanded', open);
}

function toggleFilterPanel() {
    const panel = document.getElementById('filterPanel');
    if (!panel) return;
    const open = !panel.classList.contains('open');
    setFilterPanelOpen(open);
    // Pills reload the page, so remember the choice for the rest of the visit
    try { sessionStorage.setItem(FILTER_PANEL_KEY, open ? '1' : '0'); } catch (e) { /* storage unavailable */ }
    if (window.soundFx) window.soundFx.playClick();
}

document.addEventListener('DOMContentLoaded', () => {
    let saved = null;
    try { saved = sessionStorage.getItem(FILTER_PANEL_KEY); } catch (e) { /* storage unavailable */ }
    if (saved !== null) setFilterPanelOpen(saved === '1');
});

// --- Customer self-service: Inbox FB + Create Order from the Inspect modal ---
function inspectCardLabel() {
    const card = currentInspectCard;
    return card ? `${card.name} (${card.expansionCode || 'A1'} #${card.cardNumber})` : 'Pokémon TCG Card';
}

// Copies an enquiry about the inspected card, then opens the store's Messenger chat
function inboxStoreAboutCard() {
    const message = `สวัสดีครับ สนใจการ์ด ${inspectCardLabel()} ครับ ยังมีของอยู่ไหมครับ`;
    const url = buildMessengerUrl(message);
    copyToClipboard(message, 'คัดลอกข้อความแล้ว! วางส่งในแชทได้ทันที').then(() => {
        if (!openMessengerDeepLink(url)) window.location.href = url;
    });
}

function openCustomerOrderModal() {
    const modal = document.getElementById('customerOrderModal');
    const card = currentInspectCard;
    if (!modal || !card) return;
    if (!card.inventoryId) {
        showToast('การ์ดใบนี้ยังสั่งซื้อออนไลน์ไม่ได้ กรุณา Inbox หาร้าน', 'danger');
        return;
    }

    showCustomerBookingError('');
    document.getElementById('customerOrderCardName').textContent = card.name;
    document.getElementById('customerOrderCardNumber').textContent = `${card.expansionCode || 'A1'} #${card.cardNumber}`;
    document.getElementById('customerOrderPrice').textContent = formatBaht(card.price);
    modal.inert = false;
    modal.setAttribute('aria-hidden', 'false');
    if (typeof updateCustomerFriendFeedback === 'function') updateCustomerFriendFeedback(false);
    document.getElementById('customerOrderForm').style.display = '';
    document.getElementById('customerOrderDone').style.display = 'none';

    closeInspection();
    modal.classList.add('active');
    document.getElementById('customerOrderFriendId').focus();
}

function closeCustomerOrderModal() {
    const modal = document.getElementById('customerOrderModal');
    if (!modal || document.getElementById('customerOrderSubmit')?.disabled) return;
    modal.classList.remove('active');
    // After a successful order the stock changed, so refresh the gallery
    if (document.getElementById('customerOrderDone').style.display !== 'none') {
        setTimeout(() => window.location.reload(), 300);
    }
}

async function submitCustomerOrder(event) {
    event.preventDefault();
    if (document.getElementById('customerOrderSubmit').disabled) return;
    const card = currentInspectCard;
    const userId = parseInt(document.getElementById('customerOrderUserId').value);
    const friendInput = document.getElementById('customerOrderFriendId');
    const friendId = friendInput.value.trim();
    const ign = document.getElementById('customerOrderIgn').value.trim();

    if (!card || !card.inventoryId || !userId) return;
    if (!FRIEND_ID_PATTERN.test(friendId)) {
        if (typeof updateCustomerFriendFeedback === 'function') updateCustomerFriendFeedback(true);
        friendInput.focus();
        return;
    }

    setCustomerBookingBusy(true);
    showCustomerBookingError('');

    try {
        const response = await fetch('/api/v1/orders', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                userId: userId,
                customerFriendId: friendId,
                customerInGameName: ign,
                items: [{ inventoryId: card.inventoryId, quantity: 1 }],
                notes: 'Online Vault Order'
            })
        });
        const result = await response.json();

        if (response.ok && result.success) {
            if (window.soundFx) window.soundFx.playOrderChime();
            showCustomerOrderDone(result.data, friendId, ign);
        } else {
            showCustomerBookingError(apiErrorMessage(result, 'จองไม่สำเร็จ การ์ดอาจหมดสต็อกแล้ว กรุณาตรวจสอบและลองอีกครั้ง'));
        }
    } catch (err) {
        showCustomerBookingError('การเชื่อมต่อขัดข้อง ข้อมูลที่กรอกยังอยู่ กรุณาตรวจสอบคำสั่งซื้อของฉันก่อนยืนยันซ้ำ');
    } finally {
        setCustomerBookingBusy(false);
    }
}

function showCustomerOrderDone(order, friendId, ign) {
    const finalAmount = formatBaht(order.finalAmount);
    const summary = `สวัสดีครับ สั่งจองการ์ดผ่านเว็บเรียบร้อยแล้วครับ!\n• รหัสคำสั่งซื้อ: #${order.orderCode}\n• รายการการ์ด: ${inspectCardLabel()} x1\n• ยอดชำระ: ${finalAmount} (ส่วนลดสมาชิก: -฿${order.discountAmount.toFixed(2)})\n• รหัสเพื่อนในเกม (Friend ID): ${friendId}\n• ชื่อเทรนเนอร์ (IGN): ${ign || '-'}\nขอส่งหลักฐานการโอนเงินและนัดส่งการ์ดเทรดในเกมครับ`;
    lastChatSummaryText = summary;
    const url = buildMessengerUrl(summary);

    document.getElementById('customerOrderDoneCode').textContent = `#${order.orderCode}`;
    document.getElementById('customerOrderDoneAmount').textContent = finalAmount;
    document.getElementById('customerOrderMessengerBtn').href = url;
    document.getElementById('customerOrderForm').style.display = 'none';
    document.getElementById('customerOrderDone').style.display = '';

    // No automatic jump to Messenger here: the customer stays on the confirmation and can inbox the store if they want
    document.getElementById('customerOrderDoneHint').textContent =
        'ขั้นตอนถัดไป: ติดต่อร้านเพื่อชำระเงินและนัดเทรดการ์ด ปุ่มติดต่อร้านจะคัดลอกสรุปออเดอร์ให้ หรือดูสถานะได้ที่คำสั่งซื้อของฉัน';
    showToast(`สร้างคำสั่งซื้อ ${order.orderCode} แล้ว!`, 'success');
}

// --- 5. Order State Machine Transition Action ---
const ORDER_TRANSITIONS = {
    pay: {
        title: 'ยืนยันการชำระเงิน', from: ['PENDING', 'active-pending'], to: ['PAID', 'active-paid'],
        message: 'บันทึกว่าคำสั่งซื้อนี้ชำระเงินแล้ว? ควรทำหลังตรวจสอบยอดโอนของลูกค้าแล้ว',
        confirm: 'ชำระเงินแล้ว', background: ''
    },
    ship: {
        title: 'เริ่มเทรดในเกม', from: ['PAID', 'active-paid'], to: ['SHIPPING', 'active-shipping'],
        message: 'เริ่มส่งการ์ดให้ลูกค้าผ่านการเทรดในเกม?',
        confirm: 'เริ่มเทรด', background: 'linear-gradient(135deg, #7c3aed, #8b5cf6)'
    },
    complete: {
        title: 'ยืนยันเทรดสำเร็จ', from: ['SHIPPING', 'active-shipping'], to: ['COMPLETED', 'active-completed'],
        message: 'ยืนยันว่าลูกค้าได้รับการ์ดครบแล้ว? คำสั่งซื้อที่เสร็จสิ้นแล้วจะแก้ไขไม่ได้',
        confirm: 'เทรดสำเร็จ', background: 'linear-gradient(135deg, #059669, #10b981)'
    },
    cancel: {
        title: 'ยกเลิกคำสั่งซื้อ', from: null, to: ['CANCELLED', 'active-cancelled'],
        message: 'ยกเลิกคำสั่งซื้อนี้? การ์ดที่จองไว้จะกลับเข้าสต็อก และไม่สามารถย้อนกลับได้',
        confirm: 'ยกเลิกคำสั่งซื้อ', background: 'linear-gradient(135deg, #dc2626, #ef4444)', dismiss: 'ไม่ยกเลิก'
    }
};
let pendingOrderTransition = null;

// Asks for confirmation in a modal, then runs the transition
function executeOrderTransition(orderId, action, orderCode) {
    const modal = document.getElementById('transitionModal');
    const config = ORDER_TRANSITIONS[action];
    if (!modal || !config) {
        if (confirm(`ยืนยันเปลี่ยนสถานะคำสั่งซื้อ #${orderId} (${action})?`)) {
            runOrderTransition(orderId, action);
        }
        return;
    }

    pendingOrderTransition = { orderId, action };
    const error = document.getElementById('transitionError');
    error.hidden = true;
    error.textContent = '';
    document.getElementById('transitionTitle').textContent = config.title;
    document.getElementById('transitionOrderCode').textContent = orderCode || `คำสั่งซื้อ #${orderId}`;
    // The "from" badge mirrors the order's current state in its table row
    const actionBtn = document.querySelector(`.order-actions button[data-order-id="${orderId}"]`);
    const currentNode = actionBtn ? actionBtn.closest('tr').querySelector('.state-node[class*="active-"]') : null;
    const fromNode = document.getElementById('transitionFrom');
    fromNode.textContent = currentNode ? currentNode.textContent.trim() : (config.from ? config.from[0] : 'CURRENT');
    fromNode.className = currentNode ? currentNode.className : 'state-node ' + (config.from ? config.from[1] : '');
    const toNode = document.getElementById('transitionTo');
    toNode.textContent = formatOrderStatus(config.to[0]);
    toNode.className = 'state-node ' + config.to[1];
    document.getElementById('transitionMessage').textContent = config.message;
    document.getElementById('transitionDismissBtn').textContent = config.dismiss || 'ย้อนกลับ';
    const confirmBtn = document.getElementById('transitionConfirmBtn');
    confirmBtn.textContent = config.confirm;
    confirmBtn.style.background = config.background;
    confirmBtn.disabled = false;
    modal.classList.add('active');
}

function closeTransitionModal() {
    if (document.getElementById('transitionConfirmBtn').disabled) return;
    document.getElementById('transitionModal').classList.remove('active');
    pendingOrderTransition = null;
}

async function confirmOrderTransition() {
    if (!pendingOrderTransition) return;
    const { orderId, action } = pendingOrderTransition;
    const button = document.getElementById('transitionConfirmBtn');
    if (button.disabled) return;
    const label = button.textContent;
    button.disabled = true;
    button.textContent = 'กำลังบันทึก...';
    const modal = document.getElementById('transitionModal');
    modal.setAttribute('aria-busy', 'true');
    const dismissButtons = [...modal.querySelectorAll('button')].filter(item => item !== button);
    dismissButtons.forEach(item => item.disabled = true);
    const success = await runOrderTransition(orderId, action);
    button.disabled = false;
    button.textContent = label;
    dismissButtons.forEach(item => item.disabled = false);
    modal.setAttribute('aria-busy', 'false');
    if (success) closeTransitionModal();
}

async function runOrderTransition(orderId, action) {
    try {
        const response = await fetch(`/api/v1/orders/${orderId}/status?action=${action}`, {
            method: 'PATCH'
        });
        const result = await response.json();

        if (response.ok && result.success) {
            if (window.soundFx) window.soundFx.playOrderChime();
            showToast(`เปลี่ยนสถานะคำสั่งซื้อเป็น ${result.data.orderStatus} แล้ว`, 'success');
            setTimeout(() => window.location.reload(), 1000);
            return true;
        } else if (response.status === 409) {
            // The order moved on since this page was rendered (or the action is not allowed in its state):
            // explain in Thai, then reload so the table shows the real status
            showTransitionError(orderConflictMessage(result.message) + ' กำลังโหลดสถานะล่าสุด...');
            setTimeout(() => window.location.reload(), 2500);
        } else {
            showTransitionError(result.message || 'เปลี่ยนสถานะไม่ได้ กรุณาตรวจสอบสถานะปัจจุบันแล้วลองอีกครั้ง');
        }
    } catch (e) {
        showTransitionError('เชื่อมต่อเซิร์ฟเวอร์ไม่ได้ กรุณาลองอีกครั้ง');
    }
    return false;
}

// Thai wording for the backend's 409 messages on PATCH /orders/{id}/status
function orderConflictMessage(message) {
    const text = message || '';
    const state = (text.match(/current state: (\w+)/) || [])[1];
    if (state === 'COMPLETED') return 'คำสั่งซื้อนี้เสร็จสิ้นไปแล้ว';
    if (state === 'CANCELLED') return 'คำสั่งซื้อนี้ถูกยกเลิกไปแล้ว';
    if (text.includes('not all items')) return 'ยังเทรดไม่ครบทุกรายการ จึงปิดคำสั่งซื้อไม่ได้ เปิด "จัดการเทรด" เพื่อเทรดให้ครบก่อน';
    if (text.includes('being shipped')) return 'เริ่มส่งการ์ดในเกมแล้ว จึงยกเลิกคำสั่งซื้อไม่ได้';
    return 'เปลี่ยนสถานะไม่ได้ เพราะสถานะของคำสั่งซื้อเปลี่ยนไปแล้ว';
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
// Returns a promise that settles once the copy attempt is over (never rejects)
function copyToClipboard(text, label = 'คัดลอกแล้ว!') {
    if (!text) return Promise.resolve();
    if (navigator.clipboard) {
        return navigator.clipboard.writeText(text).then(() => {
            showToast(label, 'success');
            if (window.soundFx) window.soundFx.playClick();
        }).catch(() => {
            legacyCopy(text, label);
        });
    }
    legacyCopy(text, label);
    return Promise.resolve();
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
        showToast('คัดลอกไม่สำเร็จ', 'danger');
    }
    document.body.removeChild(textarea);
}

// Friend ID inputs: digits only, capped at 16, with a dash after every 4th digit
function formatFriendId(value) {
    return (value || '').replace(/\D/g, '').slice(0, 16).replace(/(\d{4})(?=\d)/g, '$1-');
}

document.addEventListener('input', (event) => {
    const input = event.target;
    if (!input.classList || !input.classList.contains('friend-id-input')) return;

    // keep the caret after the same digit it followed before reformatting
    const digitsBeforeCaret = input.value.slice(0, input.selectionStart).replace(/\D/g, '').length;
    const formatted = formatFriendId(input.value);
    input.value = formatted;

    let caret = 0;
    for (let seen = 0; seen < digitsBeforeCaret && caret < formatted.length; caret++) {
        if (/\d/.test(formatted[caret])) seen++;
    }
    input.setSelectionRange(caret, caret);
});

function copyFriendCode(code, btn) {
    // The game's friend search takes the 16 digits without dashes
    const digits = (code || '').replace(/\D/g, '');
    copyToClipboard(digits, `Friend Code ${digits} copied!`);
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
let currentTradeOrderCode = null;
let currentTradeCustomerFriendId = null;
// Set once a trade status changes: the orders table behind the modal is server-rendered, and the
// backend completes the order by itself when the last item is traded, so the row would go stale.
let tradeStatusChanged = false;

async function openTradeModal(orderId, orderCode, customerFriendId) {
    currentTradeOrderId = orderId;
    currentTradeOrderCode = orderCode || orderId;
    currentTradeCustomerFriendId = customerFriendId || '1111-2222-3333-4444';

    const modal = document.getElementById('tradeFulfillmentModal');
    const title = document.getElementById('tradeModalOrderTitle');
    const sub = document.getElementById('tradeModalCustomerSub');
    const fcElem = document.getElementById('tradeCustomerFriendCode');
    const tbody = document.getElementById('tradeItemsTableBody');
    const banner = document.getElementById('tradeRecommendationBanner');

    if (!modal || !tbody) return;

    if (title) title.textContent = `จัดการเทรดในเกม - คำสั่งซื้อ #${orderCode || orderId}`;
    if (sub) sub.textContent = `Friend ID ของลูกค้า: ${currentTradeCustomerFriendId}`;
    if (fcElem) fcElem.textContent = currentTradeCustomerFriendId;
    if (banner) banner.style.display = 'none';

    tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; padding: 2rem; color: var(--text-muted);">กำลังคำนวณบัญชีที่ใช้เทรด...</td></tr>`;
    modal.classList.add('active');

    try {
        const response = await fetch(`/api/v1/trades/orders/${orderId}/recommendations`);
        const result = await response.json();

        if (response.ok && result.success) {
            renderTradeModalContent(result.data || []);
        } else {
            tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: #ef4444; padding: 2rem;">เกิดข้อผิดพลาด: ${apiErrorMessage(result, 'โหลดคำแนะนำการเทรดไม่สำเร็จ')}</td></tr>`;
        }
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: #ef4444; padding: 2rem;">เชื่อมต่อเซิร์ฟเวอร์ไม่ได้: ${e.message}</td></tr>`;
    }
}

function closeTradeModal() {
    const modal = document.getElementById('tradeFulfillmentModal');
    if (modal) modal.classList.remove('active');
    currentTradeOrderId = null;
    if (tradeStatusChanged) {
        window.location.reload();
    }
}

// Accounts holding the card: the backend's recommended account first, then its alternatives
function tradeCandidates(item) {
    const candidates = [];
    if (item.recommendedAccountId) {
        candidates.push({
            accountId: item.recommendedAccountId,
            accountCode: item.recommendedAccountCode,
            inGameName: item.recommendedInGameName,
            friendId: item.recommendedFriendId,
            tradeStatus: item.accountStatus,
            availableStock: item.availableStock
        });
    }
    return candidates.concat(item.alternativeCandidates || []);
}

// A READY account holding enough stock of every card in the order, if one exists
function findSingleTradeAccount(items) {
    if (!items.length) return null;
    return tradeCandidates(items[0]).find(acc => items.every(item =>
        tradeCandidates(item).some(c => c.accountId === acc.accountId
            && c.tradeStatus === 'READY'
            && c.availableStock >= item.requestedQuantity))) || null;
}

function renderTradeModalContent(items) {
    const banner = document.getElementById('tradeRecommendationBanner');
    const tbody = document.getElementById('tradeItemsTableBody');

    // Display recommendation insight
    if (banner) {
        const singleAccount = findSingleTradeAccount(items);
        const unmatched = items.find(item => !item.matchFound);

        if (singleAccount && items.some(item => item.currentAssignedAccountId !== singleAccount.accountId)) {
            banner.style.display = 'block';
            banner.style.background = 'rgba(16, 185, 129, 0.12)';
            banner.style.border = '1px solid rgba(16, 185, 129, 0.35)';
            banner.innerHTML = `
                <div style="color: #10b981; font-weight: 800; font-size: 0.95rem; margin-bottom: 0.2rem;">
                    ✨ เทรดจบได้ในบัญชีเดียว!
                </div>
                <div style="font-size: 0.85rem; color: var(--text-secondary);">
                    บัญชี <strong>${singleAccount.accountCode} (${singleAccount.inGameName})</strong> มีการ์ดครบทุกใบ เลือกบัญชีนี้ให้ทุกการ์ดเพื่อปิดออเดอร์ด้วยการเพิ่มเพื่อนครั้งเดียว
                </div>
            `;
        } else if (unmatched) {
            banner.style.display = 'block';
            banner.style.background = 'rgba(59, 130, 246, 0.1)';
            banner.style.border = '1px solid rgba(59, 130, 246, 0.3)';
            banner.innerHTML = `
                <div style="color: var(--accent-cyan); font-weight: 700; font-size: 0.88rem;">
                    💡 ${unmatched.cardName || 'รายการ #' + unmatched.orderItemId}: ${unmatched.recommendationReason}
                </div>
            `;
        } else {
            banner.style.display = 'none';
        }
    }

    if (items.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: var(--text-muted); padding: 2rem;">ไม่พบรายการในคำสั่งซื้อนี้</td></tr>`;
        return;
    }

    let rowsHtml = '';
    items.forEach(item => {
        const isAssigned = !!item.currentAssignedAccountId;
        const status = item.fulfillmentStatus || 'UNASSIGNED';
        const candidates = tradeCandidates(item);
        const assignedAccount = candidates.find(c => c.accountId === item.currentAssignedAccountId);
        const assignedFriendCode = assignedAccount ? assignedAccount.friendId : null;
        // Backend rejects reassignment once the trade has been sent
        const locked = status === 'TRADE_SENT' || status === 'COMPLETED';

        let statusBadge = `<span style="font-size: 0.75rem; padding: 2px 8px; border-radius: 4px; background: rgba(239, 68, 68, 0.15); color: #ef4444; font-weight: 700;">ยังไม่เลือกบัญชี</span>`;
        if (status === 'FRIEND_PENDING') {
            statusBadge = `<span style="font-size: 0.75rem; padding: 2px 8px; border-radius: 4px; background: rgba(245, 158, 11, 0.15); color: #f59e0b; font-weight: 700;">🤝 รอเพิ่มเพื่อน</span>`;
        } else if (status === 'TRADE_SENT') {
            statusBadge = `<span style="font-size: 0.75rem; padding: 2px 8px; border-radius: 4px; background: rgba(59, 130, 246, 0.15); color: #3b82f6; font-weight: 700;">📤 ส่งเทรดแล้ว</span>`;
        } else if (status === 'COMPLETED') {
            statusBadge = `<span style="font-size: 0.75rem; padding: 2px 8px; border-radius: 4px; background: rgba(16, 185, 129, 0.15); color: #10b981; font-weight: 700;">✓ สำเร็จ</span>`;
        }

        // Account options dropdown
        let selectHtml = `<select onchange="handleAccountSelectChange(${item.orderItemId}, this.value)" ${locked ? 'disabled' : ''} class="gallery-search-input" style="font-size: 0.8rem; padding: 0.3rem 0.5rem; border-radius: 6px; background: var(--bg-surface-elevated); color: var(--text-primary); max-width: 220px;">`;
        selectHtml += `<option value="" ${!isAssigned ? 'selected' : ''}>-- เลือกบัญชีเกม --</option>`;

        candidates.forEach(cand => {
            const sel = (item.currentAssignedAccountId === cand.accountId) ? 'selected' : '';
            // Only READY accounts can be assigned
            const ready = cand.tradeStatus === 'READY';
            const stockLabel = `มี ${cand.availableStock} ใบ${ready ? '' : ', ' + cand.tradeStatus}`;
            selectHtml += `<option value="${cand.accountId}" ${sel} ${ready ? '' : 'disabled'}>${cand.accountCode} - ${cand.inGameName} (${stockLabel})</option>`;
        });
        selectHtml += `</select>`;

        // Action buttons based on status (Sequence: UNASSIGNED -> FRIEND_PENDING -> TRADE_SENT -> COMPLETED)
        let actionButtons = '';
        if (isAssigned) {
            if (status === 'FRIEND_PENDING') {
                actionButtons += `
                    <button class="btn btn-secondary" style="padding: 0.25rem 0.6rem; font-size: 0.75rem;" onclick="advanceItemTradeStatus(${item.orderItemId}, 'TRADE_SENT')">
                        ส่งเทรดแล้ว
                    </button>
                `;
            } else if (status === 'TRADE_SENT') {
                actionButtons += `
                    <button class="btn btn-primary" style="padding: 0.25rem 0.6rem; font-size: 0.75rem;" onclick="advanceItemTradeStatus(${item.orderItemId}, 'COMPLETED')">
                        เทรดในเกมสำเร็จ
                    </button>
                `;
            } else if (status === 'COMPLETED') {
                actionButtons += `<span style="color: #10b981; font-size: 0.8rem; font-weight: 700;">เทรดในเกมแล้ว</span>`;
            }
        } else {
            actionButtons += `<span style="color: var(--text-muted); font-size: 0.75rem;">(รอจับคู่ไอดี)</span>`;
        }

        rowsHtml += `
            <tr>
                <td>
                    <div style="display: flex; align-items: center; gap: 0.6rem;">
                        <div>
                            <div style="font-weight: 700; font-size: 0.88rem;">${item.cardName}</div>
                            <div style="font-family: 'JetBrains Mono', monospace; font-size: 0.75rem; color: var(--accent-cyan);">${item.cardNumber}</div>
                        </div>
                    </div>
                </td>
                <td style="font-weight: 700;">${item.requestedQuantity}x</td>
                <td>${selectHtml}</td>
                <td>
                    ${assignedFriendCode ? `
                        <div style="display: inline-flex; align-items: center; gap: 0.4rem; background: var(--bg-surface-elevated); padding: 0.2rem 0.5rem; border-radius: 4px; border: 1px solid var(--border-color);">
                            <span style="font-family: 'JetBrains Mono', monospace; font-size: 0.78rem; color: var(--accent-gold);">${assignedFriendCode}</span>
                            <button type="button" class="icon-btn" onclick="copyFriendCode('${assignedFriendCode}', this)" title="คัดลอก Friend Code" style="background: none; border: none; cursor: pointer; color: var(--text-muted); display: inline-flex; align-items: center; padding: 0;">
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
        const response = await fetch(`/api/v1/trades/orders/${currentTradeOrderId}/auto-match`, {
            method: 'POST'
        });
        const result = await response.json();

        if (response.ok && result.success) {
            showToast('จับคู่บัญชีเกมให้การ์ดในออเดอร์แล้ว!', 'success');
            if (window.soundFx) window.soundFx.playOrderChime();
            openTradeModal(currentTradeOrderId, currentTradeOrderCode, currentTradeCustomerFriendId);
        } else {
            showToast(apiErrorMessage(result, 'จับคู่อัตโนมัติไม่สำเร็จ การ์ดบางใบอาจไม่มีในบัญชีเกม'), 'danger');
        }
    } catch (e) {
        showToast('เชื่อมต่อเซิร์ฟเวอร์ไม่ได้: ' + e.message, 'danger');
    }
}

async function handleAccountSelectChange(orderItemId, accountId) {
    if (!accountId) return;
    try {
        const response = await fetch(`/api/v1/trades/items/${orderItemId}/assign?accountId=${encodeURIComponent(accountId)}`, {
            method: 'POST'
        });
        const result = await response.json();

        if (response.ok && result.success) {
            showToast('เลือกบัญชีเกมสำหรับเทรดการ์ดใบนี้แล้ว!', 'success');
            if (window.soundFx) window.soundFx.playClick();
            openTradeModal(currentTradeOrderId, currentTradeOrderCode, currentTradeCustomerFriendId);
        } else {
            showToast(apiErrorMessage(result, 'เลือกบัญชีไม่สำเร็จ'), 'danger');
        }
    } catch (e) {
        showToast('เกิดข้อผิดพลาด: ' + e.message, 'danger');
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
            tradeStatusChanged = true;
            showToast(`อัปเดตสถานะเทรดเป็น ${newStatus} แล้ว`, 'success');
            if (window.soundFx) window.soundFx.playClick();
            openTradeModal(currentTradeOrderId, currentTradeOrderCode, currentTradeCustomerFriendId);
        } else {
            showToast(result.message || 'อัปเดตสถานะไม่สำเร็จ', 'danger');
        }
    } catch (e) {
        showToast('เกิดข้อผิดพลาด: ' + e.message, 'danger');
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
    const friendId = document.getElementById('accFriendCode').value.trim();
    const buyInCost = parseFloat(document.getElementById('accBuyInCost').value) || 0;
    const notes = document.getElementById('accNotes').value.trim();

    if (!FRIEND_ID_PATTERN.test(friendId)) {
        showToast('กรุณากรอก Friend Code ให้ครบ 16 หลัก (เช่น 1234-5678-9012-3456)', 'danger');
        return;
    }

    const payload = {
        accountCode: code,
        inGameName: ign,
        friendId: friendId,
        buyInCost: buyInCost,
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
            showToast(`เพิ่มบัญชี ${result.data.accountCode} แล้ว!`, 'success');
            if (window.soundFx) window.soundFx.playOrderChime();
            closeCreateAccountModal();
            setTimeout(() => window.location.reload(), 1000);
        } else {
            showToast(apiErrorMessage(result, 'เพิ่มบัญชีไม่สำเร็จ'), 'danger');
        }
    } catch (e) {
        showToast('เชื่อมต่อเซิร์ฟเวอร์ไม่ได้: ' + e.message, 'danger');
    }
}

// Validation failures come back as { message, details: { field: reason } }; show the field reasons
function apiErrorMessage(result, fallback) {
    const details = result && result.details ? Object.values(result.details) : [];
    return details.length ? details.join(' ') : ((result && result.message) || fallback);
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
            let options = '<option value="" disabled selected>เลือกการ์ด</option>';
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
    const sellingPrice = parseFloat(document.getElementById('pullSellingPrice').value);

    if (!accountId || !cardId) {
        showToast('กรุณาเลือกทั้งบัญชีและการ์ด', 'danger');
        return;
    }
    if (isNaN(sellingPrice) || sellingPrice < 0) {
        showToast('กรุณากรอกราคาขาย', 'danger');
        return;
    }

    const payload = {
        cardId: parseInt(cardId),
        quantity: quantity,
        condition: 'MINT',
        sellingPrice: sellingPrice
    };

    try {
        const response = await fetch(`/api/v1/accounts/${accountId}/pulls`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const result = await response.json();

        if (response.ok && result.success) {
            showToast(`บันทึกการ์ดเข้าบัญชีแล้ว!`, 'success');
            if (window.soundFx) window.soundFx.playOrderChime();
            closeAddCardModal();
            setTimeout(() => window.location.reload(), 1000);
        } else {
            showToast(apiErrorMessage(result, 'บันทึกการ์ดไม่สำเร็จ'), 'danger');
        }
    } catch (e) {
        showToast('เกิดข้อผิดพลาด: ' + e.message, 'danger');
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

    if (title) title.textContent = `การ์ดในบัญชี ${accountCode} (${inGameName})`;
    if (sub) sub.textContent = `การ์ดที่เปิดได้ในบัญชีเกมนี้ พร้อมสำหรับเทรด`;
    grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 2rem; color: var(--text-muted);">กำลังโหลดการ์ดในบัญชี...</div>';
    modal.classList.add('active');
    if (window.soundFx) window.soundFx.playInspect();

    try {
        const response = await fetch(`/api/v1/accounts/${accountId}/cards`);
        const result = await response.json();

        if (response.ok && result.success) {
            const cards = result.data;
            if (summary) summary.textContent = `ทั้งหมด ${cards.length} ชนิด`;

            if (cards.length === 0) {
                grid.innerHTML = `
                    <div style="grid-column: 1/-1; text-align: center; padding: 3rem; color: var(--text-muted);">
                        <p style="font-size: 1.1rem; margin-bottom: 1rem;">บัญชีนี้ยังไม่มีการ์ด</p>
                        <button class="btn btn-primary" onclick="closeAccountCardsModal(); openAddCardForAccount(${accountId});">
                            บันทึกการ์ดใบแรก
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
                        <div class="tcg-card-3d holo-card ${c.rarity === 'CROWN_RARE' ? 'crown-rare' : (c.rarity === 'IMMERSIVE_RARE' ? 'holo-immersive' : '')}"
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
                                <span class="stock-bullet" style="color: #10b981; font-weight: 700;">มี ${c.quantity} ใบในบัญชี</span>
                                <span class="rarity-pill">${c.rarity}</span>
                            </div>
                        </div>
                    </div>
                `;
            });

            grid.innerHTML = cardsHtml;
            init3DTilt();
        } else {
            grid.innerHTML = `<div style="grid-column: 1/-1; color: #ef4444; padding: 2rem;">เกิดข้อผิดพลาด: ${result.message}</div>`;
        }
    } catch (e) {
        grid.innerHTML = `<div style="grid-column: 1/-1; color: #ef4444; padding: 2rem;">เชื่อมต่อเซิร์ฟเวอร์ไม่ได้: ${e.message}</div>`;
    }
}

function closeAccountCardsModal() {
    const modal = document.getElementById('accountCardsModal');
    if (modal) modal.classList.remove('active');
}
