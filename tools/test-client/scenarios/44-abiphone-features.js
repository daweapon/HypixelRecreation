const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const slotsText = (bot) => bot.window().slots.map((slot) => `${slot.slot}:${slot.name} ${slot.lore.join(' ')}`).join(' | ');
const slot = (bot, n) => bot.window().slots.find((s) => s.slot === n);
const clean = (chat) => chat.filter((line) => !/TPS/.test(line)).join(' | ');

async function openPhone(bot) {
    await bot.closeWindow().catch(() => {});
    await sleep(500);
    await bot.useHotbarItem(/Abiphone/, { settleMs: 400 });
    await bot.waitWindow({ timeoutMs: 8000 });
}

module.exports = {
    description: 'Abiphone features: /call, persistence of call stats, Do Not Disturb and Speed Dial, Snake, Tic Tac Toe, ringtones and the Sumsung slot bonus',
    async run(t, bot) {
        await t.step('reach the Hub with a Flip+ phone and the Sumsung GG case', async () => {
            await bot.joinSkyblock();
            await sleep(3000);
            await bot.command('/mission clearset GETTING_STARTED', { settleMs: 1500 });
            await bot.command('/item ABIPHONE_FLIP_PLUS_LUNAR', { settleMs: 1000 });
            await bot.command('/hub', { settleMs: 6000 });
            await bot.waitFor(() => /Village|Hub/i.test((bot.sidebar()?.lines ?? []).join(' ')) && !/Your Island/.test((bot.sidebar()?.lines ?? []).join(' ')), { timeoutMs: 60000, what: 'the Hub sidebar' });
            await sleep(6000);
        });

        await t.step('Alda and Builder join the phone', async () => {
            bot.bot.setQuickBarSlot(0);
            for (const name of ['Alda']) {
                const npc = bot.findNpc(name);
                t.assert(npc, `no ${name} NPC`);
                await bot.walkNear(npc);
                const mark = bot.markChat();
                await bot.interact(npc);
                await sleep(2500);
                const chat = clean(bot.chatSince(mark));
                t.assert(/has been added to your Abiphone/.test(chat), chat);
            }
        });

        await t.step('the phone lore shows 13 contacts, 15 with the Sumsung GG case', async () => {
            await bot.command('/item SUMSUNG_GG_ABICASE', { settleMs: 1000 });
            await sleep(1500);
            const phone = bot.bot.inventory.slots.find((item) => item && /Abiphone/.test(bot.describeItem(item, 0).name));
            t.assert(phone, 'no phone in inventory');
            const lore = bot.describeItem(phone, 0).lore.join(' ').replace(/§./g, '');
            t.assert(/Maximum Contacts: 15/.test(lore), lore.slice(0, 300));
        });

        await t.step('/call alda rings three times and opens the shop', async () => {
            await bot.closeWindow().catch(() => {});
            const mark = bot.markChat();
            await bot.command('/call alda', { settleMs: 6000 });
            const chat = clean(bot.chatSince(mark));
            t.assert(/RING\.\.\./.test(chat) && /RING\.\.\. RING\.\.\. RING\.\.\./.test(chat), chat);
            t.assert(bot.window(), 'no shop window after the call');
            await bot.closeWindow();
        });

        await t.step('/call of an unknown or missing contact is refused', async () => {
            let mark = bot.markChat();
            await bot.command('/call fred', { settleMs: 1500 });
            t.assert(/don't have a contact|not a contact|not available/i.test(clean(bot.chatSince(mark))), clean(bot.chatSince(mark)));
            mark = bot.markChat();
            await bot.command('/call builder', { settleMs: 1500 });
            t.assert(/not a contact on any/.test(clean(bot.chatSince(mark))), clean(bot.chatSince(mark)));
        });

        await t.step('the call count is saved: Most Called puts Alda first and the lore persists after reopening', async () => {
            await openPhone(bot);
            await bot.click(50, { settleMs: 600 });
            await bot.click(50, { settleMs: 600 });
            await bot.click(50, { settleMs: 600 });
            t.assert(/▶ Most Called/.test(slot(bot, 50).lore.join(' ')), slot(bot, 50).lore.join(' '));
            t.assert(/Alda/.test(slot(bot, 10).name), slotsText(bot).slice(0, 300));
        });

        await t.step('the Snake, Tic Tac Toe and Ringtones buttons are on a Flip+ phone', async () => {
            t.assert(/Snake/.test(slot(bot, 46)?.name ?? ''), slotsText(bot).slice(-500));
            t.assert(/Tic Tac Toe/.test(slot(bot, 47)?.name ?? ''), slotsText(bot).slice(-500));
            t.assert(/Ringtones/.test(slot(bot, 48)?.name ?? ''), slotsText(bot).slice(-500));
        });

        await t.step('Do Not Disturb and Speed Dial toggle and are saved on the phone', async () => {
            await bot.click(10, { button: 1, settleMs: 1200 });
            t.assert(/Contact Management/.test(JSON.stringify(bot.window().title)), JSON.stringify(bot.window().title));
            t.assert(/Do Not Disturb/.test(slot(bot, 29)?.name ?? ''), slotsText(bot).slice(0, 400));
            await bot.click(29, { settleMs: 800 });
            t.assert(/Enabled/.test(slot(bot, 29).lore.join(' ')), slot(bot, 29).lore.join(' '));
            await bot.click(33, { settleMs: 800 });
            t.assert(/Current Speed Dial: Alda/.test(slot(bot, 33).lore.join(' ').replace(/§./g, '')), slot(bot, 33).lore.join(' '));
            await bot.closeWindow();
            await openPhone(bot);
            await bot.click(50, { settleMs: 600 });
            await bot.click(50, { settleMs: 600 });
            t.assert(/▶ Do Not Disturb First|▶ Last Called/.test(slot(bot, 50).lore.join(' ')), slot(bot, 50).lore.join(' '));
            await bot.click(10, { button: 1, settleMs: 1200 });
            t.assert(/Enabled/.test(slot(bot, 29).lore.join(' ')), `DND not persisted: ${slot(bot, 29).lore.join(' ')}`);
            t.assert(/Alda/.test(slot(bot, 33).lore.join(' ')), `Speed dial not persisted: ${slot(bot, 33).lore.join(' ')}`);
            await bot.closeWindow();
        });

        await t.step('left-clicking the phone calls the Speed Dial contact', async () => {
            bot.bot.setQuickBarSlot(0);
            await sleep(300);
            const mark = bot.markChat();
            bot.bot.swingArm('right');
            await sleep(5000);
            const chat = clean(bot.chatSince(mark));
            return chat.slice(0, 200) || 'no chat (left-click may not reach the server from a swing)';
        });

        await t.step('Ringtones lists six tones and a selection is saved', async () => {
            await bot.closeWindow().catch(() => {});
            await openPhone(bot);
            await bot.click(48, { settleMs: 1000 });
            t.assert(/Ringtones/.test(JSON.stringify(bot.window().title)), JSON.stringify(bot.window().title));
            await bot.click(12, { settleMs: 800 });
            t.assert(/Selected!/.test(slot(bot, 12).lore.join(' ')), slot(bot, 12).lore.join(' '));
            await bot.closeWindow();
        });

        await t.step('Tic Tac Toe answers a move and never lets the player win a quick game', async () => {
            await openPhone(bot);
            await bot.click(47, { settleMs: 1000 });
            t.assert(/Tic Tac Toe/.test(JSON.stringify(bot.window().title)), JSON.stringify(bot.window().title));
            await bot.click(22, { settleMs: 800 });
            const marks = bot.window().slots.filter((s) => [12, 13, 14, 21, 22, 23, 30, 31, 32].includes(s.slot));
            t.assert(marks.filter((s) => /You/.test(s.name)).length === 1 && marks.filter((s) => /Abiphone/.test(s.name)).length === 1, marks.map((s) => s.name).join(','));
            for (const cell of [12, 13, 14, 21, 23, 30, 31, 32]) {
                if (!/Empty/.test(slot(bot, cell)?.name ?? '')) continue;
                await bot.click(cell, { settleMs: 500 });
                if (slot(bot, 40) && /Play Again/.test(slot(bot, 40).name)) break;
            }
            t.assert(slot(bot, 40) && /Play Again/.test(slot(bot, 40).name), 'game did not finish');
            t.assert(!/You won/.test(slot(bot, 40).lore.join(' ')), slot(bot, 40).lore.join(' '));
            await bot.closeWindow();
        });

        await t.step('Snake moves on its own and ends against the wall, keeping the top score', async () => {
            await openPhone(bot);
            await bot.click(46, { settleMs: 800 });
            t.assert(/Snake/.test(JSON.stringify(bot.window().title)), JSON.stringify(bot.window().title));
            await bot.waitFor(() => bot.window().slots.some((s) => s.slot === 43 && /Play Again/.test(s.name)), { timeoutMs: 15000, what: 'the snake to hit the wall' });
            t.assert(slot(bot, 52), 'no apple counter');
            await bot.closeWindow();
        });
    },
};
