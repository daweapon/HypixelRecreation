const COLOR_CODES = /§[0-9a-fk-or]/gi;

function flatten(node) {
    if (node === null || node === undefined) return '';
    if (typeof node === 'string') return node;
    if (typeof node === 'number' || typeof node === 'boolean') return String(node);
    if (Array.isArray(node)) return node.map(flatten).join('');
    if (node.type !== undefined && node.value !== undefined && typeof node.type === 'string') {
        return flatten(simplify(node));
    }
    let out = '';
    if (node.text !== undefined) out += flatten(node.text);
    else if (node.translate !== undefined) out += node.with ? flatten(node.with) : node.translate;
    if (node.extra) out += flatten(node.extra);
    return out;
}

function simplify(nbtNode) {
    const nbt = require('prismarine-nbt');
    return nbt.simplify(nbtNode);
}

function plain(node) {
    return flatten(node).replace(COLOR_CODES, '');
}

module.exports = { flatten, plain };
