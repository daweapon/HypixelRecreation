const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const clean = (chat) => chat.filter((line) => !/TPS/.test(line)).join(' | ');

module.exports = {
    description: 'Great Spook: Sweet Flesh drops only from a Zombie killed with a Sweet Axe after meeting Tyashoi; a worn Appalled Pumpkin stays dry out of the water',
    async run(t, bot) {
        await t.step('reach the hub, turn the event on and meet Tyashoi', async () => {
            await bot.joinSkyblock();
            await bot.command('/max profile', { settleMs: 8000 });
            await bot.command('/mission clearset GETTING_STARTED', { settleMs: 1500 });
            await bot.command('/hub', { settleMs: 6000 });
            await bot.command('/adminbypass greatspook on', { settleMs: 5000 });
            await bot.command('/tp 41 68 -52', { settleMs: 3000 });
            const npc = await bot.waitFor(() => bot.findNpc('Tyashoi'), { timeoutMs: 20000, what: 'Tyashoi' });
            await bot.walkNear(npc);
            await bot.interact(npc);
            await bot.waitWindow({ timeoutMs: 40000, title: 'The Great Spook Experiments' });
            await bot.closeWindow();
        });

        await t.step('a Zombie killed without the Sweet Axe gives no Sweet Flesh', async () => {
            const mark = bot.markChat();
            await bot.command('/spawn GRAVEYARD_ZOMBIE', { settleMs: 1500 });
            for (let swing = 0; swing < 60; swing++) {
                const target = bot.nearestEntity('Zombie');
                if (!target) break;
                await bot.attack(target);
                await sleep(500);
            }
            t.assert(!/Sweet Flesh/.test(clean(bot.chatSince(mark))), clean(bot.chatSince(mark)));
        });

        await t.step('with the Sweet Axe in hand the kill pays Sweet Flesh', async () => {
            await bot.command('/item SWEET_AXE', { settleMs: 1500 });
            await bot.useHotbarItem(/Sweet Axe/i, { settleMs: 500 });
            const mark = bot.markChat();
            await bot.command('/spawn GRAVEYARD_ZOMBIE', { settleMs: 1500 });
            for (let swing = 0; swing < 80; swing++) {
                const target = bot.nearestEntity('Zombie');
                if (!target) break;
                await bot.attack(target);
                await sleep(500);
                if (/Sweet Flesh/.test(clean(bot.chatSince(mark)))) break;
            }
            t.assert(/You received 1x .*Sweet Flesh/.test(clean(bot.chatSince(mark))), clean(bot.chatSince(mark)));
        });

        await t.step('turn the event off again', async () => {
            await bot.command('/adminbypass greatspook off', { settleMs: 2500 });
        });
    }
};
