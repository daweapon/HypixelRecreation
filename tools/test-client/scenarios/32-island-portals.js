const { Vec3 } = require('vec3');
const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const sidebarText = (bot) => (bot.sidebar()?.lines ?? []).join(' | ');
const portalBlocks = (bot, x, y, z) => {
    const found = { portal: 0, frame: 0 };
    for (let dx = -4; dx <= 4; dx++) for (let dz = -4; dz <= 4; dz++) for (let dy = 0; dy <= 6; dy++) {
        const block = bot.bot.blockAt(new Vec3(x + dx, y + dy, z + dz));
        if (!block) continue;
        if (block.name === 'nether_portal') found.portal++;
        if (block.name === 'polished_andesite') found.frame++;
    }
    return found;
};
const spot = { x: 6, y: 100, z: 43 };

const deletePortalAt = async (t, bot, x, y, z) => {
    await bot.command(`/tp ${x} ${y + 1} ${z - 5}`, { settleMs: 2000 });
    const frame = bot.bot.blockAt(new Vec3(x + 2, y + 1, z));
    t.assert(frame && frame.name === 'polished_andesite', frame && frame.name);
    for (let attempt = 1; attempt <= 3 && !bot.window(); attempt++) {
        await bot.bot.lookAt(frame.position.offset(0.5, 0.5, 0.5), true);
        await bot.bot.activateBlock(frame);
        await bot.waitWindow({ timeoutMs: 4000 }).catch(() => {});
    }
    t.assert(/Delete Portal/.test(JSON.stringify(bot.window().title)), JSON.stringify(bot.window().title));
    await bot.click(11, { button: 0, mode: 0 });
    await sleep(1200);
    return portalBlocks(bot, x, y, z);
};

const placePortal = async (t, bot, itemId, matcher, standAt, target) => {
    await bot.command(`/item ${itemId}`, { settleMs: 1200 });
    await bot.command(`/tp ${standAt.x} ${standAt.y} ${standAt.z}`, { settleMs: 2000 });
    await bot.useHotbarItem(matcher, { settleMs: 300 }).catch(() => {});
    const mark = bot.markChat();
    const ground = bot.bot.blockAt(new Vec3(target.x, target.y - 1, target.z));
    t.assert(ground && ground.name !== 'air', `no ground block ${ground && ground.name}`);
    await bot.bot.lookAt(new Vec3(target.x + 0.5, target.y, target.z + 0.5), true);
    await bot.bot.placeBlock(ground, new Vec3(0, 1, 0)).catch(() => {});
    await sleep(1500);
    return bot.chatSince(mark).join(' | ');
};

module.exports = {
    description: 'Island portals: delete the starter Hub portal, place a Lotus Atoll portal, refuse an overlapping one, travel, survive an island reload',
    async run(t, bot) {
        await t.step('reach SkyBlock and finish the starting missions', async () => {
            await bot.joinSkyblock();
            await bot.command('/mission clearset GETTING_STARTED', { settleMs: 1500 });
            return sidebarText(bot);
        });

        await t.step('right-clicking the starter Hub portal frame asks before deleting, and delete removes it', async () => {
            await bot.command('/tp 6 101 38', { settleMs: 2500 });
            const before = portalBlocks(bot, spot.x, spot.y, spot.z);
            t.assert(before.portal >= 10 && before.frame >= 10, JSON.stringify(before));
            const after = await deletePortalAt(t, bot, spot.x, spot.y, spot.z);
            t.assert(after.portal === 0 && after.frame === 0, JSON.stringify(after));
            return { before, after };
        });

        await t.step('placing a Lotus Atoll portal builds it facing the player', async () => {
            const chat = await placePortal(t, bot, 'LOTUS_ATOLL_PORTAL', /Lotus Atoll/, { x: 6, y: 101, z: 38 }, spot);
            t.assert(/placed a portal to .*Lotus Atoll/i.test(chat), chat);
            const found = portalBlocks(bot, spot.x, spot.y, spot.z);
            t.assert(found.portal >= 10 && found.frame >= 10, JSON.stringify(found));
            return { chat, found };
        });

        await t.step('a second portal overlapping the first is refused for lack of space', async () => {
            const chat = await placePortal(t, bot, 'MINING_2_PORTAL', /Deep Caverns/, { x: 7, y: 101, z: 38 }, { x: 7, y: 100, z: 42 });
            t.assert(/not enough space/i.test(chat), chat);
            return chat;
        });

        await t.step('the portal and its frame survive leaving and reloading the island', async () => {
            await bot.disconnect();
            await sleep(15000);
            await bot.connect();
            await bot.joinSkyblock();
            await bot.command('/tp 6 101 38', { settleMs: 3000 });
            const found = portalBlocks(bot, spot.x, spot.y, spot.z);
            t.assert(found.portal >= 10 && found.frame >= 10, JSON.stringify(found));
            return found;
        });

        await t.step('walking into the Lotus Atoll portal reports the island is not open yet', async () => {
            await bot.command(`/tp ${spot.x} ${spot.y + 1} ${spot.z - 4}`, { settleMs: 1500 });
            const mark = bot.markChat();
            bot.bot.setControlState('sneak', true);
            bot.bot.setControlState('forward', true);
            await bot.bot.look(Math.PI, 0, true);
            await sleep(5000);
            bot.bot.setControlState('forward', false);
            bot.bot.setControlState('sneak', false);
            await sleep(2000);
            const chat = bot.chatSince(mark).join(' | ') + ' @' + JSON.stringify(bot.bot.entity.position);
            t.assert(/isn't in SkyBlock yet/i.test(chat), chat);
            return chat;
        });
    },
};
