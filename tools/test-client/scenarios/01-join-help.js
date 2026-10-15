module.exports = {
    description: 'Join through the proxy as a staff test player, run /help and read the chat',
    async run(t, bot) {
        await t.step('spawned in the Prototype Lobby with the staff rank', async () => {
            await bot.waitChat(/joined the lobby/, { timeoutMs: 20000 });
            t.assert(bot.chatText().some((line) => line.includes('[ዞ]')), 'staff prefix missing from the join message');
            return { sidebar: bot.sidebar()?.title, position: bot.position() };
        });

        await t.step('/help lists commands', async () => {
            const lines = await bot.command('/help');
            t.assertChat(lines, /Commands • Page 1\/\d+/, '/help header missing');
            t.assert(lines.filter((line) => line.startsWith('/')).length >= 5, 'fewer than five command lines');
            return { sample: lines.slice(0, 5) };
        });

        await t.step('an unknown command is rejected', async () => {
            const lines = await bot.command('/definitelynotacommand');
            t.assert(lines.length > 0, 'server said nothing about an unknown command');
            return lines;
        });
    },
};
