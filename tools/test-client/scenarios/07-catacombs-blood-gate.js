const sidebarText = (bot) => (bot.sidebar()?.lines ?? []).join(' | ');

module.exports = {
    description: 'Catacombs 3a: entering the Blood Room without the Blood Key fails the run',
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => bot.joinSkyblock());

        await t.step('/max profile as staff', async () => {
            const lines = await bot.command('/max profile', { settleMs: 8000 });
            t.assertChat(lines, /MAXED/i, 'expected the MAXED message');
            return lines.slice(-2);
        });

        await t.step('go to the Hub', async () => {
            bot.say('/hub');
            await bot.waitFor(() => /Hub|Village|Elizabeth/i.test(sidebarText(bot)), { timeoutMs: 200000, what: 'the Hub sidebar' });
            return sidebarText(bot);
        });

        await t.step('Mort opens the Catacombs Gate', async () => {
            await bot.command('/tp -88 55 -124', { settleMs: 3000 });
            await bot.waitFor(() => bot.findNpc('Mort'), { timeoutMs: 20000, what: 'Mort' });
            const npc = bot.findNpc('Mort');
            await bot.walkNear(npc);
            await bot.interact(npc);
            const window = await bot.waitWindow({ timeoutMs: 20000 });
            const inert = await bot.detectInertMenu().catch(() => null);
            t.assert(/Catacombs Gate/i.test(JSON.stringify(window.title)), `unexpected window ${JSON.stringify(window.title)}`);
            return { title: window.title, inert };
        });

        await t.step('click Entrance and arrive in the Catacombs', async () => {
            await bot.click(11, { button: 0, mode: 0 });
            await bot.waitFor(() => /Catacombs|Dungeon/i.test(sidebarText(bot)), { timeoutMs: 240000, what: 'the Catacombs sidebar' });
            return sidebarText(bot);
        });

        await t.step('the run starts after ready-up', async () => {
            const mark = bot.markChat();
            bot.say('/catacombsrun ready');
            await bot.waitChat(/Dungeon entered/i, { since: mark, timeoutMs: 60000 });
        });

        await t.step('entering the Blood Room without its key fails the run', async () => {
            const layout = await bot.command('/catacombsrun layout', { settleMs: 2500 });
            const rows = layout.filter((_, index) => index % 2 === 0);
            let target = null;
            rows.forEach((line, row) => {
                for (let col = 0; col * 4 < line.length; col++) {
                    if (line.slice(col * 4, col * 4 + 1) === 'B') target = { col, row };
                }
            });
            t.assert(target, 'no Blood Room in the layout');
            const mark = bot.markChat();
            await bot.command(`/tp ${-200 + 32 * target.col + 15} 70 ${-200 + 32 * target.row + 15}`, { settleMs: 4000 });
            await bot.waitChat(/Blood Room without|Team Score/i, { since: mark, timeoutMs: 30000 });
            return bot.chatSince(mark).slice(-8);
        });
    },
};
