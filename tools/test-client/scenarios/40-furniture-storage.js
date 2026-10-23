const { Vec3 } = require('vec3');
const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const titleText = (window) => JSON.stringify(window?.title ?? '');

const surface = (bot, x, z) => {
    for (let y = 112; y >= 90; y--) {
        const block = bot.bot.blockAt(new Vec3(x, y, z));
        if (block && block.name !== 'air') return { x, y: y + 1, z };
    }
    return { x, y: 100, z };
};

const placeFurniture = async (t, bot, itemId, matcher, x, z) => {
    await bot.command(`/item ${itemId}`, { settleMs: 1000 });
    await bot.command('/tp 6 101 36', { settleMs: 1500 });
    await bot.useHotbarItem(matcher, { settleMs: 300 }).catch(() => {});
    const target = surface(bot, x, z);
    const mark = bot.markChat();
    const ground = bot.bot.blockAt(new Vec3(target.x, target.y - 1, target.z));
    await bot.bot.lookAt(new Vec3(target.x + 0.5, target.y, target.z + 0.5), true);
    await bot.bot.placeBlock(ground, new Vec3(0, 1, 0)).catch(() => {});
    await sleep(1500);
    const chat = bot.chatSince(mark).filter((line) => !/TPS/.test(line)).join(' | ');
    t.assert(/You placed/.test(chat), chat);
    return target;
};

const openAt = async (t, bot, target) => {
    const hitbox = bot.bot.nearestEntity((entity) => /interaction/i.test(entity.name ?? '') && entity.position.distanceTo(new Vec3(target.x + 0.5, target.y + 0.5, target.z + 0.5)) < 2);
    t.assert(hitbox, 'no furniture hitbox');
    await bot.command(`/tp ${Math.floor(hitbox.position.x)} ${Math.floor(hitbox.position.y) + 1} ${Math.floor(hitbox.position.z) + 1}`, { settleMs: 1500 });
    for (let attempt = 1; attempt <= 3 && !bot.window(); attempt++) {
        await bot.interact(hitbox);
        await bot.waitWindow({ timeoutMs: 4000 }).catch(() => {});
    }
    t.assert(bot.window(), 'no window opened');
};

const inventorySlot = (bot, matcher) => {
    const window = bot.bot.currentWindow;
    const start = window.inventoryStart ?? window.slots.length - 36;
    const owned = bot.bot.inventory.slots.findIndex((item, index) => index >= 9 && index <= 44 && item
        && matcher.test(item.customName ? JSON.stringify(item.customName) : item.displayName));
    if (owned < 0) return -1;
    return owned <= 35 ? start + owned - 9 : start + 27 + (owned - 36);
};

