const mineflayer = require('mineflayer');
const { plain } = require('./text');

const DEFAULTS = {
    host: process.env.SB_HOST || '127.0.0.1',
    port: Number(process.env.SB_PORT || 25565),
    version: process.env.SB_VERSION || '26.1',
    username: process.env.SB_USER || 'ClaudeBot',
    pack: 'accept',
};

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

class TestBot {
    constructor(options = {}) {
        this.options = { ...DEFAULTS, ...options };
        this.bot = null;
        this.chat = [];
        this.titles = [];
        this.actionBars = [];
        this.sounds = [];
        this.kicks = [];
        this.errors = [];
        this.packs = [];
        this.scores = new Map();
        this.objectiveTitles = new Map();
        this.sidebarObjective = null;
        this.windowLog = [];
        this.startedAt = Date.now();
    }

    async connect({ timeoutMs = 60000 } = {}) {
        const { host, port, version, username } = this.options;
        this.bot = mineflayer.createBot({ host, port, version, username, auth: 'offline', hideErrors: true });
        const bot = this.bot;
        this.#record(bot);

        await new Promise((resolve, reject) => {
            const timer = setTimeout(() => reject(new Error(`connect timed out after ${timeoutMs}ms; chat so far: ${JSON.stringify(this.chatText().slice(-5))}`)), timeoutMs);
            bot.once('spawn', () => { clearTimeout(timer); resolve(); });
            bot.once('kicked', (reason) => { clearTimeout(timer); reject(new Error(`kicked during connect: ${plain(reason)}`)); });
            bot.once('error', (error) => { clearTimeout(timer); reject(error); });
            bot.once('end', (reason) => { clearTimeout(timer); reject(new Error(`connection ended during connect: ${reason}`)); });
        });
        return this;
    }

    #record(bot) {
        const stamp = () => Date.now() - this.startedAt;
        bot.on('message', (message, position) => {
            if (position === 'game_info') return;
            this.chat.push({ t: stamp(), text: message.toString(), json: message.json });
        });
        bot.on('actionBar', (message) => this.actionBars.push({ t: stamp(), text: message.toString() }));
        bot.on('title', (text, kind) => this.titles.push({ t: stamp(), kind, text: plain(text) }));
        bot.on('soundEffectHeard', (name) => this.sounds.push({ t: stamp(), name }));
        bot.on('hardcodedSoundEffectHeard', (id) => this.sounds.push({ t: stamp(), id }));
        bot.on('kicked', (reason) => this.kicks.push({ t: stamp(), reason: plain(reason) }));
        bot._client.on('scoreboard_objective', (packet) => {
            if (packet.action === 1) {
                this.scores.delete(packet.name);
                this.objectiveTitles.delete(packet.name);
                if (this.sidebarObjective === packet.name) this.sidebarObjective = null;
            } else {
                this.objectiveTitles.set(packet.name, plain(packet.displayText));
            }
        });
        bot._client.on('scoreboard_display_objective', (packet) => {
            if (packet.position === 1) this.sidebarObjective = packet.name || null;
        });
        bot._client.on('scoreboard_score', (packet) => {
            if (!this.scores.has(packet.scoreName)) this.scores.set(packet.scoreName, new Map());
            this.scores.get(packet.scoreName).set(packet.itemName, { value: packet.value, display: packet.display_name });
        });
        bot._client.on('reset_score', (packet) => {
            const board = this.scores.get(packet.objective_name ?? packet.objectiveName ?? packet.scoreName);
            if (board) board.delete(packet.entity_name ?? packet.entityName ?? packet.itemName);
        });
        bot._client.on('add_resource_pack', (packet) => this.#answerPack(packet.uuid));
        bot.on('windowOpen', (window) => this.windowLog.push({ t: stamp(), type: 'open', title: plain(window.title), slots: window.slots.length }));
        bot.on('windowClose', () => this.windowLog.push({ t: stamp(), type: 'close' }));
        bot._client.on('error', (error) => this.errors.push({ t: stamp(), message: error.message }));
        bot.on('error', (error) => this.errors.push({ t: stamp(), message: error.message }));
        bot.on('login', () => this.windowLog.push({ t: stamp(), type: 'login' }));
        bot.on('respawn', () => this.windowLog.push({ t: stamp(), type: 'respawn' }));
    }

