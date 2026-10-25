const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const clean = (chat) => chat.filter((line) => !/TPS/.test(line)).join(' | ');

module.exports = {
    description: 'Great Spook: Tyashoi Alchemist only exists while the event is on, introduces himself, opens The Great Spook Experiments and answers a missing ingredient with a hint and a lockout',
    async run(t, bot) {
        await t.step('reach the hub with a max profile', async () => {
            await bot.joinSkyblock();
            await bot.command('/max profile', { settleMs: 8000 });
            await bot.command('/mission clearset GETTING_STARTED', { settleMs: 1500 });
            await bot.command('/hub', { settleMs: 6000 });
            await bot.command('/adminbypass greatspook off', { settleMs: 3000 });
        });

        await t.step('Tyashoi is not there while the event is off', async () => {
            await bot.command('/tp 41 68 -52', { settleMs: 3000 });
            t.assert(!bot.findNpc('Tyashoi'), 'Tyashoi is visible with the event off');
        });

        await t.step('turning the event on makes him appear', async () => {
            await bot.command('/adminbypass greatspook on', { settleMs: 5000 });
            await bot.command('/tp 41 68 -52', { settleMs: 3000 });
            await bot.waitFor(() => bot.findNpc('Tyashoi'), { timeoutMs: 20000, what: 'Tyashoi' });
        });

        await t.step('first click plays the intro and opens the experiments menu', async () => {
            const npc = bot.findNpc('Tyashoi');
            await bot.walkNear(npc);
            const mark = bot.markChat();
            await bot.interact(npc);
            await sleep(2000);
            t.assert(/The Great Spook\.\.\./.test(clean(bot.chatSince(mark))), clean(bot.chatSince(mark)));
            const window = await bot.waitWindow({ timeoutMs: 40000, title: 'The Great Spook Experiments' });
            t.assert(window, 'the experiments menu did not open');
            t.assert(bot.findSlot(/Great Spook Staff/i), 'no Great Spook Staff in the menu');
            t.assert(bot.findSlot(/Eerie Pet/i), 'no Eerie Pet in the menu');
        });

        await t.step('trading without the ingredient gives a hint, a lockout and no item', async () => {
            const staff = bot.findSlot(/Great Spook Staff/i);
            const mark = bot.markChat();
            await bot.click(staff.slot, { settleMs: 1500 });
            await sleep(3000);
            const chat = clean(bot.chatSince(mark));
            t.assert(/Frozen Spiders/.test(chat), chat);
            t.assert(!bot.bot.inventory.items().some((item) => /great spook staff/i.test(JSON.stringify(item.customName ?? item.displayName))), 'the staff was given without the ingredient');
        });

        await t.step('with the ingredient the trade works once', async () => {
            await bot.command('/item FROZEN_SPIDER 10', { settleMs: 1500 });
            const npc = bot.findNpc('Tyashoi');
            await bot.interact(npc);
            await bot.waitWindow({ timeoutMs: 15000, title: 'The Great Spook Experiments' });
            const mark = bot.markChat();
            await bot.click(bot.findSlot(/Great Spook Staff/i).slot, { settleMs: 1500 });
            await sleep(3000);
            t.assert(/You bought .*Great Spook Staff/.test(clean(bot.chatSince(mark))), clean(bot.chatSince(mark)));
            await bot.command('/item FROZEN_SPIDER 10', { settleMs: 1500 });
            await bot.interact(npc);
            await bot.waitWindow({ timeoutMs: 15000, title: 'The Great Spook Experiments' });
            const again = bot.markChat();
            await bot.click(bot.findSlot(/Great Spook Staff/i).slot, { settleMs: 1500 });
            await sleep(1500);
            t.assert(/only trade for each item once/.test(clean(bot.chatSince(again))), clean(bot.chatSince(again)));
        });

        await t.step('turn the event off again', async () => {
            await bot.command('/adminbypass greatspook off', { settleMs: 2500 });
        });
    }
};
