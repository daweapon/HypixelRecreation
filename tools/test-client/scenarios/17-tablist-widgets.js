const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

module.exports = {
    description: 'Tablist widgets: widget tab layout, no Pet Sitter, /widgets menu toggles Stats, /viewsettings Player List Info',
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => bot.joinSkyblock());

        const lines = () => bot.tabList().map((entry) => entry.display.trim()).filter((line) => line.length > 0);
        const hasLine = (re) => lines().some((line) => re.test(line));
        const waitTab = (re, timeoutMs = 15000) => bot.waitFor(() => hasLine(re), { timeoutMs, what: `tab line ${re}` });
        const slotOf = (re) => {
            const found = bot.window().slots.find((item) => re.test(item.name));
            t.assert(found, `no slot ${re}; have ${bot.window().slots.map((item) => item.name).join(' | ')}`);
            return found.slot;
        };

        await t.step('widget tab shows Info, Area, Server, Profile, Skills, Stats', async () => {
            await waitTab(/^Info$/);
            t.assert(lines().indexOf('Info') > lines().findIndex((line) => /^Players/.test(line)), 'Info should follow the player block');
            await waitTab(/^Area:/);
            await waitTab(/^Server:/);
            await waitTab(/^Profile:/);
            await waitTab(/^Skills:/);
            await waitTab(/^Stats:/);
            t.assert(!hasLine(/Pet Sitter/), 'Pet Sitter line still shown');
            return lines().slice(0, 40);
        });

        await t.step('/widgets opens island picker then Widgets on Private Island with the video layout', async () => {
            bot.say('/widgets');
            await bot.waitWindow({ title: 'Tablist Widgets', timeoutMs: 15000 });
            await bot.click(slotOf(/Private Island/));
            await bot.waitWindow({ title: 'Widgets on Private Island', timeoutMs: 10000 });
            const view = bot.window();
            t.assert(view.slots.some((item) => /Priority Changer/.test(item.name)), 'no Priority Changer');
            t.assert(view.slots.some((item) => /Widgets Preview/.test(item.name)), 'no Preview');
            t.assert(view.slots.some((item) => /Reset All/.test(item.name)), 'no Reset All');
            t.assert(view.slots.some((item) => /General Info Widget/.test(item.name)), 'no General Info Widget');
            const stubs = view.slots.filter((item) => /Daily Quests Widget/.test(item.name));
            t.assert(stubs.length === 1 && stubs.every((item) => /UNAVAILABLE/.test(item.lore.join(' '))), `stubs: ${JSON.stringify(stubs)}`);
            return view.slots.map((item) => item.name);
        });

        await t.step('left-click disables Stats in the tab, left-click again restores it', async () => {
            await bot.click(slotOf(/Stats Widget/));
            await bot.waitFor(() => !hasLine(/^Stats:/), { timeoutMs: 8000, what: 'Stats gone from tab' });
            await bot.waitWindow({ title: 'Widgets on Private Island', timeoutMs: 5000 });
            await bot.click(slotOf(/Stats Widget/));
            await waitTab(/^Stats:/, 8000);
        });

        await t.step('right-click opens Stats Widget Settings with shown-stat toggles and Minimize', async () => {
            await bot.waitWindow({ title: 'Widgets on Private Island', timeoutMs: 5000 });
            await bot.click(slotOf(/Stats Widget/), { button: 1 });
            await bot.waitWindow({ title: 'Stats Widget Settings', timeoutMs: 10000 });
            const view = bot.window();
            t.assert(view.slots.some((item) => /Show Speed/.test(item.name)), 'no Show Speed toggle');
            t.assert(view.slots.some((item) => /Apply Settings To All Modes/.test(item.name)), 'no apply to all');
            await bot.click(slotOf(/Show Strength/));
            await bot.waitFor(() => !hasLine(/^Strength:/), { timeoutMs: 8000, what: 'Strength removed' });
            await bot.waitWindow({ title: 'Stats Widget Settings', timeoutMs: 5000 });
            await bot.click(slotOf(/Show Strength/));
            await waitTab(/^Strength:/, 8000);
            await bot.click(slotOf(/Go Back/));
            await bot.waitWindow({ title: 'Widgets on Private Island', timeoutMs: 10000 });
        });

        await t.step('Priority Changer selects a widget and shift-click reorders it', async () => {
            await bot.click(slotOf(/Priority Changer/));
            await bot.waitWindow({ title: 'Widgets on Private Island', timeoutMs: 5000 });
            const lore = bot.window().slots.find((item) => /Priority Changer/.test(item.name)).lore.join('\n');
            t.assert(/EDITING/.test(lore) && /LOCKED/.test(lore), `lore: ${lore}`);
            await bot.click(slotOf(/Priority Changer/), { button: 0, mode: 1 });
            await bot.waitWindow({ title: 'Widgets on Private Island', timeoutMs: 5000 });
        });

        await t.step('Third Column toggle and Reset All work', async () => {
            await bot.click(slotOf(/Third Column/));
            await bot.waitWindow({ title: 'Widgets on Private Island', timeoutMs: 5000 });
            await bot.click(slotOf(/Reset All/));
            await bot.waitWindow({ title: 'Widgets on Private Island', timeoutMs: 5000 });
            await waitTab(/^Stats:/, 8000);
            await bot.closeWindow();
        });

        await t.step('/viewsettings Player List Info off gives the plain list', async () => {
            bot.say('/viewsettings');
            await bot.waitWindow({ title: 'Settings', timeoutMs: 15000 });
            await bot.click(slotOf(/Player List Info/));
            await bot.waitFor(() => !hasLine(/^Info$/), { timeoutMs: 8000, what: 'plain player list' });
            await bot.waitWindow({ title: 'Settings', timeoutMs: 5000 });
            await bot.click(slotOf(/Player List Info/));
            await waitTab(/^Info$/, 8000);
            await bot.closeWindow();
        });

        await t.step('SkyBlock Menu Settings button opens the settings menu', async () => {
            await bot.useHotbarItem(/SkyBlock Menu/);
            await bot.waitWindow({ title: 'SkyBlock Menu', timeoutMs: 10000 });
            await bot.click(slotOf(/Settings/));
            await bot.waitWindow({ title: 'Settings', timeoutMs: 10000 });
            await bot.closeWindow();
        });
    },
};
