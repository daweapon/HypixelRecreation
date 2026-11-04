const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const sidebarText = (bot) => (bot.sidebar()?.lines ?? []).join(' ');
const clean = (lines) => lines.filter((line) => !/TPS|^\s*$/.test(line));

const PLANS = {
    hub: {
        warp: '/warp hub',
        sidebar: 'Village|Hub|Dark Auction|Wilderness|Mountain|Forest|Farm|Colosseum|Graveyard|Ruins|Coal|Bazaar',
        checks: [
            { label: 'Raffle of the Century can be forced on', commands: ['/force century start 2'], chat: 'Raffle|Century|started|start' },
            { label: 'Rezzus intro opens Team Assignment; slot 13 joins a team', tp: [-11, 76, 3], npc: 'Rezzus', settle: 14000, window: 'Team Assignment', clickSlot: 13, chat: 'team|Team|Ticket' },
            { label: '/century opens the Raffle Box', commands: ['/century'], window: 'Raffle' },
            { label: 'Raffle Box tasks sub-menu', commands: ['/century'], window: 'Raffle', clicksFind: /Task/i },
            { label: 'Simon hands out a Century Cake', tp: [-7, 76, 3], npc: 'Simon', chat: 'Cake|cake' },
            { label: 'Speed raffle draws', commands: ['/force century draw speed'], chat: 'draw|Draw|winner|Winner|raffle|Raffle', settle: 6000 },
            { label: 'Feast Baker Scott talks', tp: [77, 72, -83], npc: 'Feast Baker Scott|Scott', chat: 'Scott|Bakery|bread', settle: 5000 },
            { label: 'Professor Wynd talks', tp: [2, 180, 60], npc: 'Professor Wynd|METEOROLOGIST|Wynd', chat: '\\[NPC\\] Professor Wynd', settle: 16000 },
            { label: 'Elizabeth opens the Community Shop', tp: [-6, 79, 19], npc: 'Elizabeth', window: 'Community|Shop|Upgrades' },
            { label: 'Incredible Raffle Box NPC opens it', tp: [-9, 76, 6], npc: 'Incredible Raffle Box', window: 'Raffle' },
            { label: 'Tyashoi Alchemist responds during the Great Spook', commands: ['/adminbypass greatspook on'], tp: [41, 68, -54], npc: 'Tyashoi', chatOrWindow: true, settle: 12000 },
            { label: 'Chocolate Factory opens', commands: ['/cf'], window: 'Chocolate' },
            { label: 'Faction Tower (factory slot 42)', commands: ['/cf'], window: 'Chocolate', clickSlot: 42, childWindow: 'Faction' },
            { label: 'Chocobit Tower (factory slot 43)', commands: ['/cf'], window: 'Chocolate', clickSlot: 43, childWindow: 'Chocobit' },
            { label: 'Revenant T1 quest starts', commands: ['/set coins 100000000', '/slayer cancel', '/slayer start REVENANT_HORROR I', '/slayer status'], chat: 'Boss: |Revenant Horror' },
            { label: 'Spooky sea creatures spawn', tp: [60, 72, 60], commands: ['/spawn JUMPIN_JACK', '/spawn GRIM_REAPER', '/spawn WEREWOLF'], entity: 'Jumpin|Grim Reaper|Werewolf' },
            { label: 'Bayou sea creatures spawn', commands: ['/spawn TITANOBOA', '/spawn ALLIGATOR', '/spawn BANSHEE'], entity: 'Titanoboa|Alligator|Banshee' },
            { label: 'Hotspot sea creatures spawn', commands: ['/spawn FROG_MAN', '/spawn MANTA_RAY', '/spawn WIKI_TIKI'], entity: 'Frog Man|Manta Ray|Wiki Tiki' },
            { label: 'Deep Sea Protector spawns', commands: ['/spawn DEEP_SEA_PROTECTOR'], entity: 'Deep Sea Protector' },
        ],
    },
    items: {
        sidebar: 'Your Island|Village|Hub',
        checks: [
            { label: 'Glowing Mush Mixin refuses without God Potion', commands: ['/item GLOWING_MUSH_MIXIN'], use: /Glowing Mush/i, chat: 'God Potion|Booster Cookie|Mixin' },
            { label: 'Booster Cookie consumed through its menu', commands: ['/item BOOSTER_COOKIE'], use: /Booster Cookie/i, keepWindow: true, window: '.', clickSlot: 11, chat: 'Cookie|cookie' },
            { label: 'God Potion consumed', commands: ['/item GOD_POTION'], use: /God Potion/i, chat: 'God Potion' },
            { label: 'Glowing Mush Mixin consumes with both active', commands: ['/item GLOWING_MUSH_MIXIN'], use: /Glowing Mush/i, chat: 'Mixin|Fishing Speed|consum' },
            { label: 'Deepterror Mixin has a recipe', commands: ['/viewrecipe DEEPTERROR_MIXIN'], windowOrChat: 'Deepterror|Recipe|Craft' },
            { label: 'InfiniDirt Wand exists', commands: ['/item INFINIDIRT_WAND'], inventory: /InfiniDirt/i },
            { label: 'Precious loads', commands: ['/item PRECIOUS'], inventory: /Precious/i },
            { label: 'Sirius phone number adds the contact', commands: ['/item ABIPHONE_BASIC', '/item SIRIUS_PERSONAL_PHONE_NUMBER'], use: /Phone Number/i, chat: 'Sirius|contact|Abiphone' },
            { label: 'Advent day 25 preview', commands: ['/sbadvent preview 25'], windowOrChat: 'Advent|North Star|Day' },
            { label: 'Garden skin item unlocks by right-click', commands: ['/item CUBE_BARN_SKIN'], use: /Cube/i, chat: 'You unlocked the' },
            { label: 'Second use says already unlocked', commands: ['/item CUBE_BARN_SKIN'], use: /Cube/i, chat: 'already unlocked' },
            { label: 'Ender Gauntlet item exists', commands: ['/item ENDER_GAUNTLET'], inventory: /Ender Gauntlet/i },
        ],
    },
    skin: {
        sidebar: 'Your Island',
        checks: [
            { label: 'Cube Barn Skin unlocks on first use', commands: ['/item CUBE_BARN_SKIN'], use: /Cube/i, chat: 'You unlocked the', settle: 3000 },
            { label: 'Cube Barn Skin item was consumed', inventoryNot: /Cube/i },
            { label: 'Second copy says already unlocked', commands: ['/item CUBE_BARN_SKIN'], use: /Cube/i, chat: 'already unlocked', settle: 3000 },
        ],
    },
    protector: {
        warp: '/sendto SKYBLOCK_THE_END',
        sidebar: 'End|Dragon|Nest|Void',
        checks: [
            { label: 'Protector summoned with a forced Golem Pet drop', commands: ['/force drop GOLEM_PET', '/force protector kills 5000'], chat: 'Protector', settle: 6000 },
            { label: 'Protector found at a spawn point and killed', custom: 'protector' },
        ],
    },
    nodes: {
        warp: '/sendto SKYBLOCK_THE_END',
        sidebar: 'End|Dragon|Nest|Void',
        checks: [
            { label: 'Mining an Ender Node gives node loot', custom: 'node' },
            { label: 'Plain End Stone breaks into bedrock', custom: 'endstone' },
            { label: 'Mining a second Ender Node', custom: 'node' },
        ],
    },
    garden: {
        warp: '/sendto SKYBLOCK_THE_GARDEN',
        sidebar: 'Garden|Plot|Barn',
        checks: [
            { label: 'Desk -> Garden Skins menu', use: /SkyBlock Menu/i, keepView: true, path: [{ slot: 10, title: 'Desk' }, { slot: 31, title: 'Skin' }] },
            { label: 'Garden Skins -> Barn skin list', use: /SkyBlock Menu/i, keepView: true, path: [{ slot: 10, title: 'Desk' }, { slot: 31, title: 'Skin' }, { find: /Barn/i, title: 'Barn|Skin' }] },
            { label: 'Taylor can be summoned as a visitor', commands: ['/visitor spawn TAYLOR'], chat: 'Taylor|visitor|Visitor|spawned|Spawned', settle: 4000 },
            { label: 'Tyashoi visitor can be summoned', commands: ['/visitor spawn TYASHOI_ALCHEMIST'], chat: 'Tyashoi|visitor|Visitor|spawned|Spawned', settle: 4000 },
            { label: 'Archaeologist visitor can be summoned', commands: ['/visitor spawn ARCHAEOLOGIST'], chat: 'Archaeologist|visitor|Visitor|spawned|Spawned', settle: 4000 },
        ],
    },
    deep: {
        warp: '/sendto SKYBLOCK_DEEP_CAVERNS',
        sidebar: 'Deep|Caverns|Den|Lapis|Gunpowder|Slimehill|Diamond|Obsidian|Pigmen',
        checks: [
            { label: 'Redstone Pigmen are neutral until hit, then turn hostile', custom: 'pigmen' },
            { label: 'Lapis Miner dialogue', tp: [-10, 120, 35], npc: 'Lapis Miner', chat: 'Lapis Miner', settle: 6000 },
        ],
    },
    spiders: {
        warp: '/sendto SKYBLOCK_SPIDERS_DEN',
        sidebar: "Spider|Den|Arachne|Gravel|Mound|Grandma",
        checks: [
            { label: 'Archaeologist talks', tp: [-360, 111, -288], npc: 'Archaeologist', chat: 'Archaeologist', settle: 6000 },
            { label: 'Archaeologist Compass points at a relic', commands: ['/item ARCHAEOLOGIST_COMPASS'], use: /Compass/i, chat: 'Relic|relic|compass|Compass|points' },
            { label: 'Flint Skeleton spawns', commands: ['/spawn FLINT_SKELETON'], entity: 'Flint' },
            { label: 'Tarantula T3 quest starts', commands: ['/max profile', '/set coins 100000000', '/slayer cancel', '/slayer start TARANTULA_BROODFATHER III', '/slayer status'], chat: 'Boss: |Tarantula', settle: 8000 },
        ],
    },
    farming: {
        warp: '/sendto SKYBLOCK_THE_FARMING_ISLANDS',
        sidebar: 'Farming|Barn|Desert|Mushroom|Oasis|Trapper|Glowing|Jake|Overgrown|Settlement|Lab',
        checks: [
            { label: 'Talbot sells the Douce Pluie potion', tp: [285, 104, -546], npc: 'Talbot', window: '.', windowItem: /Douce Pluie/i },
            { label: 'Tony talks', tp: [278, 104, -542], npc: 'Tony', chatOrWindow: true },
            { label: 'Trevor nameplate is "Trevor"', tp: [281, 104, -540], npc: 'Trevor', chatOrWindow: true },
            { label: 'Hunter Ava stands at the wiki spot', tp: [319, 102, -468], npc: 'Hunter Ava|Ava', chatOrWindow: true },
        ],
    },
    jerry: {
        warp: '/sendto SKYBLOCK_JERRYS_WORKSHOP',
        sidebar: "Jerry|Workshop|Glacial|Einary|Reflective|Sherry|Mount",
        checks: [
            { label: 'Terry talks first', tp: [-92, 78, 27], npc: 'Terry', chatOrWindow: true, settle: 6000 },
            { label: 'Terry opens his shop', commands: ['/item ICE_HUNK 64'], tp: [-92, 78, 27], npc: 'Terry', window: 'Terry', windowItem: /Salt Cube|Rusty Anchor|Snowglobe|Frozen Bauble/i },
        ],
    },
    end: {
        warp: '/sendto SKYBLOCK_THE_END',
        sidebar: 'End|Dragon|Nest|Void',
        checks: [
            { label: 'Protector status', commands: ['/force protector status'], chat: 'Protector|Zealot|stage|Stage', only: 'protector' },
            { label: 'Protector counter set to 4999', commands: ['/force protector kills 4999'], chat: '4,?999|Zealot|Protector' },
            { label: 'Protector rises at 5000', commands: ['/force protector kills 5000'], chat: 'Protector|rises|awake|tremor|ground', settle: 6000 },
            { label: 'Protector entity is present', commands: ['/force protector status'], chat: 'Protector' },
            { label: 'Plate before talking to Guber is refused', tp: [-504, 121, -241], walkTo: [-501.5, 121, -240.5], chat: 'Guber|talk|race|Race', settle: 3000 },
            { label: 'Guber explains the race', tp: [-494, 121, -241], npc: 'Guber', chat: 'Guber', settle: 14000 },
            { label: 'Walking onto the start plate starts the race', tp: [-504, 121, -241], walkTo: [-501.5, 121, -240.5], chat: 'race|Race|started|GO|RACE', settle: 2000 },
            { label: 'Checkpoint plate', tp: [-776, 101, -299], walkTo: [-773.5, 101, -298.5], chat: 'checkpoint|Checkpoint|CHECKPOINT|back', settle: 2000 },
            { label: 'Back at the start plate finishes', tp: [-504, 121, -241], walkTo: [-501.5, 121, -240.5], chat: 'finish|Finish|FINISH|time|Time|best|Best', settle: 3000 },
            { label: 'Protector found at a spawn point and killed', custom: 'protector' },
            { label: 'Lone Adventurer has real dialogue', tp: [-524, 101, -273], npc: 'Lone Adventurer|Adventurer', chat: '\\[NPC\\]' },
            { label: 'Voidgloom T3 quest starts', commands: ['/set coins 100000000', '/slayer cancel', '/slayer start VOIDGLOOM_SERAPH III', '/slayer status'], chat: 'Boss: |Voidgloom' },
        ],
    },
    dwarven: {
        warp: '/sendto SKYBLOCK_DWARVEN_MINES',
        sidebar: 'Dwarven|Village|Forge|Mines|Quarry|Lava|Palace|Rampart|Gateway|Cliffside|Royal|Upper|Mist|Far|Goblin|Divan|Aristocrat|Hanging|Base',
        checks: [
            { label: 'Bednom first dialogue', tp: [-29, 214, -88], npc: 'Bednom', chat: '\\[NPC\\] Bednom', settle: 6000 },
            { label: 'Bednom says to wait for the next day', tp: [-29, 214, -88], npc: 'Bednom', chat: '\\[NPC\\] Bednom', settle: 4000 },
            { label: 'Dirt Guy talks', tp: [43, 108, 175], npc: 'Dirt Guy', chat: '\\[NPC\\] Dirt Guy' },
            { label: 'Guard Gornum talks', tp: [-158, 149, -14], npc: 'Gornum|Guard', chat: 'Grubber|Mithril' },
            { label: 'Mithril Grubber spawns', tp: [-145, 149, -30], commands: ['/spawn MITHRIL_GRUBBER'], entity: 'Grubber' },
            { label: 'Marigold talks', tp: [181, 150, 60], npc: 'Marigold', chatOrWindow: true },
            { label: 'Queen Mismyla talks', tp: [126, 195, 195], npc: 'Mismyla|Queen', chat: '\\[NPC\\]' },
            { label: 'Fred talks', tp: [-2, 148, -68], npc: 'Fred|Forge Foreman', chat: '\\[NPC\\]' },
            { label: 'Dalir is clickable', tp: [-44, 192, 47], npc: 'Dalir', chat: '\\[NPC\\] Dalir' },
            { label: 'Thondin no longer responds', tp: [-47, 192, 47], npc: 'Thondin', chatNone: true },
        ],
    },
};

