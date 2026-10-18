const Vec3 = require('vec3').Vec3;
const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const { plain } = require('../lib/text');

module.exports = {
    description: 'Runecrafting: pedestal fusion chance lore, level and item checks, XP, skill menu',
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => bot.joinSkyblock());

        await t.step('go to the Hub pedestal', async () => {
            let arrived = false;
            for (let attempt = 0; attempt < 4 && !arrived; attempt++) {
                const mark = bot.markChat();
                bot.say('/hub');
                arrived = await bot.waitChat(/Server Notice/, { since: mark, timeoutMs: 45000 }).then(() => true, () => false);
            }
            t.assert(arrived, 'never reached the Hub');
            await sleep(8000);
            bot.say('/tp 23 66 -131');
            await sleep(4000);
        });

        await t.step('give runes and a sword', async () => {
            const notes = [];
            for (const cmd of ['/item SNOW_RUNE', '/item SNOW_RUNE', '/item LIGHTNING_RUNE', '/item GEM_RUNE', '/item ASPECT_OF_THE_END']) {
                const mark = bot.markChat();
                await bot.command(cmd, { settleMs: 1000 });
                notes.push(cmd + ' => ' + plain(bot.chatSince(mark)));
            }
            return notes;
        });

        const invSlot = (re) => {
            const w = bot.bot.currentWindow;
            const found = bot.bot.inventory.items().find((item) => re.test(bot.describeItem(item, 0).name));
            return found ? w.inventoryStart + (found.slot - 9) : -1;
        };
        const place = async (re, target) => {
            const slot = invSlot(re);
            if (slot < 0) {
                const w = bot.bot.currentWindow;
                const names = [`start=${w.inventoryStart} len=${w.slots.length} invCount=${bot.bot.inventory.items().length}`, ...bot.bot.inventory.items().map((i) => bot.describeItem(i, 0).name)]; const _x = w.slots.slice(w.inventoryStart).filter(Boolean).map((i) => i.name + ':' + (i.customName ? plain(i.customName) : i.displayName));
                t.assert(false, `no inventory item matching ${re}; have ${names.join(' | ')}`);
            }
            await bot.click(slot, { settleMs: 500 });
            await bot.click(target, { settleMs: 800 });
        };

        await t.step('open the Runic Pedestal', async () => {
            const block = bot.bot.blockAt(new Vec3(23, 64, -135));
            await bot.bot.activateBlock(block);
            await bot.waitWindow({ timeoutMs: 15000, title: 'Runic Pedestal' });
            return bot.window().title;
        });

        await t.step('Lightning Rune on a sword is refused (level and item)', async () => {
            await place(/Aspect.*End/i, 19);
            await place(/Lightning/, 25);
            const view = bot.window();
            const text = JSON.stringify(view.slots.filter((s) => s.slot === 31));
            t.assert(/Error/.test(text), `slot 31: ${text}`);
            t.assert(/Runecrafting level|can only be applied/.test(text), `reason missing: ${text}`);
            return text;
        });

        await t.step('take the items back', async () => {
            await bot.click(19, { settleMs: 500 });
            await bot.click(25, { settleMs: 500 });
        });

        await t.step('two Snow Runes show the fusion chance', async () => {
            await place(/Snow/, 19);
            await place(/Snow/, 25);
            const combine = bot.window().slots.find((s) => s.slot === 13);
            const text = JSON.stringify(combine);
            t.assert(/Success chance/.test(text) && /25%/.test(text), text);
            return text;
        });

        await t.step('fusing awards Runecrafting XP', async () => {
            const mark = bot.markChat();
            await bot.click(13, { settleMs: 3500 });
            await bot.waitChat(/Runecrafting XP/, { since: mark, timeoutMs: 10000 });
            return bot.chatSince(mark).slice(-3);
        });

        await t.step('shift-click moves a rune from the inventory into the pedestal', async () => {
            const slot = invSlot(/Snow/);
            t.assert(slot >= 0, 'no Snow Rune');
            await bot.click(slot, { mode: 1, button: 0, settleMs: 1000 });
            const left = bot.window().slots.find((s) => s.slot === 19);
            t.assert(left && /Snow/.test(left.name), `slot 19: ${JSON.stringify(left)}`);
            await bot.click(19, { settleMs: 500 });
            return left.name;
        });

        await t.step('Gem Rune on a sword is refused (bows only)', async () => {
            await place(/Aspect.*End/i, 19);
            await place(/Gem/, 25);
            const text = JSON.stringify(bot.window().slots.filter((s) => s.slot === 31));
            t.assert(/bows/.test(text), text);
            return text;
        });

        await t.step('Runecrafting shows 25 levels in the Skills menu', async () => {
            await bot.closeWindow();
            await bot.command('/set xp 0', { settleMs: 300 }).catch(() => {});
            await bot.useHotbarItem(/SkyBlock Menu/);
            await bot.waitWindow({ timeoutMs: 15000, title: 'SkyBlock Menu' });
            await bot.click(bot.findSlot(/Your Skills/).slot, { settleMs: 1500 });
            const entry = bot.findSlot(/Runecrafting/);
            t.assert(entry, 'no Runecrafting entry');
            await bot.click(entry.slot, { settleMs: 1500 });
            const text = JSON.stringify(bot.window());
            t.assert(/25|XXV/.test(text), 'no level 25 listed');
            return bot.window().title;
        });
    },
};
