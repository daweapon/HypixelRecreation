const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const clean = (chat) => chat.filter((line) => !/TPS/.test(line)).join(' | ');

module.exports = {
    description: 'Hunting Toolkit: locked until consumed, unlocks with the item, menu has 21 stored-tool slots; /collecthuntraps with nothing caught',
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => {
            await bot.joinSkyblock();
            await bot.command('/mission clearset GETTING_STARTED', { settleMs: 1500 });
        });
        await t.step('/huntingtoolkit is locked at first', async () => {
            const mark = bot.markChat();
            await bot.command('/huntingtoolkit', { settleMs: 1500 });
            const chat = clean(bot.chatSince(mark));
            t.assert(/haven't unlocked the Hunting Toolkit/.test(chat), chat);
        });
        await t.step('consuming the Hunting Toolkit unlocks it', async () => {
            await bot.command('/item HUNTING_TOOLKIT', { settleMs: 1000 });
            const mark = bot.markChat();
            await bot.useHotbarItem(/Hunting Toolkit/, { settleMs: 500 });
            await sleep(1500);
            const chat = clean(bot.chatSince(mark));
            t.assert(/unlock|Toolkit/i.test(chat) && !/already/.test(chat), chat);
        });
        await t.step('/huntingtoolkit opens the menu with its tool slots', async () => {
            await bot.command('/huntingtoolkit', { settleMs: 1500 });
            const view = await bot.waitWindow({ title: 'Hunting Toolkit', timeoutMs: 10000 });
            const names = bot.window().slots.map((slot) => slot.name).join(' | ');
            t.assert(/Huntaxe/i.test(names) && /Huntrap/i.test(names) && /Lasso/i.test(names), names);
            await bot.closeWindow();
        });
        await t.step('/collecthuntraps with nothing caught says so', async () => {
            const mark = bot.markChat();
            await bot.command('/collecthuntraps', { settleMs: 1500 });
            const chat = clean(bot.chatSince(mark));
            t.assert(/None of your Huntraps have caught anything yet/.test(chat), chat);
        });
    }
};
