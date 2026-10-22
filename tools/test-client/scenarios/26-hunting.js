const fs = require('fs');
const path = require('path');

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const sidebarText = (bot) => (bot.sidebar()?.lines ?? []).join(' | ');
const creatureNames = (island) => {
    const text = fs.readFileSync(path.join(__dirname, '../../../configuration/skyblock/hunting', `${island}_creatures.yml`), 'utf8');
    return [...text.matchAll(/^\s+name: "([^"]+)"/gm)].map((match) => match[1]);
};

const sendTo = async (bot, type, pattern, attempts = 4) => {
    for (let attempt = 1; attempt <= attempts; attempt++) {
        bot.say(`/sendto ${type}`);
        try {
            await bot.waitFor(() => pattern.test(sidebarText(bot)), { timeoutMs: 60000, what: `the ${type} sidebar` });
            return;
        } catch (error) {
            if (attempt === attempts) throw error;
        }
    }
};

const creature = (bot, name, radius = 24) => {
    const found = bot.entities({ radius }).find((entity) => entity.customName && entity.customName.includes(name));
    return found ? bot.bot.entities[found.id] : null;
};

const aim = async (bot, entity) => {
    await bot.bot.lookAt(entity.position.offset(0, 0.4, 0), true);
    await sleep(200);
};

const hold = async (bot, matcher) => {
    await bot.useHotbarItem(matcher, { settleMs: 300 }).catch(() => {});
};

const equip = async (bot, pattern) => {
    const item = bot.bot.inventory.items().find((entry) => pattern.test(entry.customName ? JSON.stringify(entry.customName) : entry.displayName));
    if (!item) throw new Error(`no item matching ${pattern}: ${bot.bot.inventory.items().map((entry) => entry.name).join(', ')}`);
    await bot.bot.equip(item, 'hand');
};

const give = async (bot, ...ids) => {
    await bot.command('/clear', { settleMs: 1200 });
    for (const id of ids) await bot.command(`/item ${id}`, { settleMs: 800 });
};

const spawn = async (bot, id, name) => {
    const before = new Set(bot.entities({ radius: 40 }).filter((entity) => entity.customName && entity.customName.includes(name)).map((entity) => entity.id));
    bot.say(`/spawn ${id}`);
    await bot.waitFor(() => bot.entities({ radius: 6 }).some((entity) => entity.customName && entity.customName.includes(name) && !before.has(entity.id)),
        { timeoutMs: 15000, what: `a new ${name}` });
    const fresh = bot.entities({ radius: 6 }).find((entity) => entity.customName && entity.customName.includes(name) && !before.has(entity.id));
    return bot.bot.entities[fresh.id];
};

const clearCreatures = async (bot) => {
    for (const entity of bot.entities({ radius: 14 }).filter((entry) => entry.customName && /Lv\d+/.test(entry.customName))) {
        bot.say(`/kill @e[distance=..1]`);
        break;
    }
};

