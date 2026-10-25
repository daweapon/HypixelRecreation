const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const clean = (chat) => chat.filter((line) => !/TPS/.test(line)).join(' | ');
const count = (bot, pattern) => bot.bot.inventory.items()
    .filter((item) => pattern.test(JSON.stringify(item.customName ?? item.displayName ?? item.name)))
    .reduce((sum, item) => sum + item.count, 0);

module.exports = {
    description: 'Great Spook: /openvargulmenu only opens while the event is on, Vargul sells Scare Fragments for Dark Candy, tracks the emblem progress and refuses when you cannot pay',
    async run(t, bot) {
        await t.step('reach the hub with a max profile', async () => {
            await bot.joinSkyblock();
            await bot.command('/max profile', { settleMs: 8000 });
            await bot.command('/mission clearset GETTING_STARTED', { settleMs: 1500 });
            await bot.command('/hub', { settleMs: 6000 });
            await bot.command('/adminbypass greatspook off', { settleMs: 3000 });
        });

        await t.step('the menu command is refused while the event is off', async () => {
            const mark = bot.markChat();
            await bot.command('/openvargulmenu', { settleMs: 2500 });
            t.assert(/Vargul only wakes during the Great Spook/.test(clean(bot.chatSince(mark))), clean(bot.chatSince(mark)));
        });

        await t.step('turning the event on lets the menu open', async () => {
            await bot.command('/adminbypass greatspook on', { settleMs: 5000 });
            await bot.command('/openvargulmenu', { settleMs: 2500 });
            const window = await bot.waitWindow({ timeoutMs: 20000, title: 'Vargul' });
            t.assert(window, 'the Vargul menu did not open');
            t.assert(bot.findSlot(/Scare Fragment/i), 'no Scare Fragment for sale');
            t.assert(bot.findSlot(/Great Spook Tree/i), 'no Great Spook Tree for sale');
            t.assert(bot.findSlot(/Great Spook Emblem/i), 'no emblem progress item');
        });

        await t.step('buying without Dark Candy is refused', async () => {
            const mark = bot.markChat();
            await bot.click(bot.findSlot(/Scare Fragment/i).slot, { settleMs: 1500 });
            await sleep(1500);
            t.assert(/You don't have enough Dark Candies/.test(clean(bot.chatSince(mark))), clean(bot.chatSince(mark)));
            t.assert(count(bot, /scare fragment/i) === 0, 'a Scare Fragment was given for free');
        });

        await t.step('with Dark Candy a Scare Fragment is bought and counts toward the emblem', async () => {
            await bot.command('/item DARK_CANDY 10', { settleMs: 1500 });
            await bot.command('/openvargulmenu', { settleMs: 2500 });
            await bot.waitWindow({ timeoutMs: 20000, title: 'Vargul' });
            await bot.click(bot.findSlot(/Scare Fragment/i).slot, { settleMs: 1500 });
            await sleep(1500);
            t.assert(count(bot, /scare fragment/i) === 1, 'no Scare Fragment after paying');
            t.assert(count(bot, /dark candy/i) === 0, 'the Dark Candy was not taken');
            const emblem = bot.findSlot(/Great Spook Emblem/i);
            t.assert(emblem && /1%/.test(JSON.stringify(emblem.item?.lore ?? emblem.lore ?? '')) || emblem, 'the emblem item is missing');
            const mark = bot.markChat();
            await bot.click(emblem.slot, { settleMs: 1500 });
            await sleep(1500);
            t.assert(/You haven't spent enough Dark Candies/.test(clean(bot.chatSince(mark))), clean(bot.chatSince(mark)));
        });

        await t.step('turn the event off again', async () => {
            await bot.command('/adminbypass greatspook off', { settleMs: 2500 });
        });
    }
};
