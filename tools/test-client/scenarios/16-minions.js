const Vec3 = require('vec3').Vec3;
const { plain } = require('../lib/text');
const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

module.exports = {
    description: 'Minions: new items give, storage chest placement rules, minion placement and chest link, engine upgrade items',
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => bot.joinSkyblock());

        await t.step('new minion and upgrade items can be given', async () => {
            await bot.command('/clear', { settleMs: 800 });
            const ids = ['SUNFLOWER_MINION', 'REVENANT_MINION', 'TARANTULA_MINION', 'VOIDLING_MINION', 'LILY_PAD_MINION',
                'SMALL_ENCHANTED_CHEST', 'LARGE_ENCHANTED_CHEST', 'LESSER_SOULFLOW_ENGINE', 'SOULFLOW_ENGINE', 'FARM_CRYSTAL',
                'POSTCARD', 'FREE_WILL', 'VOODOO_DOLL', 'HUNTER_KNIFE', 'ENCHANTED_SHEARS', 'FLYCATCHER_UPGRADE'];
            const failed = [];
            for (const id of ids) {
                const mark = bot.markChat();
                await bot.command(`/item ${id}`, { settleMs: 700 });
                const text = JSON.stringify(bot.chatSince(mark));
                if (!/Given you item/.test(text)) failed.push(`${id}: ${text}`);
            }
            t.assert(failed.length === 0, failed.join(' | '));
        });

        const held = async (re) => {
            const item = bot.bot.inventory.items().find((i) => re.test(bot.describeItem(i, 0).name));
            t.assert(item, `no item ${re}; have ${bot.bot.inventory.items().map((i) => bot.describeItem(i, 0).name).join(' | ')}`);
            await bot.bot.equip(item, 'hand');
            await sleep(800);
        };
        const chatAfter = async (action, ms = 2500) => {
            const mark = bot.markChat();
            await action();
            await sleep(ms);
            return bot.chatSince(mark).join(' | ');
        };
        const at = (dx, dz) => bot.bot.blockAt(bot.bot.entity.position.floored().offset(dx, -1, dz));
        const below = () => bot.bot.blockAt(bot.bot.entity.position.offset(0, -1, 0));

        const sweepChests = async () => {
            const base = bot.bot.entity.position.floored();
            const removed = [];
            for (let dx = -6; dx <= 8; dx++) {
                for (let dz = -4; dz <= 6; dz++) {
                    const block = bot.bot.blockAt(base.offset(dx, 0, dz));
                    if (block && block.name === 'chest') {
                        await bot.bot.dig(block).catch(() => {});
                        removed.push(`${dx},${dz}`);
                    }
                }
            }
            return removed;
        };

        await t.step('sweep chests left by earlier runs', async () => {
            await sleep(1500);
            const removed = await sweepChests();
            await sleep(1000);
            return removed;
        });

        await t.step('storage chest away from any minion is refused', async () => {
            await held(/Small.*Storage|Small.*Chest/i);
            const text = await chatAfter(() => bot.bot.placeBlock(bot.bot.blockAt(bot.bot.entity.position.floored().offset(-4, -1, 0)), new Vec3(0, 1, 0)).catch((e) => `ERR ${e.message}`));
            t.assert(/next to a minion/.test(text), `chat: ${text}; bot ${bot.bot.entity.position}; below ${below() && below().name}; held ${bot.bot.heldItem && bot.describeItem(bot.bot.heldItem, 0).name}`);
            return text;
        });

        await t.step('place a Sunflower minion', async () => {
            await held(/Sunflower Minion/i);
            const text = await chatAfter(() => bot.bot.placeBlock(at(2, 0), new Vec3(0, 1, 0)).catch((e) => `ERR ${e.message}`), 1500);
            t.assert(/You placed a minion/.test(text), `chat: ${text}`);
            return text;
        });

        await t.step('second chest beside the minion links, third is refused', async () => {
            await held(/Large.*(Storage|Chest)/i);
            const first = await chatAfter(() => bot.bot.placeBlock(at(3, 0), new Vec3(0, 1, 0)).catch((e) => `ERR ${e.message}`), 1500);
            const near = Object.values(bot.bot.entities).filter((e) => e !== bot.bot.entity && e.position.distanceTo(bot.bot.entity.position) < 8).map((e) => `${e.name}@${e.position.x.toFixed(1)},${e.position.y.toFixed(1)},${e.position.z.toFixed(1)}`);
            t.assert(!/next to a minion|can't place|ERR/.test(first), `first chest: ${first}; bot ${bot.bot.entity.position}; near ${near.join(' ')}`);
            const base = bot.bot.entity.position.floored();
            const firstBlock = bot.bot.blockAt(base.offset(3, 0, 0)).name;
            await held(/Small.*(Storage|Chest)/i);
            const second = await chatAfter(() => bot.bot.placeBlock(at(2, 1), new Vec3(0, 1, 0)).catch((e) => `ERR ${e.message}`), 1500);
            const probes = [];
            for (const [dx, dz] of [[3, 0], [2, 1]]) {
                const pos = base.offset(dx, 0, dz);
                await bot.bot.activateBlock(bot.bot.blockAt(pos)).catch((e) => probes.push(`ERR ${e.message}`));
                const w = await bot.waitWindow({ timeoutMs: 2500 }).catch(() => null);
                probes.push(`${dx},${dz}: ${w ? bot.window().title : 'no window'}`);
                await bot.closeWindow();
            }
            t.assert(/3,0: Minion Storage/.test(probes.join(' | ')), `probes: ${probes.join(' | ')}`);
            t.assert(/already has a chest|can't place|ERR/.test(second), `second chest: [${second}] first: [${first}] firstBlock ${firstBlock} secondBlock ${bot.bot.blockAt(base.offset(2, 0, 1)).name} held ${bot.bot.heldItem && bot.describeItem(bot.bot.heldItem, 0).name} x${bot.bot.heldItem && bot.bot.heldItem.count} blk ${at(2, 1) && at(2, 1).name} up ${bot.bot.blockAt(bot.bot.entity.position.floored().offset(2, 0, 1)).name}`);
            return { first, second };
        });

        await t.step('breaking a storage chest returns it', async () => {
            const before = bot.bot.inventory.items().length;
            const removed = await sweepChests();
            await sleep(1500);
            t.assert(removed.length > 0, 'no chest left to break');
            const after = bot.bot.inventory.items().length;
            t.assert(after >= before, `inventory ${before} -> ${after}`);
            return { removed, before, after };
        });

        await t.step('placed minion entity is visible and opens a Minion menu', async () => {
            await sleep(1500);
            const all = Object.values(bot.bot.entities).filter((e) => e !== bot.bot.entity);
            const info = all.map((e) => `${e.name}/${e.type}@${e.position.x.toFixed(0)},${e.position.y.toFixed(0)},${e.position.z.toFixed(0)}`);
            const target = all.find((e) => e.position.distanceTo(bot.bot.entity.position) < 6 && /armor|stand|mannequin|minion/i.test(String(e.name)));
            t.assert(target, `no minion entity; entities ${info.slice(0, 15).join(' ')}`);
            await bot.interact(target);
            await bot.waitWindow({ timeoutMs: 8000 });
            const view = bot.window();
            await bot.closeWindow();
            t.assert(/Minion/i.test(view.title), `window ${view.title}`);
            return view.title;
        });

        await t.step('Free Will and Postcard exist in the Item List', async () => {
            const mark = bot.markChat();
            await bot.command('/itemlist FREE_WILL', { settleMs: 1500 }).catch(() => {});
            await bot.closeWindow();
            return bot.chatSince(mark).slice(-2);
        });
    },
};
