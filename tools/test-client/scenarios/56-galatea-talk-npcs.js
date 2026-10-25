const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const clean = (chat) => chat.filter((line) => !/TPS/.test(line)).join(' | ');
const sidebarText = (bot) => (bot.sidebar()?.lines ?? []).join(' | ');

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

const talk = async (t, bot, label, x, y, z, expected) => {
    await bot.command(`/tp ${x} ${y} ${z}`, { settleMs: 2500 });
    await bot.waitFor(() => bot.findNpc(label), { timeoutMs: 20000, what: label });
    const mark = bot.markChat();
    await bot.interact(bot.findNpc(label));
    await sleep(6000);
    const chat = clean(bot.chatSince(mark));
    t.assert(expected.test(chat), `${label}: ${chat}`);
};

module.exports = {
    description: 'Galatea flavour NPCs: Martin, Pounce, Talia and Tilus speak their wiki dialogue',
    async run(t, bot) {
        await t.step('reach SkyBlock and Galatea', async () => {
            await bot.joinSkyblock();
            await bot.command('/mission clearset GETTING_STARTED', { settleMs: 1500 });
            await sendTo(bot, 'galatea', /Galatea|Tangleburg|Moonglade/i);
        });
        await t.step('Martin', async () => talk(t, bot, 'Martin', -579.5, 93, 31.5, /Isn't this place fascinating/));
        await t.step('Talia', async () => talk(t, bot, 'Talia', -615.5, 108, 2.5, /view from here is just so bad/));
        await t.step('Tilus', async () => talk(t, bot, 'Tilus', -642, 55, 48, /blub/i));
        await t.step('Pounce intro', async () => talk(t, bot, 'Pounce', -598.5, 127, -20.5, /David's assistant/));
    }
};
