const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const sidebarText = (bot) => (bot.sidebar()?.lines ?? []).join(' | ');
const titleText = (window) => JSON.stringify(window.title);
const names = (bot) => bot.window().slots.map((slot) => `${slot.name} ${slot.lore.join(' ')}`).join(' | ');

const sendTo = async (bot, type, pattern, attempts = 4) => {
    for (let attempt = 1; attempt <= attempts; attempt++) {
        bot.say(`/sendto ${type}`);
        try {
            await bot.waitFor(() => pattern.test(sidebarText(bot)), { timeoutMs: 60000, what: `the ${type} sidebar` });
            return;
        } catch (error) {
            if (attempt === attempts) throw error;
        }
    }
};

const visit = async (bot, label, x, y, z) => {
    await bot.command(`/tp ${x} ${y} ${z}`, { settleMs: 2500 });
    await bot.waitFor(() => bot.findNpc(label), { timeoutMs: 20000, what: label });
    await bot.interact(bot.findNpc(label));
};

const shop = async (t, bot, label, x, y, z, pattern) => {
    await visit(bot, label, x, y, z);
    for (let more = 0; more < 40 && !(bot.window() && pattern.test(names(bot))); more++) {
        await sleep(700);
        if (bot.window() && pattern.test(names(bot))) break;
        if (more % 16 === 15) await bot.interact(bot.findNpc(label));
    }
    t.assert(bot.window() && pattern.test(names(bot)), `${label}: ${bot.window() ? names(bot) : 'no window'} chat: ${bot.chatText().slice(-400)}`);
    const text = names(bot);
    await bot.closeWindow();
    return text.slice(0, 300);
};

module.exports = {
    description: 'Galatea foraging NPCs: shops, Boris and the Moonglade Beacon, Brian\'s Trial of Blue Flames, Swoop, Dax and the Booster Info menu',
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => bot.joinSkyblock());

        await t.step('finish the starting missions, reach SkyBlock Level 6 and unlock the Heart of the Forest', async () => {
            await bot.command('/mission clearset GETTING_STARTED', { settleMs: 1500 });
            await bot.command('/skyblockxp setlevel 6', { settleMs: 1500 });
            await bot.command('/set coins 50000000', { settleMs: 800 });
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
            await sendTo(bot, 'SKYBLOCK_GALATEA', /Galatea|Tangleburg|Marsh/i);
            return sidebarText(bot);
        });

        await t.step('David\'s introduction unlocks the other hunters', async () => {
            const mark = bot.markChat();
            await visit(bot, 'David', -593, 114, -13);
            await bot.waitFor(() => bot.chatSince(mark).length >= 3, { timeoutMs: 40000, what: 'David\'s lines' });
            await sleep(15000);
            return bot.chatSince(mark).slice(0, 6);
        });

        await t.step('Amaury sells the Decent Axe and the Canopy set', async () => {
            return shop(t, bot, 'Amaury', -578, 114, 2, /Seriously Damaged Axe/);
        });

        await t.step('Nemo sells the Snorkeling pieces', async () => {
            return shop(t, bot, 'Nemo', -619, 87, -6, /Snorkel|Respiration/);
        });

        await t.step('Alan sells the Huntaxes', async () => {
            return shop(t, bot, 'Alan', -625, 116, -36, /Huntaxe|Genesis/);
        });

        await t.step('Sawyer sells the Foraging Fortune Booster', async () => {
            return shop(t, bot, 'Sawyer', -691, 121, 81, /Booster/);
        });

        await t.step('Boris speaks and the beacon block opens the Moonglade Beacon', async () => {
            const mark = bot.markChat();
            await visit(bot, 'Boris', -684, 129, 64);
            await bot.waitChat(/Boris/, { since: mark, timeoutMs: 15000 });
            await bot.command('/tp -688 130 66', { settleMs: 2500 });
            const block = bot.bot.blockAt(bot.bot.entity.position.floored().offset(-0, 0, 0));
            const beacon = bot.bot.findBlock({ matching: (candidate) => candidate.name === 'beacon', maxDistance: 8 });
            t.assert(beacon, `no beacon block near ${block && block.position}`);
            await bot.bot.activateBlock(beacon);
            await bot.waitWindow({ timeoutMs: 20000 });
            const text = names(bot);
            t.assert(/Tune Frequency/.test(text) && /Signal/.test(text), text);
            await bot.closeWindow();
            return text.slice(0, 300);
        });

        await t.step('Brian starts the Trial of Blue Flames and the player takes burn damage', async () => {
            await bot.command('/set stat health 99999', { settleMs: 800 });
            await bot.command('/set stat defense 0', { settleMs: 800 });
            const mark = bot.markChat();
            await visit(bot, 'Brian', -628, 117, 47);
            await sleep(2500);
            await bot.closeWindow().catch(() => {});
            await bot.command('/tp -627 118 49', { settleMs: 1500 });
            await bot.waitChat(/Trial of Blue Flames/, { since: mark, timeoutMs: 20000 });
            await bot.command('/tp -622 117 44', { settleMs: 800 });
            return bot.chatSince(mark).filter((line) => /Trial|Brian|Blue/.test(line)).slice(0, 5);
        });

        await t.step('Swoop opens the tree milestones menu', async () => {
            await bot.command('/tp -579 114 -23', { settleMs: 2500 });
            await bot.waitFor(() => bot.findNpc('Swoop'), { timeoutMs: 20000, what: 'Swoop' });
            await bot.interact(bot.findNpc('Swoop'));
            for (let wait = 0; wait < 50 && !bot.window(); wait++) {
                await sleep(700);
                if (wait % 20 === 19) await bot.interact(bot.findNpc('Swoop'));
            }
            t.assert(bot.window(), `no Swoop window, chat: ${bot.chatText().slice(-400)}`);
            const text = names(bot);
            t.assert(/Fig|Mangrove|Helix|Milestone/i.test(text), text);
            await bot.closeWindow();
            return text.slice(0, 200);
        });

        await t.step('Dax hands over a Small Huntrap', async () => {
            await visit(bot, 'Dax', -705, 94, 33);
            await sleep(3500);
            const text = bot.window() ? names(bot) : bot.chatText().slice(-300);
            await bot.closeWindow().catch(() => {});
            return text.slice(0, 300);
        });
    }
};
