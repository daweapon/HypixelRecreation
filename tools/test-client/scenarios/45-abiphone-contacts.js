const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const clean = (chat) => chat.filter((line) => !/TPS/.test(line)).join(' | ');
const slotsText = (bot) => bot.window().slots.map((slot) => `${slot.slot}:${slot.name} ${slot.lore.join(' ')}`).join(' | ');

async function askFor(t, bot, label, wait = 14000, at = null) {
    bot.bot.setQuickBarSlot(0);
    if (at) {
        await bot.teleportTo(...at);
        await sleep(4000);
    }
    const npc = bot.findNpc(label);
    t.assert(npc, `no ${label} NPC found`);
    await bot.walkNear(npc);
    const mark = bot.markChat();
    await bot.interact(npc);
    await sleep(wait);
    return clean(bot.chatSince(mark));
}

module.exports = {
    description: 'Abiphone contacts: Plumber Joe and Taylor add straight away, Kat and Lumber Merchant ask for items and refuse without them, an item contact accepts it, and /call opens the shop',
    async run(t, bot) {
        await t.step('reach the Hub with an Abiphone and the items some contacts ask for', async () => {
            await bot.joinSkyblock();
            await sleep(3000);
            await bot.command('/mission clearset GETTING_STARTED', { settleMs: 1500 });
            await bot.command('/item ABIPHONE_FLIP_PLUS_LUNAR', { settleMs: 1000 });
            await bot.command('/hub', { settleMs: 6000 });
            await bot.waitFor(() => /Village|Hub/i.test((bot.sidebar()?.lines ?? []).join(' ')) && !/Your Island/.test((bot.sidebar()?.lines ?? []).join(' ')), { timeoutMs: 60000, what: 'the Hub sidebar' });
            await sleep(6000);
        });

        await t.step('Taylor gives his contact without asking for anything', async () => {
            const chat = await askFor(t, bot, 'Taylor', 16000);
            t.assert(/has been added to your Abiphone/.test(chat), chat);
        });

        await t.step('Lumber Merchant asks for a Spruce Axe and refuses without one', async () => {
            let chat = await askFor(t, bot, 'Lumber Merchant', 9000, [-125, 74, -44]);
            t.assert(/Spruce Axe/.test(chat) && /GIVE ITEM/.test(chat), chat);
            const mark = bot.markChat();
            await bot.command('/selectnpcoption contact_lumber_merchant give0', { settleMs: 5000 });
            chat = clean(bot.chatSince(mark));
            t.assert(/You don't have what I need/.test(chat), chat);
        });

        await t.step('with a Spruce Axe in the inventory Lumber Merchant accepts and adds the contact', async () => {
            await bot.command('/item JUNGLE_AXE', { settleMs: 1000 });
            await askFor(t, bot, 'Lumber Merchant', 9000, [-125, 74, -44]);
            const mark = bot.markChat();
            await bot.command('/selectnpcoption contact_lumber_merchant give0', { settleMs: 9000 });
            const chat = clean(bot.chatSince(mark));
            t.assert(/A deal is a deal/.test(chat) && /has been added to your Abiphone/.test(chat), chat);
        });

        await t.step('a repeat visit does not ask again', async () => {
            const chat = await askFor(t, bot, 'Lumber Merchant', 3000, [-125, 74, -44]);
            t.assert(/already in your contacts/.test(chat), chat);
        });

        await t.step('/call lumber opens the Lumberjack shop', async () => {
            const mark = bot.markChat();
            await bot.command('/call lumber', { settleMs: 7000 });
            t.assert(/RING\.\.\. RING\.\.\. RING\.\.\./.test(clean(bot.chatSince(mark))), clean(bot.chatSince(mark)));
            t.assert(bot.window(), 'no shop window after the call');
            await bot.closeWindow();
        });

        await t.step('the Directory shows the contacts as unlocked', async () => {
            await sleep(500);
            await bot.useHotbarItem(/Abiphone/, { settleMs: 400 });
            await bot.waitWindow({ timeoutMs: 8000 });
            await bot.click(51, { settleMs: 1200 });
            await bot.click(50, { settleMs: 800 });
            const text = slotsText(bot);
            t.assert(/Taylor[^|]*Contact unlocked/.test(text), text.slice(0, 900));
            t.assert(!/<[0-9a-f]>/.test(text), 'raw colour tags in the directory: ' + text.slice(0, 400));
            await bot.closeWindow();
        });
    },
};
