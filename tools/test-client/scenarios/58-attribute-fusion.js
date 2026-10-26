const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const clean = (chat) => chat.filter((line) => !/TPS/.test(line)).join(' | ');
const sidebarText = (bot) => (bot.sidebar()?.lines ?? []).join(' | ');

async function absorb(bot, itemId, amount, name) {
    await bot.command(`/item ${itemId} ${amount}`, { settleMs: 900 });
    const slot = bot.bot.inventory.slots.findIndex((item, index) => index >= 9 && index <= 44 && item && new RegExp(name).test(bot.describeItem(item, index).name));
    if (slot < 0) throw new Error(`no ${name} item after /item ${itemId}`);
    let hotbar = slot - 36;
    if (slot < 36) {
        await bot.bot.clickWindow(slot, 4, 2);
        await sleep(400);
        hotbar = 4;
    }
    bot.bot.setQuickBarSlot(hotbar);
    await sleep(300);
    bot.bot.activateItem();
    await sleep(700);
}

const find = (bot, pattern) => bot.window()?.slots.find((slot) => pattern.test(`${slot.name} ${slot.lore.join(' ')}`));

async function pick(t, bot, pattern) {
    const entry = find(bot, pattern);
    t.assert(entry, `no menu entry ${pattern}; window: ${JSON.stringify(bot.window()?.slots.filter((slot) => slot.item === 'player_head').slice(0, 3))}`);
    await bot.click(entry.slot, { settleMs: 900 });
}

async function openFusionBox(t, bot) {
    const npc = bot.findNpc('Kysha');
    t.assert(npc, 'no Kysha NPC');
    await bot.walkNear(npc);
    await bot.interact(npc);
    await bot.waitWindow({ title: 'Fusion Box', timeoutMs: 15000 });
}

async function fuse(t, bot, firstName, secondName, resultName) {
    await openFusionBox(t, bot);
    await pick(t, bot, firstName);
    await pick(t, bot, secondName);
    const mark = bot.markChat();
    const resultSlots = [29, 31, 33].map((slot) => (({ name, lore }) => `${name} ${lore.join(' ')}`)(bot.window().slots.find((entry) => entry.slot === slot) ?? { name: '', lore: [] })).filter(Boolean);
    const entry = find(bot, resultName);
    t.assert(entry, `result ${resultName} not offered; offered: ${JSON.stringify(resultSlots)}`);
    await bot.click(entry.slot, { settleMs: 2500 });
    const chat = clean(bot.chatSince(mark)) || `no chat after clicking slot ${entry.slot} (${entry.name}); window now: ${JSON.stringify(bot.window()?.slots.map((slot) => slot.slot + ':' + slot.name))}`;
    await bot.closeWindow();
    return chat;
}

module.exports = {
    description: 'Attribute Fusion: ID result, special fusion (x2), same shard twice, Chameleon, no recipe offers an input',
    async run(t, bot) {
        await t.step('reach Galatea with the shards', async () => {
            await bot.joinSkyblock();
            await sleep(2000);
            await bot.command('/mission clearset GETTING_STARTED', { settleMs: 1500 });
            await bot.command('/skyblockxp setlevel 6', { settleMs: 1500 });
            bot.say('/centauri');
            await bot.waitWindow({ timeoutMs: 20000 });
            await bot.click(15, { button: 0, mode: 0 });
            await bot.waitFor(() => /Toy Box/.test(bot.window()?.title ?? ''), { timeoutMs: 10000, what: 'the Toy Box window' });
            await bot.click(24, { button: 0, mode: 0 });
            await bot.waitFor(() => /Toy Box - Foraging/.test(bot.window()?.title ?? ''), { timeoutMs: 10000, what: 'the Foraging Toy Box' });
            const unlock = bot.findSlot(/Unlock Heart of the Forest/);
            t.assert(unlock, 'no unlock button');
            await bot.click(unlock.slot, { button: 0, mode: 0 });
            await bot.closeWindow();
            for (let attempt = 1; attempt <= 4; attempt++) {
                bot.say('/sendto SKYBLOCK_GALATEA');
                try {
                    await bot.waitFor(() => /Galatea|Tangleburg|Moonglade/i.test(sidebarText(bot)), { timeoutMs: 60000, what: 'the Galatea sidebar' });
                    break;
                } catch (error) {
                    if (attempt === 4) throw error;
                }
            }
            await sleep(3000);
            const kysha = bot.findNpc('Kysha');
            t.assert(kysha, 'no Kysha NPC');
            await bot.walkNear(kysha);
            await bot.interact(kysha);
            await sleep(40000);
            await absorb(bot, 'SHARD_GROVE', 4, 'Grove');
            await absorb(bot, 'SHARD_MIST', 2, 'Mist');
            await absorb(bot, 'SHARD_SHYWORM', 5, 'Shyworm');
            await absorb(bot, 'SHARD_GOLDFIN', 5, 'Goldfin');
            await absorb(bot, 'SHARD_PHANFLARE', 10, 'Phanflare');
            await absorb(bot, 'SHARD_CHAMELEON', 1, 'Chameleon');
        });

        await t.step('Grove + Mist offers the ID results Phanpyre and Cod, never Grove or Mist', async () => {
            await openFusionBox(t, bot);
            await pick(t, bot, /Grove/);
            await pick(t, bot, /Mist/);
            const names = [29, 31, 33].map((slot) => (({ name, lore }) => `${name} ${lore.join(' ')}`)(bot.window().slots.find((entry) => entry.slot === slot) ?? { name: '', lore: [] })).filter(Boolean).join(' | ');
            t.assert(/Phanpyre/.test(names) && /Cod/.test(names) && !/Grove|Mist/.test(names), names);
            await bot.closeWindow();
        });

        await t.step('special fusion: Shyworm + Goldfin gives 2 Grove Shards', async () => {
            const chat = await fuse(t, bot, /Shyworm/, /Goldfin/, /Grove/);
            t.assert(/Fusion complete.*2x.*Grove/.test(chat), chat);
        });

        await t.step('the same shard fused twice: Phanflare + Phanflare gives Hideonleaf', async () => {
            const chat = await fuse(t, bot, /Phanflare/, /Phanflare/, /Hideonleaf/);
            t.assert(/Fusion complete.*1x.*Hideonleaf/.test(chat), chat);
        });

        await t.step('Chameleon + Grove offers the sheet results Mist, Flash and Phanpyre', async () => {
            await openFusionBox(t, bot);
            await pick(t, bot, /Chameleon/);
            await pick(t, bot, /Grove/);
            const names = [29, 31, 33].map((slot) => (({ name, lore }) => `${name} ${lore.join(' ')}`)(bot.window().slots.find((entry) => entry.slot === slot) ?? { name: '', lore: [] })).filter(Boolean).join(' | ');
            t.assert(/Mist/.test(names) && /Flash/.test(names) && /Phanpyre/.test(names), names);
            await bot.closeWindow();
        });
    }
};
