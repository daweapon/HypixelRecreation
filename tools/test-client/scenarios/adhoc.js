module.exports = {
    description: 'Ad-hoc: SB_COMMANDS=\'["/cmd one","/cmd two"]\' [SB_SKYBLOCK=1] [SB_SETTLE=1500]; prints chat and state after each command',
    async run(t, bot) {
        const commands = JSON.parse(process.env.SB_COMMANDS || '[]');
        const settleMs = Number(process.env.SB_SETTLE || 1500);
        if (process.env.SB_SKYBLOCK === '1') {
            await t.step('reach SkyBlock', async () => bot.joinSkyblock());
        }
        for (const command of commands) {
            await t.step(command, async () => {
                const chat = await bot.command(command, { settleMs });
                const window = bot.window();
                return { chat, sidebar: bot.sidebar()?.lines, window: window && { title: window.title, slots: window.slots.length } };
            });
        }
    },
};
