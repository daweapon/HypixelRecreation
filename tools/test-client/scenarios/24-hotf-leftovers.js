const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const sidebarText = (bot) => (bot.sidebar()?.lines ?? []).join(' | ');
const titleText = (window) => JSON.stringify(window.title);
const names = (bot) => bot.window().slots.map((slot) => `${slot.name} ${slot.lore.join(' ')}`).join(' | ');

module.exports = {
    description: 'Galatea: Hina chapters menu and tasks, Quests & Chapters menu, Agatha gate, Lushlilac bush and sea pickle gathering',
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => bot.joinSkyblock());

        await t.step('finish the starting missions and reach SkyBlock Level 6', async () => {
            await bot.command('/mission clearset GETTING_STARTED', { settleMs: 1500 });
            await bot.command('/skyblockxp setlevel 6', { settleMs: 1500 });
        });

        await t.step('Toy Box unlocks the Heart of the Forest', async () => {
            bot.say('/centauri');
            await bot.waitWindow({ timeoutMs: 20000 });
            await bot.click(15, { button: 0, mode: 0 });
            await bot.waitFor(() => /Toy Box/.test(titleText(bot.window())), { timeoutMs: 10000, what: 'the Toy Box window' });
            await bot.click(24, { button: 0, mode: 0 });
            await bot.waitFor(() => /Toy Box - Foraging/.test(titleText(bot.window())), { timeoutMs: 10000, what: 'the Foraging Toy Box' });
            const unlock = bot.findSlot(/Unlock Heart of the Forest/);
            t.assert(unlock, names(bot));
            const chapters = bot.findSlot(/Complete Hina's Chapters/);
            t.assert(chapters, 'Complete Hina\'s Chapters button missing');
            await bot.click(unlock.slot ?? bot.window().slots.indexOf(unlock), { button: 0, mode: 0 });
            await bot.closeWindow();
        });

        await t.step('send the bot to Galatea', async () => {
            await sleep(3000);
            bot.say('/sendto SKYBLOCK_GALATEA');
            await bot.waitFor(() => /Galatea|Tangleburg|Marsh/i.test(sidebarText(bot)), { timeoutMs: 60000, what: 'the Galatea sidebar' }).catch(async () => {
                bot.say('/sendto SKYBLOCK_GALATEA');
                await bot.waitFor(() => /Galatea|Tangleburg|Marsh/i.test(sidebarText(bot)), { timeoutMs: 90000, what: 'the Galatea sidebar' });
            });
            return sidebarText(bot);
        });

        await t.step('/chapters opens Hina\'s Chapters with seven chapters', async () => {
            bot.say('/chapters');
            await bot.waitWindow({ timeoutMs: 20000 });
            const inert = await bot.detectInertMenu().catch(() => null);
            const text = names(bot);
            for (let n = 1; n <= 7; n++) t.assert(new RegExp(`Chapter ${n}\\b`).test(text), `chapter ${n} missing: ${text}`);
            return { title: titleText(bot.window()), inert };
        });

        await t.step('chapter 1 lists Swoop, the Fusion Machine and David', async () => {
            const slot = bot.window().slots.find((entry) => /Chapter 1\b/.test(entry.name));
            await bot.click(slot.slot ?? bot.window().slots.indexOf(slot), { button: 0, mode: 0 });
            await sleep(800);
            const text = names(bot);
            t.assert(/Swoop/.test(text) && /Fusion Machine/.test(text) && /David/.test(text), text);
            await bot.closeWindow();
        });

        await t.step('clicking Hina opens the Chapters menu', async () => {
            await bot.command('/tp -553 110 -14', { settleMs: 3000 });
            await bot.waitFor(() => bot.findNpc('Hina'), { timeoutMs: 20000, what: 'Hina' });
            await bot.interact(bot.findNpc('Hina'));
            await bot.waitWindow({ timeoutMs: 30000 });
            t.assert(/Chapter/.test(names(bot)), names(bot));
            await bot.closeWindow();
        });

        await t.step('SkyBlock Menu has Quests & Chapters with both buttons', async () => {
            await bot.useHotbarItem(/SkyBlock Menu/i);
            await bot.waitWindow({ timeoutMs: 20000 });
            await bot.click(23, { button: 0, mode: 0 });
            await bot.waitFor(() => /Quests & Chapters/.test(titleText(bot.window())), { timeoutMs: 10000, what: 'Quests & Chapters' });
            const text = names(bot);
            t.assert(/Chapters/.test(text) && /Quest Log/.test(text), text);
            await bot.click(20, { button: 0, mode: 0 });
            await bot.waitFor(() => /Chapters/.test(titleText(bot.window())) && !/Quests/.test(titleText(bot.window())), { timeoutMs: 10000, what: 'the Chapters menu' });
            await bot.closeWindow();
        });

        await t.step('Agatha holds her contest back until the introductions are done', async () => {
            await bot.command('/tp -648 89 -1', { settleMs: 3000 });
            await bot.waitFor(() => bot.findNpc('Agatha'), { timeoutMs: 20000, what: 'Agatha' });
            const mark = bot.markChat();
            await bot.interact(bot.findNpc('Agatha'));
            const line = await bot.waitChat(/Agatha/i, { since: mark, timeoutMs: 30000 });
            return line;
        });

        await t.step('a Lushlilac bush can be harvested and regrows', async () => {
            const bush = () => bot.bot.findBlock({ matching: (block) => block.name === 'flowering_azalea', maxDistance: 120 });
            await bot.command('/tp -700 125 10', { settleMs: 4000 });
            await bot.waitFor(() => bush(), { timeoutMs: 20000, what: 'a flowering azalea bush' });
            const target = bush();
            await bot.command(`/tp ${target.position.x} ${target.position.y + 1} ${target.position.z + 1}`, { settleMs: 3000 });
            const mark = bot.markChat();
            await bot.bot.activateBlock(bot.bot.blockAt(target.position));
            await bot.waitFor(() => bot.bot.blockAt(target.position)?.name === 'azalea', { timeoutMs: 10000, what: 'the bush to be harvested' });
            await bot.waitFor(() => bot.bot.blockAt(target.position)?.name === 'flowering_azalea', { timeoutMs: 60000, what: 'the bush to regrow' });
            return { at: target.position.toString(), chat: bot.chatSince(mark) };
        });

        await t.step('a sea pickle can be harvested and returns', async () => {
            const pickle = () => bot.bot.findBlock({ matching: (block) => block.name === 'sea_pickle', maxDistance: 120 });
            await bot.command('/tp -650 40 10', { settleMs: 4000 });
            await bot.waitFor(() => pickle(), { timeoutMs: 20000, what: 'a sea pickle' });
            const target = pickle();
            await bot.command(`/tp ${target.position.x} ${target.position.y + 2} ${target.position.z}`, { settleMs: 3000 });
            await bot.bot.activateBlock(bot.bot.blockAt(target.position));
            await bot.waitFor(() => bot.bot.blockAt(target.position)?.name !== 'sea_pickle', { timeoutMs: 10000, what: 'the pickle to be harvested' });
            await bot.waitFor(() => bot.bot.blockAt(target.position)?.name === 'sea_pickle', { timeoutMs: 90000, what: 'the pickle to return' });
            return target.position.toString();
        });
    }
};