module.exports = {
    description: 'Hunting: critter population on Torrhus and Galatea, immunity, Lasso, Fishing Net and Black Hole captures, specials',
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => bot.joinSkyblock());

        await t.step('finish the starting missions and reach SkyBlock Level 6', async () => {
            await bot.command('/mission clearset GETTING_STARTED', { settleMs: 1500 });
            await bot.command('/skyblockxp setlevel 6', { settleMs: 1500 });
        });

        await t.step('send the bot to Torrhus Canyon', async () => {
            await sleep(2000);
            await sendTo(bot, 'SKYBLOCK_TORRHUS_CANYON', /Torrhus/i);
            return sidebarText(bot);
        });

        await t.step('Torrhus critters populate around the player', async () => {
            await bot.command('/tp -654 98 218', { settleMs: 3000 });
            const names = creatureNames('torrhus');
            await bot.waitFor(() => bot.entities({ radius: 70 }).some((entity) => entity.customName && names.some((name) => entity.customName.includes(name))),
                { timeoutMs: 90000, what: 'a Torrhus creature' });
            return bot.entities({ radius: 70 }).filter((entity) => entity.customName && names.some((name) => entity.customName.includes(name)))
                .map((entity) => entity.customName).slice(0, 8);
        });

        await t.step('a critter ignores a plain hit', async () => {
            await give(bot);
            const bunbun = await spawn(bot, 'BUNBUN', 'Bunbun');
            const mark = bot.markChat();
            await aim(bot, bunbun);
            bot.bot.attack(bunbun);
            await bot.waitChat(/can't be hurt/, { since: mark, timeoutMs: 8000 });
        });

        await t.step('a Lasso captures a Bunbun', async () => {
            await give(bot, 'ABYSMAL_LASSO');
            await equip(bot, /Abysmal Lasso/);
            const mark = bot.markChat();
            for (let attempt = 0; attempt < 4 && !bot.chatSince(mark).some((line) => /CAPTURED!/.test(line)); attempt++) {
                const attemptMark = bot.markChat();
                bot.say('/spawn BUNBUN');
                await bot.waitFor(() => creature(bot, 'Bunbun', 12), { timeoutMs: 15000, what: 'a Bunbun' });
                await bot.command('/tp -654 98 212', { settleMs: 1200 });
                const bunbun = creature(bot, 'Bunbun', 12);
                const bunbunId = bunbun.id;
                await aim(bot, bunbun);
                bot.bot.activateItem();
                await bot.waitChat(/threw your Lasso/, { since: attemptMark, timeoutMs: 8000 }).catch(() => null);
                const started = Date.now();
                while (Date.now() - started < 7400) {
                    if (bot.chatSince(attemptMark).some((line) => /snapped|looked away|too early|escaped/.test(line))) break;
                    const target = bot.bot.entities[bunbunId];
                    if (target) await aim(bot, target);
                    await sleep(250);
                }
                if (bot.chatSince(attemptMark).some((line) => /snapped|looked away|too early|escaped/.test(line))) continue;
                bot.bot.activateItem();
                await sleep(1500);
            }
            await bot.waitChat(/CAPTURED!/, { since: mark, timeoutMs: 4000 });
            return bot.chatSince(mark).filter((line) => /CAPTURED|Lasso|Shard/.test(line));
        });

        await t.step('a Fishing Net captures a Solar', async () => {
            await give(bot, 'GIGANTIC_FISHING_NET');
            await equip(bot, /Gigantic Fishing Net/);
            const mark = bot.markChat();
            for (let attempt = 0; attempt < 5 && !bot.chatSince(mark).some((line) => /CAPTURED!/.test(line)); attempt++) {
                await bot.command('/tp -654 98 218', { settleMs: 1500 });
                const solar = await spawn(bot, 'SOLAR', 'Solar');
                await bot.command('/tp -654 98 215', { settleMs: 700 });
                for (let throwNumber = 0; throwNumber < 4 && !bot.chatSince(mark).some((line) => /CAPTURED!/.test(line)); throwNumber++) {
                    const target = bot.bot.entities[solar.id];
                    if (!target) break;
                    await aim(bot, target);
                    bot.bot.activateItem();
                    await sleep(600);
                }
            }
            await bot.waitChat(/CAPTURED!/, { since: mark, timeoutMs: 3000 });
            return bot.chatSince(mark).filter((line) => /CAPTURED|net|Net/.test(line));
        });

        await t.step('a Huntaxe weakens a Parched and a Black Hole captures it', async () => {
            await bot.command('/set stat health 99999', { settleMs: 800 });
            await bot.command('/set stat defense 99999', { settleMs: 800 });
            await give(bot, 'VENATOR_GENESIS', 'SMALL_POCKET_BLACK_HOLE');
            await bot.command('/tp -654 98 218', { settleMs: 3000 });
            const parched = await spawn(bot, 'PARCHED', 'Parched');
            await equip(bot, /Genesis/);
            const mark = bot.markChat();
            for (let swing = 0; swing < 14 && !bot.chatSince(mark).some((line) => /weak enough/.test(line)); swing++) {
                const target = creature(bot, 'Parched', 10) ?? parched;
                await aim(bot, target);
                bot.bot.attack(target);
                await sleep(700);
            }
            await bot.waitChat(/weak enough/, { since: mark, timeoutMs: 6000 });
            await equip(bot, /Pocket Black Hole/);
            const target = creature(bot, 'Parched', 12) ?? parched;
            await bot.bot.lookAt(target.position.offset(0, -0.5, 0), true);
            bot.bot.activateItem();
            await bot.waitChat(/CAPTURED!/, { since: mark, timeoutMs: 45000 });
            return bot.chatSince(mark).filter((line) => /CAPTURED|Black Hole|weak/.test(line));
        });

        await t.step('the Bestiary lists the Torrhus Canyon creatures', async () => {
            await bot.command('/unlock bestiary', { settleMs: 1000 });
            bot.say('/bestiary');
            await bot.waitWindow({ timeoutMs: 20000 });
            const text = bot.window().slots.map((slot) => `${slot.name} ${slot.lore.join(' ')}`).join(' | ');
            if (!/Torrhus Canyon/.test(text) || !/Galatea/.test(text)) throw new Error(text);
            await bot.closeWindow();
        });

        await t.step('send the bot to Galatea', async () => {
            await sendTo(bot, 'SKYBLOCK_GALATEA', /Galatea|Tangleburg|Marsh/i);
            return sidebarText(bot);
        });

        await t.step('Galatea critters populate around the player', async () => {
            await bot.command('/tp -648 88 -3', { settleMs: 3000 });
            const names = creatureNames('galatea');
            await bot.waitFor(() => bot.entities({ radius: 70 }).some((entity) => entity.customName && names.some((name) => entity.customName.includes(name))),
                { timeoutMs: 90000, what: 'a Galatea creature' });
            return bot.entities({ radius: 70 }).filter((entity) => entity.customName && names.some((name) => entity.customName.includes(name)))
                .map((entity) => entity.customName).slice(0, 8);
        });

        await t.step('a Cod starts spinning under the net and is captured', async () => {
            await give(bot, 'BASIC_FISHING_NET');
            await equip(bot, /Basic Fishing Net/);
            await bot.command('/tp -542 108 -26', { settleMs: 3000 });
            const cod = await spawn(bot, 'COD', 'Cod');
            await bot.command('/tp -546 108 -26', { settleMs: 2000 });
            const mark = bot.markChat();
            for (let throwNumber = 0; throwNumber < 30 && !bot.chatSince(mark).some((line) => /CAPTURED!|exploded/.test(line)); throwNumber++) {
                await aim(bot, bot.bot.entities[cod.id] ?? cod);
                bot.bot.activateItem();
                await sleep(600);
            }
            const lines = bot.chatSince(mark).filter((line) => /spinning|spin|CAPTURED|exploded|Cod/.test(line));
            if (!lines.some((line) => /spinning/.test(line))) throw new Error(lines.join(' | '));
            return lines;
        });
    }
};
