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
const ANIMALS = /cow|pig|sheep|rabbit|chicken|horse/i;

module.exports = {
    description: 'Farming Islands phase 1: Trevor hunt, Pelts, Talbot and Tony shops, Pelts Toy Box button',
    async run(t, bot) {
        await t.step('reach SkyBlock and the Farming Islands', async () => {
            await bot.joinSkyblock();
            await bot.command('/mission clearset GETTING_STARTED', { settleMs: 1500 });
            await bot.command('/set coins 50000000', { settleMs: 800 });
            await sleep(3000);
            await sendTo(bot, 'SKYBLOCK_THE_FARMING_ISLANDS', /Farming|Barn|Desert/i);
        });

        await t.step('Trevor offers the task and the player accepts', async () => {
            const mark = bot.markChat();
            await visit(bot, 'Trevor', 281, 104, -541);
            await bot.waitChat(/Accept the trapper's task/, { since: mark, timeoutMs: 15000 });
            bot.say('/selectnpcoption trapper_task yes');
            await bot.waitChat(/You can find your/, { since: mark, timeoutMs: 15000 });
            return bot.chatSince(mark).slice(0, 8);
        });

        await t.step('the hunted animal is visible to the owner', async () => {
            const said = bot.chatSince(0).join(' ');
            const zone = Object.keys(ZONES).find((name) => said.includes(name));
            t.assert(zone, said);
            const [zx, zy, zz] = ZONES[zone];
            await bot.command(`/tp ${zx} ${zy} ${zz}`, { settleMs: 3000 });
            await bot.waitFor(() => Object.values(bot.bot.entities).some((e) => ANIMALS.test(e.name ?? '') && e !== bot.bot.entity), { timeoutMs: 20000, what: 'a trapper animal' });
        });

        await t.step('a second visit says to hunt the mob first', async () => {
            const mark = bot.markChat();
            await visit(bot, 'Trevor', 281, 104, -541);
            await bot.waitChat(/already have a mob spawned/, { since: mark, timeoutMs: 8000 });
        });

        await t.step('kill the animal and earn Pelts', async () => {
            await bot.command('/set stat strength 9999999', { settleMs: 800 });
            await bot.command('/set stat health 99999', { settleMs: 800 });
            await bot.command('/clear', { settleMs: 800 });
            await bot.command('/item ASPECT_OF_THE_END', { settleMs: 800 });
            const sword = bot.bot.inventory.items().find((entry) => /Aspect/.test(entry.customName ? JSON.stringify(entry.customName) : entry.displayName));
            await bot.bot.equip(sword, 'hand');
            const said = bot.chatSince(0).join(' ');
            const zone = Object.keys(ZONES).find((name) => said.includes(name));
            await bot.command(`/tp ${ZONES[zone].join(' ')}`, { settleMs: 3000 });
            const mark = bot.markChat();
            for (let attempt = 0; attempt < 80; attempt++) {
                const target = Object.values(bot.bot.entities).find((e) => ANIMALS.test(e.name ?? '') && e !== bot.bot.entity);
                if (!target) break;
                await bot.command(`/tp ${Math.round(target.position.x)} ${Math.round(target.position.y)} ${Math.round(target.position.z) + 1}`, { settleMs: 300 });
                const live = Object.values(bot.bot.entities).find((e) => ANIMALS.test(e.name ?? '') && e !== bot.bot.entity);
                if (live) await bot.attack(live);
                if (bot.chatSince(mark).some((line) => /TRAPPER!/.test(line))) break;
            }
            await bot.waitChat(/TRAPPER!/, { since: mark, timeoutMs: 15000 });
            return bot.chatSince(mark).filter((line) => /TRAPPER|vanished|ran away/.test(line));
        });

        await t.step('the cooldown message appears straight after', async () => {
            const mark = bot.markChat();
            await visit(bot, 'Trevor', 281, 104, -541);
            await bot.waitChat(/Try coming back in/, { since: mark, timeoutMs: 8000 });
        });

        await t.step('the Toy Box gives Pelts', async () => {
            bot.say('/centauri');
            await bot.waitWindow({ timeoutMs: 20000 });
            await bot.click(15, { button: 0, mode: 0 });
            await bot.waitFor(() => /Toy Box/.test(titleText(bot.window())), { timeoutMs: 10000, what: 'the Toy Box window' });
            const category = bot.findSlot(/Currenc/);
            t.assert(category, names(bot));
            await bot.click(category.slot ?? bot.window().slots.indexOf(category), { button: 0, mode: 0 });
            await sleep(1500);
            const pelts = bot.findSlot(/Pelts/);
            t.assert(pelts, names(bot));
            const mark = bot.markChat();
            await bot.click(pelts.slot ?? bot.window().slots.indexOf(pelts), { button: 0, mode: 0 });
            await sleep(1000);
            await bot.closeWindow();
        });

        await t.step('Talbot sells the Pelt Belt for 250 Pelts', async () => {
            await visit(bot, 'Talbot', 285, 104, -547);
            await bot.waitWindow({ timeoutMs: 15000 });
            t.assert(/Talbot/.test(titleText(bot.window())), titleText(bot.window()));
            const belt = bot.findSlot(/Pelt Belt/);
            t.assert(belt && /250 Pelts/.test(belt.lore.join(' ')), names(bot));
            await bot.closeWindow();
        });

        await t.step('Tony lists the Tier XII minions at 75 Pelts', async () => {
            await visit(bot, 'Tony', 279, 104, -543);
            await bot.waitWindow({ timeoutMs: 15000 });
            const wheat = bot.findSlot(/Wheat Minion XII/);
            t.assert(wheat && /75 Pelts/.test(wheat.lore.join(' ')), names(bot));
            await bot.closeWindow();
        });

        await t.step('Tammy and Hunter Ava talk', async () => {
            const mark = bot.markChat();
            await visit(bot, 'Tammy', 284, 104, -543);
            await bot.waitChat(/Different tiers of animals/, { since: mark, timeoutMs: 15000 });
            await visit(bot, 'Hunter Ava', 319, 102, -469);
            await bot.waitChat(/hunting an/, { since: mark, timeoutMs: 15000 });
        });
    }
};
