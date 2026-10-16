const titleText = (window) => JSON.stringify(window.title);

module.exports = {
    description: "Elizabeth's Account & Profile Upgrades: open, start free tiers, one per scope, skip with Gems, Magic Find applies",
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => bot.joinSkyblock());

        await t.step('give 5k Gems from the Toy Box', async () => {
            bot.say('/centauri');
            await bot.waitWindow({ timeoutMs: 20000 });
            await bot.click(15, { button: 0, mode: 0 });
            await bot.waitFor(() => /Toy Box/.test(titleText(bot.window())), { timeoutMs: 10000, what: 'Toy Box' });
            await bot.click(22, { button: 0, mode: 0 });
            await bot.waitFor(() => /Currencies/.test(titleText(bot.window())), { timeoutMs: 10000, what: 'Currencies menu' });
            await bot.click(bot.findSlot(/5k Gems/).slot, { settleMs: 1200 });
            await bot.closeWindow();
        });

        const magicFind = async () => {
            await bot.closeWindow();
            await bot.useHotbarItem(/SkyBlock Menu/);
            await bot.waitWindow({ timeoutMs: 15000, title: 'SkyBlock Menu' });
            await bot.click(bot.findSlot(/Profile|Stats/).slot, { settleMs: 1500 });
            await bot.click(bot.findSlot(/Misc Stats/).slot, { settleMs: 1500 });
            const lines = bot.window().slots.flatMap((entry) => [entry.name, ...entry.lore]);
            const line = lines.find((text) => /Magic Find/.test(text) && /\d/.test(text));
            await bot.closeWindow();
            return Number(line.match(/([\d,.]+)\s*$/)[1].replace(/,/g, ''));
        };

        let before;
        await t.step('read Magic Find before', async () => {
            before = await magicFind();
            return before;
        });

        await t.step('go to the Hub and open Elizabeth', async () => {
            let arrived = false;
            for (let attempt = 0; attempt < 4 && !arrived; attempt++) {
                const mark = bot.markChat();
                bot.say('/hub');
                arrived = await bot.waitChat(/Server Notice/, { since: mark, timeoutMs: 45000 }).then(() => true, () => false);
            }
            t.assert(arrived, 'never reached the Hub');
            await new Promise((resolve) => setTimeout(resolve, 8000));
            bot.say('/tp -6 80 17');
            const npc = await bot.waitFor(() => bot.findNpc('Elizabeth'), { timeoutMs: 40000, what: 'Elizabeth' });
            await bot.walkNear(npc);
            await bot.interact(npc);
            await bot.waitWindow({ timeoutMs: 15000, title: 'Community Shop' });
            return bot.window().title;
        });

        await t.step('Account & Profile Upgrades tab lists the upgrades', async () => {
            await bot.click(2, { settleMs: 1500 });
            const view = bot.window();
            const names = view.slots.filter((entry) => [19, 20, 21, 22, 23, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40].includes(entry.slot)).map((entry) => entry.name);
            t.assert(names.length === 16, `expected 16 upgrades, got ${names.length}: ${names}`);
            const inert = await bot.detectInertMenu().catch(() => null);
            return { names, inert };
        });

        await t.step('start Magic Find I, a second Account upgrade is refused', async () => {
            const mf = bot.window().slots.find((entry) => entry.name.includes('Magic Find'));
            await bot.click(mf.slot, { settleMs: 1500 });
            const armor = bot.window().slots.find((entry) => entry.name.includes('Armor Wardrobe'));
            const lore = armor.lore.join(' ');
            t.assert(/Another Account upgrade is underway/.test(lore), `Armor Wardrobe lore: ${lore}`);
            const running = bot.window().slots.find((entry) => entry.name.includes('Magic Find')).lore.join(' ');
            t.assert(/Underway/.test(running), `Magic Find lore: ${running}`);
            return running;
        });

        await t.step('start Minion Slots on the Profile row', async () => {
            const minions = bot.window().slots.find((entry) => entry.name.includes('Minion Slots'));
            await bot.click(minions.slot, { settleMs: 1500 });
            const lore = bot.window().slots.find((entry) => entry.name.includes('Minion Slots')).lore.join(' ');
            t.assert(/Underway/.test(lore), `Minion Slots lore: ${lore}`);
            return lore;
        });

        await t.step('Coins Allowance I: start, skip, and the daily payout arrives', async () => {
            const minions = bot.window().slots.find((entry) => entry.name.includes('Minion Slots'));
            await bot.click(minions.slot, { button: 0, mode: 1, settleMs: 2000 });
            const allowance = bot.window().slots.find((entry) => entry.name.includes('Coins Allowance'));
            await bot.click(allowance.slot, { settleMs: 1500 });
            const mark = bot.markChat();
            await bot.click(allowance.slot, { button: 0, mode: 1, settleMs: 2000 });
            const lore = bot.window().slots.find((entry) => entry.name.includes('Coins Allowance')).lore.join(' ');
            t.assert(/Tier: 1\/5/.test(lore), `Coins Allowance lore after skip: ${lore}`);
            const paid = await bot.waitChat(/Coins Allowance paid you/, { since: mark, timeoutMs: 40000 });
            return paid;
        });

        await t.step('shift-click skips Magic Find I for Gems', async () => {
            const mf = bot.window().slots.find((entry) => entry.name.includes('Magic Find'));
            const mark = bot.markChat();
            await bot.click(mf.slot, { button: 0, mode: 1, settleMs: 2000 });
            t.notes.skipChat = bot.chatSince(mark);
            const lore = bot.window().slots.find((entry) => entry.name.includes('Magic Find')).lore.join(' ');
            t.assert(/Tier: 1\/5/.test(lore), `Magic Find lore after skip: ${lore}`);
            return lore;
        });

        for (const [name, scopeNote] of [['Island Size', 'profile'], ['Bazaar Flipper', 'account']]) {
            await t.step(`${name} I starts and skips (${scopeNote})`, async () => {
                const entry = bot.window().slots.find((slot) => slot.name.includes(name));
                await bot.click(entry.slot, { settleMs: 1500 });
                await bot.click(entry.slot, { button: 0, mode: 1, settleMs: 2000 });
                const lore = bot.window().slots.find((slot) => slot.name.includes(name)).lore.join(' ');
                t.assert(/Tier: 1\//.test(lore), `${name} lore after skip: ${lore}`);
                return lore;
            });
        }

        await t.step('Magic Find went up by 1', async () => {
            const after = await magicFind();
            t.assert(after - before === 1, `Magic Find ${before} -> ${after}`);
            return { before, after };
        });
    },
};
