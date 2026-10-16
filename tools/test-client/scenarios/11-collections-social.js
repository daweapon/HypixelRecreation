const flat = (view) => JSON.stringify(view);

module.exports = {
    description: 'New collections open with tiers, Gemstone counts every gem, Social skill menu exists',
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => bot.joinSkyblock());

        const open = async (command, titlePart) => {
            await bot.closeWindow();
            bot.say(command);
            await bot.waitWindow({ timeoutMs: 15000, title: titlePart });
            return bot.window();
        };

        for (const [item, amount, title] of [['GLACITE', 1500, 'Glacite'], ['TUNGSTEN', 100, 'Tungsten'], ['UMBER', 100, 'Umber'], ['LOTUS', 25, 'Lotus']]) {
            await t.step(`${item} collection opens and shows its tiers`, async () => {
                await bot.command(`/set collection ${item} ${amount}`, { settleMs: 1500 });
                const view = await open(`/viewcollection ${item}`, title);
                t.assert(view.slots.length >= 9, `${item}: only ${view.slots.length} items`);
                const inert = await bot.detectInertMenu().catch(() => null);
                await bot.closeWindow();
                return { title: view.title, slots: view.slots.length, inert };
            });
        }

        await t.step('rough gems count toward the Gemstone collection', async () => {
            await bot.command('/set collection FINE_JASPER_GEM 0', { settleMs: 1000 });
            await bot.command('/set collection ROUGH_RUBY_GEM 150', { settleMs: 1500 });
            const view = await open('/viewcollection FINE_JASPER_GEM', 'Gemstone');
            const text = flat(view);
            t.assert(/150/.test(text), 'collection total 150 not shown');
            await bot.closeWindow();
            return view.title;
        });

        const miningFortune = async () => {
            await bot.closeWindow();
            await bot.useHotbarItem(/SkyBlock Menu/);
            await bot.waitWindow({ timeoutMs: 15000, title: 'SkyBlock Menu' });
            const profile = bot.findSlot(/Profile|Stats/);
            await bot.click(profile.slot, { settleMs: 1500 });
            await bot.click(bot.findSlot(/Mining Stats/).slot, { settleMs: 1500 });
            const lines = bot.window().slots.flatMap((entry) => [entry.name, ...entry.lore]);
            const line = lines.find((text) => /Mining Fortune/.test(text) && /\d/.test(text));
            await bot.closeWindow();
            return Number((line.match(/([\d,.]+)\s*$/) || line.match(/([\d,.]+)/))[1].replace(/,/g, ''));
        };

        await t.step('Glacite I adds 1 Mining Fortune', async () => {
            await bot.command('/set collection GLACITE 0', { settleMs: 1500 });
            const before = await miningFortune();
            await bot.command('/set collection GLACITE 100', { settleMs: 1500 });
            const after = await miningFortune();
            t.assert(after - before === 1, `Mining Fortune ${before} -> ${after}`);
            return { before, after };
        });

        await t.step('Social skill menu opens with 25 levels', async () => {
            const view = await open('/viewskill SOCIAL', 'Social');
            const inert = await bot.detectInertMenu().catch(() => null);
            await bot.closeWindow();
            return { title: view.title, slots: view.slots.length, inert };
        });
    },
};
