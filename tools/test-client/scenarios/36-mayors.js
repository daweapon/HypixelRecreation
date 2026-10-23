const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const names = (bot) => bot.window().slots.map((slot) => `${slot.name} ${slot.lore.join(' ')}`).join(' | ');

module.exports = {
    description: 'One-time mayors: Dante vendor and Goons, Technoblade Shrine, relics, Technoshop, perks',
    async run(t, bot) {
        await t.step('reach the Hub', async () => {
            await bot.joinSkyblock();
            await bot.command('/mission clearset GETTING_STARTED', { settleMs: 1500 });
            await bot.command('/set coins 50000000', { settleMs: 800 });
            await sleep(3000);
        });

        await t.step('forcing Dante installs him and his perks', async () => {
            const mark = bot.markChat();
            await bot.command('/adminbypass forcemayor DANTE', { settleMs: 2500 });
            await bot.waitChat(/Force-elected mayor/, { since: mark, timeoutMs: 15000 });
            return bot.chatSince(mark).slice(0, 4);
        });

        await t.step('Dante sells the Happy Mask and Talisman', async () => {
            await bot.command('/tp 5 79 18', { settleMs: 3000 });
            await bot.waitFor(() => bot.findNpc('Dante'), { timeoutMs: 25000, what: 'Dante' });
            await bot.interact(bot.findNpc('Dante'));
            await bot.waitFor(() => bot.window(), { timeoutMs: 10000, what: 'the Dante shop' });
            const text = names(bot);
            t.assert(/Happy Mask/.test(text) && /Talisman/.test(text), text);
            bot.closeWindow?.();
        });

        await t.step('a Goon guards the Bank', async () => {
            await bot.command('/tp -22 73 -40', { settleMs: 3000 });
            await bot.waitFor(() => bot.findNpc('Goon'), { timeoutMs: 25000, what: 'a Goon' });
        });

        await t.step('the economy perk charges the chat fee', async () => {
            const mark = bot.markChat();
            bot.say('hello world');
            await bot.waitChat(/chat fee|costs/i, { since: mark, timeoutMs: 10000 });
        });

        await t.step('forcing Technoblade stands up the Shrine and Technoshop', async () => {
            const mark = bot.markChat();
            await bot.command('/adminbypass forcemayor TECHNOBLADE', { settleMs: 2500 });
            await bot.waitChat(/Force-elected mayor/, { since: mark, timeoutMs: 15000 });
            await bot.command('/tp 15 71 -8', { settleMs: 3500 });
            await bot.waitFor(() => bot.findNpc('Technoshop'), { timeoutMs: 25000, what: 'the Technoshop pig' });
            await bot.interact(bot.findNpc('Technoshop'));
            await bot.waitFor(() => bot.window(), { timeoutMs: 10000, what: 'the Technoshop' });
            const text = names(bot);
            t.assert(/Pig Mask/.test(text) && /Blood God Crest/.test(text) && /Art of War/.test(text), text);
            bot.closeWindow?.();
        });

        await t.step('finding every relic calls the Ancient Pig', async () => {
            const mark = bot.markChat();
            await bot.command('/force shrine relic all', { settleMs: 3000 });
            await bot.waitFor(() => bot.findNpc('Ancient Pig'), { timeoutMs: 25000, what: 'the Ancient Pig' });
            await bot.interact(bot.findNpc('Ancient Pig'));
            await sleep(2500);
            return bot.chatSince(mark).slice(0, 8);
        });

        await t.step('a global shrine effect can be rolled', async () => {
            const mark = bot.markChat();
            await bot.command('/force shrine effect thunder', { settleMs: 2500 });
            return bot.chatSince(mark).slice(0, 4);
        });

        await t.step('Dante can start the Uprising', async () => {
            const mark = bot.markChat();
            await bot.command('/uprising start', { settleMs: 4000 });
            const lines = bot.chatSince(mark).slice(0, 6);
            await bot.command('/uprising stop', { settleMs: 1500 });
            return lines;
        });

        await t.step('back to a regular mayor', async () => {
            await bot.command('/adminbypass forcemayor DIANA', { settleMs: 2000 });
            await bot.command('/force shrine reset', { settleMs: 1000 });
        });
    },
};
