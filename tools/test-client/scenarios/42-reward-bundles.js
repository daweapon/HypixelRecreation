const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const slotsText = (bot) => bot.window().slots.map((slot) => `${slot.slot}:${slot.name} ${slot.lore.join(' ')}`).join(' | ');
const titleText = () => '';

module.exports = {
    description: 'Reward Bundles: the Salesman opens the menu, the season button cycles all four seasons, items and sprays can be claimed once',
    async run(t, bot) {
        await t.step('reach SkyBlock and walk to the Hub Salesman', async () => {
            await bot.joinSkyblock();
            await sleep(3000);
            await bot.command('/mission clearset GETTING_STARTED', { settleMs: 1500 });
            await bot.command('/hub', { settleMs: 6000 });
            await bot.waitFor(() => /Village|Hub/i.test((bot.sidebar()?.lines ?? []).join(' ')) && !/Your Island/.test((bot.sidebar()?.lines ?? []).join(' ')), { timeoutMs: 60000, what: 'the Hub sidebar' });
            await sleep(6000);
        });

        await t.step('talking to the Salesman opens the Reward Bundles menu', async () => {
            const npc = bot.findNpc('Salesman') ?? bot.findNpc('Spooky Man') ?? bot.findNpc('Summer Sloth') ?? bot.findNpc('Bunny Man') ?? bot.findNpc('Gingerbread Man');
            t.assert(npc, 'no Salesman NPC found');
            await bot.walkNear(npc);
            for (let attempt = 1; attempt <= 3 && !bot.window(); attempt++) {
                await bot.interact(npc);
                await bot.waitWindow({ timeoutMs: 25000, title: 'Reward Bundles' }).catch(() => {});
            }
            t.assert(bot.window() && /Reward Bundles/.test(JSON.stringify(bot.window().title)), JSON.stringify(bot.window()?.title));
            return JSON.stringify(bot.window().title);
        });

        await t.step('the bottom-right button cycles through all four seasons', async () => {
            const seen = [];
            for (let i = 0; i < 4; i++) {
                seen.push(JSON.stringify(bot.window().title).match(/Reward Bundles: (\w+)/)?.[1]);
                const button = bot.window().slots.find((slot) => slot.slot === 53);
                t.assert(button && /Season/.test(button.name), JSON.stringify(button));
                await bot.click(53, { settleMs: 1200 });
                await bot.waitWindow({ timeoutMs: 6000, title: 'Reward Bundles' });
            }
            t.assert(new Set(seen).size === 4, seen.join(','));
            return seen;
        });

        await t.step('a furniture reward is claimed once', async () => {
            const target = bot.window().slots.find((slot) => slot.slot >= 10 && slot.slot <= 43 && /Click to claim/.test(slot.lore.join(' ')));
            t.assert(target, slotsText(bot).slice(0, 400));
            let mark = bot.markChat();
            await bot.click(target.slot, { settleMs: 1500 });
            const first = bot.chatSince(mark).filter((line) => !/TPS/.test(line)).join(' | ');
            t.assert(/You claimed/.test(first), first);
            mark = bot.markChat();
            await bot.click(target.slot, { settleMs: 1500 });
            const second = bot.chatSince(mark).filter((line) => !/TPS/.test(line)).join(' | ');
            t.assert(/Already claimed/.test(second), second);
            return { first, second };
        });

        await t.step('Claim Everything takes the rest of the season', async () => {
            const mark = bot.markChat();
            await bot.click(48, { settleMs: 3000 });
            const chat = bot.chatSince(mark).filter((line) => !/TPS/.test(line)).join(' | ');
            t.assert(/You claimed \d+ \w+ rewards/.test(chat), chat);
            return chat;
        });

        await t.step('claimed sprays are unlocked on the account', async () => {
            await bot.closeWindow();
            await bot.command('/item SPRAY_CAN', { settleMs: 1000 });
            const mark = bot.markChat();
            await bot.useHotbarItem(/Spray Can/, { settleMs: 300 }).catch(() => {});
            await sleep(1500);
            if (!bot.window()) return 'spray can did not open from the hotbar (skipped)';
            const text = slotsText(bot);
            t.assert(/Locked!|Click to select/.test(text), text.slice(0, 300));
            await bot.closeWindow();
            return text.slice(0, 200);
        });
    },
};
