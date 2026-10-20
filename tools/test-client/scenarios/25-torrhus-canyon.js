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
    description: 'Torrhus Canyon: arrival, Helia objective, Helia chapters menu, Miria, Safari Tickets and Desert Whispers Toy Box buttons, Honeyhive loot and cooldown, Ruby Veilshroom gathering',
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

        await t.step('send the bot to Torrhus Canyon', async () => {
            await sleep(3000);
            bot.say('/sendto SKYBLOCK_TORRHUS_CANYON');
            await bot.waitFor(() => /Torrhus/i.test(sidebarText(bot)), { timeoutMs: 60000, what: 'the Torrhus sidebar' }).catch(async () => {
                bot.say('/sendto SKYBLOCK_TORRHUS_CANYON');
                await bot.waitFor(() => /Torrhus/i.test(sidebarText(bot)), { timeoutMs: 90000, what: 'the Torrhus sidebar' });
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
    }
};
