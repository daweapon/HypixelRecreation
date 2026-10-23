const { Vec3 } = require('vec3');
const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const slotsText = (bot) => bot.window().slots.map((slot) => `${slot.slot}:${slot.name} ${slot.lore.join(' ')}`).join(' | ');
const hubPortalBlocks = (bot) => {
    let portal = 0;
    for (let dx = -4; dx <= 4; dx++) for (let dz = -4; dz <= 4; dz++) for (let dy = 0; dy <= 6; dy++) {
        const block = bot.bot.blockAt(new Vec3(6 + dx, 100 + dy, 43 + dz));
        if (block && block.name === 'nether_portal') portal++;
    }
    return portal;
};

const openProperties = async (bot) => {
    await bot.command('/viewsettings', { settleMs: 2000 });
    await bot.waitWindow({ timeoutMs: 8000 });
    await bot.click(bot.findSlot(/Island Settings/).slot);
    await bot.waitWindow({ timeoutMs: 8000, title: 'Island Settings' });
    await bot.click(bot.findSlot(/Island Properties/).slot);
    await bot.waitWindow({ timeoutMs: 8000, title: 'Island Properties' });
};

const openJerry = async (bot) => {
    await bot.command('/tp 6 101 36', { settleMs: 2000 });
    const jerry = bot.findNpc('jerry');
    if (!jerry) throw new Error('no Jerry NPC found');
    await bot.walkNear(jerry);
    for (let attempt = 1; attempt <= 3 && !bot.window(); attempt++) {
        await bot.interact(jerry);
        await bot.waitWindow({ timeoutMs: 4000 }).catch(() => {});
    }
    if (!bot.window()) throw new Error('Jerry did not open a menu');
};

module.exports = {
    description: 'Island leftovers: Jerry removes the Hub portal behind a skill gate and sells a new one, Weather Node unlocks island weather',
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => {
            await bot.joinSkyblock();
            await sleep(3000);
        });

        await t.step('Jerry removes the Hub portal (local profiles start with maxed skills, so the level V gate is covered by a unit test) and offers a replacement', async () => {
            await openJerry(bot);
            t.assert(!bot.findSlot(/Portal to the Hub/), 'trade offered before the portal was removed');
            const mark = bot.markChat();
            await bot.click(bot.findSlot(/Remove Hub Portal/).slot, { settleMs: 2000 });
            const chat = bot.chatSince(mark).join(' | ');
            t.assert(/removed the portal to the Hub/i.test(chat), chat);
            t.assert(hubPortalBlocks(bot) === 0, `portal blocks left: ${hubPortalBlocks(bot)}`);
            await openJerry(bot);
            t.assert(bot.findSlot(/Portal to the Hub/), slotsText(bot));
            await bot.closeWindow();
            return chat;
        });

        await t.step('the replacement costs 100 coins and arrives as an item', async () => {
            await bot.command('/set coins 50', { settleMs: 1000 });
            await openJerry(bot);
            let mark = bot.markChat();
            await bot.click(bot.findSlot(/Portal to the Hub/).slot, { settleMs: 1500 });
            const poor = bot.chatSince(mark).join(' | ');
            t.assert(/need 100 coins/.test(poor), poor);
            await bot.command('/set coins 1000', { settleMs: 1000 });
            await openJerry(bot);
            mark = bot.markChat();
            await bot.click(bot.findSlot(/Portal to the Hub/).slot, { settleMs: 1500 });
            const bought = bot.chatSince(mark).join(' | ');
            t.assert(/bought a Portal to the Hub/.test(bought), bought);
            const held = bot.bot.inventory.items().map((item) => item.customName ? JSON.stringify(item.customName) : item.displayName).join(' | ');
            t.assert(/Hub/.test(held), held);
            return { poor, bought };
        });

        await t.step('Island Properties locks the weather until a Weather Node is consumed', async () => {
            await openProperties(bot);
            await bot.waitWindow({ timeoutMs: 8000, title: 'Island Properties' });
            t.assert(/Locked/.test(slotsText(bot)), slotsText(bot));
            let mark = bot.markChat();
            await bot.click(11, { settleMs: 1200 });
            t.assert(/need a Weather Node/.test(bot.chatSince(mark).join(' | ')), bot.chatSince(mark).join(' | '));
            await bot.closeWindow();
            await bot.command('/item WEATHER_NODE', { settleMs: 1000 });
            await openProperties(bot);
            await bot.waitWindow({ timeoutMs: 8000, title: 'Island Properties' });
            mark = bot.markChat();
            await bot.click(11, { settleMs: 1500 });
            const chat = bot.chatSince(mark).join(' | ');
            t.assert(/unlocked the/i.test(chat), chat);
            await bot.waitWindow({ timeoutMs: 8000, title: 'Island Weather' });
            return chat;
        });

        await t.step('choosing Thundering sets the island weather', async () => {
            const mark = bot.markChat();
            await bot.click(15, { settleMs: 2500 });
            const chat = bot.chatSince(mark).join(' | ');
            t.assert(/changed the weather to/i.test(chat) && /Thundering/.test(chat), chat);
            t.assert(bot.bot.thunderState > 0 || bot.bot.isRaining, `thunder ${bot.bot.thunderState} rain ${bot.bot.isRaining}`);
            return chat;
        });

        await t.step('the weather survives leaving and reloading the island', async () => {
            await bot.closeWindow();
            await bot.disconnect();
            await sleep(30000);
            await bot.connect();
            await bot.joinSkyblock({ timeoutMs: 240000 });
            await sleep(3000);
            t.assert(bot.bot.thunderState > 0 || bot.bot.isRaining, `thunder ${bot.bot.thunderState} rain ${bot.bot.isRaining}`);
        });
    },
};
