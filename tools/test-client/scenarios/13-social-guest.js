module.exports = {
    description: 'Guest side of the Social XP check: visit ClaudeBot and stay for two minutes',
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => bot.joinSkyblock());
        await t.step('visit ClaudeBot', async () => bot.command('/visit ClaudeBot', { settleMs: 8000 }));
        await t.step('stay as a guest', async () => {
            await new Promise((resolve) => setTimeout(resolve, 120000));
            return bot.sidebar().lines;
        });
    },
};
