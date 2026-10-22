const { Vec3 } = require('vec3');
const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const sidebarText = (bot) => (bot.sidebar()?.lines ?? []).join(' | ');
const titleText = (window) => JSON.stringify(window?.title ?? '');

const surface = (bot, x, z) => {
    for (let y = 112; y >= 90; y--) {
        const block = bot.bot.blockAt(new Vec3(x, y, z));
        if (block && block.name !== 'air' && block.name !== 'beacon') return { x, y: y + 1, z };
    }
    return { x, y: 100, z };
};

const placeOn = async (t, bot, itemId, matcher, target, stand = { x: 6, y: 101, z: 36 }) => {
    await bot.command(`/item ${itemId}`, { settleMs: 1000 });
    await bot.command(`/tp ${stand.x} ${stand.y} ${stand.z}`, { settleMs: 1500 });
    const hot = await bot.useHotbarItem(matcher, { settleMs: 300 }).then(() => 'ok', (e) => String(e.message).slice(0, 300));
    const mark = bot.markChat();
    const ground = bot.bot.blockAt(new Vec3(target.x, target.y - 1, target.z));
    t.assert(ground && ground.name !== 'air', `no ground at ${JSON.stringify(target)}: ${ground && ground.name}`);
    await bot.bot.lookAt(new Vec3(target.x + 0.5, target.y, target.z + 0.5), true);
    await bot.bot.placeBlock(ground, new Vec3(0, 1, 0)).catch(() => {});
    await sleep(1500);
    return `[hotbar ${hot}] ` + bot.chatSince(mark).join(' | ');
};

const rightClickBlock = async (bot, x, y, z) => {
    const block = bot.bot.blockAt(new Vec3(x, y, z));
    await bot.bot.lookAt(block.position.offset(0.5, 0.5, 0.5), true);
    await bot.bot.activateBlock(block);
};

const openWindow = async (bot, x, y, z) => {
    for (let attempt = 1; attempt <= 3 && !bot.window(); attempt++) {
        await rightClickBlock(bot, x, y, z);
        await bot.waitWindow({ timeoutMs: 4000 }).catch(() => {});
    }
    return bot.window();
};

