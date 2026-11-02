const SKIP = /^(close|go back|back)$/i;

const settle = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

const hasMenuItem = (bot) => bot.bot.inventory.slots.slice(36, 45).some((item) => item && /SkyBlock Menu/.test(JSON.stringify(item.customName ?? item.displayName)));

const openMenu = async (bot) => {
    await bot.closeWindow();
    await bot.waitFor(() => hasMenuItem(bot), { timeoutMs: 8000, what: 'the SkyBlock Menu hotbar item' }).catch(() => {});
    await bot.useHotbarItem(/SkyBlock Menu/);
    await bot.waitWindow({ timeoutMs: 15000 });
};

const summarise = (bot) => {
    const view = bot.window();
    return view && { title: view.title, rows: view.containerSlots / 9, filled: view.slots.length };
};

const COMMAND_MENUS = [
    ['/viewsettings', 'Settings'],
    ['/pity', 'Pity'],
    ['/centauri', 'Centauri'],
    ['/etable', 'Enchant'],
    ['/pets', 'Pets'],
    ['/calendar', 'Calendar'],
    ['/bank', 'Bank'],
    ['/wardrobe', 'Wardrobe'],
    ['/lb', 'Leaderboard'],
    ['/chips', 'Chips'],
    ['/attributemenu', 'Attribute'],
    ['/bestiary', 'Bestiary'],
    ['/rngmeter', 'RNG'],
    ['/quiver', 'Quiver'],
    ['/accessorybag', 'Accessory'],
    ['/fishingbag', 'Fishing'],
    ['/potionbag', 'Potion'],
    ['/sacks', 'Sack'],
    ['/storage', 'Storage'],
    ['/recipes', 'Recipe'],
    ['/warp', 'Fast Travel'],
    ['/collections', 'Collection'],
    ['/skills', 'Skill'],
    ['/levels', 'Level'],
    ['/profiles', 'Profile'],
    ['/enchantmentguide', 'Enchantment Guide'],
];

