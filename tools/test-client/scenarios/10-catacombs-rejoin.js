const sidebarText = (bot) => (bot.sidebar()?.lines ?? []).join(' | ');

module.exports = {
    description: 'Catacombs: disconnecting mid-run and rejoining SkyBlock sends the player back into the run',
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
            await new Promise((resolve) => setTimeout(resolve, 5000));
            const mark = bot.markChat();
            bot.say('/catacombsrun ready');
            await bot.waitChat(/Dungeon entered/i, { since: mark, timeoutMs: 150000 });
            return bot.chatSince(mark).slice(-6);
        });

        await t.step('disconnect and rejoin SkyBlock', async () => {
            bot.bot.end();
            await new Promise((resolve) => setTimeout(resolve, 8000));
            await bot.connect();
            await bot.joinSkyblock().catch(() => null);
            await bot.waitFor(() => /Crypts|Dungeon Cleared|Catacombs/i.test(sidebarText(bot)) && !/Your Island|Village/i.test(sidebarText(bot)), { timeoutMs: 240000, what: 'back in the Catacombs run' });
            return { sidebar: sidebarText(bot), chat: bot.chatText().filter((line) => /run|Dungeon/i.test(line)).slice(-5) };
        });

        await t.step('the run still works after rejoining', async () => {
            const score = await bot.command('/catacombsrun score', { settleMs: 3000 });
            t.assert(score.some((line) => /Skill/.test(line)), `no score line: ${JSON.stringify(score)}`);
            return score;
        });
    },
};
