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

const ZONES = {
    'Desert Settlement': [182, 100, -392],
    Oasis: [322, 100, -435],
    'Archaeological Site': [365, 100, -485],
    'Desert Mountain': [222, 100, -525],
    'Mushroom Gorge': [277, 100, -495],
    "Shepherd's Keep": [385, 100, -375],
};
const NPCS = [
    ['Farmhand', 144, 73, -239],
    ['Husbandry Merchant', 116, 73, -254],
    ['Windmill Operator', 102, 82, -281],
    ['Mason', 177, 77, -355],
    ['Farmer Jon', 167, 91, -597],
    ['Friendly Hiker', 181, 77, -380],
    ['Hungry Hiker', 269, 48, -479],
    ['Treasure Hunter', 200, 92, -436],
    ['Shepherd', 388, 85, -371],
    ['Jake', 261, 184, -564],
    ['Moby', 206, 43, -499],
    ['Beth', 163, 77, -358],
];

module.exports = {
    description: 'Farming Islands phase 1: every quest NPC answers a click without errors',
    async run(t, bot) {
        await t.step('reach the Farming Islands', async () => {
            await bot.joinSkyblock();
            await bot.command('/mission clearset GETTING_STARTED', { settleMs: 1500 });
            await bot.command('/set coins 50000000', { settleMs: 800 });
            await sleep(3000);
            await sendTo(bot, 'SKYBLOCK_THE_FARMING_ISLANDS', /Farming|Barn|Desert/i);
        });
        for (const [label, x, y, z] of NPCS) {
            await t.step(`${label} answers a click`, async () => {
                const mark = bot.markChat();
                await bot.command(`/tp ${x} ${y} ${z}`, { settleMs: 2500 });
                await bot.waitFor(() => bot.findNpc(label), { timeoutMs: 20000, what: label });
                await bot.interact(bot.findNpc(label));
                await sleep(4000);
                const window = bot.window();
                const chat = bot.chatSince(mark);
                t.assert(window || chat.length > 0, `${label}: nothing happened`);
                if (window) await bot.closeWindow();
                return chat.slice(0, 6);
            });
        }
        await t.step('Shepherd\'s Keep has sheep', async () => {
            await bot.command('/tp 388 86 -368', { settleMs: 5000 });
            await bot.waitFor(() => Object.values(bot.bot.entities).some((e) => /sheep/i.test(e.name ?? '')), { timeoutMs: 30000, what: 'a keep sheep' });
        });
        await t.step('barn animals spawn', async () => {
            await bot.command('/tp 140 76 -250', { settleMs: 6000 });
            await bot.waitFor(() => Object.values(bot.bot.entities).some((e) => /cow|pig|chicken/i.test(e.name ?? '')), { timeoutMs: 40000, what: 'a barn animal' });
        });
    }
};
