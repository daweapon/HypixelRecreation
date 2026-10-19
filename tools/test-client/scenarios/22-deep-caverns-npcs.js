const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const sidebarText = (bot) => (bot.sidebar()?.lines ?? []).join(' | ');

module.exports = {
    description: 'Deep Caverns NPCs: Rhys quest, Lapis Miner Compactor, Redstone Miner tiers, lift travel',
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => bot.joinSkyblock());

        await t.step('finish the starting missions and reach SkyBlock Level 5', async () => {
            await bot.command('/mission clearset GETTING_STARTED', { settleMs: 1500 });
            await bot.command('/skyblockxp setlevel 5', { settleMs: 1500 });
        });

        await t.step('send the bot to the Deep Caverns', async () => {
            bot.say('/sendto SKYBLOCK_DEEP_CAVERNS');
            await bot.waitFor(() => /Deep|Caverns|Obsidian|Gunpowder/i.test(sidebarText(bot)), { timeoutMs: 200000, what: 'the Deep Caverns sidebar' });
            return sidebarText(bot);
        });

        const talk = async (label, x, y, z, pattern, { timeoutMs = 30000 } = {}) => {
            await bot.command(`/tp ${x} ${y} ${z}`, { settleMs: 2500 });
            await bot.waitFor(() => bot.findNpc(label), { timeoutMs: 20000, what: label });
            const mark = bot.markChat();
            await bot.interact(bot.findNpc(label));
            const line = await bot.waitChat(pattern, { since: mark, timeoutMs }).catch((error) => {
                throw new Error(`${error.message}; chat: ${JSON.stringify(bot.chatSince(mark))}`);
            });
            await sleep(6000);
            return line;
        };

        await t.step('Rhys sends a low-level miner away', async () => talk('Rhys', 31, 12, 17, /Come back when you are Mining Level XII/i));

        await t.step('Lapis Miner asks for a pickaxe, then points at Collection III', async () => {
            await talk('Lapis Miner', -10, 120, 37, /broke my Lapis Pickaxe/i, { timeoutMs: 40000 });
            return talk('Lapis Miner', -10, 120, 37, /Some items in SkyBlock can only be crafted/i);
        });

        await t.step('at Lapis Collection III he asks for the crafted pickaxe', async () => {
            await bot.command('/set collection LAPIS_LAZULI 1000', { settleMs: 1200 });
            return talk('Lapis Miner', -10, 120, 37, /Oh! You reached/i);
        });

        await t.step('handing over the Lapis Pickaxe gives a Compactor once', async () => {
            await bot.command('/item LAPIS_PICKAXE', { settleMs: 1200 });
            await talk('Lapis Miner', -10, 120, 37, /Thank you! I appreciate/i, { timeoutMs: 40000 });
            await sleep(4000);
            const held = bot.bot.inventory.items().map((item) => bot.describeItem(item, 0).name);
            t.assert(held.some((name) => /Compactor/.test(name)), `inventory: ${held.join(' | ')}`);
            t.assert(!held.some((name) => /Lapis Pickaxe/.test(name)), 'the pickaxe was not consumed');
            return talk('Lapis Miner', -10, 120, 37, /might not mine the fastest/i);
        });

        await t.step('Redstone Miner waits for Redstone Collection II', async () => talk('Redstone Miner', 25, 104, 18, /Redstone Collection II/i));

        await t.step('then offers 128 Redstone for a slot and rewards it', async () => {
            await bot.command('/set collection REDSTONE 300', { settleMs: 1200 });
            await talk('Redstone Miner', 25, 104, 18, /bring me .*128 Redstone/i, { timeoutMs: 40000 });
            await bot.command('/item REDSTONE 128', { settleMs: 1200 });
            await talk('Redstone Miner', 25, 104, 18, /here's an extra .*Accessory Bag.* slot/i, { timeoutMs: 40000 });
            const held = bot.bot.inventory.items().map((item) => `${bot.describeItem(item, 0).name} x${item.count}`);
            t.assert(!held.some((name) => /Redstone x128/.test(name)), `redstone not taken: ${held.join(' | ')}`);
            return talk('Redstone Miner', 25, 104, 18, /256 Redstone/i);
        });

        await t.step('Max Skills from the Toy Box lifts the bot to Mining XII', async () => {
            bot.say('/centauri');
            await bot.waitWindow({ timeoutMs: 20000 });
            await bot.click(15, { button: 0, mode: 0 });
            await bot.waitFor(() => /Toy Box/.test(JSON.stringify(bot.window()?.title)), { timeoutMs: 10000, what: 'the Toy Box' });
            const mark = bot.markChat();
            await bot.click(15, { button: 0, mode: 0 });
            await bot.waitChat(/Centauri granted your request/i, { since: mark, timeoutMs: 10000 });
            await bot.closeWindow();
        });

        await t.step('Rhys takes a partial hand-in and then finishes the quest', async () => {
            await talk('Rhys', 31, 12, 17, /bring me .*15/i, { timeoutMs: 40000 });
            await bot.command('/item ENCHANTED_COAL 10', { settleMs: 1200 });
            await talk('Rhys', 31, 12, 17, /I still need .*5/i);
            await bot.command('/item ENCHANTED_DIAMOND 20', { settleMs: 1200 });
            await talk('Rhys', 31, 12, 17, /Perhaps you are someone we can get along with/i, { timeoutMs: 40000 });
            const held = bot.bot.inventory.items().map((item) => `${bot.describeItem(item, 0).name} x${item.count}`);
            t.assert(held.some((name) => /Enchanted Diamond x15/.test(name)), `leftover diamonds: ${held.join(' | ')}`);
            return talk('Rhys', 31, 12, 17, /Dwarven Mines are open to you now/i);
        });

        await t.step('the Lift Operator lets a discovered layer be used', async () => {
            await bot.command('/tp 45 150 17', { settleMs: 4000 });
            await bot.waitFor(() => bot.findNpc('Lift Operator'), { timeoutMs: 20000, what: 'the Lift Operator' });
            await bot.interact(bot.findNpc('Lift Operator'));
            let window = await bot.waitWindow({ timeoutMs: 8000 }).catch(() => null);
            for (let attempt = 0; !window && attempt < 3; attempt++) {
                await sleep(8000);
                await bot.interact(bot.findNpc('Lift Operator'));
                window = await bot.waitWindow({ timeoutMs: 8000 }).catch(() => null);
            }
            t.assert(window, 'the lift menu did not open');
            await sleep(800);
            const lapis = bot.window().slots.find((slot) => slot.slot === 12);
            t.assert(/Click to travel/.test(lapis.lore.join(' ')), `lore: ${lapis.lore.join(' | ')}`);
            await bot.click(12, { settleMs: 2500 });
            await sleep(1500);
            const y = bot.position().y;
            t.assert(y > 115 && y < 126, `expected the Lapis Quarry (y 121), at y ${y}`);
        });
    },
};
