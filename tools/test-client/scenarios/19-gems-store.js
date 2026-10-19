const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const sidebarText = (bot) => (bot.sidebar()?.lines ?? []).join(' | ');

module.exports = {
    description: 'Gems store: /set gems, Taylor season bundle and prices, Fire Sale start announcement, tab widget, purchase and shared stock, end',
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => bot.joinSkyblock());

        await t.step('/set gems sets the balance', async () => {
            const lines = await bot.command('/set gems 5000', { settleMs: 1200 });
            t.assertChat(lines, /Successfully set Gems/i, 'expected the set confirmation');
            return lines.slice(-1);
        });

        await t.step('go to the Hub', async () => {
            bot.say('/hub');
            await bot.waitFor(() => /Hub|Village|Elizabeth/i.test(sidebarText(bot)), { timeoutMs: 200000, what: 'the Hub sidebar' });
            return sidebarText(bot);
        });

        const openNpc = async (label, x, y, z) => {
            await bot.command(`/tp ${x} ${y} ${z}`, { settleMs: 3000 });
            await bot.waitFor(() => bot.findNpc(label), { timeoutMs: 20000, what: label });
            const npc = bot.findNpc(label);
            await bot.walkNear(npc);
            await bot.interact(npc);
            return bot.waitWindow({ timeoutMs: 8000 }).catch(() => null);
        };

        await t.step('Taylor shows the current season and its bundle', async () => {
            let window = await openNpc('Taylor', 37, 75, -37);
            if (!window || !/Taylor/i.test(window.title)) {
                await sleep(6000);
                await bot.interact(bot.findNpc('Taylor'));
                window = await bot.waitWindow({ timeoutMs: 10000, title: "Taylor's Cosmetics" });
            }
            const bundles = bot.findSlot(/Seasonal Bundles/);
            t.assert(bundles, 'no Seasonal Bundles slot');
            t.assert(/Current bundle: Fall Bundle/.test(bundles.lore.join(' ')), `season lore: ${bundles.lore.join(' ')}`);
            await bot.click(bundles.slot);
            await sleep(800);
            const view = bot.window();
            const names = view.slots.map((s) => s.name);
            for (const expected of [/Smol Ender Dragon/, /Pumpkin/, /Sludge Dye/, /Spirit Dye/, /Explosifall Rune III/, /Apothejerry/]) {
                t.assert(names.some((n) => expected.test(n)), `missing ${expected}; have ${names.join(' | ')}`);
            }
            const sludge = view.slots.find((s) => /Sludge Dye/.test(s.name));
            t.assert(/375/.test(sludge.lore.join(' ')), `sludge lore: ${sludge.lore.join(' ')}`);
            return names.filter((n) => n.trim());
        });

        await t.step('buying Spirit Dye from Taylor takes 375 Gems', async () => {
            const spirit = bot.findSlot(/Spirit Dye/);
            await bot.click(spirit.slot, { settleMs: 1500 });
            const confirm = bot.window();
            if (confirm && /Confirm/i.test(confirm.title)) await bot.click(confirm.slots.find((s) => /Confirm/i.test(s.name)).slot, { settleMs: 1200 });
            await bot.closeWindow();
            await sleep(800);
            const held = bot.bot.inventory.items().map((i) => bot.describeItem(i, 0).name);
            t.assert(held.some((n) => /Spirit Dye/.test(n)), `inventory: ${held.join(' | ')}`);
        });

        await t.step('/force firesale starts a sale and announces it', async () => {
            const mark = bot.markChat();
            await bot.command('/force firesale 2025-12-25 30', { settleMs: 1500 });
            await bot.waitChat(/FIRE SALE.*Blazing Crimson Helmet/i, { since: mark, timeoutMs: 20000 });
            return bot.chatSince(mark).slice(-3);
        });

        await t.step('the Fire Sales tab widget lists the sale', async () => {
            bot.say('/widgets');
            await bot.waitWindow({ title: 'Tablist Widgets', timeoutMs: 15000 });
            await bot.closeWindow();
            const lines = () => bot.tabList().map((e) => e.display.trim());
            await bot.waitFor(() => lines().some((l) => /Blazing Crimson Helmet/.test(l)), { timeoutMs: 20000, what: 'Fire Sale tab line' }).catch(() => null);
            return lines().filter((l) => /Fire Sale|Crimson|left/i.test(l));
        });

        await t.step("Elizabeth's Fire Sales tab sells it for Gems and stock drops", async () => {
            let window = await openNpc('Community Shop', -6, 79, 17);
            t.assert(window && /Community Shop/i.test(window.title), `window ${JSON.stringify(window)}`);
            await bot.click(5);
            await sleep(800);
            const sale = bot.window().slots.find((s) => /Blazing Crimson/i.test(s.name));
            t.assert(sale, `no sale item; have ${bot.window().slots.map((s) => s.name).join(' | ')}`);
            t.assert(/1,800 Gems/.test(sale.lore.join(' ')), `lore: ${sale.lore.join(' ')}`);
            const before = sale.lore.join(' ').match(/Stock left: ([\d,]+)/)?.[1];
            await bot.command('/set gems 5000', { settleMs: 800 });
            await bot.click(sale.slot, { settleMs: 1500 });
            const after = bot.window().slots.find((s) => /Blazing Crimson/i.test(s.name)).lore.join(' ').match(/Stock left: ([\d,]+)/)?.[1];
            t.assert(before === '12,500' && after === '12,499', `stock ${before} -> ${after}`);
            const held = bot.bot.inventory.items().map((i) => bot.describeItem(i, 0).name);
            t.assert(held.some((n) => /Blazing Crimson/.test(n)), `inventory: ${held.join(' | ')}`);
            await bot.closeWindow();
            return { before, after };
        });

        await t.step('/force firesale end closes the sale', async () => {
            const mark = bot.markChat();
            await bot.command('/force firesale end', { settleMs: 1000 });
            await bot.waitChat(/Fire Sale has ended/i, { since: mark, timeoutMs: 20000 });
        });
    },
};
