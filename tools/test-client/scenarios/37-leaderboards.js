const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const names = (bot) => bot.window().slots.map((slot) => `${slot.name} ${slot.lore.join(' ')}`).join(' | ');

module.exports = {
    description: 'Leaderboards: /leaderboard menu, board view with own rank, slayer lore rank',
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => {
            await bot.joinSkyblock();
            await sleep(2000);
        });

        if (process.env.LB_PHASE === 'seed') {
            await t.step('max the profile, then drop staff rank so the save is ranked', async () => {
                await bot.command('/max profile', { settleMs: 3000 });
                await bot.command(`/rank ${process.env.LB_USER} MVP_PLUS`, { settleMs: 2000 });
            });
            return;
        }

        await t.step('/leaderboard opens the category menu', async () => {
            await bot.command('/leaderboard', { settleMs: 2000 });
            await bot.waitFor(() => bot.window(), { timeoutMs: 8000, what: 'the leaderboards menu' });
            const text = names(bot);
            t.assert(/General/.test(text) && /Skills/.test(text) && /Slayers/.test(text) && /Collections/.test(text), text);
        });

        await t.step('the skills category lists boards', async () => {
            await bot.click(bot.findSlot(/Skills/).slot);
            await bot.waitFor(() => /Skills Leaderboards/.test(JSON.stringify(bot.window()?.title)), { timeoutMs: 8000, what: 'the skills boards' });
            t.assert(/Farming XP/.test(names(bot)), names(bot));
        });

        await t.step('a board shows the top profiles and your rank', async () => {
            await bot.click(bot.findSlot(/Farming XP/).slot);
            await bot.waitFor(() => /Farming XP/.test(JSON.stringify(bot.window()?.title)) && /Your Ranking/.test(names(bot)), { timeoutMs: 8000, what: 'the board view' });
            return names(bot).slice(0, 700);
        });
    },
};
