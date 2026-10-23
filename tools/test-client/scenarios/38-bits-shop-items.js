const { Vec3 } = require('vec3');
const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const titleText = (window) => JSON.stringify(window?.title ?? '');
const slotsText = (bot) => bot.window().slots.map((slot) => `${slot.slot}:${slot.name} ${slot.lore.join(' ')}`).join(' | ');

const surface = (bot, x, z) => {
    for (let y = 112; y >= 90; y--) {
        const block = bot.bot.blockAt(new Vec3(x, y, z));
        if (block && block.name !== 'air' && block.name !== 'beacon') return { x, y: y + 1, z };
    }
    return { x, y: 100, z };
};

const placeOn = async (t, bot, itemId, matcher, target, stand) => {
    await bot.command(`/item ${itemId}`, { settleMs: 1000 });
    await bot.command(`/tp ${stand.x} ${stand.y} ${stand.z}`, { settleMs: 1500 });
    await bot.useHotbarItem(matcher, { settleMs: 300 }).catch(() => {});
    const mark = bot.markChat();
    const ground = bot.bot.blockAt(new Vec3(target.x, target.y - 1, target.z));
    t.assert(ground && ground.name !== 'air', `no ground at ${JSON.stringify(target)}`);
    await bot.bot.lookAt(new Vec3(target.x + 0.5, target.y, target.z + 0.5), true);
    await bot.bot.placeBlock(ground, new Vec3(0, 1, 0)).catch(() => {});
    await sleep(1500);
    return bot.chatSince(mark).join(' | ');
};

const useWithFace = async (bot, matcher, at, face) => {
    await bot.useHotbarItem(matcher, { settleMs: 300 }).catch(() => {});
    const block = bot.bot.blockAt(new Vec3(at.x, at.y, at.z));
    await bot.bot.lookAt(block.position.offset(0.5, 1, 0.5), true);
    const mark = bot.markChat();
    await bot.bot.activateBlock(block, face);
    await sleep(1500);
    return bot.chatSince(mark).join(' | ');
};

