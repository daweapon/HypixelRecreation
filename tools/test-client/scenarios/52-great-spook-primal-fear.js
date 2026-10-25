const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const clean = (chat) => chat.filter((line) => !/TPS/.test(line)).join(' | ');

async function hitUntil(bot, name, done, swings = 40) {
    for (let swing = 0; swing < swings; swing++) {
        const target = bot.nearestEntity(name);
        if (!target) return false;
        await bot.command(`/tp ${Math.round(target.x)} ${Math.round(target.y)} ${Math.round(target.z) + 2}`, { settleMs: 250 });
        const near = bot.nearestEntity(name);
        if (near) await bot.attack(near);
        await sleep(650);
        if (done()) return true;
    }
    return false;
}

module.exports = {
    description: 'Great Spook: a Primal Fear spawns, dies in five hits, pays Dark Candy and turns into a manifestation; Deadline demands standing still and Math Teacher an answer',
    async run(t, bot) {
        await t.step('reach the hub with the event on', async () => {
            await bot.joinSkyblock();
            await bot.command('/max profile', { settleMs: 8000 });
            await bot.command('/adminbypass greatspook on', { settleMs: 4000 });
        });

        await t.step('a Primal Fear spawns with its chat line and dies with the rewards line', async () => {
            const mark = bot.markChat();
            await bot.command('/adminbypass primalfear', { settleMs: 2500 });
            t.assert(/A Primal Fear has been summoned!/.test(clean(bot.chatSince(mark))), clean(bot.chatSince(mark)));
            const dead = await hitUntil(bot, 'primal fear', () => /turned into a/.test(clean(bot.chatSince(mark))));
            t.assert(dead, clean(bot.chatSince(mark)));
            t.assert(/You got some rewards from killing a Primal Fear!/.test(clean(bot.chatSince(mark))), clean(bot.chatSince(mark)));
        });

        await t.step('Deadline holds at 1 health until the player stands still for five seconds', async () => {
            const mark = bot.markChat();
            await bot.command('/adminbypass primalfear deadline', { settleMs: 2000 });
            await hitUntil(bot, 'deadline', () => bot.titles.some((title) => /STAND STILL/.test(title.text)), 20);
            t.assert(bot.titles.some((title) => /STAND STILL/.test(title.text)), 'no STAND STILL title');
            await sleep(6500);
            t.assert(bot.titles.some((title) => /Curse Lifted/.test(title.text)), 'no Curse Lifted title');
            await hitUntil(bot, 'deadline', () => /rewards from killing/.test(clean(bot.chatSince(mark))), 6);
            t.assert(/rewards from killing a Primal Fear/.test(clean(bot.chatSince(mark))), clean(bot.chatSince(mark)));
        });

        await t.step('Math Teacher asks an equation and dies when it is answered', async () => {
            const mark = bot.markChat();
            await bot.command('/adminbypass primalfear math_teacher', { settleMs: 2000 });
            await hitUntil(bot, 'math teacher', () => /Solve:/.test(clean(bot.chatSince(mark))), 20);
            const line = clean(bot.chatSince(mark)).match(/Solve: ([0-9 *]+) =/);
            t.assert(line, clean(bot.chatSince(mark)));
            const answer = Function(`return ${line[1]}`)();
            bot.say(String(answer));
            await sleep(2500);
            t.assert(/rewards from killing a Primal Fear/.test(clean(bot.chatSince(mark))), clean(bot.chatSince(mark)));
        });

        await t.step('turn the event off again', async () => {
            await bot.command('/adminbypass greatspook off', { settleMs: 2500 });
        });
    }
};
