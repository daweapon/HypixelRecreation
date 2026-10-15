#!/usr/bin/env node
const fs = require('fs');
const path = require('path');
const { TestBot } = require('./lib/testbot');
const { ScenarioContext } = require('./lib/scenario');

const args = process.argv.slice(2);
const flags = { json: false, keep: false, options: {} };
const names = [];
for (let i = 0; i < args.length; i++) {
    const arg = args[i];
    if (arg === '--json') flags.json = true;
    else if (arg === '--user') flags.options.username = args[++i];
    else if (arg === '--version') flags.options.version = args[++i];
    else if (arg === '--pack') flags.options.pack = args[++i];
    else if (arg === '--list') flags.list = true;
    else names.push(arg);
}

const dir = path.join(__dirname, 'scenarios');
const available = fs.readdirSync(dir).filter((file) => file.endsWith('.js')).map((file) => file.replace(/\.js$/, '')).sort();

if (flags.list || names.length === 0) {
    console.log(available.join('\n'));
    process.exit(flags.list ? 0 : 2);
}

const selected = names.includes('all') ? available : names;

async function runOne(name) {
    const file = path.join(dir, `${name}.js`);
    if (!fs.existsSync(file)) return { scenario: name, ok: false, error: `unknown scenario (have: ${available.join(', ')})`, steps: [] };
    const scenario = require(file);
    const bot = new TestBot({ ...scenario.options, ...flags.options });
    const context = new ScenarioContext(name, bot);
    const startedAt = Date.now();
    let error = null;
    try {
        await bot.connect();
        await scenario.run(context, bot);
    } catch (caught) {
        error = caught.message;
    }
    const result = {
        scenario: name,
        ok: error === null && context.steps.every((step) => step.ok),
        ms: Date.now() - startedAt,
        steps: context.steps,
        notes: context.notes,
        kicks: bot.kicks,
    };
    if (error !== null) result.error = error;
    await bot.disconnect().catch(() => {});
    return result;
}

(async () => {
    const results = [];
    for (const name of selected) results.push(await runOne(name));
    const ok = results.every((result) => result.ok);
    if (flags.json) {
        console.log(JSON.stringify({ ok, results }, null, 2));
    } else {
        for (const result of results) {
            console.log(`${result.ok ? 'PASS' : 'FAIL'} ${result.scenario} (${result.ms}ms)`);
            for (const step of result.steps) console.log(`  ${step.ok ? 'ok  ' : 'FAIL'} ${step.step}${step.error ? ` -> ${step.error}` : ''}`);
            if (result.error && !result.steps.some((step) => !step.ok)) console.log(`  error: ${result.error}`);
        }
        fs.writeFileSync(path.join(__dirname, 'last-result.json'), JSON.stringify({ ok, results }, null, 2));
    }
    process.exit(ok ? 0 : 1);
})();
