const INNER = new Set();
for (let row = 1; row <= 4; row++) for (let col = 1; col <= 7; col++) INNER.add(row * 9 + col);
const NAV = new Set([4, 48, 49]);

const occupied = (bot, rows) => {
    const window = bot.window();
    const out = [];
    for (let slot = 0; slot < rows * 9; slot++) {
        const item = window.slots[slot];
        if (item && item.name.trim() !== '') out.push(slot);
    }
    return out;
};

const titleText = (window) => JSON.stringify(window.title);

module.exports = {
    description: 'Centauri Toy Box: category buttons open sub-menus, every button sits in the inner 7x4 grid, a button still grants',
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => bot.joinSkyblock());

        await t.step('/centauri then Toy Box opens', async () => {
            bot.say('/centauri');
            await bot.waitWindow({ timeoutMs: 20000 });
            await bot.click(15, { button: 0, mode: 0 });
            await bot.waitFor(() => /Toy Box/.test(titleText(bot.window())), { timeoutMs: 10000, what: 'the Toy Box window' });
            const inert = await bot.detectInertMenu().catch(() => null);
            return { title: titleText(bot.window()), inert };
        });

        let mainSlots;
        await t.step('main menu: only the grid and the nav row hold items', async () => {
            mainSlots = occupied(bot, 6);
            const stray = mainSlots.filter((slot) => !INNER.has(slot) && !NAV.has(slot));
            t.assert(stray.length === 0, `items outside the grid: ${stray}`);
            t.assert(mainSlots.filter((slot) => INNER.has(slot)).length === 13, `expected 7 general + 5 category + 1 loose button, got ${mainSlots}`);
            return mainSlots;
        });

        for (const [index, name] of [[7, 'Mining'], [8, 'Garden'], [9, 'Experimentation'], [10, 'Currencies'], [11, 'Dungeons']]) {
            await t.step(`${name} category opens with every button inside the grid`, async () => {
                const slot = 19 + (index - 7);
                await bot.click(slot, { button: 0, mode: 0 });
                await bot.waitFor(() => new RegExp(`Toy Box - ${name}`).test(titleText(bot.window())), { timeoutMs: 10000, what: `${name} menu` });
                const inert = await bot.detectInertMenu().catch(() => null);
                const used = occupied(bot, 6);
                const stray = used.filter((s) => !INNER.has(s) && !NAV.has(s));
                t.assert(stray.length === 0, `${name}: items outside the grid: ${stray}`);
                t.assert(used.filter((s) => INNER.has(s)).length >= 1, `${name}: no buttons`);
                if (name !== 'Dungeons') {
                    await bot.click(48, { button: 0, mode: 0 });
                    await bot.waitFor(() => /^\{.*Toy Box"/.test(titleText(bot.window())) || /"Toy Box"/.test(titleText(bot.window())), { timeoutMs: 10000, what: 'back to the main Toy Box' });
                }
                return { title: titleText(bot.window()), buttons: used.length, inert };
            });
        }

        await t.step('a Dungeons button still grants Catacombs XP', async () => {
            const mark = bot.markChat();
            await bot.click(10, { button: 0, mode: 0 });
            await bot.waitChat(/Centauri granted your request/i, { since: mark, timeoutMs: 10000 });
            return bot.chatSince(mark);
        });
    },
};
