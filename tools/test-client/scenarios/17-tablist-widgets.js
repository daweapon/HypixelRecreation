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
            await waitTab(/^Area:/);
            await waitTab(/^Server:/);
            await waitTab(/^Profile:/);
            await waitTab(/^Skills:/);
            await waitTab(/^Stats:/);
            t.assert(!hasLine(/Pet Sitter/), 'Pet Sitter line still shown');
            return lines().slice(0, 40);
        });

        await t.step('/widgets opens island picker, Private Island list, Stats settings', async () => {
            bot.say('/widgets');
            await bot.waitWindow({ title: 'Tablist Widgets', timeoutMs: 15000 });
            await bot.click(slotOf(/Private Island/));
            await bot.waitWindow({ title: 'Widgets on Private Island', timeoutMs: 10000 });
            await bot.click(slotOf(/^Stats/));
            await bot.waitWindow({ title: 'Stats Widget Settings', timeoutMs: 10000 });
            return bot.window().slots.map((item) => item.name);
        });

        await t.step('disabling Stats removes it from the tab within 8 s', async () => {
            await bot.click(slotOf(/Enabled/));
            await bot.waitFor(() => !hasLine(/^Stats:/), { timeoutMs: 8000, what: 'Stats gone from tab' });
        });

        await t.step('re-enabling Stats brings it back', async () => {
            await bot.waitWindow({ title: 'Stats Widget Settings', timeoutMs: 5000 });
            await bot.click(slotOf(/Enabled/));
            await waitTab(/^Stats:/, 8000);
        });

        await t.step('Move Earlier changes order without error', async () => {
            await bot.click(slotOf(/Move Earlier/));
            await bot.waitWindow({ title: 'Stats Widget Settings', timeoutMs: 5000 });
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
