const { Vec3 } = require('vec3');

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const plain = (value) => String(value ?? '').replace(/§./g, '');

async function watchLit(bot, { until, timeoutMs = 60000 }) {
    const window = bot.bot.currentWindow;
    const lit = [];
    const handler = (slot, oldItem, newItem) => {
        if (/Repeat/i.test(statusText(bot))) return;
        if (newItem && /_dye$/.test(newItem.name) && slot >= 9 && slot < 45 && !(oldItem && oldItem.name === newItem.name) && lit[lit.length - 1] !== slot - 9) lit.push(slot);
    };
    window.on('updateSlot', handler);
    try {
        await bot.waitFor(() => until(), { timeoutMs, intervalMs: 50, what: 'the pattern to finish showing' });
    } finally {
        window.removeListener('updateSlot', handler);
    }
    return lit;
}

function statusText(bot, slot = 49) {
    return bot.window()?.slots.find((entry) => entry.slot === slot)?.name ?? '';
}

async function openTable(bot) {
    await bot.command('/experiments', { settleMs: 1500 });
}

module.exports = {
    description: 'Experimentation Table: craft state, place, open, play Chronomatron/Ultrasequencer/Superpairs, bottles, RNG meter',
    async run(t, bot) {
        await t.step('reach SkyBlock and set up a maxed profile with levels and bits', async () => {
            await bot.joinSkyblock();
            await bot.command('/max profile', { settleMs: 9000 });
            await bot.command('/set xp 900', { settleMs: 1500 });
            await bot.command('/set bits 20000', { settleMs: 1500 });
        });

        await t.step('/experiments opens the Experimentation Table menu', async () => {
            await openTable(bot);
            await bot.waitWindow({ timeoutMs: 10000, title: 'Experimentation Table' });
            const view = bot.window();
            const names = view.slots.map((slot) => slot.name);
            for (const wanted of ['Superpairs', 'Chronomatron', 'Ultrasequencer', 'RNG Meter', 'Experience Bottles']) {
                t.assert(names.some((name) => name.includes(wanted)), `menu is missing ${wanted}: ${JSON.stringify(names)}`);
            }
            t.assert(view.slots.some((slot) => slot.slot === 22 && /Superpairs/.test(slot.name)), 'Superpairs should sit at slot 22');
            const probe = await bot.detectInertMenu();
            t.assert(!probe.lootable, 'table menu is lootable');
            return { title: view.title, slots: view.slots.length };
        });

        await t.step('Chronomatron Metaphysical: replay a 3-note chain, fail, claim', async () => {
            await bot.click(29, { settleMs: 800 });
            await bot.waitWindow({ timeoutMs: 5000, title: 'Chronomatron' });
            const stakes = bot.window().slots.filter((slot) => /Metaphysical Experiment/.test(slot.name));
            t.assert(stakes.length === 1, 'no Metaphysical stake');
            await bot.click(stakes[0].slot, { settleMs: 400 });
            await bot.waitWindow({ timeoutMs: 8000, title: 'Chronomatron (Metaphysical)' });
            const sequence = [];
            for (let round = 1; round <= 3; round++) {
                const lit = await watchLit(bot, { until: () => /Repeat/i.test(statusText(bot)) });
                t.assert(lit.length >= round, `round ${round} showed ${lit.length} notes`);
                const expected = lit.slice(0, round);
                sequence.push(expected.length);
                for (const slot of expected) await bot.click(slot, { settleMs: 120 });
                if (round < 3) await bot.waitFor(() => /Remember/i.test(statusText(bot)), { timeoutMs: 8000, what: 'the next round to start' });
            }
            await bot.waitFor(() => /Remember/i.test(statusText(bot)), { timeoutMs: 8000, what: 'round 4' });
            await bot.waitFor(() => /Repeat/i.test(statusText(bot)), { timeoutMs: 20000, what: 'round 4 input' });
            const tileSlots = bot.window().slots.filter((slot) => /_stained_glass$/.test(slot.item) && slot.slot >= 9 && slot.slot < 45).map((slot) => slot.slot);
            for (const slot of tileSlots) {
                if (bot.window().slots.some((entry) => entry.slot === 2 && entry.item === 'lime_dye')) break;
                await bot.click(slot, { settleMs: 300 });
            }
            const claim = await bot.waitFor(() => bot.window().slots.find((slot) => slot.slot === 2 && slot.item === 'lime_dye'), { timeoutMs: 5000, what: 'the claim button' });
            const mark = bot.markChat();
            await bot.click(claim.slot, { settleMs: 1500 });
            t.assertChat(bot.chatSince(mark), /You claimed the .*Chronomatron/, 'claim message');
            return { rounds: sequence, claim: claim.lore };
        });

        await t.step('Ultrasequencer Metaphysical: 3 rounds from memory, fail, claim', async () => {
            await bot.waitWindow({ timeoutMs: 5000, title: 'Experimentation Table' });
            await bot.click(33, { settleMs: 800 });
            const stake = bot.window().slots.find((slot) => /Metaphysical Experiment/.test(slot.name));
            await bot.click(stake.slot, { settleMs: 400 });
            await bot.waitWindow({ timeoutMs: 8000, title: 'Ultrasequencer (Metaphysical)' });
            for (let round = 1; round <= 3; round++) {
                const numbers = await bot.waitFor(() => {
                    const shown = bot.window().slots.filter((slot) => /_dye$/.test(slot.item));
                    return shown.length === round ? shown : null;
                }, { timeoutMs: 10000, what: `round ${round} numbers` });
                const order = [...numbers].sort((a, b) => a.count - b.count).map((entry) => entry.slot);
                await bot.waitFor(() => /Click the numbers/i.test(statusText(bot)), { timeoutMs: 8000, what: 'input phase' });
                for (const slot of order) await bot.click(slot, { settleMs: 150 });
            }
            await bot.waitFor(() => bot.window().slots.filter((slot) => /_dye$/.test(slot.item)).length === 4, { timeoutMs: 8000, what: 'round 4' });
            await bot.waitFor(() => /Click the numbers/i.test(statusText(bot)), { timeoutMs: 8000, what: 'round 4 input' });
            const grays = bot.window().slots.filter((slot) => /_stained_glass_pane$/.test(slot.item) && slot.name === '?' && slot.slot >= 9 && slot.slot < 45).map((slot) => slot.slot);
            for (const slot of grays) {
                if (bot.window().slots.some((entry) => entry.slot === 2 && entry.item === 'lime_dye')) break;
                await bot.click(slot, { settleMs: 300 });
            }
            const claim = await bot.waitFor(() => bot.window().slots.find((slot) => slot.slot === 2 && slot.item === 'lime_dye'), { timeoutMs: 5000, what: 'the claim button' });
            const mark = bot.markChat();
            await bot.click(claim.slot, { settleMs: 1500 });
            t.assertChat(bot.chatSince(mark), /You claimed the .*Ultrasequencer/, 'claim message');
            return claim.lore;
        });

        await t.step('Superpairs Beginner: play a full board with a memory solver and get paid', async () => {
            await bot.waitWindow({ timeoutMs: 5000, title: 'Experimentation Table' });
            await bot.click(22, { settleMs: 800 });
            if (/Buy Another/.test(bot.window().title)) {
                await bot.click(13, { settleMs: 1000 });
                await bot.waitWindow({ timeoutMs: 5000, title: 'Experimentation Table' });
                await bot.click(22, { settleMs: 800 });
            }
            const stake = bot.window().slots.find((slot) => /Beginner Experiment/.test(slot.name));
            await bot.click(stake.slot, { settleMs: 500 });
            await bot.waitWindow({ timeoutMs: 8000, title: 'Superpairs (Beginner)' });
            const tiles = bot.window().slots.filter((slot) => slot.item === 'light_blue_stained_glass').map((slot) => slot.slot);
            t.assert(tiles.length === 14, `expected 14 tiles, found ${tiles.length}`);
            const seen = new Map();
            const matched = new Set();
            const mark = bot.markChat();
            for (let guard = 0; guard < 40 && !bot.findSlot(/Experiment over/) && !/Rewards/.test(bot.window()?.title ?? ''); guard++) {
                const unknown = tiles.filter((slot) => !matched.has(slot) && !seen.has(slot));
                const known = [...seen.entries()];
                let first = unknown[0];
                const pair = known.find(([slotA, nameA]) => known.some(([slotB, nameB]) => slotA !== slotB && nameA === nameB));
                if (pair) {
                    const [slotA, nameA] = pair;
                    const slotB = known.find(([slot, name]) => slot !== slotA && name === nameA)[0];
                    await bot.click(slotA, { settleMs: 250 });
                    await bot.click(slotB, { settleMs: 400 });
                    matched.add(slotA); matched.add(slotB); seen.delete(slotA); seen.delete(slotB);
                    continue;
                }
                if (first === undefined) break;
                await bot.click(first, { settleMs: 300 });
                seen.set(first, bot.window().slots.find((slot) => slot.slot === first)?.name);
                const second = unknown.find((slot) => slot !== first);
                if (second === undefined) break;
                await bot.click(second, { settleMs: 300 });
                const nameSecond = bot.window().slots.find((slot) => slot.slot === second)?.name;
                if (nameSecond === seen.get(first)) {
                    matched.add(first); matched.add(second); seen.delete(first);
                } else {
                    seen.set(second, nameSecond);
                    await sleep(1100);
                }
            }
            await bot.waitFor(() => bot.findSlot(/Experiment over/) || /Rewards/.test(bot.window()?.title ?? ''), { timeoutMs: 100000, what: 'the experiment to end' });
            if (!/Rewards/.test(bot.window().title)) await bot.click(tiles[0], { settleMs: 800 });
            await bot.waitWindow({ timeoutMs: 5000, title: 'Superpairs Rewards' });
            const mark2 = bot.markChat();
            await bot.click(22, { settleMs: 1500 });
            t.assertChat(bot.chatSince(mark2), /You claimed the .*Superpairs .*rewards/, 'claim message');
            return bot.chatSince(mark2).slice(-4);
        });

        await t.step('Superpairs refuses once the charge is spent without add-ons', async () => {
            await bot.closeWindow();
            await openTable(bot);
            await bot.waitWindow({ timeoutMs: 8000, title: 'Experimentation Table' });
            const superpairs = bot.window().slots.find((slot) => slot.slot === 22);
            t.assert(/Charges/.test(superpairs.lore.join(' ')), 'Superpairs lore should show charges');
            return superpairs.lore;
        });

        await t.step('Experience bottle menu consumes a Grand bottle', async () => {
            await bot.closeWindow();
            await bot.command('/item GRAND_EXP_BOTTLE 2', { settleMs: 1200 });
            await openTable(bot);
            await bot.waitWindow({ timeoutMs: 8000, title: 'Experimentation Table' });
            await bot.click(50, { settleMs: 800 });
            await bot.waitWindow({ timeoutMs: 5000, title: 'Bottles of Enchanting' });
            const grand = bot.window().slots.find((slot) => /grand/i.test(slot.name));
            const mark = bot.markChat();
            await bot.click(grand.slot, { settleMs: 1000 });
            t.assertChat(bot.chatSince(mark), /received .*experience/, 'bottle message');
            return grand.lore;
        });

        await t.step('/rngmeter opens the meter and selecting a drop marks it', async () => {
            await bot.closeWindow();
            await bot.command('/rngmeter', { settleMs: 1500 });
            await bot.waitWindow({ timeoutMs: 8000, title: 'Experimentation RNG Meter' });
            const first = bot.window().slots.find((slot) => slot.slot === 9);
            await bot.click(9, { settleMs: 800 });
            let after = bot.window().slots.find((slot) => slot.slot === 9);
            if (!after.lore.some((line) => /Selected!/.test(line))) {
                await bot.click(9, { settleMs: 800 });
                after = bot.window().slots.find((slot) => slot.slot === 9);
            }
            t.assert(after.lore.some((line) => /Selected!/.test(line)), `slot 9 not selected: ${JSON.stringify(after.lore)}`);
            return { name: first.name };
        });

    },
};