async function performUse(bot, matcher) {
    await bot.closeWindow();
    await bot.bot.look(bot.bot.entity.yaw, Math.PI / 2, true);
    await sleep(500);
    await bot.useHotbarItem(matcher, { settleMs: 1500 });
    await sleep(1500);
}

module.exports = {
    description: 'Loose Ends update (2026-11-04) checks. SB_SECTION=hub|items|end|dwarven',
    async run(t, bot) {
        const section = PLANS[process.env.SB_SECTION || 'hub'];
        const step = t.step.bind(t);
        t.step = async (name, fn) => step(name, fn).catch(() => {});
        await t.step('reach SkyBlock', async () => bot.joinSkyblock());
        for (const command of (process.env.SB_SETUP ? JSON.parse(process.env.SB_SETUP) : [])) await bot.command(command, { settleMs: command.startsWith('/max') ? 10000 : 1500 });
        if (section.warp) {
            await t.step(`${section.warp} reaches the island`, async () => {
                await bot.command('/set coins 1', { settleMs: 1500 });
                bot.say(section.warp);
                await bot.waitFor(() => new RegExp(section.sidebar, 'i').test(sidebarText(bot)), { timeoutMs: 240000, what: `sidebar /${section.sidebar}/` });
                await sleep(5000);
                return bot.sidebar()?.lines;
            });
        }
        for (const check of section.checks) {
            await t.step(check.label, async () => {
                await bot.closeWindow();
                const mark = bot.markChat();
                const result = {};
                if (check.custom === 'pigmen') {
                    await bot.command('/set stat clear all', { settleMs: 1200 });
                    await bot.command('/tp 52 101 15', { settleMs: 4000 });
                    await bot.command('/spawn REDSTONE_PIGMAN 2', { settleMs: 2500 });
                    result.spawnChat = clean(bot.chatSince(mark)).slice(-2);
                    let pig = null;
                    for (let i = 0; i < 40 && !(pig = bot.nearestEntity('Pigman')); i++) await sleep(500);
                    t.assert(pig, 'no Redstone Pigman near the Pigmen\'s Den lift');
                    const near = () => bot.command(`/tp ${Math.floor(pig.position.x) + 2} ${Math.floor(pig.position.y)} ${Math.floor(pig.position.z)}`, { settleMs: 500 });
                    await near();
                    const calmMark = bot.markChat();
                    const before = bot.bot.health;
                    let lowest = before;
                    for (let i = 0; i < 30; i++) { await sleep(500); lowest = Math.min(lowest, bot.bot.health); }
                    result.calm = { before, lowest, chat: clean(bot.chatSince(calmMark)).slice(-4) };
                    t.assert(lowest >= before, `took damage while not provoking: ${JSON.stringify(result.calm)}`);
                    const angryMark = bot.markChat();
                    pig = bot.nearestEntity('Pigman') || pig;
                    await near();
                    await bot.attack(pig).catch(() => {});
                    let angryLowest = bot.bot.health;
                    for (let i = 0; i < 24; i++) { await sleep(500); angryLowest = Math.min(angryLowest, bot.bot.health); }
                    result.angry = { lowest: angryLowest, chat: clean(bot.chatSince(angryMark)).slice(-6) };
                    t.assert(angryLowest < before || result.angry.chat.some((line) => /Pigm|angry|hostile|attack/i.test(line)), `no aggro after hitting: ${JSON.stringify(result.angry)}`);
                    return result;
                }
                if (check.custom === 'node' || check.custom === 'endstone') {
                    await bot.command('/item DIAMOND_PICKAXE', { settleMs: 1500 });
                    await bot.command('/set stat health 99999', { settleMs: 800 });
                    await bot.command('/set stat defense 99999', { settleMs: 800 });
                    let block = null;
                    for (const spot of [[-690, 8, -280], [-660, 8, -250], [-720, 8, -300], [-650, 8, -320]]) {
                        await bot.command(`/tp ${spot.join(' ')}`, { settleMs: 4000 });
                        block = bot.bot.findBlock({ matching: (b) => b.name === (check.custom === 'node' ? 'purple_terracotta' : 'end_stone'), maxDistance: 48 });
                        if (block) break;
                    }
                    t.assert(block, 'no Ender Node (purple terracotta) found near the Nest');
                    result.node = block.position.toString();
                    const p = block.position;
                    await bot.command(`/tp ${p.x} ${p.y + 1} ${p.z}`, { settleMs: 2500 });
                    const pick = bot.bot.inventory.items().find((item) => /pickaxe/.test(item.name));
                    if (pick) await bot.bot.equip(pick, 'hand');
                    const digMark = bot.markChat();
                    const target = bot.bot.blockAt(p);
                    result.before = target?.name;
                    await bot.bot.lookAt(p.offset(0.5, 0.5, 0.5), true);
                    bot.bot._client.write('block_dig', { status: 0, location: p, face: 1, sequence: 1 });
                    for (let i = 0; i < 60 && !bot.chatSince(digMark).some((line) => /ENDER NODE|Endermite Nest/i.test(line)) && bot.bot.blockAt(p)?.name === result.before; i++) {
                        bot.bot.swingArm('right');
                        await sleep(250);
                    }
                    bot.bot._client.write('block_dig', { status: 2, location: p, face: 1, sequence: 2 });
                    await sleep(1500);
                    result.after = bot.bot.blockAt(p)?.name;
                    result.chat = clean(bot.chatSince(digMark)).slice(-6);
                    if (check.custom === 'endstone') { t.assert(result.after !== 'end_stone', `End Stone did not break: ${JSON.stringify(result)}`); return result; }
                    t.assert(result.chat.some((line) => /ENDER NODE|Endermite Nest/i.test(line)), `no ENDER NODE message; ${JSON.stringify(result)}`);
                    return result;
                }
                if (check.custom === 'protector') {
                    const spots = [[-726, 6, -283], [-688, 6, -271], [-643, 7, -269], [-677, 6, -331], [-637, 6, -326], [-647, 6, -217]];
                    let found = null;
                    for (const spot of spots) {
                        await bot.command(`/tp ${spot.join(' ')}`, { settleMs: 3500 });
                        found = bot.nearestEntity('Protector');
                        if (found) { result.spot = spot; break; }
                    }
                    t.assert(found, 'no End Stone Protector at any spawn point');
                    await bot.command('/set stat health 99999', { settleMs: 1000 });
                    await bot.command('/set stat defense 99999', { settleMs: 1000 });
                    await bot.command('/set stat strength 50000', { settleMs: 1000 });
                    await bot.command('/set stat critical_damage 50000', { settleMs: 1000 });
                    await bot.command('/set stat critical_chance 100', { settleMs: 1000 });
                    const killMark = bot.markChat();
                    for (let i = 0; i < 120; i++) {
                        const target = bot.nearestEntity('Protector');
                        if (!target) break;
                        if (target.position.distanceTo(bot.bot.entity.position) > 4) await bot.command(`/tp ${Math.floor(target.position.x)} ${Math.floor(target.position.y)} ${Math.floor(target.position.z + 2)}`, { settleMs: 600 });
                        await bot.attack(target).catch(() => {});
                        await sleep(550);
                        if (bot.chatSince(killMark).some((line) => /DOWN!/.test(line))) break;
                    }
                    await sleep(4000);
                    result.killChat = clean(bot.chatSince(killMark)).filter((line) => !/slain by/.test(line)).slice(-14);
                    t.assert(bot.chatSince(killMark).some((line) => /DOWN!/.test(line)), 'no DOWN! message');
                    return result;
                }
                if (check.tp) await bot.command(`/tp ${check.tp.join(' ')}`, { settleMs: check.settle ?? 2500 });
                if (check.walkTo) {
                    await bot.bot.lookAt(new (require('vec3').Vec3)(...check.walkTo), true);
                    bot.bot.setControlState('forward', true);
                    await sleep(1200);
                    bot.bot.setControlState('forward', false);
                    await sleep(check.settle ?? 2000);
                }
                for (const command of check.commands ?? []) await bot.command(command, { settleMs: check.settle ?? 2000 });
                if (check.use) await performUse(bot, check.use);
                for (const extra of check.then ?? []) {
                    for (const command of extra.commands ?? []) await bot.command(command, { settleMs: 2000 });
                    if (extra.use) await performUse(bot, extra.use);
                }
                if (check.npc) {
                    let entity = null;
                    await bot.waitFor(() => (entity = check.npc.split('|').map((label) => bot.findNpc(label)).find(Boolean)), { timeoutMs: 20000, what: `NPC /${check.npc}/` });
                    await bot.walkNear(entity).catch(() => {});
                    await bot.interact(entity);
                    await bot.waitWindow({ timeoutMs: check.window || check.chatOrWindow ? 6000 : 1500 }).catch(() => {});
                    await sleep(check.settle ?? 2500);
                }
                if (check.window) await bot.waitWindow({ timeoutMs: 6000 }).catch(() => {});
                const view = bot.window();
                result.window = view ? { title: view.title, rows: view.containerSlots / 9, items: view.slots.filter((s) => s.name.trim()).map((s) => `${s.slot}:${s.name}`).slice(0, 30) } : null;
                if (check.window) {
                    t.assert(view && new RegExp(check.window, 'i').test(view.title), `window ${JSON.stringify(view?.title)}`);
                    const probe = await bot.detectInertMenu().catch(() => null);
                    t.assert(!probe || !probe.lootable, 'menu is inert/lootable');
                }
                if (check.clickSlot != null || check.clicksFind) {
                    const slot = check.clickSlot ?? view?.slots.find((s) => check.clicksFind.test(s.name))?.slot;
                    t.assert(slot != null, 'no slot to click');
                    await bot.click(slot, { settleMs: 2500 });
                    const child = bot.window();
                    result.child = child ? { title: child.title, items: child.slots.filter((s) => s.name.trim()).map((s) => `${s.slot}:${s.name}`).slice(0, 30) } : null;
                    if (check.childWindow) t.assert(child && new RegExp(check.childWindow, 'i').test(child.title), `child window ${JSON.stringify(child?.title)}`);
                    const probe = child ? await bot.detectInertMenu().catch(() => null) : null;
                    t.assert(!probe || !probe.lootable, 'child menu inert');
                }
                if (check.path) {
                    result.path = [];
                    for (const hop of check.path) {
                        await bot.waitWindow({ timeoutMs: 15000 }).catch(() => {});
                        if (!bot.window() && check.use) {
                            await bot.useHotbarItem(check.use, { settleMs: 800 });
                            await bot.waitWindow({ timeoutMs: 15000 }).catch(() => {});
                        }
                        const here = bot.window();
                        const slot = hop.slot ?? here?.slots.find((entry) => hop.find.test(entry.name))?.slot;
                        t.assert(here && slot != null, `no window/slot for ${JSON.stringify(hop)}; window ${here?.title}`);
                        await bot.click(slot, { settleMs: 2500 });
                        const next = bot.window();
                        result.path.push({ title: next?.title, items: next?.slots.filter((entry) => entry.name.trim()).map((entry) => `${entry.slot}:${entry.name}`).slice(0, 24) });
                        t.assert(next && new RegExp(hop.title, 'i').test(next.title), `expected /${hop.title}/ got ${next?.title}`);
                        const probe = await bot.detectInertMenu().catch(() => null);
                        t.assert(!probe || !probe.lootable, `${next.title} is inert`);
                    }
                }
                const chat = clean(bot.chatSince(mark));
                result.chat = chat.slice(-8);
                if (check.chat) t.assert(chat.some((line) => new RegExp(check.chat).test(line)), `no chat /${check.chat}/: ${JSON.stringify(chat.slice(-6))}`);
                if (check.windowItem) t.assert(view && view.slots.some((entry) => check.windowItem.test(entry.name)), `no ${check.windowItem} in ${JSON.stringify(result.window?.items)}`);
                if (check.chatNone) t.assert(chat.length === 0 || !chat.some((line) => /Thondin/.test(line)), `unexpected chat ${JSON.stringify(chat)}`);
                if (check.chatOrWindow) t.assert(view || chat.length, 'neither chat nor window');
                if (check.windowOrChat) t.assert((view && new RegExp(check.windowOrChat, 'i').test(view.title)) || chat.some((line) => new RegExp(check.windowOrChat, 'i').test(line)), `neither /${check.windowOrChat}/`);
                if (check.inventory) {
                    const items = bot.bot.inventory.items().map((item) => bot.describeItem(item, item.slot).name);
                    t.assert(items.some((name) => check.inventory.test(name)) || chat.some((line) => /stash|Stash/.test(line)), `not in inventory: ${JSON.stringify(items.slice(0, 12))}`);
                }
                if (check.entityAnywhere) {
                    const names = bot.entities({ radius: 400 }).map((entity) => entity.customName || entity.type).filter(Boolean);
                    result.entities = [...new Set(names)].filter((name) => /Protector|Golem|iron_golem/i.test(name));
                    t.assert(names.some((name) => new RegExp(check.entityAnywhere, 'i').test(name)), `no ${check.entityAnywhere} loaded nearby`);
                }
                if (check.inventoryNot) {
                    const items = bot.bot.inventory.items().map((item) => bot.describeItem(item, item.slot).name);
                    t.assert(!items.some((name) => check.inventoryNot.test(name)), `still in inventory: ${JSON.stringify(items)}`);
                }
                if (check.entity) {
                    await sleep(1500);
                    const names = bot.entities({ radius: 24 }).map((entity) => entity.customName || entity.type).filter(Boolean);
                    result.entities = [...new Set(names)].slice(0, 20);
                    t.assert(names.some((name) => new RegExp(check.entity, 'i').test(name)), `no entity /${check.entity}/ among ${JSON.stringify(result.entities)}`);
                }
                return result;
            });
        }
    },
};
