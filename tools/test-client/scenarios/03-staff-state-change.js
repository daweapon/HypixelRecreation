module.exports = {
    description: 'Run a staff command that changes state and verify the effect through chat and the sidebar',
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => {
            await bot.joinSkyblock();
        });

        await t.step('/set coins changes the purse shown on the sidebar', async () => {
            const before = bot.sidebar().lines.find((line) => /Purse|Piggy/.test(line));
            const lines = await bot.command('/set coins 12345', { settleMs: 2500 });
            const after = await bot.waitFor(() => {
                const line = bot.sidebar().lines.find((entry) => /Purse|Piggy/.test(entry));
                return line && line.includes('12,345') ? line : null;
            }, { timeoutMs: 8000, what: 'purse to read 12,345 on the sidebar' });
            return { before, after, chat: lines };
        });
    },
};
