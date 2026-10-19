const { plain } = require('../lib/text');
const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

const ACTIVATED = ['BLAZE_BELT', 'VANQUISHED_BLAZE_BELT', 'MAGMA_NECKLACE', 'VANQUISHED_MAGMA_NECKLACE', 'SCOVILLE_BELT',
    'DELIRIUM_NECKLACE', 'LAVA_SHELL_NECKLACE', 'TERA_SHELL_NECKLACE', 'FLAMING_FIST', 'IMPLOSION_BELT',
    'GAUNTLET_OF_CONTAGION', 'VANQUISHED_GLOWSTONE_GAUNTLET', 'ANCIENT_CLOAK', 'SCOURGE_CLOAK', 'MOODY_GRAPPLESHOT',
    'SULPHUR_BOW', 'SLUG_BOOTS', 'MAGMA_LORD_NECKLACE', 'MAGMA_FISH_HAT', 'HOT_BAIT', 'CORRUPTED_BAIT',
    'BLAZETEKK_HAM_RADIO', 'TACTICAL_INSERTION', 'ALERT_FLARE', 'SOS_FLARE', 'UPGRADE_STONE_FROST',
    'UPGRADE_STONE_GLACIAL', 'UPGRADE_STONE_SUBZERO'];

module.exports = {
    description: 'Crimson activation: activated items give with no Not-active marker, Big Pull, Ham Radio tuning, Tactical Insertion, Slug Boots bounce',
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => bot.joinSkyblock());
        await bot.command('/clear', { settleMs: 800 });

        const lore = {};
        await t.step('every activated item gives and carries no Not-active marker', async () => {
            const bad = [];
            for (const id of ACTIVATED) {
                const mark = bot.markChat();
                await bot.command(`/item ${id}`, { settleMs: 500 });
                if (!/Given you item/.test(JSON.stringify(bot.chatSince(mark)))) bad.push(`${id}: not given`);
            }
            await sleep(800);
            for (const item of bot.bot.inventory.items()) {
                const d = bot.describeItem(item, 0);
                lore[d.name] = d.lore.join(' | ');
                if (/not active yet|isn't available yet|isn.t active/i.test(lore[d.name])) bad.push(`${d.name}: ${lore[d.name].match(/.{0,40}(not active yet|available yet|active).{0,40}/i)?.[0]}`);
            }
            t.assert(bad.length === 0, bad.join(' || '));
            return Object.keys(lore).length;
        });

        const held = async (re) => {
            const item = bot.bot.inventory.items().find((i) => re.test(bot.describeItem(i, 0).name));
            t.assert(item, `no item ${re}; have ${bot.bot.inventory.items().map((i) => bot.describeItem(i, 0).name).join(" , ")}`);
            await bot.bot.equip(item, 'hand');
            await sleep(600);
            return item;
        };
        const chatAfter = async (action, ms = 2500) => {
            const mark = bot.markChat();
            await action();
            await sleep(ms);
            return bot.chatSince(mark).join(' | ');
        };

        await t.step('Ham Radio right-click cycles the channel', async () => {
            const item = await held(/Blazetekk.*Ham Radio/i);
            const before = bot.describeItem(bot.bot.heldItem, 0).lore.join('|');
            const chat = await chatAfter(() => bot.bot.activateItem());
            const after = bot.describeItem(bot.bot.heldItem, 0).lore.join('|');
            t.assert(before !== after || /channel/i.test(chat), `lore unchanged and no chat: ${chat}`);
            return { chat, after: after.match(/Channel[^|]*/i)?.[0] };
        });

        await t.step('Big Pull casts on a spawned Blaze', async () => {
            await bot.command('/spawn BLAZE 2', { settleMs: 2500 });
            await held(/Moody Grappleshot/i);
            const chat = await chatAfter(() => bot.bot.activateItem(), 2000);
            t.assert(!/error|exception/i.test(chat), chat);
            return { chat, blazes: bot.entities({ radius: 20 }).length };
        });

        await t.step('Tactical Insertion returns you at half health', async () => {
            await bot.command('/set stat health 1000', { settleMs: 800 });
            await held(/Tactical Insertion/i);
            const start = bot.bot.health;
            const chat = await chatAfter(() => bot.bot.activateItem(), 4500);
            return { start, end: bot.bot.health, chat };
        });

        await t.step('Slug Boots bounce when landing sneaking from height', async () => {
            const boots = bot.bot.inventory.items().find((i) => /Slug Boots/i.test(bot.describeItem(i, 0).name));
            t.assert(boots, 'no Slug Boots');
            await bot.bot.equip(boots, 'feet');
            const p = bot.bot.entity.position;
            bot.bot.setControlState('sneak', true);
            const chat = await chatAfter(() => bot.say(`/tp ${Math.floor(p.x)} ${Math.floor(p.y) + 25} ${Math.floor(p.z)}`), 5000);
            bot.bot.setControlState('sneak', false);
            return { chat, y: bot.bot.entity.position.y, startY: p.y, health: bot.bot.health };
        });

        await t.step('/max profile still completes', async () => {
            const lines = await bot.command('/max profile', { settleMs: 8000 });
            t.assertChat(lines, /MAXED/i, 'expected MAXED');
            t.assert(!/PARTLY/i.test(lines.join(' ')), lines.join(' '));
        });
    },
};
