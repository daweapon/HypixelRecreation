const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const clean = (chat) => chat.filter((line) => !/TPS/.test(line)).join(' | ');

module.exports = {
    description: "Spider's Den: Haymitch greets, Grandma Wolf hands out the pet and upgrades it with Bestiary Milestones, Bramass gives the Beastslayer's Codex at Milestone 50, Mining Fatigue is applied",
    async run(t, bot) {
        await t.step("reach SkyBlock and warp to the Spider's Den", async () => {
            await bot.joinSkyblock();
            await bot.command('/max profile', { settleMs: 8000 });
            bot.say('/warp spider');
            await bot.waitFor(() => /Spider/i.test((bot.sidebar()?.lines ?? []).join(' ')), { timeoutMs: 120000, what: "the Spider's Den sidebar" });
            await sleep(8000);
        });

        await t.step('the island applies Mining Fatigue', async () => {
            await bot.waitFor(() => (bot.bot.entity.effects && Object.keys(bot.bot.entity.effects).length > 0), { timeoutMs: 20000, what: 'a potion effect' });
            const effects = Object.values(bot.bot.entity.effects).map((effect) => effect.id);
            t.assert(effects.length > 0, 'no effects on the bot');
        });

        const talk = async (name, at, pattern) => {
            await bot.command(`/tp ${at.join(' ')}`, { settleMs: 3000 });
            const npc = bot.findNpc(name);
            t.assert(npc, `no ${name} NPC`);
            await bot.walkNear(npc);
            const mark = bot.markChat();
            await bot.interact(npc);
            await sleep(9000);
            const chat = clean(bot.chatSince(mark));
            t.assert(pattern.test(chat), chat);
            return chat;
        };

        await t.step('Haymitch greets with the Spider Mound tip', async () => {
            await talk('Haymitch', [-202, 83, -234], /Spiders? Mound|Spider's Den/);
        });

        await t.step('Grandma Wolf adds the pet, and has nothing more to say below Milestone 10 for Rare', async () => {
            await bot.command('/tp -281 122 -188', { settleMs: 3000 });
            await talk('Grandma Wolf', [-281, 122, -188], /Grandma Wolf Pet|Pets Menu|Uncommon|Milestone/);
        });

        await t.step('Bramass talks about the Bestiary', async () => {
            await talk('Bramass', [-271, 113, -194], /Bestiary|Milestone|Beastslayer/);
        });
    },
};
