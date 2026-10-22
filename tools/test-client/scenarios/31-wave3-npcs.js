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

module.exports = {
    description: 'Wave 3 NPCs: Oden\'s Chillshot quest, Coral, Teddy\'s paid tips, Swoop Sweep Details and Tree Gift menus',
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
        await t.step('send the bot to Galatea', async () => {
            await sleep(3000);
            await sendTo(bot, 'SKYBLOCK_GALATEA', /Galatea|Tangleburg|Marsh/i);
        });

        await t.step('Oden\'s dialogue ends with a Chillshot beside him', async () => {
            await bot.command('/set stat health 99999', { settleMs: 800 });
            await bot.command('/set stat defense 99999', { settleMs: 800 });
            const mark = bot.markChat();
            await visit(bot, 'Oden', -584, 113, -40);
            await bot.waitFor(() => bot.nearestEntity('chillshot'), { timeoutMs: 60000, what: 'a Chillshot' });
            return bot.chatSince(mark).slice(0, 6);
        });

        await t.step('a bare-handed hit on the Chillshot is refused', async () => {
            const chillshot = bot.nearestEntity('chillshot');
            const mark = bot.markChat();
            await bot.command(`/tp ${Math.round(chillshot.position.x)} ${Math.round(chillshot.position.y)} ${Math.round(chillshot.position.z) + 2}`, { settleMs: 1500 });
            await bot.attack(bot.nearestEntity('chillshot'));
            await bot.waitChat(/Huntaxe/, { since: mark, timeoutMs: 8000 });
        });

        await t.step('a Huntaxe and Oden\'s Black Hole capture the Chillshot', async () => {
            await bot.command('/clear', { settleMs: 1000 });
            await bot.command('/item VENATOR_GENESIS', { settleMs: 800 });
            const item = bot.bot.inventory.items().find((entry) => /Genesis/.test(entry.customName ? JSON.stringify(entry.customName) : entry.displayName));
            await bot.bot.equip(item, 'hand');
            const mark = bot.markChat();
            for (let swing = 0; swing < 30 && !bot.chatSince(mark).some((line) => /CAPTURED/.test(line)); swing++) {
                const target = bot.nearestEntity('chillshot');
                if (target) await bot.attack(target);
                await sleep(700);
            }
            await bot.waitChat(/CAPTURED/, { since: mark, timeoutMs: 20000 }).catch((error) => { throw new Error(`${error.message} chat: ${bot.chatSince(mark).filter((line) => !/TPS/.test(line)).slice(-6).join(' | ')} near: ${JSON.stringify(bot.entities({ radius: 20 }).map((entity) => entity.customName).filter(Boolean))}`); });
        });

        await t.step('Coral greets the bot in the Red House', async () => {
            const mark = bot.markChat();
            await visit(bot, 'Coral', -626, 99, -87);
            for (let attempt = 0; attempt < 3 && !bot.chatSince(mark).some((line) => /Fish Family/.test(line)); attempt++) {
                await sleep(6000);
                if (!bot.chatSince(mark).some((line) => /Fish Family/.test(line))) await bot.interact(bot.findNpc('Coral'));
            }
            t.assert(bot.chatSince(mark).some((line) => /Fish Family/.test(line)), `chat: ${bot.chatSince(mark).filter((line) => !/TPS/.test(line)).slice(-4).join(' | ')} entities: ${JSON.stringify(bot.entities({ radius: 10 }).map((entity) => entity.customName))}`);
            return bot.chatSince(mark).slice(0, 4);
        });

        await t.step('Teddy\'s tips cost coins', async () => {
            await visit(bot, 'Teddy', -656, 115, -39);
            for (let wait = 0; wait < 40 && !bot.window(); wait++) await sleep(700);
            const mark = bot.markChat();
            bot.say('/selectnpcoption teddy thanks');
            for (let wait = 0; wait < 40 && !bot.window(); wait++) await sleep(700);
            t.assert(bot.window(), `no tips menu, chat ${bot.chatSince(mark).slice(-3).join(' | ')}`);
            const text = names(bot);
            t.assert(/coins/i.test(text), text.slice(0, 300));
            await bot.closeWindow();
            return text.slice(0, 200);
        });

        await t.step('Swoop shows Sweep Details and the Tree Gifts menu', async () => {
            await visit(bot, 'Swoop', -579, 114, -23);
            for (let wait = 0; wait < 50 && !bot.window(); wait++) {
                await sleep(700);
                if (wait % 20 === 19) await bot.interact(bot.findNpc('Swoop'));
            }
            t.assert(bot.window(), 'no Swoop window');
            const text = names(bot);
            t.assert(/Sweep Details/.test(text) && /Tree Gift/i.test(text), text.slice(0, 400));
            await bot.closeWindow();
        });
    }
};
