const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const clean = (chat) => chat.filter((line) => !/TPS/.test(line)).join(' | ');

module.exports = {
    description: 'Great Spook: Sirius hands over the riddle book only during the event and only with exactly 7,871,265 coins in the purse',
    async run(t, bot) {
        await t.step('reach the hub with the event off', async () => {
            await bot.joinSkyblock();
            await bot.command('/mission clearset GETTING_STARTED', { settleMs: 1500 });
            await bot.command('/hub', { settleMs: 6000 });
            await bot.command('/adminbypass greatspook off', { settleMs: 3000 });
        });

        await t.step('the event is on and the purse is exact', async () => {
            await bot.command('/adminbypass greatspook on', { settleMs: 5000 });
            await bot.command('/set coins 7871265', { settleMs: 2000 });
            await bot.command('/tp 91 75 179', { settleMs: 3000 });
            await bot.waitFor(() => bot.findNpc('Sirius'), { timeoutMs: 20000, what: 'Sirius' });
        });

        await t.step('Sirius offers the book and the option gives it', async () => {
            const npc = bot.findNpc('Sirius');
            await bot.walkNear(npc);
            const mark = bot.markChat();
            await bot.interact(npc);
            await sleep(9000);
            const chat = clean(bot.chatSince(mark));
            t.assert(/exact amount of coins/.test(chat), chat);
            t.assert(/Yes of course/.test(chat), chat);
            const accept = bot.markChat();
            await bot.command('/selectnpcoption sirius yes', { settleMs: 8000 });
            t.assert(/riddles, illuminating unseen stars/.test(clean(bot.chatSince(accept))), clean(bot.chatSince(accept)));
        });

        await t.step('a different purse gets no riddle', async () => {
            await bot.command('/set coins 7871264', { settleMs: 2000 });
            const npc = bot.findNpc('Sirius');
            const mark = bot.markChat();
            await bot.interact(npc);
            await sleep(4000);
            t.assert(!/exact amount of coins/.test(clean(bot.chatSince(mark))), clean(bot.chatSince(mark)));
        });

        await t.step('turn the event off again', async () => {
            await bot.command('/adminbypass greatspook off', { settleMs: 2500 });
        });
    }
};
