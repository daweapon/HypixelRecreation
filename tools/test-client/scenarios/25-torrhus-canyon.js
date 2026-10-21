const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const sidebarText = (bot) => (bot.sidebar()?.lines ?? []).join(' | ');
const titleText = (window) => JSON.stringify(window.title);
const names = (bot) => bot.window().slots.map((slot) => `${slot.name} ${slot.lore.join(' ')}`).join(' | ');

const openToyBoxCategory = async (t, bot, pattern, titlePattern) => {
    bot.say('/centauri');
    await bot.waitWindow({ timeoutMs: 20000 });
    await bot.click(15, { button: 0, mode: 0 });
    await bot.waitFor(() => /Toy Box/.test(titleText(bot.window())), { timeoutMs: 10000, what: 'the Toy Box window' });
    const category = bot.findSlot(pattern);
    t.assert(category, `Toy Box category ${pattern} missing: ${names(bot)}`);
    await bot.click(bot.window().slots.indexOf(category), { button: 0, mode: 0 });
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
            await bot.click(bot.window().slots.indexOf(unlock), { button: 0, mode: 0 });
            await bot.closeWindow();
        });

        await t.step('Toy Box Safari Tickets button hands over the four tickets', async () => {
            await openToyBoxCategory(t, bot, /Currencies and Essence/, /Toy Box - Currencies/);
            const tickets = bot.findSlot(/Safari Tickets/);
            t.assert(tickets, names(bot));
            await bot.click(bot.window().slots.indexOf(tickets), { button: 0, mode: 0 });
            await sleep(800);
            const desert = bot.findSlot(/Desert Whispers/);
            t.assert(desert, names(bot));
            await bot.click(bot.window().slots.indexOf(desert), { button: 0, mode: 0 });
            await sleep(800);
            await bot.closeWindow();
            const held = bot.bot.inventory.items().map((item) => item.displayName ?? item.name).join(' | ');
            t.assert(/Safari Ticket/i.test(held), held);
            return held;
        });

        await t.step('send the bot to Galatea', async () => {
            await sleep(3000);
            bot.say('/sendto SKYBLOCK_GALATEA');
            await bot.waitFor(() => /Galatea|Tangleburg|Marsh/i.test(sidebarText(bot)), { timeoutMs: 60000, what: 'the Galatea sidebar' }).catch(async () => {
                bot.say('/sendto SKYBLOCK_GALATEA');
                await bot.waitFor(() => /Galatea|Tangleburg|Marsh/i.test(sidebarText(bot)), { timeoutMs: 90000, what: 'the Galatea sidebar' });
            });
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

        await t.step('the Moonglade\'s Edge launch pad sends the bot to Torrhus Canyon', async () => {
            await bot.command('/tp -728 99 89', { settleMs: 1000 });
            await bot.waitFor(() => /Torrhus/i.test(sidebarText(bot)), { timeoutMs: 90000, what: 'the Torrhus sidebar' }).catch(async () => {
                bot.say('/sendto SKYBLOCK_TORRHUS_CANYON');
                await bot.waitFor(() => /Torrhus/i.test(sidebarText(bot)), { timeoutMs: 90000, what: 'the Torrhus sidebar' });
                t.assert(false, 'the launch pad did not move the bot; /sendto had to be used');
            });
            return sidebarText(bot);
        });

        await t.step('the sidebar asks the bot to talk to Helia', async () => {
            await bot.waitFor(() => /Talk to Helia/.test(sidebarText(bot)), { timeoutMs: 20000, what: 'the Helia objective' });
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

        await t.step('fishing in the Springs catches Torrhus sea creatures or treasure', async () => {
            await bot.command('/max profile', { settleMs: 4000 });
            await bot.command('/item FISHING_ROD', { settleMs: 1500 });
            await bot.command('/tp -643 157 186', { settleMs: 3000 });
            const mark = bot.markChat();
            const caught = /Haggard|Brineling|Sprawl|Torrid|Silkbreeze|Giant Isopod|CATCH!|You found/;
            for (let attempt = 0; attempt < 12 && !caught.test(bot.chatSince(mark).join(' ')); attempt++) {
                await bot.useHotbarItem(/Fishing Rod/, { settleMs: 8000 });
            }
            const lines = bot.chatSince(mark);
            t.assert(caught.test(lines.join(' ')), lines.join(' | '));
            return lines;
        });
    }
};
