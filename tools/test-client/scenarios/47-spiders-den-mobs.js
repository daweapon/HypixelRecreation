const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const SPIDER_NAMES = ['dasher spider', 'splitter spider', 'weaver spider', 'voracious spider', 'spider jockey', 'jockey skeleton', 'flint skeleton', 'broodmother', 'silverfish'];

module.exports = {
    description: "Spider's Den: zones are named, spiders spawn on the Spider Mound and in Arachne's Burrow, skeletons in the Gravel Mines, a Splitter Spider splits into Silverfish",
    async run(t, bot) {
        await t.step('reach SkyBlock and warp to the Spider\'s Den', async () => {
            await bot.joinSkyblock();
            await bot.command('/max profile', { settleMs: 8000 });
            const mark = bot.markChat();
            bot.say('/warp spider');
            await bot.waitFor(() => /Spider/i.test((bot.sidebar()?.lines ?? []).join(' ')), { timeoutMs: 90000, what: 'the Spider\'s Den sidebar' })
                .catch((error) => { throw new Error(`${error.message} chat: ${bot.chatSince(mark).filter((line) => !/TPS/.test(line)).join(' | ')}`); });
        });

        const zones = [
            { zone: /Spider Mound/i, at: [-300, 95, -252], mobs: ['dasher spider', 'splitter spider', 'weaver spider', 'voracious spider', 'spider jockey'] },
            { zone: /Arachne's Burrow/i, at: [-230, 80, -260], mobs: ['dasher spider', 'splitter spider', 'weaver spider', 'voracious spider', 'spider jockey'] },
            { zone: /Gravel Mines/i, at: [-280, 87, -320], mobs: ['flint skeleton'] },
        ];
        for (const entry of zones) {
            await t.step(`${entry.zone} is named and has its mobs`, async () => {
                await bot.command(`/tp ${entry.at.join(' ')}`, { settleMs: 3000 });
                await bot.waitFor(() => entry.zone.test((bot.sidebar()?.lines ?? []).join(' ')), { timeoutMs: 15000, what: `the ${entry.zone} sidebar` });
                await bot.waitFor(() => bot.entities({ radius: 70, names: entry.mobs }).length >= 2, { timeoutMs: 90000, what: `mobs near ${entry.at}` });
                return bot.entities({ radius: 70, names: entry.mobs }).slice(0, 5).map((mob) => mob.customName);
            });
        }

        await t.step('a one-hit sword kills a spider and it drops loot', async () => {
            await bot.command('/tp -300 95 -252', { settleMs: 3000 });
            await bot.command('/item ONE_HIT_SWORD', { settleMs: 1500 });
            const sword = bot.bot.inventory.items().find((item) => /one hit/i.test(JSON.stringify(item.customName ?? item.displayName)));
            t.assert(sword, 'no sword in the inventory');
            await bot.bot.equip(sword, 'hand');
            await bot.waitFor(() => bot.entities({ radius: 70, names: ['splitter spider'] }).length > 0, { timeoutMs: 90000, what: 'a Splitter Spider' });
            const mark = bot.markChat();
            for (let swing = 0; swing < 25; swing++) {
                const target = bot.entities({ radius: 70, names: ['splitter spider'] })[0];
                if (!target) break;
                await bot.command(`/tp ${Math.round(target.x)} ${Math.round(target.y)} ${Math.round(target.z) + 2}`, { settleMs: 400 });
                const entity = bot.nearestEntity('splitter spider');
                if (entity) await bot.attack(entity);
                await sleep(600);
                if (bot.entities({ radius: 6, names: ['silverfish'] }).length) break;
            }
            await sleep(1500);
            t.assert(bot.entities({ radius: 20, names: ['silverfish'] }).length >= 1, 'no Silverfish after the Splitter Spider died');
            return bot.chatSince(mark).filter((line) => !/TPS/.test(line)).slice(-4);
        });

        await t.step('the Broodmother waits at the top of the Mound', async () => {
            await bot.command('/tp -231 121 -307', { settleMs: 4000 });
            await bot.waitFor(() => bot.entities({ radius: 30, names: ['broodmother'] }).length > 0, { timeoutMs: 60000, what: 'the Broodmother' });
        });
    },
};