module.exports = {
    description: 'Island build: Beacon (place, fuel, menu, persistence), Teleport Pad, Carpentry Table, furniture, Training Dummy',
    async run(t, bot) {
        await t.step('reach SkyBlock, max the profile and finish the starting missions', async () => {
            await bot.joinSkyblock();
            await bot.command('/mission clearset GETTING_STARTED', { settleMs: 1500 });
            await bot.command('/max profile', { settleMs: 6000 });
            await sleep(12000);
            await bot.command('/clear', { settleMs: 1500 });
            return sidebarText(bot);
        });

        const beacon = { x: 6, y: 100, z: 40 };
        await t.step('placing a Beacon works and a second one is refused', async () => {
            const chat = await placeOn(t, bot, 'BEACON_3', /Beacon III/, beacon);
            const placed = bot.bot.blockAt(new Vec3(beacon.x, beacon.y, beacon.z));
            t.assert(placed && placed.name === 'beacon', `${placed && placed.name}: ${chat}`);
            const second = await placeOn(t, bot, 'BEACON_1', /Beacon I/, { x: 8, y: 100, z: 40 });
            t.assert(/only|one|already|maximum/i.test(second), second);
            return { chat, second };
        });

        await t.step('the Beacon menu opens and accepts a Power Crystal', async () => {
            await bot.command('/item POWER_CRYSTAL 2', { settleMs: 1000 });
            await bot.useHotbarItem(/Power Crystal/, { settleMs: 300 }).catch(() => {});
            const mark = bot.markChat();
            await rightClickBlock(bot, beacon.x, beacon.y, beacon.z);
            await sleep(1500);
            const inserted = bot.chatSince(mark).join(' | ');
            await bot.command('/clear', { settleMs: 500 });
            const window = await openWindow(bot, beacon.x, beacon.y, beacon.z);
            t.assert(window, `no window; chat: ${inserted}`);
            const slots = window.slots.map((slot) => `${slot.name} ${slot.lore.join(' ')}`).join(' | ');
            t.assert(/Beacon Power/.test(slots) && /48 hours|47h|48h|Remaining/i.test(slots + inserted), slots);
            await bot.closeWindow();
            return { inserted, title: titleText(window) };
        });

        await t.step('a Teleport Pad places', async () => {
            const chat = await placeOn(t, bot, 'TELEPORT_PAD', /Teleport Pad/, surface(bot, 4, 38));
            const pad = bot.bot.blockAt(new Vec3(4, 100, 38));
            t.assert(pad && pad.name !== 'air', `${chat} / ${pad && pad.name}`);
            return { chat, pad: pad.name };
        });

        await t.step('a Carpentry Table places and opens its recipe list', async () => {
            const tableAt = surface(bot, 8, 38);
            const chat = await placeOn(t, bot, 'CARPENTRY_BENCH', /Carpentry Table/, tableAt);
            await bot.command('/clear', { settleMs: 500 });
            const furniture = bot.nearestEntity('item_display') ?? bot.nearestEntity('display');
            const interaction = bot.bot.nearestEntity((entity) => /interaction/i.test(entity.name ?? '') && entity.position.distanceTo(new Vec3(tableAt.x + 0.5, tableAt.y + 0.5, tableAt.z + 0.5)) < 2);
            t.assert(interaction, `no furniture hitbox; chat: ${chat}`);
            await bot.command(`/tp ${Math.floor(interaction.position.x)} ${Math.floor(interaction.position.y) + 1} ${Math.floor(interaction.position.z) + 1}`, { settleMs: 1500 });
            const clickMark = bot.markChat();
            for (let attempt = 1; attempt <= 3 && !bot.window(); attempt++) {
                await bot.interact(interaction);
                await bot.waitWindow({ timeoutMs: 4000 }).catch(() => {});
            }
            t.assert(bot.window(), `no window; chat: ${bot.chatSince(clickMark).join(' | ')}; entity ${interaction.name} at ${interaction.position}`);
            const window = bot.window();
            const names = window.slots.map((slot) => slot.name).join(' | ');
            t.assert(/Carpentry|Table/i.test(titleText(window)), titleText(window));
            await bot.closeWindow();
            return { chat, title: titleText(window), names: names.slice(0, 300) };
        });

        await t.step('a Stool places as furniture', async () => {
            const chat = await placeOn(t, bot, 'STOOL', /Stool/, surface(bot, 10, 38));
            t.assert(/place/i.test(chat) && !/must|need|only/i.test(chat), chat);
            return chat;
        });

        await t.step('a Training Dummy places', async () => {
            const chat = await placeOn(t, bot, 'TRAINING_DUMMY', /Training Dummy/, surface(bot, 2, 36), { x: 2, y: 101, z: 33 });
            const dummy = bot.nearestEntity('dummy');
            t.assert(dummy || /place/i.test(chat), chat);
            return chat;
        });

        await t.step('everything survives leaving and reloading the island', async () => {
            await bot.disconnect();
            await sleep(15000);
            await bot.connect();
            await bot.joinSkyblock();
            await bot.command('/tp 6 101 36', { settleMs: 3500 });
            const beaconBlock = bot.bot.blockAt(new Vec3(beacon.x, beacon.y, beacon.z));
            t.assert(beaconBlock && beaconBlock.name === 'beacon', beaconBlock && beaconBlock.name);
            const interaction = bot.bot.nearestEntity((entity) => /interaction/i.test(entity.name ?? ''));
            t.assert(interaction, 'furniture hitboxes did not return');
            const window = await openWindow(bot, beacon.x, beacon.y, beacon.z);
            t.assert(window, 'beacon menu did not open after reload');
            const slots = window.slots.map((slot) => `${slot.name} ${slot.lore.join(' ')}`).join(' | ');
            t.assert(/4[67]|48|hours|Remaining/i.test(slots), slots);
            await bot.closeWindow();
            return slots.slice(0, 300);
        });
    },
};
