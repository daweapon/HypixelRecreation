const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const clean = (chat) => chat.filter((line) => !/TPS/.test(line)).join(' | ');

module.exports = {
    description: "Spider's Den phase 4: the Spider Tamer and Shaggy talk, Shaggy's crystal trade, the Archaeologist gives the compass, relics pay 10,000 coins",
    async run(t, bot) {
        await t.step("reach the Spider's Den with a max profile", async () => {
            await bot.joinSkyblock();
            await bot.command('/max profile', { settleMs: 8000 });
            const mark = bot.markChat();
            bot.say('/warp spider');
            await bot.waitFor(() => /Spider/i.test((bot.sidebar()?.lines ?? []).join(' ')), { timeoutMs: 90000, what: "the Spider's Den sidebar" })
                .catch((error) => { throw new Error(`${error.message} chat: ${clean(bot.chatSince(mark))}`); });
        });

        await t.step('Shaggy introduces himself, then trades a crystal', async () => {
            await bot.command('/tp -278 121 -180', { settleMs: 3000 });
            await bot.command('/item ARACHNE_FRAGMENT 2', { settleMs: 800 });
            await bot.command('/item ENCHANTED_SPIDER_EYE 16', { settleMs: 800 });
            await bot.command('/item ENCHANTED_STRING 16', { settleMs: 800 });
            const npc = bot.findNpc('Shaggy');
            t.assert(npc, 'no Shaggy');
            await bot.walkNear(npc);
            const mark = bot.markChat();
            await bot.interact(npc);
            await sleep(8000);
            t.assert(/Arachne/.test(clean(bot.chatSince(mark))), clean(bot.chatSince(mark)));
        });

        await t.step('the Spider Tamer opens the Spider Essence Shop after Arachne is beaten', async () => {
            await bot.command('/tp -298 62 -192', { settleMs: 3000 });
            const npc = bot.findNpc('Spider Tamer');
            t.assert(npc, 'no Spider Tamer');
            await bot.walkNear(npc);
            const mark = bot.markChat();
            await bot.interact(npc);
            await sleep(8000);
            t.assert(/Thank you so much/.test(clean(bot.chatSince(mark))), clean(bot.chatSince(mark)));
        });

        await t.step('the Archaeologist gives the compass and a relic pays 10,000 coins', async () => {
            await bot.command('/tp -360 111 -289', { settleMs: 3000 });
            const npc = bot.findNpc('Archaeologist');
            t.assert(npc, 'no Archaeologist');
            await bot.walkNear(npc);
            const mark = bot.markChat();
            await bot.interact(npc);
            await sleep(8000);
            t.assert(/compass/i.test(clean(bot.chatSince(mark))), clean(bot.chatSince(mark)));
            await bot.command('/tp -341 123 -251', { settleMs: 4000 });
            const relic = bot.entities({ radius: 6, names: [] }).find((entity) => entity.name === 'armor_stand' || /armor/i.test(String(entity.name)));
            t.assert(relic, 'no relic near the dragon head');
            const before = bot.markChat();
            await bot.interact(relic);
            await sleep(1500);
            t.assert(/RELIC!/.test(clean(bot.chatSince(before))), clean(bot.chatSince(before)));
        });
    },
};