module.exports = {
    description: 'Storage furniture: a Wood Chest keeps three pages across reopening and an island reload, Furnace+ smelts',
    async run(t, bot) {
        await t.step('reach SkyBlock and max the skills so Carpentry gates are met', async () => {
            await bot.joinSkyblock();
            await bot.command('/mission clearset GETTING_STARTED', { settleMs: 1500 });
            await bot.command('/centauri', { settleMs: 2000 });
            await bot.waitWindow({ timeoutMs: 8000 });
            await bot.click(bot.findSlot(/Toy Box/i).slot);
            await bot.waitWindow({ timeoutMs: 8000 });
            await bot.click(bot.findSlot(/Max Skills/i).slot, { settleMs: 2500 });
            await bot.closeWindow();
            await sleep(6000);
        });

        let chest;
        await t.step('a Wood Chest places and opens with three pages', async () => {
            chest = await placeFurniture(t, bot, 'WOOD_CHEST', /Wood Chest/, 8, 40);
            await bot.command('/clear', { settleMs: 500 });
            await openAt(t, bot, chest);
            t.assert(/Wood Chest.*Page 1\/3/.test(titleText(bot.window())), titleText(bot.window()));
            const names = bot.window().slots.map((slot) => slot.name).join(' | ');
            t.assert(/Next Page/.test(names) && !/Previous Page/.test(names), names);
        });

        await t.step('items put in the chest survive closing and reopening', async () => {
            await bot.closeWindow();
            await bot.command('/item OAK_PLANKS 7', { settleMs: 1000 });
            await openAt(t, bot, chest);
            const from = inventorySlot(bot, /Oak Planks/);
            t.assert(from >= 0, 'no planks in the inventory');
            await bot.click(from, { settleMs: 500 });
            await bot.click(10, { settleMs: 800 });
            const placed = bot.window().slots.find((entry) => entry.slot === 10);
            t.assert(placed, 'the item did not land in the chest slot; cursor=' + JSON.stringify(bot.bot.inventory.selectedItem?.name) + ' from=' + from);
            await bot.closeWindow();
            await sleep(500);
            await openAt(t, bot, chest);
            const slot = bot.window().slots.find((entry) => entry.slot === 10);
            t.assert(slot && /Oak Planks/.test(slot.name), JSON.stringify(slot));
            return slot.name + ' x' + slot.count;
        });

        await t.step('the chest page buttons move between pages and keep each page', async () => {
            await bot.click(53, { settleMs: 1200 });
            await bot.waitWindow({ timeoutMs: 6000, title: 'Page 2/3' });
            t.assert(!bot.window().slots.some((slot) => slot.slot === 10 && /Oak Planks/.test(slot.name)), 'page 2 should be empty');
            await bot.click(45, { settleMs: 1200 });
            await bot.waitWindow({ timeoutMs: 6000, title: 'Page 1/3' });
            const slot = bot.window().slots.find((entry) => entry.slot === 10);
            t.assert(slot && /Oak Planks/.test(slot.name), JSON.stringify(slot));
            await bot.closeWindow();
        });

        await t.step('a chest holding items cannot be picked up', async () => {
            const hitbox = bot.bot.nearestEntity((entity) => /interaction/i.test(entity.name ?? ''));
            bot.bot.setControlState('sneak', true);
            await sleep(300);
            await bot.bot.lookAt(hitbox.position.offset(0, 0.5, 0), true);
            await bot.interact(hitbox);
            await bot.waitWindow({ timeoutMs: 5000, title: 'Furniture' });
            const pickup = bot.findSlot(/Pickup/);
            const mark = bot.markChat();
            await bot.click(pickup.slot, { settleMs: 1500 });
            bot.bot.setControlState('sneak', false);
            const chat = bot.chatSince(mark).join(' | ');
            t.assert(/Empty this out/.test(chat), chat);
        });

        let furnace;
        await t.step('a Furnace+ places and smelts an ore with coal', async () => {
            furnace = await placeFurniture(t, bot, 'FURNACE_PLUS', /Furnace/, 11, 40);
            await bot.command('/clear', { settleMs: 500 });
            await bot.command('/item IRON_ORE 2', { settleMs: 1000 });
            await bot.command('/item OAK_PLANKS 2', { settleMs: 1000 });
            await openAt(t, bot, furnace);
            t.assert(/Furnace/.test(titleText(bot.window())), titleText(bot.window()));
            const ore = inventorySlot(bot, /Iron Ore/);
            await bot.click(ore, { settleMs: 500 });
            await bot.click(0, { settleMs: 500 });
            const coal = inventorySlot(bot, /Oak Planks/);
            await bot.click(coal, { settleMs: 500 });
            await bot.click(1, { settleMs: 500 });
            await sleep(13000);
            const output = bot.window().slots.find((slot) => slot.slot === 2);
            t.assert(output && /Iron Ingot/.test(output.name), JSON.stringify(output));
            return output.name + ' x' + output.count;
        });

        await t.step('the furnace keeps cooking while it is closed', async () => {
            await bot.closeWindow();
            await sleep(11000);
            await openAt(t, bot, furnace);
            const output = bot.window().slots.find((slot) => slot.slot === 2);
            t.assert(output && output.count >= 2, JSON.stringify(output));
            await bot.closeWindow();
        });

        await t.step('chest and furnace contents survive an island reload', async () => {
            await bot.disconnect();
            await sleep(30000);
            await bot.connect();
            await bot.joinSkyblock({ timeoutMs: 240000 });
            await sleep(6000);
            await openAt(t, bot, chest);
            const slot = bot.window().slots.find((entry) => entry.slot === 10);
            t.assert(slot && /Oak Planks/.test(slot.name), JSON.stringify(slot));
            await bot.closeWindow();
            await openAt(t, bot, furnace);
            const output = bot.window().slots.find((entry) => entry.slot === 2);
            t.assert(output && /Iron Ingot/.test(output.name), JSON.stringify(output));
            await bot.closeWindow();
        });
    },
};
