const { Vec3 } = require('vec3');
const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const sidebarText = (bot) => (bot.sidebar()?.lines ?? []).join(' | ');
const FRAMES = [[-672, 9, -278], [-670, 9, -278], [-669, 9, -277], [-669, 9, -275], [-670, 9, -274], [-672, 9, -274], [-673, 9, -275], [-673, 9, -277]];

module.exports = {
    description: 'The End: regions, NPCs and shops, End mobs, altar, Ender Dragon fight, loot, Draconic Altar',
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => bot.joinSkyblock());

        await t.step('finish the starting missions and reach SkyBlock Level 6 so drops go to the inventory', async () => {
            await bot.command('/mission clearset GETTING_STARTED', { settleMs: 1500 });
            await bot.command('/skyblockxp setlevel 6', { settleMs: 1500 });
        });

        await t.step('send the bot to The End', async () => {
            bot.say('/sendto SKYBLOCK_THE_END');
            await bot.waitFor(() => /The End/i.test(sidebarText(bot)), { timeoutMs: 200000, what: 'The End sidebar' });
            return sidebarText(bot);
        });

        await t.step('the Dragon\'s Nest is its own region', async () => {
            await bot.command('/tp -640 6 -276', { settleMs: 3000 });
            await bot.waitFor(() => /Dragon's Nest/i.test(sidebarText(bot)), { timeoutMs: 15000, what: 'the Dragon\'s Nest sidebar' });
            return sidebarText(bot);
        });

        const talk = async (label, x, y, z, pattern, { timeoutMs = 30000 } = {}) => {
            await bot.command(`/tp ${x} ${y} ${z}`, { settleMs: 2500 });
            await bot.waitFor(() => bot.findNpc(label), { timeoutMs: 20000, what: label });
            const mark = bot.markChat();
            await bot.interact(bot.findNpc(label));
            const line = await bot.waitChat(pattern, { since: mark, timeoutMs }).catch((error) => {
                throw new Error(`${error.message}; chat: ${JSON.stringify(bot.chatSince(mark))}`);
            });
            await sleep(3000);
            return line;
        };

        await t.step('Guber talks about the End Race but has no prize yet', async () => talk('Guber', -494, 121, -241, /End Race isn't open/i, { timeoutMs: 40000 }));

        await t.step('the Pearl Dealer opens a shop with the Remnant of the Eye', async () => {
            await bot.command('/tp -504 101 -283', { settleMs: 2500 });
            await bot.waitFor(() => bot.findNpc('Pearl Dealer'), { timeoutMs: 20000, what: 'Pearl Dealer' });
            await bot.interact(bot.findNpc('Pearl Dealer'));
            await bot.waitWindow({ timeoutMs: 60000, title: 'Pearl Dealer' });
            const names = bot.window().slots.map((slot) => `${slot.name} ${slot.lore.join(' ')}`).join(' | ');
            t.assert(/Remnant of the Eye/.test(names), names);
            t.assert(/Gravity VI|Gravity/.test(names), names);
            await bot.closeWindow();
        });

        await t.step('Lone Adventurer starts the Enderman quest', async () => talk('Lone Adventurer', -524, 101, -275, /Endermen|Enderman/i, { timeoutMs: 40000 }));

        await t.step('Gregory is away while no dragon is alive', async () => {
            await bot.command('/tp -607 22 -283', { settleMs: 3000 });
            t.assert(!bot.findNpc('Gregory'), 'Gregory should be hidden');
        });

        await t.step('End mobs live in the Nest and the Zealot waves start', async () => {
            await bot.command('/tp -672 6 -262', { settleMs: 2000 });
            await bot.waitFor(() => bot.entities({ radius: 96, names: ['Zealot'] }).length > 0, { timeoutMs: 60000, what: 'a Zealot' });
            const seen = new Set(bot.entities({ radius: 120 }).map((entity) => (entity.customName ?? '').replace(/\[.*?\]\s*/, '').replace(/[0-9.kM]+\/.*$/, '').trim()).filter(Boolean));
            return [...seen].join(' | ');
        });

        await t.step('Endermen spawn on the End island', async () => {
            await bot.command('/tp -520 80 -270', { settleMs: 3000 });
            await bot.waitFor(() => bot.entities({ radius: 110, names: ['Enderman'] }).length > 0, { timeoutMs: 90000, what: 'an Enderman' });
            return bot.entities({ radius: 110, names: ['Enderman'] }).length;
        });

        await t.step('Seers and Obsidian Defenders spawn in the Nest', async () => {
            await bot.command('/tp -650 6 -300', { settleMs: 3000 });
            await bot.waitFor(() => bot.entities({ radius: 110, names: ['Seer'] }).length > 0 && bot.entities({ radius: 110, names: ['Obsidian Defender'] }).length > 0, { timeoutMs: 90000, what: 'a Seer and an Obsidian Defender' });
        });

        await t.step('Tyzzo opens his menu with the shop and the Draconic Altar', async () => {
            await bot.command('/tp -596 5 -270', { settleMs: 2500 });
            await bot.waitFor(() => bot.findNpc('Tyzzo'), { timeoutMs: 20000, what: 'Tyzzo' });
            await bot.interact(bot.findNpc('Tyzzo'));
            await bot.waitWindow({ timeoutMs: 40000, title: 'Tyzzo' });
            await bot.click(11, { button: 0, mode: 0 });
            await bot.waitWindow({ timeoutMs: 10000, title: 'Dragon Essence Shop' });
            const perks = bot.window().slots.filter((slot) => /One Punch|Recharge|Rageborn|Zealuck|Dragon Piper/.test(slot.name));
            t.assert(perks.length === 5, `perks: ${JSON.stringify(perks.map((p) => p.name))}`);
            t.assert(perks.every((perk) => !perk.lore.some((line) => /Unavailable/.test(line))), 'a live perk is flagged unavailable');
            await bot.closeWindow();
        });

        await t.step('the Draconic Altar turns a Dragon helmet into Dragon Essence', async () => {
            await bot.command('/item STRONG_DRAGON_HELMET', { settleMs: 1500 });
            await bot.interact(bot.findNpc('Tyzzo'));
            await bot.waitWindow({ timeoutMs: 20000, title: 'Tyzzo' });
            await bot.click(15, { button: 0, mode: 0 });
            await bot.waitWindow({ timeoutMs: 10000, title: 'Draconic Altar' });
            const mark = bot.markChat();
            const entry = bot.window().slots.find((slot) => /Dragon Helmet/i.test(slot.name));
            t.assert(entry, `slots: ${JSON.stringify(bot.window().slots.map((s) => s.name))}`);
            await bot.click(entry.slot, { button: 0, mode: 0 });
            await bot.waitChat(/You sacrificed .* for .*25 Dragon Essence/i, { since: mark, timeoutMs: 10000 });
            await bot.closeWindow();
        });

        await t.step('placing eight Summoning Eyes on the altar wakes a dragon', async () => {
            await bot.command('/item SUMMONING_EYE 8', { settleMs: 1500 });
            await bot.command('/tp -671 10 -276', { settleMs: 3000 });
            for (let i = 0; i < FRAMES.length; i++) {
                const eye = bot.bot.inventory.items().find((item) => /summoning eye/i.test(item.customName ? JSON.stringify(item.customName) : item.displayName));
                t.assert(eye, `no Summoning Eye left before frame ${i}`);
                await bot.bot.equip(eye, 'hand');
                const block = bot.bot.blockAt(new Vec3(...FRAMES[i]));
                t.assert(block && /end_portal_frame/.test(block.name), `frame ${i} is ${block?.name}`);
                const mark = bot.markChat();
                await bot.bot.lookAt(block.position.offset(0.5, 0.5, 0.5), true);
                await bot.bot.activateBlock(block);
                await bot.waitChat(/placed a Summoning Eye/i, { since: mark, timeoutMs: 8000 });
            }
            const mark = bot.markChat();
            await bot.waitChat(/has awoken from the Dragon Egg/i, { since: mark, timeoutMs: 30000 }).catch(() => null);
            await bot.waitFor(() => /has awoken/i.test(bot.chatText()), { timeoutMs: 30000, what: 'the dragon spawn message' });
        });

        await t.step('the Sleeping Eyes became Remnants of the Eye and Gregory arrived', async () => {
            const held = bot.bot.inventory.items().map((item) => `${bot.describeItem(item, 0).name} x${item.count}`);
            t.assert(held.some((name) => /Remnant of the Eye x8/.test(name)), held.join(' | '));
            await bot.command('/tp -607 22 -283', { settleMs: 3000 });
            await bot.waitFor(() => bot.findNpc('Gregory'), { timeoutMs: 20000, what: 'Gregory' });
        });

        await t.step('a one-hit sword brings the dragon down and rewards the fight', async () => {
            await bot.command('/item ONE_HIT_SWORD', { settleMs: 1500 });
            const sword = bot.bot.inventory.items().find((item) => /one hit|one-hit/i.test(bot.describeItem(item, 0).name));
            t.assert(sword, 'no sword');
            await bot.bot.equip(sword, 'hand');
            const mark = bot.markChat();
            for (let i = 0; i < 60 && !/has been defeated/i.test(bot.chatSince(mark).join(' ')); i++) {
                const dragon = bot.entities({ radius: 200 }).find((entity) => /ender_dragon|Dragon/.test(`${entity.type} ${entity.customName}`) && !/Egg/.test(entity.customName ?? ''));
                if (dragon) {
                    await bot.command(`/tp ${Math.round(dragon.x)} ${Math.round(dragon.y)} ${Math.round(dragon.z) + 2}`, { settleMs: 400 });
                    const entity = bot.bot.entities[dragon.id];
                    if (entity) await bot.attack(entity);
                }
                await sleep(500);
            }
            await bot.waitChat(/has been defeated/i, { since: mark, timeoutMs: 10000 });
            await bot.waitChat(/You placed #1/i, { since: mark, timeoutMs: 10000 });
            await sleep(3000);
            const held = bot.bot.inventory.items().map((item) => `${bot.describeItem(item, 0).name} x${item.count}`);
            t.assert(held.some((name) => /Dragon Fragment x/.test(name)), held.join(' | '));
            return held.join(' | ');
        });

        await t.step('Gregory leaves and the altar is sealed while the egg respawns', async () => {
            await bot.command('/tp -607 22 -283', { settleMs: 3000 });
            t.assert(!bot.findNpc('Gregory'), 'Gregory should be gone');
            await bot.waitFor(() => /Dragon Egg has respawned/i.test(bot.chatText()), { timeoutMs: 90000, what: 'the egg respawn message' });
        });
    },
};
