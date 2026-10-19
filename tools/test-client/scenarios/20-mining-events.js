const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const sidebarText = (bot) => (bot.sidebar()?.lines ?? []).join(' | ');

module.exports = {
    description: 'Mining Events: Garry menu, forced events announce and end, Don Expresso, raid mobs, Raffle tickets and box, Fallen Star, Powder Ghast',
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => bot.joinSkyblock());

        await t.step('/max profile as staff', async () => {
            const lines = await bot.command('/max profile', { settleMs: 8000 });
            t.assertChat(lines, /MAXED/i, 'expected the MAXED message');
        });

        await t.step('warp to the Dwarven Mines', async () => {
            bot.say('/warp dwarven');
            await bot.waitFor(() => /Dwarven|Forge|Village|Mines/i.test(sidebarText(bot)), { timeoutMs: 180000, what: 'the Dwarven Mines sidebar' });
            return sidebarText(bot);
        });

        const force = async (name, pattern, timeoutMs = 15000) => {
            const mark = bot.markChat();
            bot.say(`/force event ${name}`);
            await bot.waitChat(pattern, { since: mark, timeoutMs });
            return bot.chatSince(mark).slice(-5);
        };
        const near = (radius = 60) => bot.entities({ radius }).map((e) => `${e.customName ?? ''} ${e.type}`);

        await t.step('Old Man Garry opens the event menu', async () => {
            await bot.command('/tp 5 201 -105', { settleMs: 3000 });
            await bot.waitFor(() => bot.findNpc('Garry'), { timeoutMs: 20000, what: 'Old Man Garry' });
            await bot.interact(bot.findNpc('Garry'));
            let window = await bot.waitWindow({ timeoutMs: 6000 }).catch(() => null);
            if (!window) {
                await sleep(12000);
                await bot.interact(bot.findNpc('Garry'));
                window = await bot.waitWindow({ timeoutMs: 15000 });
            }
            t.assert(/Old Man Garry/.test(JSON.stringify(window.title)), `window ${JSON.stringify(window.title)}`);
            const schedule = bot.findSlot(/Event Schedule/);
            t.assert(schedule && /Next event in/.test(schedule.lore.join(' ')), `schedule ${JSON.stringify(schedule)}`);
            await bot.closeWindow();
            return schedule.lore;
        });

        await t.step('2x Powder and Better Together announce, start and end', async () => {
            await force('2x_powder', /2X POWDER STARTED/i);
            await force('end', /2X POWDER ENDED/i);
            await force('better_together', /BETTER TOGETHER STARTED/i);
            return force('end', /BETTER TOGETHER ENDED/i);
        });

        await t.step('Mithril Gourmand: Don Expresso appears, rejects wrong and stale items', async () => {
            await force('mithril_gourmand', /MITHRIL GOURMAND STARTED/i);
            await bot.waitFor(() => bot.findNpc('Don Expresso'), { timeoutMs: 20000, what: 'Don Expresso' });
            const don = bot.findNpc('Don Expresso');
            await bot.walkNear(don);
            let mark = bot.markChat();
            await bot.interact(don);
            await sleep(1500);
            t.assert(/Don Expresso/.test(bot.chatSince(mark).join(' ')), `no reply: ${bot.chatSince(mark).join(' | ')}`);
            await bot.command('/item MITHRIL_GOURMAND 3', { settleMs: 1000 });
            mark = bot.markChat();
            await bot.interact(don);
            await sleep(1500);
            const chat = bot.chatSince(mark).join(' | ');
            t.assert(/STALE/.test(chat) && /different event/.test(chat), `stale: ${chat}`);
            const reply = await force('end', /MITHRIL GOURMAND ENDED/i);
            return reply;
        });

        await t.step('Goblin Raid spawns raiders and ends', async () => {
            await force('goblin_raid', /GOBLIN RAID STARTED/i);
            await sleep(5000);
            const raiders = near(400).filter((n) => /Weakling|Fireslinger|Creeperlobber|Pitfighter|Murderlover|Superprotectron|Goblin/i.test(n));
            t.assert(raiders.length > 0, `no raiders: ${near(400).slice(0, 12).join(', ')}`);
            const reply = await force('end', /GOBLIN RAID ENDED/i);
            return { raiders: raiders.length, reply };
        });

        await t.step('Raffle: tickets can be picked up and deposited at the Raffle Box', async () => {
            await force('raffle', /RAFFLE STARTED/i);
            await bot.command('/tptogarry', { settleMs: 3000 });
            await bot.waitFor(() => bot.findNpc('Garry'), { timeoutMs: 20000, what: 'Old Man Garry' });
            await bot.interact(bot.findNpc('Garry'));
            await bot.waitWindow({ timeoutMs: 15000 });
            const warp = bot.findSlot(/Warp to the Event/);
            t.assert(warp, 'no warp button');
            await bot.click(warp.slot, { settleMs: 5000 });
            const items = Object.values(bot.bot.entities).filter((e) => e.name === 'item' || e.displayName === 'Item');
            t.assert(items.length > 0, 'no ticket entities');
            const box = Object.values(bot.bot.entities).find((e) => e.name === 'interaction');
            t.assert(box, 'no Raffle Box');
            const ticket = items[0];
            await bot.command(`/tp ${Math.floor(ticket.position.x)} ${Math.floor(ticket.position.y) + 1} ${Math.floor(ticket.position.z)}`, { settleMs: 3000 });
            const held = () => bot.bot.inventory.items().map((i) => bot.describeItem(i, 0).name).join(' | ');
            await bot.waitFor(() => /Raffle Ticket/i.test(held()), { timeoutMs: 15000, what: 'a Raffle Ticket in the inventory' });
            await bot.command(`/tp ${Math.floor(box.position.x)} ${Math.floor(box.position.y) + 1} ${Math.floor(box.position.z)}`, { settleMs: 3000 });
            const mark = bot.markChat();
            await bot.interact(box);
            await sleep(2000);
            const chat = bot.chatSince(mark).join(' | ');
            t.assert(/deposited/i.test(chat), `deposit: ${chat}`);
            const reply = await force('end', /RAFFLE ENDED/i);
            return { chat, reply };
        });

        await t.step('Fallen Star crashes with Star Sentries', async () => {
            const reply = await force('fallen_star', /Fallen Star .*crashed/i);
            await sleep(2000);
            const sentries = near(300).filter((n) => /Star Sentry/i.test(n));
            t.assert(sentries.length > 0, `no sentries: ${near(300).slice(0, 10).join(', ')}`);
            await force('end', /Done/i);
            return { reply, sentries: sentries.length };
        });

        await t.step('Powder Ghast appears', async () => {
            await bot.command('/tp -1 140 90', { settleMs: 3000 });
            return force('powder_ghast', /POWDER GHAST/i);
        });
    },
};
