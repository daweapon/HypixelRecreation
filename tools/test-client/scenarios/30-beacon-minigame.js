const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const sidebarText = (bot) => (bot.sidebar()?.lines ?? []).join(' | ');
const TARGET_ROW = 9;
const PLAYER_ROW = 18;
const COLORS = ['red', 'orange', 'yellow', 'lime', 'green', 'cyan', 'light_blue', 'blue', 'purple', 'pink'];

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

const paneAt = (bot, row) => {
    const view = bot.window();
    if (!view) return null;
    const found = view.slots.find((slot) => slot.slot >= row && slot.slot < row + 9 && /stained_glass_pane/.test(slot.item) && !/black/.test(slot.item));
    return found ? { column: found.slot - row, color: COLORS.indexOf(found.item.replace('_stained_glass_pane', '')) } : null;
};

const current = (bot, slot) => {
    const entry = bot.window()?.slots.find((candidate) => candidate.slot === slot);
    const match = entry && entry.lore.join(' ').match(/Current:\s*(\d+)/);
    return match ? Number(match[1]) : null;
};

const press = async (bot, slot, button) => bot.click(slot, { button, mode: 0, settleMs: 250 });

const openBeacon = async (bot, x, y, z, at) => {
    await bot.command(`/tp ${at}`, { settleMs: 3000 });
    const Vec = bot.bot.entity.position.constructor;
    const block = bot.bot.blockAt(new Vec(x, y, z));
    if (!block || block.name !== 'beacon') throw new Error(`no beacon at ${x} ${y} ${z}: ${block && block.name}`);
    await bot.bot.lookAt(new Vec(x + 0.5, y + 0.5, z + 0.5), true);
    await bot.bot.activateBlock(block);
    await bot.waitWindow({ timeoutMs: 20000 });
};

module.exports = {
    description: 'Beacon minigame: moving panes, controls, pause, and a full solve by watching the signal; Torrhus Beacon opens with Helix Fortune',
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => bot.joinSkyblock());
        await t.step('finish the starting missions and reach Level 6', async () => {
            await bot.command('/mission clearset GETTING_STARTED', { settleMs: 1500 });
            await bot.command('/skyblockxp setlevel 6', { settleMs: 1500 });
        });
        await t.step('send the bot to Galatea', async () => {
            await sleep(3000);
            await sendTo(bot, 'SKYBLOCK_GALATEA', /Galatea|Tangleburg|Marsh/i);
        });

        await t.step('the tuning menu shows a signal row and a player row with working controls', async () => {
            await openBeacon(bot, -688, 128, 65, '-688 130 66');
            await bot.click(30, { settleMs: 800 });
            await bot.waitFor(() => paneAt(bot, TARGET_ROW) && paneAt(bot, PLAYER_ROW), { timeoutMs: 10000, what: 'both pane rows' });
            const before = current(bot, 41);
            await press(bot, 41, 0);
            t.assert(current(bot, 41) === Math.min(5, before + 1), `speed ${before} -> ${current(bot, 41)}`);
            await press(bot, 41, 1);
            t.assert(current(bot, 41) === before, `speed back to ${before}: ${current(bot, 41)}`);
            return { before };
        });

        await t.step('watching the signal gives its colour and speed, then pausing at the start lets the bot solve it', async () => {
            const seen = [];
            const started = Date.now();
            let last = null;
            while (Date.now() - started < 20000 && seen.length < 4) {
                const pane = paneAt(bot, TARGET_ROW);
                if (pane && pane.column !== last) { seen.push({ column: pane.column, at: Date.now() }); last = pane.column; }
                await sleep(40);
            }
            t.assert(seen.length >= 3, 'the signal never moved');
            const gaps = seen.slice(1).map((entry, index) => entry.at - seen[index].at);
            const stepMillis = gaps.reduce((a, b) => a + b, 0) / gaps.length;
            const speed = Math.max(1, Math.min(5, Math.round(1200 / stepMillis)));
            const target = paneAt(bot, TARGET_ROW);
            const mark = bot.markChat();
            for (let guard = 0; guard < 12 && paneAt(bot, PLAYER_ROW)?.color !== target.color; guard++) await press(bot, 39, 0);
            for (let guard = 0; guard < 6 && current(bot, 41) !== speed; guard++) await press(bot, 41, current(bot, 41) < speed ? 0 : 1);
            for (let pitch = 1; pitch <= 5; pitch++) {
                for (let guard = 0; guard < 6 && current(bot, 43) !== pitch; guard++) await press(bot, 43, current(bot, 43) < pitch ? 0 : 1);
                const deadline = Date.now() + 21000;
                while (Date.now() < deadline && !bot.chatSince(mark).some((line) => /tuned the frequency/.test(line))) await sleep(150);
                if (bot.chatSince(mark).some((line) => /tuned the frequency/.test(line))) return { speed, pitch, color: target.color };
            }
            throw new Error(`no solve; speed ${speed} (control ${current(bot, 41)}) color ${target.color} (player ${JSON.stringify(paneAt(bot, PLAYER_ROW))}, target ${JSON.stringify(paneAt(bot, TARGET_ROW))}) pitch ${current(bot, 43)} step ${stepMillis}`);
        });

        await t.step('the cooldown blocks a second tuning right away', async () => {
            await sleep(1500);
            if (!bot.window()) await openBeacon(bot, -688, 128, 65, '-688 130 66');
            const mark = bot.markChat();
            await bot.click(30, { settleMs: 800 });
            await bot.waitChat(/again in/, { since: mark, timeoutMs: 6000 });
            await bot.closeWindow().catch(() => {});
        });

        await t.step('send the bot to Torrhus Canyon', async () => {
            await sendTo(bot, 'SKYBLOCK_TORRHUS_CANYON', /Torrhus/i);
        });

        await t.step('the Torrhus Beacon opens with Helix Fortune', async () => {
            await openBeacon(bot, -595, 123, 205, '-595 125 207');
            const text = bot.window().slots.map((slot) => `${slot.name} ${slot.lore.join(' ')}`).join(' | ');
            t.assert(/Torrhus Beacon/.test(JSON.stringify(bot.window().title)) && /Helix Fortune/.test(text), text.slice(0, 400));
            await bot.closeWindow();
        });
    }
};
