const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const sidebarText = (bot) => (bot.sidebar()?.lines ?? []).join(' | ');

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

const named = (bot, name, radius) => bot.entities({ radius }).filter((entity) => entity.customName && entity.customName.includes(name));

const spawn = async (bot, id, name) => {
    const before = new Set(named(bot, name, 40).map((entity) => entity.id));
    bot.say(`/spawn ${id}`);
    await bot.waitFor(() => named(bot, name, 6).some((entity) => !before.has(entity.id)), { timeoutMs: 15000, what: `a new ${name}` });
    return bot.bot.entities[named(bot, name, 6).find((entity) => !before.has(entity.id)).id];
};

const give = async (bot, ...ids) => {
    await bot.command('/clear', { settleMs: 1200 });
    for (const id of ids) await bot.command(`/item ${id}`, { settleMs: 800 });
};

const equip = async (bot, pattern) => {
    const item = bot.bot.inventory.items().find((entry) => pattern.test(entry.customName ? JSON.stringify(entry.customName) : entry.displayName));
    if (!item) throw new Error(`no item matching ${pattern}: ${bot.bot.inventory.items().map((entry) => entry.name).join(', ')}`);
    await bot.bot.equip(item, 'hand');
};

const approach = async (bot, entity, gap = 1.6) => {
    const at = entity.position;
    await bot.command(`/tp ${Math.round(at.x - gap)} ${Math.round(at.y)} ${Math.round(at.z)}`, { settleMs: 700 });
};

const aim = async (bot, entity) => {
    await bot.bot.lookAt(entity.position.offset(0, 0.4, 0), true);
    await sleep(200);
};

module.exports = {
    description: 'Hunting specials: Dustybit hops, Blue Jay refuses an early Lasso, Bambuleaf bamboo feeding, Behemoth Pot on a Fig tree',
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => bot.joinSkyblock());

        await t.step('finish the starting missions and reach SkyBlock Level 6', async () => {
            await bot.command('/mission clearset GETTING_STARTED', { settleMs: 1500 });
            await bot.command('/skyblockxp setlevel 6', { settleMs: 1500 });
            await bot.command('/set stat health 99999', { settleMs: 800 });
            await bot.command('/set stat defense 99999', { settleMs: 800 });
        });

        await t.step('send the bot to Torrhus Canyon', async () => {
            await sleep(2000);
            await sendTo(bot, 'SKYBLOCK_TORRHUS_CANYON', /Torrhus/i);
        });

        await t.step('a Dustybit hops away on each click and is captured after four', async () => {
            await give(bot);
            await bot.command('/tp -654 98 218', { settleMs: 2500 });
            const dustybit = await spawn(bot, 'DUSTYBIT', 'Dustybit');
            const mark = bot.markChat();
            for (let click = 0; click < 8 && !bot.chatSince(mark).some((line) => /CAPTURED!/.test(line)); click++) {
                const target = bot.bot.entities[dustybit.id];
                if (!target) break;
                await approach(bot, target);
                await aim(bot, target);
                bot.bot.activateEntity(target);
                await sleep(1500);
            }
            await bot.waitChat(/CAPTURED!/, { since: mark, timeoutMs: 6000 });
            return bot.chatSince(mark).filter((line) => /exhaust|CAPTURED|hop/i.test(line));
        });

        await t.step('a Blue Jay refuses a Lasso thrown too early', async () => {
            await give(bot, 'ABYSMAL_LASSO');
            await equip(bot, /Abysmal Lasso/);
            await bot.command('/tp -654 98 218', { settleMs: 2500 });
            const jay = await spawn(bot, 'BLUE_JAY', 'Blue Jay');
            await bot.command('/tp -654 98 214', { settleMs: 800 });
            const mark = bot.markChat();
            await aim(bot, bot.bot.entities[jay.id]);
            bot.bot.activateItem();
            await sleep(1500);
            const lines = bot.chatSince(mark).filter((line) => !/TPS/.test(line));
            if (lines.some((line) => /threw your Lasso/.test(line))) throw new Error(lines.join(' | '));
            return lines;
        });

        await t.step('send the bot to Galatea', async () => {
            await sendTo(bot, 'SKYBLOCK_GALATEA', /Galatea|Tangleburg|Marsh/i);
        });

        await t.step('feeding a Bambuleaf 40 to 44 Bamboo captures it', async () => {
            await give(bot, 'BAMBOO');
            await bot.command('/item BAMBOO 64', { settleMs: 800 });
            await equip(bot, /Bamboo/);
            await bot.command('/tp -542 108 -26', { settleMs: 3000 });
            const panda = await spawn(bot, 'BAMBULEAF', 'Bambuleaf');
            await approach(bot, bot.bot.entities[panda.id]);
            const mark = bot.markChat();
            await aim(bot, bot.bot.entities[panda.id]);
            for (let feed = 0; feed < 220 && !bot.chatSince(mark).some((line) => /CAPTURED!/.test(line)); feed++) {
                const target = bot.bot.entities[panda.id];
                if (!target) break;
                bot.bot.activateEntity(target);
                await sleep(130);
            }
            await sleep(1500);
            t.notes.bambooLeft = bot.bot.heldItem ? bot.bot.heldItem.count : 0;
            await bot.waitChat(/CAPTURED!/, { since: mark, timeoutMs: 6000 });
            return bot.chatSince(mark).filter((line) => /CAPTURED|passed out|bamboo/i.test(line)).slice(0, 4);
        });

        await t.step('a Behemoth Pot of Honeycomb on a Fig tree attracts a critter at once', async () => {
            await give(bot, 'BEHEMOTH_POT_OF_HONEYCOMB');
            await equip(bot, /Behemoth Pot/);
            await bot.command('/tp -560 112 44', { settleMs: 3500 });
            const logs = bot.bot.findBlocks({ matching: (block) => block.name === 'stripped_spruce_wood', maxDistance: 24, count: 300 })
                .filter((point) => bot.bot.blockAt(point.offset(2, 0, 0))?.name === 'air' && bot.bot.blockAt(point.offset(1, 0, 0))?.name === 'air')
                .sort((a, b) => a.y - b.y);
            if (!logs.length) throw new Error('no fig wood near Evergreen Plateau');
            const log = { position: logs[0] };
            await bot.command(`/tp ${log.position.x + 2} ${log.position.y} ${log.position.z}`, { settleMs: 2500 });
            await bot.bot.lookAt(log.position.offset(0.5, 0.5, 0.5), true);
            await sleep(400);
            const mark = bot.markChat();
            bot.bot.activateItem();
            await bot.waitChat(/lathered the tree/, { since: mark, timeoutMs: 8000 });
            await bot.waitFor(() => ['Honeyhog', 'Honeymite', 'Hewver'].some((name) => named(bot, name, 16).length), { timeoutMs: 15000, what: 'a honey critter' });
            return bot.chatSince(mark).filter((line) => /lathered|Honey|critter/i.test(line));
        });
    }
};
