const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const sidebarText = (bot) => (bot.sidebar()?.lines ?? []).join(' | ');
const titleText = (window) => JSON.stringify(window.title);
const names = (bot) => bot.window().slots.map((slot) => `${slot.name} ${slot.lore.join(' ')}`).join(' | ');

module.exports = {
    description: 'Forest essence shop: Trapped, Axed and Tasty are buyable; harvesting Lushlilac bushes spawns Birries',
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => bot.joinSkyblock());

        await t.step('finish the starting missions and unlock the Heart of the Forest', async () => {
            await bot.command('/mission clearset GETTING_STARTED', { settleMs: 1500 });
            await bot.command('/skyblockxp setlevel 6', { settleMs: 1500 });
            bot.say('/centauri');
            await bot.waitWindow({ timeoutMs: 20000 });
            await bot.click(15, { button: 0, mode: 0 });
            await bot.waitFor(() => /Toy Box/.test(titleText(bot.window())), { timeoutMs: 10000, what: 'the Toy Box window' });
            await bot.click(24, { button: 0, mode: 0 });
            await bot.waitFor(() => /Toy Box - Foraging/.test(titleText(bot.window())), { timeoutMs: 10000, what: 'the Foraging Toy Box' });
            const unlock = bot.findSlot(/Unlock Heart of the Forest/);
            t.assert(unlock, names(bot));
            await bot.click(unlock.slot ?? bot.window().slots.indexOf(unlock), { button: 0, mode: 0 });
            await bot.closeWindow();
        });

        await t.step('send the bot to Galatea', async () => {
            await sleep(3000);
            for (let attempt = 1; attempt <= 3; attempt++) {
                bot.say('/sendto SKYBLOCK_GALATEA');
                try {
                    await bot.waitFor(() => /Galatea|Tangleburg|Marsh/i.test(sidebarText(bot)), { timeoutMs: 70000, what: 'the Galatea sidebar' });
                    break;
                } catch (error) {
                    if (attempt === 3) throw error;
                }
            }
            return sidebarText(bot);
        });

        await t.step('Rosemary sells Trapped, Axed and Tasty and a purchase works', async () => {
            await bot.command('/set essence FOREST 100000', { settleMs: 1500 });
            await bot.command('/tp -703 162 5', { settleMs: 3000 });
            await bot.waitFor(() => bot.findNpc('Rosemary'), { timeoutMs: 20000, what: 'Rosemary' });
            await bot.interact(bot.findNpc('Rosemary'));
            await bot.waitWindow({ timeoutMs: 20000 });
            const text = names(bot);
            for (const perk of ['Trapped', 'Axed', 'Tasty']) {
                const entry = bot.window().slots.find((slot) => slot.name.includes(perk));
                t.assert(entry, `${perk} missing: ${text}`);
                t.assert(!entry.lore.some((line) => /isn't in this server|Unavailable/i.test(line)), `${perk} flagged unavailable: ${entry.lore.join(' | ')}`);
            }
            const trapped = bot.window().slots.find((slot) => slot.name.includes('Trapped'));
            const mark = bot.markChat();
            await bot.click(trapped.slot, { button: 0, mode: 0, settleMs: 1200 });
            const chat = bot.chatSince(mark).join(' | ');
            const after = bot.window()?.slots.find((slot) => slot.name.includes('Trapped'));
            await bot.closeWindow();
            t.assert(/Trapped|purchased|unlocked|bought/i.test(chat) || /1%|II/.test(`${after?.name} ${after?.lore.join(' ')}`), `no purchase confirmation: ${chat}`);
            return chat;
        });

        await t.step('harvesting Lushlilac bushes can spawn Birries (10% per bush)', async () => {
            await bot.command('/tp -700 125 10', { settleMs: 4000 });
            await bot.waitFor(() => bot.bot.findBlock({ matching: (block) => block.name === 'flowering_azalea', maxDistance: 120 }), { timeoutMs: 20000, what: 'a flowering azalea bush' });
            let harvested = 0;
            let seen = false;
            for (let attempt = 0; attempt < 60 && !seen; attempt++) {
                const target = bot.bot.findBlock({ matching: (block) => block.name === 'flowering_azalea', maxDistance: 120 });
                if (!target) break;
                await bot.command(`/tp ${target.position.x} ${target.position.y + 1} ${target.position.z + 1}`, { settleMs: 900 });
                await bot.bot.activateBlock(bot.bot.blockAt(target.position));
                harvested++;
                await sleep(500);
                seen = bot.entities({ radius: 12 }).some((entity) => /Birries/i.test(entity.customName ?? ''));
            }
            t.assert(seen, `no Birries after ${harvested} bushes (chance of that is ${(0.9 ** harvested * 100).toFixed(1)}%)`);
            return `Birries seen after ${harvested} bushes`;
        });
    },
};