module.exports = {
    description: 'GUI sweep: SkyBlock Menu click-through, command menus, Settings hub and toggles, Booster Cookie purchase, SkyBlock Guide after /max profile',
    async run(t, bot) {
        const step = t.step.bind(t);
        t.step = async (name, fn) => step(name, fn).catch(() => {});
        await step('reach SkyBlock', async () => bot.joinSkyblock());
        await t.step('/max profile', async () => {
            await bot.command('/max profile', { settleMs: 8000 });
            return bot.chatSince(0).slice(-3);
        });

        const top = [];
        await t.step('SkyBlock Menu opens and lists its tiles', async () => {
            await openMenu(bot);
            for (const slot of bot.window().slots) if (!SKIP.test(slot.name.trim()) && slot.name.trim() !== '') top.push(slot);
            return { tiles: top.length };
        });

        for (const tile of top) {
            await t.step(`SkyBlock Menu tile "${tile.name}" (slot ${tile.slot})`, async () => {
                await openMenu(bot);
                const mark = bot.markChat();
                await bot.click(tile.slot, { settleMs: 1500 });
                const result = summarise(bot);
                const chat = bot.chatSince(mark).filter((line) => /error|exception|not been added|cannot/i.test(line));
                if (result) {
                    const probe = await bot.detectInertMenu().catch(() => null);
                    t.assert(!probe || !probe.lootable, `inert menu after clicking ${tile.name}: ${JSON.stringify(probe)}`);
                }
                return { result, chat };
            });
        }

        for (const [command, expect] of COMMAND_MENUS) {
            await t.step(`${command} opens a "${expect}" menu`, async () => {
                await bot.closeWindow();
                const mark = bot.markChat();
                bot.say(command);
                await settle(2000);
                const result = summarise(bot);
                const chat = bot.chatSince(mark).slice(-2);
                t.assert(result, `no window opened; chat: ${JSON.stringify(chat)}`);
                t.assert(new RegExp(expect, 'i').test(result.title), `title was "${result.title}"`);
                const probe = await bot.detectInertMenu().catch(() => null);
                t.assert(!probe || !probe.lootable, `inert: ${JSON.stringify(probe)}`);
                return result;
            });
        }

        await t.step('Settings hub lists the seven categories and the island buttons', async () => {
            await bot.closeWindow();
            bot.say('/viewsettings');
            await bot.waitWindow({ timeoutMs: 10000, title: 'Settings' });
            const names = bot.window().slots.map((slot) => slot.name);
            for (const wanted of ['Personal', 'Comms', 'User Interface', 'Fishing', 'Sounds', 'Chat', 'API', 'Island']) {
                t.assert(names.some((name) => name.includes(wanted)), `no "${wanted}" button in ${JSON.stringify(names)}`);
            }
            return names;
        });

        const categories = ['Personal', 'Comms', 'User Interface', 'Fishing', 'Sounds', 'Chat', 'API'];
        for (const category of categories) {
            await t.step(`Settings > ${category} opens and a toggle flips and flips back`, async () => {
                await bot.closeWindow();
                bot.say('/viewsettings');
                await bot.waitWindow({ timeoutMs: 10000, title: 'Settings' });
                const tile = bot.findSlot(new RegExp(category, 'i'));
                t.assert(tile, `no ${category} tile`);
                await bot.click(tile.slot, { settleMs: 1500 });
                const view = bot.window();
                t.assert(view && view.slots.length >= 4, `${category}: only ${view?.slots.length} items`);
                const dyes = view.slots.filter((slot) => /dye/.test(slot.item));
                t.assert(dyes.length >= 1, `${category}: no toggle dyes`);
                const target = dyes[0];
                await bot.click(target.slot, { settleMs: 1200 });
                const after = bot.window().slots.find((slot) => slot.slot === target.slot);
                t.assert(after && after.item !== target.item, `${category}: toggle did not change (${target.item} -> ${after?.item})`);
                await bot.click(target.slot, { settleMs: 1200 });
                const restored = bot.window().slots.find((slot) => slot.slot === target.slot);
                t.assert(restored && restored.item === target.item, `${category}: toggle did not restore`);
                return { title: view.title, toggles: dyes.length, flipped: `${target.item} -> ${after.item}` };
            });
        }

        await t.step('Booster Cookie: buying with enough Gems gives a cookie', async () => {
            await bot.command('/set gems 1000', { settleMs: 1500 });
            await openMenu(bot);
            const tile = bot.findSlot(/Booster Cookie/);
            t.assert(tile, 'no Booster Cookie tile in the SkyBlock Menu');
            await bot.click(tile.slot, { settleMs: 1500 });
            const cookie = bot.findSlot(/^Booster Cookie$/);
            t.assert(cookie, 'no Booster Cookie item in the cookie menu');
            t.assert(cookie.lore.some((line) => /Click to purchase/.test(line)), `lore did not offer a purchase: ${JSON.stringify(cookie.lore.slice(-4))}`);
            const fame = bot.findSlot(/Fame Rank/);
            t.assert(fame && !fame.lore.some((line) => /not yet|implemented/i.test(line)), 'Fame Rank still carries the dev note');
            const mark = bot.markChat();
            await bot.click(cookie.slot, { settleMs: 2000 });
            const chat = bot.chatSince(mark);
            t.assert(chat.some((line) => /purchased a Booster Cookie/i.test(line)), `no purchase message: ${JSON.stringify(chat)}`);
            return chat.slice(-2);
        });

        await t.step('SkyBlock Levels > Guide opens after /max profile and Starter is complete', async () => {
            await openMenu(bot);
            const levels = bot.findSlot(/SkyBlock Level/);
            t.assert(levels, 'no SkyBlock Level tile');
            await bot.click(levels.slot, { settleMs: 1500 });
            const guide = bot.findSlot(/Guide/);
            t.assert(guide, 'no Guide button in the levels menu');
            await bot.click(guide.slot, { settleMs: 1800 });
            const view = bot.window();
            t.assert(view && /Guide/.test(view.title), `title was ${view?.title}`);
            const tabs = view.slots.filter((slot) => slot.slot >= 1 && slot.slot <= 7).map((slot) => ({ slot: slot.slot, name: slot.name, item: slot.item }));
            t.notes.guideTabs = tabs;
            const groups = view.slots.filter((slot) => slot.slot >= 9 && slot.slot < 45 && slot.item !== 'black_stained_glass_pane');
            t.assert(groups.length >= 4, `only ${groups.length} task groups`);
            return { title: view.title, tabs, groups: groups.length };
        });

        await t.step('Guide: each Starter group opens a task list with ticks', async () => {
            const view = bot.window();
            const groups = view.slots.filter((slot) => slot.slot >= 9 && slot.slot < 45 && slot.item !== 'black_stained_glass_pane' && !SKIP.test(slot.name.trim()));
            const opened = [];
            for (const group of groups.slice(0, 8)) {
                await bot.click(group.slot, { settleMs: 1500 });
                const sub = bot.window();
                if (sub && sub.title !== view.title) {
                    const entries = sub.slots.filter((slot) => slot.slot < 45 && slot.item !== 'black_stained_glass_pane');
                    const done = entries.filter((slot) => /✔/.test(slot.name)).length;
                    opened.push({ group: group.name, title: sub.title, entries: entries.length, done });
                    t.assert(entries.length >= 1, `${group.name} opened an empty list`);
                    const back = bot.findSlot(/Go Back/);
                    if (back) await bot.click(back.slot, { settleMs: 1200 });
                }
            }
            t.assert(opened.length >= 1, 'no group opened a sub-view');
            t.notes.guideGroups = opened;
            return opened;
        });
    },
};
