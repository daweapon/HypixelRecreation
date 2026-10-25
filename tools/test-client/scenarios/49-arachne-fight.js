const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const clean = (chat) => chat.filter((line) => !/TPS/.test(line)).join(' | ');

module.exports = {
    description: "Arachne: Keepers roam the Sanctuary, 4 Arachne's Callings on the altar wake the Lesser Arachne, a 1,000 hit cap, brood at 50% and 20%, loot and the first-kill SkyBlock XP",
    async run(t, bot) {
        await t.step("reach the Spider's Den with a max profile", async () => {
            await bot.joinSkyblock();
            await bot.command('/max profile', { settleMs: 8000 });
            const mark = bot.markChat();
            bot.say('/warp spider');
            await bot.waitFor(() => /Spider/i.test((bot.sidebar()?.lines ?? []).join(' ')), { timeoutMs: 90000, what: "the Spider's Den sidebar" })
                .catch((error) => { throw new Error(`${error.message} chat: ${clean(bot.chatSince(mark))}`); });
        });

        await t.step("a Keeper roams Arachne's Sanctuary", async () => {
            await bot.command('/tp -283 48 -184', { settleMs: 3000 });
            await bot.waitFor(() => /Arachne's Sanctuary/i.test((bot.sidebar()?.lines ?? []).join(' ')), { timeoutMs: 15000, what: 'the Sanctuary sidebar' });
            await bot.waitFor(() => bot.entities({ radius: 70, names: ["arachne's keeper"] }).length > 0, { timeoutMs: 90000, what: "an Arachne's Keeper" });
        });

        await t.step('placing 4 Callings wakes Arachne after her dialogue', async () => {
            await bot.command('/item ARACHNE_KEEPER_FRAGMENT 4', { settleMs: 1500 });
            await bot.command('/item ONE_HIT_SWORD', { settleMs: 1000 });
            const callings = bot.bot.inventory.items().find((item) => /arachne's calling/i.test(JSON.stringify(item.customName ?? item.displayName)));
            t.assert(callings, "no Arachne's Calling in the inventory");
            await bot.bot.equip(callings, 'hand');
            await bot.command('/tp -283 49 -180', { settleMs: 1500 });
            const altar = bot.bot.blockAt(bot.bot.entity.position.offset(0, -1, 2).floored());
            const mark = bot.markChat();
            for (let placed = 0; placed < 4; placed++) {
                await bot.bot.activateBlock(bot.bot.blockAt({ x: -282, y: 48, z: -178 }) ?? altar);
                await sleep(800);
            }
            await sleep(8000);
            const chat = clean(bot.chatSince(mark));
            t.assert(/\(1\/4\)/.test(chat) && /placed the last/i.test(chat), chat);
            t.assert(/\[BOSS\] Arachne: A befitting welcome!/.test(chat), chat);
            await bot.waitFor(() => bot.entities({ radius: 40, names: ['arachne'] }).length > 0, { timeoutMs: 15000, what: 'Arachne' });
            t.assert(bot.bossBars().length > 0, 'no boss bar');
        });

        await t.step('a one-hit sword only takes 1,000 and the brood come at 50%', async () => {
            const sword = bot.bot.inventory.items().find((item) => /one hit/i.test(JSON.stringify(item.customName ?? item.displayName)));
            await bot.bot.equip(sword, 'hand');
            const mark = bot.markChat();
            for (let swing = 0; swing < 40; swing++) {
                const arachne = bot.nearestEntity('arachne');
                if (!arachne) break;
                await bot.command(`/tp ${Math.round(arachne.x)} ${Math.round(arachne.y)} ${Math.round(arachne.z) + 2}`, { settleMs: 300 });
                const target = bot.nearestEntity('arachne');
                if (target) await bot.attack(target);
                await sleep(500);
                if (/Spiders in my Den/.test(clean(bot.chatSince(mark)))) break;
            }
            t.assert(/Spiders in my Den, can you count to ten\?/.test(clean(bot.chatSince(mark))), clean(bot.chatSince(mark)));
            t.assert(bot.entities({ radius: 40, names: ["arachne's brood"] }).length >= 5, 'no brood');
        });

        await t.step('killing everything pays the loot and the first-kill XP', async () => {
            const mark = bot.markChat();
            for (let swing = 0; swing < 120; swing++) {
                const target = bot.nearestEntity("arachne's brood") ?? bot.nearestEntity('arachne');
                if (!target) break;
                await bot.command(`/tp ${Math.round(target.x)} ${Math.round(target.y)} ${Math.round(target.z) + 2}`, { settleMs: 250 });
                const near = bot.nearestEntity("arachne's brood") ?? bot.nearestEntity('arachne');
                if (near) await bot.attack(near);
                await sleep(350);
                if (/No, this is impossible/.test(clean(bot.chatSince(mark)))) break;
            }
            await sleep(2000);
            const chat = clean(bot.chatSince(mark));
            t.assert(/I will be back, even stronger!/.test(chat), chat);
            await bot.command('/sbmenu', { settleMs: 800 });
            await bot.closeWindow();
        });
    },
};
