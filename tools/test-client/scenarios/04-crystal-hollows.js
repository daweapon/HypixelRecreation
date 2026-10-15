const STRUCTURES = [
    { id: 'jungle_temple', anchor: 'door_guardian', npc: 'Kalhuiki Door Guardian', zone: /Jungle Temple/i },
    { id: 'mines_of_divan', anchor: 'keeper_gold', npc: 'Keeper of Gold', zone: /Mines of Divan/i },
    { id: 'lost_precursor_city', anchor: 'professor_robot', npc: 'Professor Robot', zone: /Lost Precursor City/i },
    { id: 'king_yolkars_tower', anchor: 'king_yolkar', npc: 'King Yolkar', zone: null },
    { id: 'odawa_hut', anchor: 'odawa', npc: 'Odawa', zone: null },
];

module.exports = {
    description: 'Crystal Hollows: warp in, spawn NPCs, layout, every placed structure has its NPC and zone, /max crystals',
    async run(t, bot) {
        await t.step('reach SkyBlock', async () => bot.joinSkyblock());

        await t.step('/max profile as staff', async () => {
            const lines = await bot.command('/max profile', { settleMs: 8000 });
            t.assertChat(lines, /MAXED/i, 'expected the MAXED message');
            return lines.slice(-3);
        });

        await t.step('warp to the Crystal Hollows', async () => {
            const mark = bot.markChat();
            bot.say('/warp crystals');
            await bot.waitFor(() => /Crystal Hollows|Heat|Nucleus/i.test((bot.sidebar()?.lines ?? []).join(' ')), { timeoutMs: 120000, what: 'the Crystal Hollows sidebar' });
            return { sidebar: bot.sidebar().lines, chat: bot.chatSince(mark).slice(-4) };
        });

        await t.step('Nucleus NPCs are present', async () => {
            await bot.waitFor(() => bot.findNpc('Sisko'), { timeoutMs: 20000, what: 'Emissary Sisko' });
            const found = {};
            for (const name of ['Sisko', 'Gemma', 'Geonathan']) found[name] = Boolean(bot.findNpc(name));
            t.assert(found.Sisko && found.Gemma && found.Geonathan, `NPCs at the Nucleus: ${JSON.stringify(found)}`);
            return found;
        });

        await t.step('Sisko opens a menu', async () => {
            const npc = bot.findNpc('Sisko');
            await bot.walkNear(npc);
            await bot.interact(npc);
            const window = await bot.waitWindow({ timeoutMs: 8000 });
            const inert = await bot.detectInertMenu().catch(() => null);
            await bot.closeWindow();
            return { title: window.title, inert };
        });

        let layout = [];
        await t.step('/hollows layout lists the placed structures', async () => {
            layout = await bot.command('/hollows layout', { settleMs: 2500 });
            const text = layout.join('\n');
            for (const structure of STRUCTURES) t.assert(text.includes(structure.id), `layout is missing ${structure.id}`);
            return layout.slice(0, 14);
        });

        await t.step('the Jungle Temple has two Door Guardians and a sealed door', async () => {
            const layoutText = layout.join('\n');
            const match = layoutText.match(/jungle_temple at (-?\d+) (-?\d+) (-?\d+)/);
            t.assert(match, 'jungle_temple origin not in the layout');
            const [ox, oy, oz] = match.slice(1).map(Number);
            await bot.command(`/hollows tp jungle_temple/door_guardian`, { settleMs: 2500 });
            await bot.waitFor(() => bot.entities({ radius: 24 }).filter((e) => (e.customName ?? '').includes('Door Guardian')).length >= 2, { timeoutMs: 15000, what: 'two Door Guardian holograms' });
            const sealed = [];
            for (const [dx, dy, dz] of [[34, 48, 40], [34, 52, 41], [32, 46, 40], [36, 53, 41]]) {
                const block = bot.bot.blockAt(bot.bot.entity.position.constructor ? new (bot.bot.entity.position.constructor)(ox + dx, oy + dy, oz + dz) : null);
                sealed.push(block ? block.name : 'unloaded');
            }
            t.assert(sealed.every((name) => name !== 'air' && name !== 'unloaded'), `door cells are not sealed: ${JSON.stringify(sealed)}`);
            return { origin: [ox, oy, oz], sealed };
        });

        for (const structure of STRUCTURES) {
            await t.step(`${structure.id}: anchor has its NPC${structure.zone ? ' and zone' : ''}`, async () => {
                const lines = await bot.command(`/hollows tp ${structure.id}/${structure.anchor}`, { settleMs: 2500 });
                const found = await bot.waitFor(() => bot.findNpc(structure.npc), { timeoutMs: 15000, what: structure.npc }).then(() => true).catch(() => false);
                const sidebar = (bot.sidebar()?.lines ?? []).join(' | ');
                t.assert(found, `${structure.npc} not found at ${structure.id}/${structure.anchor}; chat ${JSON.stringify(lines)}`);
                if (structure.zone) t.assert(structure.zone.test(sidebar), `sidebar does not show the zone: ${sidebar}`);
                return { pos: bot.bot.entity.position, sidebar };
            });
        }

        await t.step('/max crystals hands out the crystals and is idempotent', async () => {
            const ask = async () => {
                const mark = bot.markChat();
                bot.say('/max crystals');
                return bot.waitChat(/You found|already found/i, { since: mark, timeoutMs: 15000 });
            };
            const first = await ask();
            const second = await ask();
            t.assert(/already found/i.test(second), `second run should add nothing, got ${second}`);
            return { first, second };
        });
    },
};
