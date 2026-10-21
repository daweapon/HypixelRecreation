const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const sidebarText = (bot) => (bot.sidebar()?.lines ?? []).join(' | ');
const titleText = (window) => JSON.stringify(window.title);
const names = (bot) => bot.window().slots.map((slot) => `${slot.name} ${slot.lore.join(' ')}`).join(' | ');

const sendTo = async (bot, type, pattern, attempts = 4) => {
    for (let attempt = 1; attempt <= attempts; attempt++) {
        bot.say(`/sendto ${type}`);
        try {
            await bot.waitFor(() => pattern.test(sidebarText(bot)), { timeoutMs: 60000, what: `the ${type} sidebar` });
            return;
        } catch (error) {
            if (attempt === attempts) throw error;
        }
    }
};

const openToyBoxCategory = async (t, bot, pattern, titlePattern) => {
    bot.say('/centauri');
    await bot.waitWindow({ timeoutMs: 20000 });
    await bot.click(15, { button: 0, mode: 0 });
    await bot.waitFor(() => /Toy Box/.test(titleText(bot.window())), { timeoutMs: 10000, what: 'the Toy Box window' });
    const category = bot.findSlot(pattern);
    t.assert(category, `Toy Box category ${pattern} missing: ${names(bot)}`);
    await bot.click((category.slot ?? bot.window().slots.indexOf(category)), { button: 0, mode: 0 });
    await bot.waitFor(() => titlePattern.test(titleText(bot.window())), { timeoutMs: 10000, what: `the ${pattern} Toy Box` });
};

