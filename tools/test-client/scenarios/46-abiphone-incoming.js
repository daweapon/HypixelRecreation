const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const clean = (chat) => chat.filter((line) => !/TPS/.test(line)).join(' | ');
const slot = (bot, n) => bot.window().slots.find((s) => s.slot === n);

module.exports = {
    description: 'Abiphone incoming calls: /force call rings with the header, three RING lines and PICK UP, picking up opens the contact, Do Not Disturb silences the call',
    async run(t, bot) {
        await t.step('reach the Hub with a Flip phone and add Taylor', async () => {
            await bot.joinSkyblock();
            await sleep(3000);
            await bot.command('/mission clearset GETTING_STARTED', { settleMs: 1500 });
            await bot.command('/item ABIPHONE_FLIP_PLUS_LUNAR', { settleMs: 1000 });
            await bot.command('/hub', { settleMs: 6000 });
            await bot.waitFor(() => /Village|Hub/i.test((bot.sidebar()?.lines ?? []).join(' ')) && !/Your Island/.test((bot.sidebar()?.lines ?? []).join(' ')), { timeoutMs: 60000, what: 'the Hub sidebar' });
            await sleep(6000);
            bot.bot.setQuickBarSlot(0);
            const npc = bot.findNpc('Taylor');
            t.assert(npc, 'no Taylor NPC');
            await bot.walkNear(npc);
            const mark = bot.markChat();
            await bot.interact(npc);
            await sleep(16000);
            t.assert(/has been added to your Abiphone/.test(clean(bot.chatSince(mark))), clean(bot.chatSince(mark)));
        });

        await t.step('an incoming call shows the header, three ring lines and a PICK UP button', async () => {
            const mark = bot.markChat();
            await bot.command('/force call taylor', { settleMs: 5000 });
            const chat = clean(bot.chatSince(mark));
            t.assert(/✆ Taylor ✆/.test(chat), chat);
            t.assert(/✆ RING\.\.\. \[PICK UP\]/.test(chat) && /✆ RING\.\.\. RING\.\.\. RING\.\.\. \[PICK UP\]/.test(chat), chat);
        });

        await t.step('picking up opens Taylor, and the call cannot be picked up twice', async () => {
            await bot.command('/abiphonepickup taylor', { settleMs: 3000 });
            t.assert(bot.window(), 'no window after picking up');
            await bot.closeWindow();
            const mark = bot.markChat();
            await bot.command('/abiphonepickup taylor', { settleMs: 1500 });
            t.assert(/already ended/.test(clean(bot.chatSince(mark))), clean(bot.chatSince(mark)));
        });

        await t.step('Do Not Disturb on Taylor silences the call', async () => {
            await sleep(500);
            await bot.useHotbarItem(/Abiphone/, { settleMs: 400 });
            await bot.waitWindow({ timeoutMs: 8000 });
            await bot.click(10, { button: 1, settleMs: 1200 });
            await bot.click(29, { settleMs: 800 });
            t.assert(/Enabled/.test(slot(bot, 29).lore.join(' ')), slot(bot, 29).lore.join(' '));
            await bot.closeWindow();
            await sleep(16000);
            const mark = bot.markChat();
            await bot.command('/force call taylor', { settleMs: 2000 });
            t.assert(/SILENCED/.test(clean(bot.chatSince(mark))), clean(bot.chatSince(mark)));
        });

        await t.step('a contact that is not on a phone cannot ring', async () => {
            const mark = bot.markChat();
            await bot.command('/force call alda', { settleMs: 1500 });
            t.assert(/NO_CONTACT/.test(clean(bot.chatSince(mark))), clean(bot.chatSince(mark)));
        });
    },
};
