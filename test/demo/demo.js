async (arg) => {
    // Read-only preparation/assertions. All mutations use real UI clicks.
    async function get(path) {
        const response = await fetch(path, {credentials: 'same-origin'});
        if (!response.ok) throw new Error(`GET ${path}: HTTP ${response.status}`);
        const body = await response.json();
        if (!body.success) throw new Error(body.message || `GET ${path} failed`);
        return body.data;
    }
    if (arg.action === 'plan') {
        const accounts = await get('/api/v1/accounts');
        const readyStock = new Map();
        for (const account of accounts.filter(a => a.tradeStatus === 'READY')) {
            for (const inventory of await get(`/api/v1/accounts/${account.id}/cards`)) {
                if (inventory.quantity > 0) readyStock.set(String(inventory.inventoryId), inventory);
            }
        }
        const offers = [...document.querySelectorAll('.gallery-cards-grid .tcg-card-3d[data-inventory-id]')];
        const offer = offers.find(el => readyStock.has(el.dataset.inventoryId)
            && (!arg.cardName || el.dataset.name.toLowerCase().includes(arg.cardName.toLowerCase())));
        if (!offer) throw new Error('No gallery offer backed by a READY account with stock. Prepare demo stock on /accounts first.');
        const inv = readyStock.get(offer.dataset.inventoryId);
        return {inventoryId: inv.inventoryId, cardName: inv.cardName, cardNumber: inv.cardNumber,
            condition: inv.condition, quantityBefore: inv.quantity, price: Number(inv.sellingPrice)};
    }
    if (arg.action === 'tier') {
        const select = document.querySelector(`select[data-user-id="${Number(arg.userId)}"]`);
        if (!select) throw new Error('Customer membership selector not found on /accounts.');
        const tier = select.value;
        const rates = {REGULAR: 0, VIP: 0.10, WHOLESALE: 0.15};
        if (!(tier in rates)) throw new Error(`Unsupported tier ${tier}`);
        return {tier, rate: rates[tier]};
    }
    if (arg.action === 'orderByCode') {
        const order = (await get('/api/v1/orders')).find(o => o.orderCode === arg.code);
        if (!order) throw new Error(`Order ${arg.code} not found. Do not retry booking automatically.`);
        return order;
    }
    if (arg.action === 'order') return await get(`/api/v1/orders/${Number(arg.orderId)}`);
    if (arg.action === 'stock') {
        for (const account of await get('/api/v1/accounts')) {
            const inv = (await get(`/api/v1/accounts/${account.id}/cards`))
                .find(i => i.inventoryId === Number(arg.inventoryId));
            if (inv) return inv.quantity;
        }
        throw new Error('Booked inventory no longer exists.');
    }
    if (arg.action === 'cue') {
        document.getElementById('demo-next')?.remove();
        window.__demoContinue = false;
        const button = document.createElement('button');
        button.id = 'demo-next';
        button.type = 'button';
        button.textContent = `${arg.label} · คลิกเพื่อไปต่อ`;
        button.style.cssText = 'position:fixed;right:18px;bottom:18px;z-index:2147483647;background:#11151a;color:#f2efe6;border:1px solid #f05436;border-radius:9px;padding:12px 18px;font:600 15px system-ui;box-shadow:0 4px 20px #0008;cursor:pointer';
        button.onclick = () => { window.__demoContinue = true; button.remove(); };
        document.body.append(button);
        return true;
    }
    if (arg.action === 'removeCue') {
        document.getElementById('demo-next')?.remove();
        return true;
    }
    throw new Error(`Unknown demo action ${arg.action}`);
}
