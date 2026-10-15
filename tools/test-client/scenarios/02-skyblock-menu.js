module.exports = {
    description: 'Enter SkyBlock, open the SkyBlock Menu, list its slots, click one, and check the menu is not lootable',
    async run(t, bot) {
        await t.step('click the SkyBlock NPC and reach the island', async () => {
            const sidebar = await bot.joinSkyblock();
            return { title: sidebar.title, lines: sidebar.lines };
        });

        await t.step('right-clicking the SkyBlock Menu item opens the menu', async () => {
            await bot.useHotbarItem(/SkyBlock Menu/);
            const window = await bot.waitWindow({ timeoutMs: 15000, title: 'SkyBlock Menu' });
            const view = bot.window();
            t.assert(view.slots.length >= 10, `only ${view.slots.length} filled slots`);
            t.notes.menu = view.slots.map((slot) => ({ slot: slot.slot, name: slot.name, lore: slot.lore.length }));
            return { title: view.title, filled: view.slots.length };
        });

        await t.step('the menu is not lootable (no inert-menu failure)', async () => {
            const probe = await bot.detectInertMenu();
            t.assert(!probe.lootable, `a click picked an item up off the menu: ${JSON.stringify(probe)}`);
            return probe;
        });

        await t.step('clicking Your Skills opens another view', async () => {
            const target = bot.findSlot(/Your Skills/);
            t.assert(target, 'no Your Skills slot');
            const before = bot.window().title;
            await bot.click(target.slot, { settleMs: 1500 });
            const after = bot.window();
            t.assert(after, 'window closed after click');
            t.assert(after.title !== before, `title still ${before}`);
            return { from: before, to: after.title, slots: after.slots.length };
        });
    },
};