    #answerPack(uuid) {
        const results = this.options.pack === 'decline' ? [1] : [3, 4, 0];
        results.forEach((result, index) => {
            setTimeout(() => this.bot?._client.write('resource_pack_receive', { uuid, result }), 30 * (index + 1));
        });
        this.packs.push({ uuid, answered: this.options.pack });
    }

    chatText() {
        return this.chat.map((entry) => entry.text);
    }

    markChat() {
        return this.chat.length;
    }

    chatSince(mark = 0) {
        return this.chat.slice(mark).map((entry) => entry.text);
    }

    async waitFor(predicate, { timeoutMs = 10000, intervalMs = 100, what = 'condition' } = {}) {
        const deadline = Date.now() + timeoutMs;
        for (;;) {
            const value = await predicate();
            if (value) return value;
            if (Date.now() > deadline) throw new Error(`timed out after ${timeoutMs}ms waiting for ${what}`);
            await sleep(intervalMs);
        }
    }

    async waitChat(pattern, { since = 0, timeoutMs = 10000 } = {}) {
        const matches = typeof pattern === 'function' ? pattern : (text) => (pattern instanceof RegExp ? pattern.test(text) : text.includes(pattern));
        return this.waitFor(() => this.chat.slice(since).find((entry) => matches(entry.text)), { timeoutMs, what: `chat matching ${pattern}` }).then((entry) => entry.text);
    }

    say(message) {
        this.bot.chat(message);
    }

    async command(command, { settleMs = 1500 } = {}) {
        const mark = this.markChat();
        this.bot.chat(command.startsWith('/') ? command : `/${command}`);
        await sleep(settleMs);
        return this.chatSince(mark);
    }

    async sendAndWait(command, pattern, { timeoutMs = 10000 } = {}) {
        const mark = this.markChat();
        this.bot.chat(command.startsWith('/') ? command : `/${command}`);
        const line = await this.waitChat(pattern, { since: mark, timeoutMs });
        return { line, lines: this.chatSince(mark) };
    }

    sidebar() {
        const objective = this.sidebarObjective;
        if (!objective) return null;
        const entries = [...(this.scores.get(objective) ?? new Map()).entries()].sort((a, b) => b[1].value - a[1].value);
        const title = this.objectiveTitles.get(objective) ?? '';
        return { title, lines: entries.map(([name, score]) => this.#scoreLine(name, score)) };
    }

    #scoreLine(name, score) {
        const custom = score.display ? plain(score.display) : '';
        if (custom) return custom;
        const team = this.bot.teamMap?.[name];
        const prefix = team ? plain(team.prefix) : '';
        const suffix = team ? plain(team.suffix) : '';
        return `${prefix}${name.replace(/§./g, '')}${suffix}`;
    }

    tabList() {
        return Object.values(this.bot.players).map((player) => ({
            username: player.username,
            display: plain(player.displayName),
            ping: player.ping,
            gamemode: player.gamemode,
        }));
    }

    bossBars() {
        return Object.values(this.bot.bossBars ?? {}).map((bar) => ({ title: plain(bar.title), health: bar.health, color: bar.color }));
    }

    position() {
        const { x, y, z } = this.bot.entity.position;
        return { x, y, z, yaw: this.bot.entity.yaw, pitch: this.bot.entity.pitch };
    }

    entities({ radius = 32, names = null } = {}) {
        const me = this.bot.entity.position;
        return Object.values(this.bot.entities)
            .filter((entity) => entity !== this.bot.entity && entity.position.distanceTo(me) <= radius)
            .map((entity) => ({
                id: entity.id,
                type: entity.name ?? entity.type,
                kind: entity.type,
                player: entity.username ?? null,
                customName: this.entityCustomName(entity),
                x: +entity.position.x.toFixed(2),
                y: +entity.position.y.toFixed(2),
                z: +entity.position.z.toFixed(2),
                distance: +entity.position.distanceTo(me).toFixed(2),
            }))
            .filter((entity) => !names || names.some((name) => `${entity.customName} ${entity.type}`.toLowerCase().includes(name.toLowerCase())))
            .sort((a, b) => a.distance - b.distance);
    }

    entityCustomName(entity) {
        const raw = entity.metadata?.[2];
        return raw && typeof raw === 'object' ? plain(raw) : null;
    }

    nearestEntity(filter) {
        const needle = filter.toLowerCase();
        const found = this.entities({ radius: 64 }).find((entity) => `${entity.customName ?? ''} ${entity.type}`.toLowerCase().includes(needle));
        return found ? this.bot.entities[found.id] : null;
    }

    findNpc(label) {
        const needle = label.toLowerCase();
        const holograms = this.entities({ radius: 96 }).filter((entity) => entity.customName && entity.customName.toLowerCase().includes(needle));
        for (const hologram of holograms) {
            const bodies = Object.values(this.bot.entities)
                .filter((entity) => entity !== this.bot.entity && entity.name !== 'armor_stand' && entity.name !== 'text_display')
                .map((entity) => ({ entity, flat: Math.hypot(entity.position.x - hologram.x, entity.position.z - hologram.z) }))
                .filter((candidate) => candidate.flat < 1.5)
                .sort((a, b) => a.flat - b.flat);
            if (bodies.length) return bodies[0].entity;
        }
        return null;
    }

    async walkNear(entity, { within = 2.5, timeoutMs = 8000 } = {}) {
        const target = entity.position;
        const start = this.bot.entity.position;
        const distance = start.distanceTo(target);
        if (distance <= within) return;
        const mark = this.markChat();
        const stand = target.offset(0, 0, 0);
        this.bot.chat(`/tp ${Math.floor(stand.x)} ${Math.floor(stand.y)} ${Math.floor(stand.z) + 2}`);
        await this.waitFor(() => this.bot.entity.position.distanceTo(target) < within + 1, { timeoutMs, what: 'teleport next to the entity' }).catch((error) => {
            throw new Error(`${error.message}; chat: ${JSON.stringify(this.chatSince(mark))}`);
        });
    }

    async lookAt(x, y, z) {
        await this.bot.lookAt({ x, y, z }, true);
    }

    async interact(entity) {
        await this.bot.lookAt(entity.position.offset(0, (entity.height ?? 1.8) / 2, 0), true);
        this.bot.activateEntity(entity);
    }

    async attack(entity) {
        await this.bot.lookAt(entity.position.offset(0, (entity.height ?? 1.8) / 2, 0), true);
        this.bot.attack(entity);
    }

    async teleportTo(x, y, z) {
        const mark = this.markChat();
        this.bot.chat(`/tp ${Math.round(x)} ${Math.round(y)} ${Math.round(z)}`);
        await sleep(1200);
        return this.chatSince(mark);
    }

    async joinSkyblock({ timeoutMs = 120000 } = {}) {
        await this.waitFor(() => this.findNpc('skyblock'), { timeoutMs: 20000, what: 'the SkyBlock NPC in the lobby' });
        const npc = this.findNpc('skyblock');
        await this.walkNear(npc);
        await this.interact(npc);
        await this.waitFor(() => /SKYBLOCK/i.test(this.sidebar()?.title ?? '') && this.sidebar().lines.length > 3, { timeoutMs, what: 'the SkyBlock sidebar' });
        return this.sidebar();
    }

    async useHotbarItem(matcher, { settleMs = 400 } = {}) {
        const test = matcher instanceof RegExp ? (text) => matcher.test(text) : (text) => text.toLowerCase().includes(String(matcher).toLowerCase());
        const slot = this.bot.inventory.slots.findIndex((item, index) => index >= 36 && index <= 44 && item && test(item.customName ? plain(item.customName) : item.displayName));
        if (slot < 0) throw new Error(`no hotbar item matching ${matcher}; hotbar: ${JSON.stringify(this.bot.inventory.slots.slice(36, 45).map((item) => item && (item.customName ? plain(item.customName) : item.displayName)))}`);
        this.bot.setQuickBarSlot(slot - 36);
        await sleep(settleMs);
        this.bot.activateItem();
    }

    async waitWindow({ timeoutMs = 10000, title = null } = {}) {
        return this.waitFor(() => {
            const window = this.bot.currentWindow;
            if (!window) return null;
            if (title && !plain(window.title).includes(title)) return null;
            return window;
        }, { timeoutMs, what: `a window${title ? ` titled ${title}` : ''}` });
    }

    window() {
        const window = this.bot.currentWindow;
        if (!window) return null;
        const containerSlots = window.inventoryStart ?? window.slots.length;
        return {
            title: plain(window.title),
            type: window.type,
            containerSlots,
            slots: window.slots.slice(0, containerSlots).map((item, slot) => this.describeItem(item, slot)).filter(Boolean),
        };
    }

    describeItem(item, slot) {
        if (!item) return null;
        const name = item.customName ? plain(item.customName) : item.displayName;
        const lore = (item.customLore ?? []).map((line) => plain(line));
        return { slot, item: item.name, count: item.count, name, lore };
    }

    findSlot(matcher) {
        const view = this.window();
        if (!view) return null;
        const test = matcher instanceof RegExp ? (text) => matcher.test(text) : (text) => text.toLowerCase().includes(String(matcher).toLowerCase());
        return view.slots.find((entry) => test(entry.name)) ?? null;
    }

    async click(slot, { button = 0, mode = 0, settleMs = 600 } = {}) {
        await this.bot.clickWindow(slot, button, mode);
        await sleep(settleMs);
    }

    async closeWindow() {
        if (this.bot.currentWindow) this.bot.closeWindow(this.bot.currentWindow);
        await sleep(300);
    }

    async detectInertMenu({ settleMs = 700 } = {}) {
        const window = this.bot.currentWindow;
        if (!window) throw new Error('no window open');
        const containerSlots = window.inventoryStart ?? window.slots.length;
        const candidate = window.slots.slice(0, containerSlots).findIndex((item) => item);
        if (candidate < 0) return { lootable: false, reason: 'empty window' };
        const before = window.slots[candidate]?.name;
        await this.bot.clickWindow(candidate, 0, 0).catch(() => {});
        await sleep(settleMs);
        const cursor = this.bot.currentWindow?.selectedItem ?? this.bot.inventory.selectedItem ?? null;
        const slotNow = this.bot.currentWindow?.slots[candidate] ?? null;
        const lootable = Boolean(cursor) || !slotNow;
        if (lootable) {
            await this.bot.clickWindow(candidate, 0, 0).catch(() => {});
            await sleep(200);
        }
        return { lootable, slot: candidate, item: before, cursor: cursor ? cursor.name : null, slotStillFilled: Boolean(slotNow) };
    }

    snapshot() {
        return {
            position: this.bot.entity ? this.position() : null,
            health: this.bot.health,
            sidebar: this.sidebar(),
            window: this.window(),
            bossBars: this.bossBars(),
            lastChat: this.chatText().slice(-15),
            titles: this.titles.slice(-5),
            actionBar: this.actionBars.slice(-1),
        };
    }

    async disconnect() {
        if (!this.bot) return;
        const ended = new Promise((resolve) => this.bot.once('end', resolve));
        this.bot.quit();
        await Promise.race([ended, sleep(2000)]);
    }
}

module.exports = { TestBot, sleep, plain };
