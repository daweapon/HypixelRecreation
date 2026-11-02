const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const sidebarText = (bot) => (bot.sidebar()?.lines ?? []).join(' ');

module.exports = {
    description: 'NPC menus: SB_PLAN=\'{"warp":"/warp x","sidebar":"regex","npcs":[{"label":"Name","pos":[x,y,z],"title":"regex","rows":6}]}\'; opens each NPC menu and checks title, rows and that it is not lootable',
    async run(t, bot) {
        const plan = JSON.parse(process.env.SB_PLAN || '{}');
        const step = t.step.bind(t);
        t.step = async (name, fn) => step(name, fn).catch(() => {});

        await step('reach SkyBlock', async () => bot.joinSkyblock());
        if (plan.setup) for (const command of plan.setup) await bot.command(command, { settleMs: command.startsWith('/max') ? 8000 : 1200 });
        if (plan.warp) {
            await step(`${plan.warp} reaches the island`, async () => {
                bot.say(plan.warp);
                await bot.waitFor(() => new RegExp(plan.sidebar, 'i').test(sidebarText(bot)), { timeoutMs: 240000, what: `a sidebar matching ${plan.sidebar}` });
                await sleep(4000);
                return bot.sidebar()?.lines;
            });
        }

        for (const npc of plan.npcs ?? []) {
            await t.step(`${npc.label}: menu opens${npc.title ? ` as /${npc.title}/` : ''}${npc.rows ? ` with ${npc.rows} rows` : ''}`, async () => {
                await bot.closeWindow();
                if (npc.pos) {
                    await bot.command(`/tp ${npc.pos.map(Math.floor).join(' ')}`, { settleMs: 3000 });
                }
                if (npc.commands) for (const command of npc.commands) await bot.command(command, { settleMs: 1500 });
                let entity = null;
                await bot.waitFor(() => (entity = bot.findNpc(npc.label)), { timeoutMs: 25000, what: `the ${npc.label} NPC` });
                await bot.walkNear(entity).catch(() => {});
                const mark = bot.markChat();
                await bot.interact(entity);
                await bot.waitWindow({ timeoutMs: 8000 }).catch(() => {});
                let view = bot.window();
                for (let attempt = 0; !view && npc.retry !== false && attempt < (npc.retries ?? 1); attempt++) {
                    await sleep(npc.retryDelay ?? 2500);
                    await bot.interact(entity);
                    await bot.waitWindow({ timeoutMs: 8000 }).catch(() => {});
                    view = bot.window();
                }
                const chat = bot.chatSince(mark).filter((line) => !/TPS/.test(line)).slice(-4);
                t.assert(view, `no window; chat: ${JSON.stringify(chat)}`);
                if (npc.title) t.assert(new RegExp(npc.title, 'i').test(view.title), `title "${view.title}"`);
                if (npc.rows) t.assert(view.containerSlots / 9 === npc.rows, `rows ${view.containerSlots / 9}`);
                const probe = await bot.detectInertMenu().catch(() => null);
                t.assert(!probe || !probe.lootable, `inert: ${JSON.stringify(probe)}`);
                const result = { title: view.title, rows: view.containerSlots / 9, items: view.slots.filter((slot) => slot.name.trim()).map((slot) => `${slot.slot}:${slot.name}`) };
                if (npc.clicks) {
                    result.clicks = [];
                    for (const slot of npc.clicks) {
                        const parent = bot.window()?.title;
                        const clickMark = bot.markChat();
                        await bot.click(slot, { settleMs: 1800 });
                        const child = bot.window();
                        const childProbe = child ? await bot.detectInertMenu().catch(() => null) : null;
                        result.clicks.push({ slot, title: child?.title ?? null, rows: child ? child.containerSlots / 9 : null, lootable: childProbe?.lootable ?? null,
                            items: child ? child.slots.filter((entry) => entry.name.trim()).map((entry) => `${entry.slot}:${entry.name}`).slice(0, 40) : null,
                            chat: bot.chatSince(clickMark).filter((line) => !/TPS/.test(line)).slice(-3) });
                        t.assert(!childProbe || !childProbe.lootable, `slot ${slot} opened an inert menu`);
                        if (child && child.title !== parent) {
                            await bot.closeWindow();
                            await bot.interact(entity);
                            await bot.waitWindow({ timeoutMs: 8000 }).catch(() => {});
                        }
                    }
                }
                return result;
            });
        }
    },
};
