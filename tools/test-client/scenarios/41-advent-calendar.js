const { Vec3 } = require('vec3');
const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const slotsText = (bot) => bot.window().slots.map((slot) => `${slot.slot}:${slot.name} ${slot.lore.join(' ')}`).join(' | ');

const surface = (bot, x, z) => {
    for (let y = 120; y >= 60; y--) {
        const block = bot.bot.blockAt(new Vec3(x, y, z));
        if (block && block.name !== 'air') return { x, y: y + 1, z };
    }
    return { x, y: 100, z };
};

module.exports = {
    description: 'Advent Calendar: December gate, staff preview, claiming a day, no double claims, rank furniture placeable',
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => {
            await bot.joinSkyblock();
            await sleep(4000);
        });

        await t.step('outside December the calendar stays closed', async () => {
            const mark = bot.markChat();
            await bot.command('/sbadvent', { settleMs: 2000 });
            await bot.waitWindow({ timeoutMs: 8000, title: 'Advent Calendar' });
            await bot.click(bot.findSlot(/Day 1\b/).slot, { settleMs: 1200 });
            const chat = bot.chatSince(mark).join(' | ');
            t.assert(/only open in December/.test(chat), chat);
            await bot.closeWindow();
        });

        await t.step('a staff preview of day 3 unlocks days 1 to 3 only', async () => {
            await bot.command('/sbadvent preview 3', { settleMs: 1500 });
            await bot.command('/sbadvent', { settleMs: 2000 });
            await bot.waitWindow({ timeoutMs: 8000, title: 'Advent Calendar' });
            const slots = bot.window().slots;
            const day3 = slots.find((slot) => /Day 3\b/.test(slot.name));
            const day4 = slots.find((slot) => /Day 4\b/.test(slot.name));
            t.assert(/Click to claim/.test(day3.lore.join(' ')), day3.lore.join(' '));
            t.assert(/Not available yet/.test(day4.lore.join(' ')), day4.lore.join(' '));
        });

        await t.step('claiming day 1 gives gifts, coins and the Advent Calendar Display', async () => {
            const mark = bot.markChat();
            await bot.click(bot.findSlot(/Day 1\b/).slot, { settleMs: 1500 });
            const chat = bot.chatSince(mark).join(' | ');
            t.assert(/claimed the day 1 Advent Calendar reward/.test(chat) && /Advent Calendar Display/.test(chat), chat);
            t.assert(/Already claimed/.test(bot.findSlot(/Day 1\b/).lore.join(' ')), bot.findSlot(/Day 1\b/).lore.join(' '));
            return chat;
        });

        await t.step('a day cannot be claimed twice, and a future day is refused', async () => {
            let mark = bot.markChat();
            await bot.click(bot.findSlot(/Day 1\b/).slot, { settleMs: 1200 });
            t.assert(/already claimed/.test(bot.chatSince(mark).join(' | ')), bot.chatSince(mark).join(' | '));
            mark = bot.markChat();
            await bot.click(bot.findSlot(/Day 4\b/).slot, { settleMs: 1200 });
            t.assert(/can't claim this reward yet/.test(bot.chatSince(mark).join(' | ')), bot.chatSince(mark).join(' | '));
            await bot.closeWindow();
        });

        await t.step('the Advent Calendar Display places as furniture', async () => {
            await bot.command('/tp 6 101 36', { settleMs: 1500 });
            const held = bot.bot.inventory.items().find((item) => /Advent Calendar Display/.test(item.customName ? JSON.stringify(item.customName) : item.displayName));
            t.assert(held, 'the display is not in the inventory');
            await bot.bot.equip(held, 'hand');
            await sleep(400);
            const target = surface(bot, 6, 40);
            const ground = bot.bot.blockAt(new Vec3(target.x, target.y - 1, target.z));
            const mark = bot.markChat();
            await bot.bot.lookAt(new Vec3(target.x + 0.5, target.y, target.z + 0.5), true);
            await bot.bot.placeBlock(ground, new Vec3(0, 1, 0)).catch(() => {});
            await sleep(1500);
            const chat = bot.chatSince(mark).filter((line) => !/TPS/.test(line)).join(' | ');
            t.assert(/You placed/.test(chat), chat);
            return chat;
        });
    },
};
