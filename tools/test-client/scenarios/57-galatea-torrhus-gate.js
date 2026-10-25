const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const clean = (chat) => chat.filter((line) => !/TPS/.test(line)).join(' | ');
const sidebarText = (bot) => (bot.sidebar()?.lines ?? []).join(' | ');

module.exports = {
    description: 'Galatea to Torrhus gate: the pad launches a player who meets the requirement and starts the server transfer',
    async run(t, bot) {
        await t.step('reach Galatea', async () => {
            await bot.joinSkyblock();
            await bot.command('/mission clearset GETTING_STARTED', { settleMs: 1500 });
            for (let attempt = 1; attempt <= 4; attempt++) {
                bot.say('/sendto galatea');
                try {
                    await bot.waitFor(() => /Galatea|Tangleburg|Moonglade/i.test(sidebarText(bot)), { timeoutMs: 60000, what: 'the Galatea sidebar' });
                    break;
                } catch (error) {
                    if (attempt === 4) throw error;
                }
            }
        });
        await t.step('the gate hologram is there', async () => {
            await bot.command('/tp -728.5 98 85', { settleMs: 3000 });
            const hologram = bot.entities({ radius: 16 }).map((entity) => bot.entityCustomName(entity)).join(' | ');
            t.assert(/Torrhus Canyon/.test(hologram), hologram);
        });
        await t.step('stepping on the pad starts the transfer', async () => {
            const mark = bot.markChat();
            await bot.command('/tp -728.5 98.2 89.5', { settleMs: 8000 });
            const chat = clean(bot.chatSince(mark));
            t.assert(/Sending to server|Torrhus|Heart of the Forest|Foraging/i.test(chat), chat);
        });
    }
};