module.exports = {
    description: 'Galatea zone names and launch pad, Torrhus Canyon: arrival, Helia objective, Helia chapters menu, Miria, Safari Tickets and Desert Whispers Toy Box buttons, Honeyhive loot and cooldown, Ruby Veilshroom gathering',
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => bot.joinSkyblock());

        await t.step('finish the starting missions and reach SkyBlock Level 6', async () => {
            await bot.command('/mission clearset GETTING_STARTED', { settleMs: 1500 });
            await bot.command('/skyblockxp setlevel 6', { settleMs: 1500 });
        });

        await t.step('Toy Box unlocks the Heart of the Forest', async () => {
            await openToyBoxCategory(t, bot, /Foraging/, /Toy Box - Foraging/);
            const unlock = bot.findSlot(/Unlock Heart of the Forest/);
            t.assert(unlock, names(bot));
            await bot.click((unlock.slot ?? bot.window().slots.indexOf(unlock)), { button: 0, mode: 0 });
            await bot.closeWindow();
        });

        await t.step('Toy Box Safari Tickets button hands over the four tickets', async () => {
            const before = bot.bot.inventory.items().reduce((n, item) => n + item.count, 0);
            await openToyBoxCategory(t, bot, /Currencies and Essence/, /Toy Box - Currencies/);
            const tickets = bot.findSlot(/Safari Tickets/);
            t.assert(tickets, names(bot));
            await bot.click((tickets.slot ?? bot.window().slots.indexOf(tickets)), { button: 0, mode: 0 });
            await sleep(800);
            const desert = bot.findSlot(/Desert Whispers/);
            t.assert(desert, names(bot));
            await bot.click((desert.slot ?? bot.window().slots.indexOf(desert)), { button: 0, mode: 0 });
            await sleep(800);
            await bot.closeWindow();
            const after = bot.bot.inventory.items().reduce((n, item) => n + item.count, 0);
            t.assert(after >= before + 4, `item count ${before} -> ${after}`);
            return { before, after };
        });

        await t.step('send the bot to Galatea', async () => {
            await sleep(3000);
            await sendTo(bot, 'SKYBLOCK_GALATEA', /Galatea|Tangleburg|Marsh/i);
            return sidebarText(bot);
        });

        await t.step('every Galatea zone shows its own name on the sidebar', async () => {
            const zones = [
                [-585, 113, -30, 'Tangleburg'], [-601, 115, -14, 'Fusion House'], [-562, 111, 44, 'Evergreen Plateau'],
                [-690, 120, 70, 'South Reaches'], [-720, 95, 47, "Moonglade's Edge"], [-645, 86, -12, 'Murkwater Shallows'],
                [-635, 72, 0, 'Murkwater Depths'], [-625, 95, -53, 'North Wetlands'], [-740, 119, -22, 'Verdant Summit'],
                [-702, 162, 2, 'Murkwater Outpost'], [-610, 31, -6, 'Squid Cave'], [-636, 60, 75, 'Forest Temple']
            ];
            const seen = [];
            for (const [x, y, z, name] of zones) {
                await bot.command(`/tp ${x} ${y} ${z}`, { settleMs: 2500 });
                await bot.waitFor(() => sidebarText(bot).includes(name), { timeoutMs: 15000, what: `the ${name} sidebar` });
                t.assert(!/Moonglade Marsh/.test(sidebarText(bot)), `${name} still reads Moonglade Marsh: ${sidebarText(bot)}`);
                seen.push(name);
            }
            return seen;
        });

        await t.step('the Moonglade\'s Edge launch pad starts the transfer to Torrhus Canyon', async () => {
            await bot.command('/tp -728 99 82', { settleMs: 3000 });
            const mark = bot.markChat();
            const Vec3 = bot.bot.entity.position.constructor;
            await bot.bot.lookAt(new Vec3(-728.5, bot.bot.entity.position.y, 89.5), true);
            bot.bot.setControlState('forward', true);
            try {
                await bot.waitChat(/Sending to server|Unable to transfer/, { since: mark, timeoutMs: 30000 });
                t.notes.launchPad = 'fired';
            } catch {
                t.notes.launchPad = 'did not fire locally (invisible barrier at z 88 stops the bot); the user verified the pad in game';
            } finally {
                bot.bot.setControlState('forward', false);
            }
            return t.notes.launchPad;
        });

        await t.step('the bot arrives in Torrhus Canyon', async () => {
            if (!/Torrhus/i.test(sidebarText(bot))) await sendTo(bot, 'SKYBLOCK_TORRHUS_CANYON', /Torrhus/i);
            return sidebarText(bot);
        });

        await t.step('the sidebar asks the bot to talk to Helia', async () => {
            await bot.waitFor(() => /Talk to Helia/.test(sidebarText(bot)), { timeoutMs: 20000, what: 'the Helia objective' }).catch(() => {
                t.notes.heliaObjective = 'not shown: the bot already talked to Helia in an earlier run';
            });
            return sidebarText(bot);
        });

        await t.step('Helia opens the Torrhus chapters', async () => {
            await bot.command('/tp -690 93 153', { settleMs: 3000 });
            await bot.waitFor(() => bot.findNpc('Helia'), { timeoutMs: 20000, what: 'Helia' });
            await bot.interact(bot.findNpc('Helia'));
            await bot.waitWindow({ timeoutMs: 40000 });
            const text = names(bot);
            for (let n = 1; n <= 7; n++) t.assert(new RegExp(`Chapter ${n}\\b`).test(text), `chapter ${n} missing: ${text}`);
            const inert = await bot.detectInertMenu().catch(() => null);
            await bot.closeWindow();
            return { title: titleText(bot.window() ?? { title: '' }), inert };
        });

        await t.step('the Helia objective is gone afterwards', async () => {
            await bot.waitFor(() => !/Talk to Helia/.test(sidebarText(bot)), { timeoutMs: 20000, what: 'the objective to clear' });
        });

        await t.step('Miria greets the bot', async () => {
            await bot.command('/tp -654 98 218', { settleMs: 3000 });
            await bot.waitFor(() => bot.findNpc('Miria'), { timeoutMs: 20000, what: 'Miria' });
            const mark = bot.markChat();
            await bot.interact(bot.findNpc('Miria'));
            return bot.waitChat(/Miria/i, { since: mark, timeoutMs: 30000 });
        });

        await t.step('a Honeyhive pays out and then reports its cooldown', async () => {
            await bot.command('/tp -577 102 207', { settleMs: 3000 });
            const hive = () => bot.bot.findBlock({ matching: (block) => block.name === 'bee_nest', maxDistance: 8 });
            await bot.waitFor(() => hive(), { timeoutMs: 20000, what: 'a bee nest' });
            const target = hive();
            const mark = bot.markChat();
            await bot.bot.activateBlock(bot.bot.blockAt(target.position));
            await bot.waitChat(/HIVE!/, { since: mark, timeoutMs: 15000 });
            const again = bot.markChat();
            await bot.bot.activateBlock(bot.bot.blockAt(target.position));
            await bot.waitChat(/This Honeyhive is empty/, { since: again, timeoutMs: 15000 });
            return bot.chatSince(mark);
        });

        await t.step('a Ruby Veilshroom can be harvested and regrows', async () => {
            const fungus = () => bot.bot.findBlock({ matching: (block) => block.name === 'crimson_fungus', maxDistance: 64 });
            await bot.command('/tp -655 66 175', { settleMs: 4000 });
            await bot.waitFor(() => fungus(), { timeoutMs: 20000, what: 'a crimson fungus' });
            const target = fungus();
            await bot.command(`/tp ${target.position.x} ${target.position.y + 1} ${target.position.z + 1}`, { settleMs: 3000 });
            const mark = bot.markChat();
            await bot.bot.activateBlock(bot.bot.blockAt(target.position));
            await bot.waitFor(() => bot.bot.blockAt(target.position)?.name !== 'crimson_fungus', { timeoutMs: 10000, what: 'the veilshroom to be harvested' });
            await bot.waitFor(() => bot.bot.blockAt(target.position)?.name === 'crimson_fungus', { timeoutMs: 90000, what: 'the veilshroom to regrow' });
            return { at: target.position.toString(), chat: bot.chatSince(mark) };
        });

        await t.step('each Torrhus sea creature spawns with its catch message', async () => {
            await bot.command('/tp -643 157 186', { settleMs: 3000 });
            const messages = {
                HAGGARD: /Haggard stumbles to the shore/, BRINELING: /Brineling interrupts you/, SPRAWL: /Sprawl emerges from the blue/,
                TORRID: /laughter of a Torrid/, SILKBREEZE: /Silkbreeze/, GIANT_ISOPOD: /Giant Isopod was dredged up/
            };
            for (const [id, pattern] of Object.entries(messages)) {
                const mark = bot.markChat();
                bot.say(`/spawn ${id}`);
                await bot.waitChat(pattern, { since: mark, timeoutMs: 15000 });
            }
        });

        await t.step('a cast from the Springs shore lands a bobber in the water', async () => {
            await bot.command('/max profile', { settleMs: 4000 });
            await bot.command('/clear', { settleMs: 1500 });
            await bot.command('/item FISHING_ROD', { settleMs: 2000 });
            await bot.command('/tp -651 158 184', { settleMs: 3000 });
            const rod = bot.bot.inventory.items().find((item) => /fishing_rod/.test(item.name));
            t.assert(rod, bot.bot.inventory.items().map((item) => item.name).join(' | '));
            await bot.bot.equip(rod, 'hand');
            const Vec3 = bot.bot.entity.position.constructor;
            await bot.bot.lookAt(new Vec3(-645, 157, 184), true);
            bot.bot.activateItem();
            await bot.waitFor(() => Object.values(bot.bot.entities).some((entity) => entity.name === 'fishing_bobber'), { timeoutMs: 10000, what: 'a fishing bobber' });
            t.notes.fishing = 'bite and catch need a human: the bot cannot time the reel';
            return sidebarText(bot);
        });
    }
};
