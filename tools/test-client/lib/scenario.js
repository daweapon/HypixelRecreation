class ScenarioContext {
    constructor(name, bot) {
        this.name = name;
        this.bot = bot;
        this.steps = [];
        this.notes = {};
    }

    async step(description, action) {
        const startedAt = Date.now();
        const entry = { step: description, ok: true, ms: 0 };
        this.steps.push(entry);
        try {
            const result = await action();
            if (result !== undefined) entry.data = result;
        } catch (error) {
            entry.ok = false;
            entry.error = error.message;
            entry.state = this.bot.bot ? this.bot.snapshot() : null;
            throw error;
        } finally {
            entry.ms = Date.now() - startedAt;
        }
    }

    assert(condition, message) {
        if (!condition) throw new Error(`assertion failed: ${message}`);
    }

    assertChat(lines, pattern, message) {
        const test = pattern instanceof RegExp ? (line) => pattern.test(line) : (line) => line.includes(pattern);
        if (!lines.some(test)) throw new Error(`assertion failed: ${message ?? `expected chat matching ${pattern}`}; got ${JSON.stringify(lines.slice(-10))}`);
    }
}

module.exports = { ScenarioContext };
