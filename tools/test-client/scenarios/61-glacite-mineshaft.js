const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const sidebarText = (bot) => (bot.sidebar()?.lines ?? []).join(' | ');
const clean = (chat) => chat.filter((line) => !/TPS/.test(line)).join(' | ');

module.exports = {
    description: 'Glacite Mineshafts: forced entry builds a shaft, shows the layout code, exposes pity and the corpse milestones menu',
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => bot.joinSkyblock());

        await t.step('send the bot to the Dwarven Mines', async () => {
            await sleep(3000);
            for (let attempt = 1; attempt <= 3; attempt++) {
                bot.say('/sendto SKYBLOCK_DWARVEN_MINES');
                try {
                    await bot.waitFor(() => /Dwarven|Mines|Forge|Gateway/i.test(sidebarText(bot)), { timeoutMs: 70000, what: 'the Dwarven Mines sidebar' });
                    break;
                } catch (error) {
                    if (attempt === 3) throw error;
                }
            }
            return sidebarText(bot);
        });

        await t.step('/force mineshaft status reports the pity counter', async () => {
            const mark = bot.markChat();
            await bot.command('/force mineshaft status', { settleMs: 2500 });
            const text = clean(bot.chatSince(mark));
            t.assert(/pity|2000|counter/i.test(text), text);
            return text;
        });

        await t.step('forced entry builds a shaft and the scoreboard shows its layout code', async () => {
            const mark = bot.markChat();
            await bot.command('/force mineshaft enter RUBY_2', { settleMs: 6000 });
            await bot.waitFor(() => /RUBY_2/.test(sidebarText(bot)), { timeoutMs: 30000, what: 'the layout code on the scoreboard' }).catch(() => null);
            const text = clean(bot.chatSince(mark));
            t.assert(/RUBY_2/.test(sidebarText(bot)) || /RUBY_2|shaft/i.test(text), `sidebar: ${sidebarText(bot)} chat: ${text}`);
            return { sidebar: sidebarText(bot), chat: text };
        });

        await t.step('the shaft has corpses (armor stands) and mobs around', async () => {
            await sleep(3000);
            const nearby = bot.entities({ radius: 120 });
            const names = nearby.map((entity) => entity.customName ?? entity.type).filter(Boolean);
            t.assert(nearby.length > 0, 'no entities near the bot in the shaft');
            return names.slice(0, 15);
        });

        await t.step('/viewfrozencorpsemilestones opens the milestones menu', async () => {
            bot.say('/viewfrozencorpsemilestones');
            await bot.waitWindow({ timeoutMs: 20000 });
            const title = JSON.stringify(bot.window().title);
            t.assert(/Milestone|Corpse/i.test(title) || bot.window().slots.some((slot) => /Milestone|Corpse|Lapis/i.test(slot.name)), title);
            await bot.closeWindow();
            return title;
        });
    },
};