module.exports = {
    description: 'Bits Shop items: Autopet Rules 2-Pack and Autopets menu, Hologram, Block Zapper, Builder\'s Wand',
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => {
            await bot.joinSkyblock();
            await bot.command('/mission clearset GETTING_STARTED', { settleMs: 1500 });
        });

        await t.step('an Autopet Rules 2-Pack unlocks two slots', async () => {
            await bot.command('/item AUTOPET_RULES_2_PACK', { settleMs: 1000 });
            const mark = bot.markChat();
            await bot.useHotbarItem(/Autopet Rules/, { settleMs: 300 });
            await sleep(1200);
            const chat = bot.chatSince(mark).join(' | ');
            t.assert(/unlocked/i.test(chat) && /2\/28/.test(chat), chat);
            return chat;
        });

        await t.step('max the profile (adds pets and all 28 rule slots), then a further pack is refused', async () => {
            await bot.command('/max profile', { settleMs: 6000 });
            await sleep(12000);
            await bot.command('/clear', { settleMs: 1500 });
            await bot.command('/item AUTOPET_RULES_2_PACK', { settleMs: 1000 });
            const mark = bot.markChat();
            await bot.useHotbarItem(/Autopet Rules/, { settleMs: 300 });
            await sleep(1200);
            const chat = bot.chatSince(mark).join(' | ');
            t.assert(/maximum/i.test(chat), chat);
            await bot.command('/clear', { settleMs: 500 });
            return chat;
        });

        await t.step('the Pets menu opens the Autopets menu with 28 open slots', async () => {
            await bot.command('/pets', { settleMs: 2000 });
            await bot.waitWindow({ timeoutMs: 8000 });
            await bot.click(6);
            await bot.waitWindow({ timeoutMs: 8000, title: 'Autopets' });
            const text = slotsText(bot);
            t.assert(/Create Rule #1/.test(text) && /Create Rule #28/.test(text) && !/Locked Rule Slot/.test(text), text);
        });

        await t.step('a LOGIN rule can be built through the menus and saved', async () => {
            await bot.click(10);
            await bot.waitWindow({ timeoutMs: 8000, title: 'Autopet Rule' });
            await bot.click(10);
            await bot.waitWindow({ timeoutMs: 8000, title: 'Select Trigger' });
            const triggers = slotsText(bot);
            t.assert(/On login/.test(triggers) && /Not available yet/.test(triggers), triggers);
            await bot.click(10);
            await bot.waitWindow({ timeoutMs: 8000, title: 'Autopet Rule' });
            await bot.click(13);
            await bot.waitWindow({ timeoutMs: 8000, title: 'Select Pet' });
            await bot.click(10);
            await bot.waitWindow({ timeoutMs: 8000, title: 'Autopet Rule' });
            const rule = slotsText(bot);
            t.assert(/Save Rule/.test(rule), rule);
            await bot.click(22);
            await bot.waitWindow({ timeoutMs: 8000, title: 'Autopets' });
            const menu = slotsText(bot);
            t.assert(/Rule #1/.test(menu) && /On login/.test(menu), menu);
            await bot.closeWindow();
            return menu.slice(0, 400);
        });

        await t.step('the rule equips its pet on the next login', async () => {
            await bot.command('/pets', { settleMs: 2000 });
            await bot.waitWindow({ timeoutMs: 8000 });
            await bot.closeWindow();
            await bot.disconnect();
            await sleep(15000);
            await bot.connect();
            const mark = bot.markChat();
            await bot.joinSkyblock();
            await sleep(6000);
            const chat = bot.chatSince(mark).join(' | ');
            t.assert(/Autopet/.test(chat) && /equipped/.test(chat), chat);
            return chat;
        });

        const holo = { x: 6, y: 100, z: 40 };
        await t.step('a Hologram places and opens its menu', async () => {
            const at = surface(bot, holo.x, holo.z);
            const chat = await placeOn(t, bot, 'HOLOGRAM', /Hologram/, at, { x: 6, y: 101, z: 36 });
            await bot.command('/clear', { settleMs: 500 });
            const hitbox = bot.bot.nearestEntity((entity) => /interaction/i.test(entity.name ?? '') && entity.position.distanceTo(new Vec3(at.x + 0.5, at.y + 0.5, at.z + 0.5)) < 2);
            t.assert(hitbox, `no hitbox; chat: ${chat}`);
            await bot.command(`/tp ${Math.floor(hitbox.position.x)} ${Math.floor(hitbox.position.y) + 1} ${Math.floor(hitbox.position.z) + 1}`, { settleMs: 1500 });
            for (let attempt = 1; attempt <= 3 && !bot.window(); attempt++) {
                await bot.interact(hitbox);
                await bot.waitWindow({ timeoutMs: 4000 }).catch(() => {});
            }
            t.assert(bot.window() && /Hologram/.test(titleText(bot.window())), titleText(bot.window()));
            return slotsText(bot).slice(0, 300);
        });

        await t.step('picking a line and /hgl writes floating text', async () => {
            await bot.click(10);
            await sleep(800);
            const mark = bot.markChat();
            await bot.command('/hgl &aHello &lSkyBlock', { settleMs: 2500 });
            const chat = bot.chatSince(mark).join(' | ');
            t.assert(/Set line 1/.test(chat), chat);
            const text = bot.bot.nearestEntity((entity) => /text_display/i.test(entity.name ?? ''));
            t.assert(text, 'no text display entity');
            return chat;
        });

        await t.step('a line longer than 35 characters is refused', async () => {
            const hitbox = bot.bot.nearestEntity((entity) => /interaction/i.test(entity.name ?? ''));
            for (let attempt = 1; attempt <= 3 && !bot.window(); attempt++) {
                await bot.interact(hitbox);
                await bot.waitWindow({ timeoutMs: 4000 }).catch(() => {});
            }
            await bot.click(11);
            await sleep(800);
            const mark = bot.markChat();
            await bot.command('/hgl ' + 'x'.repeat(40), { settleMs: 1500 });
            const chat = bot.chatSince(mark).join(' | ');
            t.assert(/at most 35/.test(chat), chat);
            return chat;
        });

        const stand = { x: 14, y: 101, z: 30 };
        const blockA = surface(bot, 14, 34);
        const blockB = surface(bot, 15, 34);
        await t.step('two dirt blocks are placed to practise on', async () => {
            await bot.command('/item DIRT 64', { settleMs: 1000 });
            await bot.command(`/tp ${stand.x} ${stand.y} ${stand.z}`, { settleMs: 1500 });
            for (const target of [blockA, blockB]) {
                await bot.useHotbarItem(/Dirt/, { settleMs: 300 }).catch(() => {});
                const ground = bot.bot.blockAt(new Vec3(target.x, target.y - 1, target.z));
                await bot.bot.lookAt(new Vec3(target.x + 0.5, target.y, target.z + 0.5), true);
                await bot.bot.placeBlock(ground, new Vec3(0, 1, 0)).catch(() => {});
                await sleep(800);
            }
            const placed = [blockA, blockB].map((p) => bot.bot.blockAt(new Vec3(p.x, p.y, p.z))?.name);
            t.assert(placed.every((name) => name === 'dirt'), placed.join(','));
        });

        await t.step('the Block Zapper zaps both blocks and the undo puts them back', async () => {
            await bot.command('/item BLOCK_ZAPPER', { settleMs: 1000 });
            const chat = await useWithFace(bot, /Zapper/, blockA, new Vec3(0, 1, 0));
            t.assert(/Zapped/.test(chat) && /2/.test(chat), chat);
            const gone = bot.bot.blockAt(new Vec3(blockA.x, blockA.y, blockA.z));
            t.assert(gone.name === 'air', gone.name);
            const mark = bot.markChat();
            await bot.command('/undobuild zap', { settleMs: 1500 });
            const undo = bot.chatSince(mark).join(' | ');
            t.assert(/Unzapped/.test(undo), undo);
            const back = bot.bot.blockAt(new Vec3(blockA.x, blockA.y, blockA.z));
            t.assert(back.name === 'dirt', back.name);
            return { chat, undo };
        });

        await t.step('the Builder\'s Wand extends the dirt and the undo removes it', async () => {
            await bot.command('/item BUILDERS_WAND', { settleMs: 1000 });
            const chat = await useWithFace(bot, /Builder/, blockA, new Vec3(0, 1, 0));
            t.assert(/You built/.test(chat), chat);
            const raised = bot.bot.blockAt(new Vec3(blockA.x, blockA.y + 1, blockA.z));
            t.assert(raised.name === 'dirt', raised.name);
            const mark = bot.markChat();
            await bot.command('/undobuild wand', { settleMs: 1500 });
            const undo = bot.chatSince(mark).join(' | ');
            t.assert(/Undid/.test(undo), undo);
            return { chat, undo };
        });

        await t.step('left-clicking the wand opens its storage', async () => {
            await bot.useHotbarItem(/Builder/, { settleMs: 300 }).catch(() => {});
            bot.bot.swingArm('right');
            await bot.waitWindow({ timeoutMs: 6000, title: 'Builder' });
            t.assert(/Storage/.test(titleText(bot.window())), titleText(bot.window()));
            await bot.closeWindow();
        });
    },
};
