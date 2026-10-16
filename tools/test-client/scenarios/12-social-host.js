const socialLore = (bot) => JSON.stringify(bot.window().slots.map((entry) => entry.lore));

module.exports = {
    description: 'Host side of the Social XP check; run 13-social-guest as ClaudeAlt while this waits',
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => bot.joinSkyblock());

        let before;
        await t.step('read Social progress before the guest arrives', async () => {
            bot.say('/viewskill SOCIAL');
            await bot.waitWindow({ timeoutMs: 15000, title: 'Social' });
            before = socialLore(bot);
            await bot.closeWindow();
            return before.slice(0, 400);
        });

        await t.step('wait for the guest to arrive and spend a minute', async () => {
            await new Promise((resolve) => setTimeout(resolve, 190000));
        });

        await t.step('Social progress went up', async () => {
            bot.say('/viewskill SOCIAL');
            await bot.waitWindow({ timeoutMs: 15000, title: 'Social' });
            const after = socialLore(bot);
            t.assert(after !== before, 'Social progress did not change');
            return after.slice(0, 400);
        });
    },
};
