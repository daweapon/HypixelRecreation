const sidebarText = (bot) => (bot.sidebar()?.lines ?? []).join(' | ');

module.exports = {
    description: 'Catacombs 3a: Mort opens the Gate, queue Entrance as staff, run lifecycle on the placeholder arena, /catacombsrun win and score',
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
            return bot.chatSince(mark).slice(-6);
        });

        await t.step('layout, seed and score are reachable', async () => {
            const layout = await bot.command('/catacombsrun layout', { settleMs: 2500 });
            const score = await bot.command('/catacombsrun score', { settleMs: 2000 });
            t.assert(layout.length > 3, 'layout printed nothing');
            return { layout: layout.slice(0, 8), score };
        });

        await t.step('door next opens a key door and keys can be granted', async () => {
            const keys = await bot.command('/catacombsrun key blood', { settleMs: 1500 });
            const door = await bot.command('/catacombsrun door next', { settleMs: 1500 });
            return { keys, door };
        });

        await t.step('open every key door, enter the Blood Room: the Watcher appears, clearing it ends the Entrance with the Team Score summary', async () => {
            for (let i = 0; i < 8; i++) {
                const lines = await bot.command('/catacombsrun door next', { settleMs: 1200 });
                if (lines.some((line) => /No closed key door/i.test(line))) break;
            }
            const layout = await bot.command('/catacombsrun layout', { settleMs: 2500 });
            const rows = layout.filter((_, index) => index % 2 === 0);
            let target = null;
            rows.forEach((line, row) => {
                for (let col = 0; col * 4 < line.length; col++) {
                    if (line.slice(col * 4, col * 4 + 1) === 'B') target = { col, row };
                }
            });
            t.assert(target, `no Blood Room in the layout ${JSON.stringify(layout)}`);
            const mark = bot.markChat();
            await bot.command(`/tp ${-200 + 32 * target.col + 15} 70 ${-200 + 32 * target.row + 15}`, { settleMs: 6000 });
            await bot.waitChat(/Watcher/i, { since: mark, timeoutMs: 30000 });
            const afterIntro = bot.chatSince(mark);
            await bot.command('/catacombsrun clear', { settleMs: 4000 });
            await bot.waitChat(/made it through the Entrance/i, { since: mark, timeoutMs: 30000 });
            await bot.waitChat(/Team Score/i, { since: mark, timeoutMs: 30000 });
            return { target, chat: afterIntro.concat(bot.chatSince(mark)).slice(-8) };
        });

    },
};
