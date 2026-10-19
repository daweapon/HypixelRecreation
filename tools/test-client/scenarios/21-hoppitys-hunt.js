const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const sidebarText = (bot) => (bot.sidebar()?.lines ?? []).join(' | ');

module.exports = {
    description: "Hoppity's Hunt: egg spawn and announcement, claim and visibility, Egglocator, Hoppity's shop, factory prestige/Hitman/collection filter, /pity",
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => bot.joinSkyblock());

        await t.step('finish the starting missions and reach SkyBlock Level 5 so /hub works', async () => {
            await bot.command('/mission clearset GETTING_STARTED', { settleMs: 1500 });
            await bot.command('/skyblockxp setlevel 5', { settleMs: 1500 });
            await bot.command('/set coins 20000000', { settleMs: 1000 });
        });

        await t.step('go to the Hub', async () => {
            bot.say('/hub');
            await bot.waitFor(() => /Hub|Village|Elizabeth/i.test(sidebarText(bot)), { timeoutMs: 200000, what: 'the Hub sidebar' });
            return sidebarText(bot);
        });

        let eggSpot = null;
        let huntMark = 0;
        await t.step('/adminbypass hunt 0 jumps to the first egg and reports where it will be', async () => {
            const mark = bot.markChat();
            huntMark = mark;
            await bot.command('/adminbypass hunt 0', { settleMs: 1500 });
            const line = bot.chatSince(mark).find((entry) => /it will be at/i.test(entry));
            t.assert(line, `no location line: ${JSON.stringify(bot.chatSince(mark))}`);
            const match = line.match(/(-?\d+) (-?\d+) (-?\d+)/);
            eggSpot = { x: Number(match[1]), y: Number(match[2]), z: Number(match[3]) };
            return eggSpot;
        });

        await t.step('the egg is announced in chat', async () => {
            const line = await bot.waitChat(/HOPPITY'S HUNT.*Chocolate Breakfast Egg.*has appeared/i, { since: huntMark, timeoutMs: 20000 }).catch((error) => { throw new Error(`${error.message}; chat: ${JSON.stringify(bot.chatSince(huntMark))}; sidebar ${sidebarText(bot)}`); });
            return line;
        });

        const eggEntity = () => Object.values(bot.bot.entities).find((entity) => entity.name === 'interaction'
            && Math.hypot(entity.position.x - (eggSpot.x + 0.5), entity.position.z - (eggSpot.z + 0.5)) < 1.5
            && Math.abs(entity.position.y - eggSpot.y) < 6);

        await t.step('the egg entity exists at its wiki location', async () => {
            await bot.command(`/tp ${eggSpot.x} ${eggSpot.y + 1} ${eggSpot.z + 2}`, { settleMs: 3000 });
            await bot.waitFor(() => eggEntity(), { timeoutMs: 20000, what: 'the egg hitbox' });
            return eggEntity().position;
        });

        await t.step('the Egglocator points at the egg and respects its cooldown', async () => {
            await bot.command('/item EGGLOCATOR', { settleMs: 1500 });
            let particles = 0;
            const count = () => { particles++; };
            bot.bot._client.on('world_particles', count);
            const mark = bot.markChat();
            await bot.useHotbarItem(/Egglocator/i, { settleMs: 800 });
            await sleep(800);
            bot.bot._client.removeListener('world_particles', count);
            const lines = bot.chatSince(mark);
            t.assert(!lines.some((line) => /no Chocolate Rabbit Eggs|doesn't spawn/i.test(line)), `unexpected: ${JSON.stringify(lines)}`);
            t.assert(particles > 0, 'no particles received');
            const again = bot.markChat();
            bot.bot.activateItem();
            await sleep(800);
            t.assert(bot.chatSince(again).some((line) => /cooldown/i.test(line)), `no cooldown message: ${JSON.stringify(bot.chatSince(again))}`);
        });

        await t.step('right-clicking the egg claims it, rolls a rabbit and hides it', async () => {
            const mark = bot.markChat();
            await bot.interact(eggEntity());
            await bot.waitChat(/HOPPITY'S HUNT You found a Chocolate Breakfast Egg/i, { since: mark, timeoutMs: 10000 }).catch((error) => { throw new Error(`${error.message}; chat: ${JSON.stringify(bot.chatSince(mark))}`); });
            await bot.waitChat(/NEW RABBIT|DUPLICATE RABBIT|BARN FULL/i, { since: mark, timeoutMs: 10000 });
            await sleep(6500);
            t.assert(!eggEntity(), 'the egg is still visible after being claimed');
            return bot.chatSince(mark).slice(-3);
        });

        await t.step('a stray rabbit appears in the factory menu after an egg and can be caught', async () => {
            bot.say('/cf');
            await bot.waitWindow({ title: 'Chocolate Factory', timeoutMs: 15000 });
            const stray = () => bot.window()?.slots.find((slot) => /Stray Rabbit|Golden Rabbit/.test(slot.name));
            await bot.waitFor(() => stray(), { timeoutMs: 60000, what: 'a stray rabbit item' });
            const found = stray();
            const mark = bot.markChat();
            await bot.click(found.slot, { settleMs: 1500 });
            await bot.closeWindow();
            t.assert(bot.chatSince(mark).some((line) => /STRAY RABBIT|GOLDEN RABBIT|NEW RABBIT/.test(line)), `chat: ${JSON.stringify(bot.chatSince(mark))}`);
            return found.name;
        });

        await t.step('/pity shows the progress', async () => {
            const lines = await bot.command('/pity', { settleMs: 1200 });
            t.assertChat(lines, /Every 250th rabbit/i, 'expected the Mythic line');
            t.assertChat(lines, /Every 1250th|Every 1,250th/i, 'expected the Divine line');
        });

        await t.step('Hoppity is in the Hub during the Hunt and sells seven rabbits', async () => {
            await bot.command('/tp 56 70 -2', { settleMs: 3000 });
            await bot.waitFor(() => bot.findNpc('Hoppity'), { timeoutMs: 20000, what: 'Hoppity' });
            const npc = bot.findNpc('Hoppity');
            await bot.interact(npc);
            let window = await bot.waitWindow({ timeoutMs: 6000 }).catch(() => null);
            if (!window) {
                await sleep(12000);
                await bot.interact(npc);
                window = await bot.waitWindow({ timeoutMs: 15000 });
            }
            await sleep(800);
            const view = bot.window();
            const items = view.slots.filter((slot) => slot.slot >= 10 && slot.slot <= 16 && slot.name.trim());
            t.assert(items.length === 7, `expected 7 rabbits, saw ${items.length}: ${items.map((i) => i.name).join(' | ')}`);
            t.assert(/200,000/.test(items[0].lore.join(' ')), `first price: ${items[0].lore.join(' ')}`);
            const before = Number((sidebarText(bot).match(/Purse: ([\d,]+)/) ?? [])[1]?.replace(/,/g, ''));
            const buyMark = bot.markChat();
            await bot.click(items[0].slot, { settleMs: 1500 });
            t.assert(bot.chatSince(buyMark).some((line) => /NEW RABBIT|DUPLICATE RABBIT/.test(line)), `purchase chat: ${JSON.stringify(bot.chatSince(buyMark))}`);
            await bot.closeWindow();
            return { before, name: items[0].name };
        });

        await t.step('the Chocolate Factory menu shows prestige, hitman slots and the Hitman opens', async () => {
            bot.say('/cf');
            await bot.waitWindow({ title: 'Chocolate Factory', timeoutMs: 15000 });
            await sleep(800);
            const prestige = bot.window().slots.find((slot) => slot.slot === 27);
            t.assert(/Max Employee: \[120\]/.test(prestige.lore.join(' ')), `prestige lore: ${prestige.lore.join(' | ')}`);
            t.assert(/Requires/.test(prestige.lore.join(' ')), `prestige lore: ${prestige.lore.join(' | ')}`);
            await bot.click(51, { settleMs: 1500 });
            const hitman = bot.window();
            t.assert(hitman && /Hitman/i.test(JSON.stringify(hitman.title)), `hitman window: ${JSON.stringify(hitman?.title)}`);
            await bot.closeWindow();
        });

        await t.step('the collection has the working Rabbit Locations filter', async () => {
            bot.say('/cf');
            await bot.waitWindow({ title: 'Chocolate Factory', timeoutMs: 15000 });
            await bot.click(50, { settleMs: 1500 });
            await bot.waitFor(() => /Hoppity|Collection/i.test(JSON.stringify(bot.window()?.title)), { timeoutMs: 10000, what: 'the collection' });
            const slot = bot.window().slots.find((s) => s.slot === 47);
            await bot.click(47, { settleMs: 1200 });
            const after = bot.window().slots.find((s) => s.slot === 47);
            t.assert(/Currently selected: Hotspot/i.test(after.lore.join(' ')), `filter lore: ${after.lore.join(' | ')}`);
            await bot.closeWindow();
        });
    },
};
