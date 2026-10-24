const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const slotsText = (bot) => bot.window().slots.map((slot) => `${slot.slot}:${slot.name} ${slot.lore.join(' ')}`).join(' | ');
const grid = (bot) => bot.window().slots.filter((slot) => slot.slot >= 10 && slot.slot <= 43 && slot.slot % 9 >= 1 && slot.slot % 9 <= 7 && slot.name && !/glass|pane/i.test(slot.name));
const names = (bot) => grid(bot).map((slot) => slot.name.replace(/§./g, ''));

module.exports = {
    description: 'Abiphone: the Contacts Directory lists the 85 contacts with sort, filter and search, the phone sort button cycles, Do Not Disturb toggles',
    async run(t, bot) {
        await t.step('reach SkyBlock and get a Flip phone', async () => {
            await bot.joinSkyblock();
            await sleep(3000);
            await bot.command('/item ABIPHONE_FLIP_DRAGON', { settleMs: 1200 });
        });

        await t.step('the phone opens and shows the sort and directory buttons', async () => {
            await bot.useHotbarItem(/Abiphone/, { settleMs: 400 });
            await bot.waitWindow({ timeoutMs: 8000 });
            const text = slotsText(bot);
            t.assert(/50:.*Sort/.test(text) && /51:.*Contacts Directory/.test(text), text.slice(0, 600));
            t.assert(!/TO-DO/.test(text), 'TO-DO label still present');
            return text.slice(0, 400);
        });

        await t.step('the sort button cycles forwards and backwards', async () => {
            const lore = () => bot.window().slots.find((slot) => slot.slot === 50).lore.join(' ');
            t.assert(/▶ First Added/.test(lore()), lore());
            await bot.click(50, { settleMs: 800 });
            t.assert(/▶ Alphabetical/.test(lore()), lore());
            await bot.click(50, { button: 1, settleMs: 800 });
            await bot.click(50, { button: 1, settleMs: 800 });
            t.assert(/▶ Do Not Disturb First/.test(lore()), lore());
        });

        await t.step('the directory opens with the first 28 contacts A to Z', async () => {
            await bot.click(51, { settleMs: 1200 });
            t.assert(/Contacts Directory/.test(JSON.stringify(bot.window().title)), JSON.stringify(bot.window().title));
            const first = names(bot);
            t.assert(first.length === 28, `${first.length} entries`);
            t.assert(/Agatha/.test(first[0]), first.slice(0, 3).join(','));
            const entry = grid(bot)[0];
            t.assert(/Difficulty/.test(entry.lore.join(' ')) && /Usual location/.test(entry.lore.join(' ')), entry.lore.join(' '));
            return first.slice(0, 6);
        });

        await t.step('sort Z to A puts Zog first and Difficulty puts Easy first', async () => {
            await bot.click(50, { settleMs: 800 });
            t.assert(/Zog/.test(names(bot)[0]), names(bot)[0]);
            await bot.click(50, { settleMs: 800 });
            t.assert(/Easy/.test(grid(bot)[0].lore.join(' ')), grid(bot)[0].lore.join(' '));
        });

        await t.step('the Not Added filter keeps contacts, Completed Quest but Not Added is empty for a new player', async () => {
            await bot.click(51, { settleMs: 800 });
            t.assert(names(bot).length > 0, 'Not Added empty');
            await bot.click(51, { settleMs: 800 });
            t.assert(names(bot).length === 0, names(bot).join(','));
            await bot.click(51, { settleMs: 800 });
            t.assert(names(bot).length === 28, 'All Contacts restored');
        });

        await t.step('pagination reaches the 85th contact on page 4', async () => {
            await bot.click(53, { settleMs: 800 });
            await bot.click(53, { settleMs: 800 });
            await bot.click(53, { settleMs: 800 });
            t.assert(names(bot).length === 1, `${names(bot).length} entries on page 4`);
            await bot.closeWindow();
        });
    },
};
