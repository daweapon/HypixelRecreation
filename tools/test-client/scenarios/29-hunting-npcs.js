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

const menu = async (t, bot, label, x, y, z, pattern) => {
    await bot.command(`/tp ${x} ${y} ${z}`, { settleMs: 2500 });
    await bot.waitFor(() => bot.findNpc(label), { timeoutMs: 20000, what: label });
    await bot.interact(bot.findNpc(label));
    for (let wait = 0; wait < 60 && !(bot.window() && pattern.test(names(bot))); wait++) {
        await sleep(700);
        if (wait % 20 === 19) await bot.interact(bot.findNpc(label));
    }
    t.assert(bot.window() && pattern.test(names(bot)), `${label}: ${bot.window() ? names(bot).slice(0, 300) : 'no window'} chat: ${bot.chatText().slice(-300)}`);
    const text = names(bot);
    await bot.closeWindow();
    return text.slice(0, 250);
};


module.exports = {
    description: 'Hunting NPCs on Galatea and Torrhus (Auryon, Albert, Kiara, Jaeger, Ant, Freddy, Maxim), the Desert Temple buttons and the Mob the Fish drop',
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => bot.joinSkyblock());

        await t.step('finish the starting missions, reach Level 6, unlock the Heart of the Forest and get coins', async () => {
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

        await t.step('send the bot to Galatea and meet David', async () => {
            await sleep(3000);
            await sendTo(bot, 'SKYBLOCK_GALATEA', /Galatea|Tangleburg|Marsh/i);
            const mark = bot.markChat();
            await bot.command('/tp -593 114 -13', { settleMs: 2500 });
            await bot.waitFor(() => bot.findNpc('David'), { timeoutMs: 20000, what: 'David' });
            await bot.interact(bot.findNpc('David'));
            await bot.waitFor(() => bot.chatSince(mark).length >= 3, { timeoutMs: 40000, what: 'David\'s lines' });
            await sleep(15000);
        });

        await t.step('Albert opens the Gravity Condenser shop', async () => menu(t, bot, 'Albert', -592, 113, 8, /Small|Condenser|Gravity/i));
        await t.step('Kiara sells shards with a stock', async () => menu(t, bot, 'Kiara', -654, 113, -70, /Stock|Viper|Shard/));
        await t.step('Auryon hands out the Abysmal Lasso', async () => menu(t, bot, 'Auryon', -557, 111, 48, /Lasso|claim/i));
        await t.step('Jaeger hands out the Basic Fishing Net', async () => menu(t, bot, 'Jaeger', -651, 89, -24, /Net|claim/i));

        await t.step('Mob the Fish drops from a kill via /force drop', async () => {
            await bot.command('/set stat health 99999', { settleMs: 800 });
            await bot.command('/set stat damage 99999', { settleMs: 800 });
            await bot.command('/force drop MOB_THE_FISH', { settleMs: 800 });
            await bot.command('/give ASPECT_OF_THE_END', { settleMs: 800 }).catch(() => {});
            await bot.command('/tp -648 88 -3', { settleMs: 2500 });
            const mark = bot.markChat();
            for (let kill = 0; kill < 12 && !bot.chatSince(mark).some((line) => /Mob the Fish/.test(line)); kill++) {
                bot.say('/spawn ZOMBIE');
                await sleep(1500);
                const zombie = bot.nearestEntity('zombie');
                if (zombie) await bot.attack(zombie);
                await sleep(800);
            }
            t.assert(bot.chatSince(mark).some((line) => /Mob the Fish/.test(line)), bot.chatSince(mark).slice(-6).join(' | '));
        });

        await t.step('send the bot to Torrhus Canyon', async () => {
            await sendTo(bot, 'SKYBLOCK_TORRHUS_CANYON', /Torrhus/i);
        });

        await t.step('Ant sells the Generator Upgrade Stones', async () => menu(t, bot, 'Ant', -615, 75, 208, /Stone|Upgrade/));
        await t.step('Freddy sells hunting hints', async () => menu(t, bot, 'Freddy', -694, 94, 200, /Catching|Hint/));
        await t.step('Maxim sells booster upgrades', async () => menu(t, bot, 'Maxim', -574, 153, 248, /Booster|Upgrade/));

        await t.step('the Desert Temple buttons accept the right order', async () => {
            const mark = bot.markChat();
            await bot.command('/tp -613 46 230', { settleMs: 3000 });
            const Vec = bot.bot.entity.position.constructor;
            for (const [x, y, z] of [[-613, 45, 232], [-614, 48, 232], [-612, 49, 232], [-611, 49, 232], [-613, 49, 232]]) {
                const block = bot.bot.blockAt(new Vec(x, y, z));
                await bot.bot.lookAt(new Vec(x + 0.5, y + 0.5, z + 0.5), true);
                await bot.bot.activateBlock(block);
                await sleep(700);
            }
            await bot.waitChat(/guardian of the temple|opens the door/i, { since: mark, timeoutMs: 8000 });
            return bot.chatSince(mark).slice(-8);
        });
    }
};
