#!/usr/bin/env python3
"""Draft accessory item YAML (ACCESSORY_FORMAT.md) from the A1 research data.

Usage: python3 -I tools/accessories/accessory_yaml.py <repo root> <ID or "Name"> [more...]
       python3 -I tools/accessories/accessory_yaml.py <repo root> --missing      (list ids not in the repo yet)

Prints one item block per accessory: rarity, stats, ACCESSORY flags (dungeon, recombobulate,
only_in, power, dynamic_max_rarity), the effect text as an INACTIVE effect, the skull texture, a
shaped recipe when every ingredient exists, and DEFAULT_SOULBOUND. Wire each INACTIVE effect to a
typed effect by hand, or leave it INACTIVE when the system it needs isn't built. Warnings go to stderr.
"""
import json
import os
import re
import sys

DATA = '/home/bradl/hypixel-dev/.claude/pets-acc-work/research/A1_accessories_data.json'
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from accessory_families import ALIASES  # noqa: E402

STAT_ALIASES = {
    'crit_damage': 'critical_damage', 'crit_chance': 'critical_chance', 'health_regen': 'health_regeneration',
    'attack_speed': 'bonus_attack_speed', 'bonus_attack_speed': 'bonus_attack_speed',
    'trophy_chance': 'trophy_fish_chance', 'melon_slice_fortune': 'melon_fortune',
}
RARITIES = ['COMMON', 'UNCOMMON', 'RARE', 'EPIC', 'LEGENDARY', 'MYTHIC', 'DIVINE']
FOOTER = re.compile(r'^(VERY SPECIAL|SPECIAL|COMMON|UNCOMMON|RARE|EPIC|LEGENDARY|MYTHIC|DIVINE)\b.*(ACCESSORY|HATCESSORY)')


def warn(msg):
    print('WARN: ' + msg, file=sys.stderr)


def repo_ids(repo):
    path = os.path.join(repo, 'commons/src/generated/java/net/swofty/commons/skyblock/item/ItemType.java')
    return set(re.findall(r'^\s+([A-Z0-9_]+)\(', open(path).read(), re.M))


def stat_name(label, known):
    key = re.sub(r'[^a-z0-9]+', '_', label.lower()).strip('_')
    key = STAT_ALIASES.get(key, key)
    return key if key.upper() in known else None


def known_stats(repo):
    path = os.path.join(repo, 'commons/src/main/java/net/swofty/commons/skyblock/statistics/ItemStatistic.java')
    return set(re.findall(r'^\s+([A-Z_]+)\(', open(path).read(), re.M))


def number(text):
    value = float(re.sub(r'[^0-9.\-]', '', text))
    return int(value) if value.is_integer() else value


def effect_lines(acc):
    lines = []
    for line in acc.get('lore') or []:
        if FOOTER.match(line) or line.startswith('Accessory Power:') or line.startswith('Works while'):
            continue
        if re.match(r'^[A-Z][A-Za-z\' ]+: [+-]', line):
            continue
        lines.append(line)
    while lines and not lines[0]:
        lines.pop(0)
    while lines and not lines[-1]:
        lines.pop()
    return lines


def quote(text):
    return '"' + text.replace('\\', '\\\\').replace('"', '\\"') + '"'


def recipe(acc, ids, sid):
    crafts = (acc.get('obtain') or {}).get('crafting') or []
    if not crafts:
        return []
    grid = crafts[0].get('grid') or {}
    letters, pattern, ingredients = {}, [], {}
    for row in 'ABC':
        line = ''
        for col in '123':
            cell = grid.get(row + col)
            if not cell:
                line += ' '
                continue
            item, _, count = cell.partition(':')
            item = ALIASES.get(item, item)
            if item not in ids:
                warn('%s recipe needs %s, which is not in the repo; recipe left out' % (sid, item))
                return []
            key = (item, count or '1')
            if key not in letters:
                letters[key] = chr(ord('A') + len(letters))
                ingredients[letters[key]] = key
            line += letters[key]
        pattern.append(line)
    out = ['      - id: DEFAULT_CRAFTABLE', '        default-craftable: false', '        recipes:',
           '          - type: SHAPED', '            recipe-type: SPECIAL', '            pattern:']
    out += ['              - %s' % quote(p) for p in pattern]
    out.append('            ingredients:')
    for letter, (item, count) in ingredients.items():
        out += ['              %s:' % letter, '                type: %s' % item, '                amount: %s' % int(float(count))]
    out += ['            result:', '              type: %s' % sid, '              amount: 1']
    return out


def block(acc, ids, stats_known):
    sid = ALIASES.get(acc['id'], acc['id'])
    out = ['  - id: %s' % sid, '    material: PLAYER_HEAD' if acc.get('skull_texture') else '    material: %s' % (acc.get('material') or 'stone').replace('minecraft:', '').upper(),
           '    name: %s' % quote(acc['name']), '    rarity: %s' % acc['rarity'].replace(' ', '_')]
    stats = []
    for label, value in (acc.get('stats') or {}).items():
        name = stat_name(label, stats_known)
        if name is None:
            warn('%s: stat %r has no ItemStatistic; put it in an effect or add the stat' % (sid, label))
            continue
        stats.append('      %s: %s' % (name, number(value)))
    if stats:
        out += ['    default_statistics:'] + stats

    out += ['    components:', '      - id: ACCESSORY']
    if acc.get('dungeon_accessory'):
        out.append('        dungeon: true')
    if acc.get('can_recombobulate') is False:
        out.append('        recombobulate: false')
    if acc.get('rift_dimension_only'):
        out.append('        only_in: RIFT')
    if acc.get('bingo_only'):
        out.append('        only_in: BINGO')
    special = acc.get('special_ap') or ''
    if 'twice' in special.lower() or 'double:' in special.lower() or 'counts double' in special.lower():
        out.append('        power: DOUBLE')
    if 'abiphone' in special.lower():
        out.append('        power: ABIPHONE_CONTACTS')
    if 'Dynamic rarity' in (acc.get('legacy_note') or ''):
        top = RARITIES.index(acc['max_rarity']) - (1 if acc.get('can_recombobulate') else 0)
        out.append('        dynamic_max_rarity: %s' % RARITIES[top])
    text = effect_lines(acc)
    if text:
        out += ['        effects:', '          - type: INACTIVE', '            text:']
        out += ['              - %s' % quote(line) for line in text]
    if acc.get('skull_texture'):
        out += ['      - id: SKULL_HEAD', '        texture: %s' % acc['skull_texture']]
    out += recipe(acc, ids, sid)
    if acc.get('soulbound'):
        out += ['      - id: DEFAULT_SOULBOUND', '        coop_allowed: %s' % ('true' if acc['soulbound'] == 'co-op' else 'false')]
    obtain = acc.get('obtain') or {}
    sources = [s for s in obtain.get('wiki_source') or [] if not s.startswith('{{')]
    if sources or obtain.get('npc_shop') or obtain.get('mob_drop'):
        warn('%s: wire its source (%s)' % (sid, '; '.join(sources + [str(x) for x in (obtain.get('npc_shop') or []) + (obtain.get('mob_drop') or [])])[:200]))
    return out


def main():
    repo = sys.argv[1]
    data = json.load(open(DATA))
    by_id = {a['id']: a for a in data['accessories']}
    by_name = {a['name'].lower(): a for a in data['accessories']}
    ids = repo_ids(repo)
    if sys.argv[2:] == ['--missing']:
        for a in sorted(data['accessories'], key=lambda a: a['id']):
            if ALIASES.get(a['id'], a['id']) not in ids:
                print('%-36s %-10s %s' % (a['id'], a['rarity'], a['line_id'] or 'standalone'))
        return
    stats_known = known_stats(repo)
    out = ['items:']
    for query in sys.argv[2:]:
        acc = by_id.get(query.upper()) or by_name.get(query.lower())
        if acc is None:
            warn('no accessory called %r' % query)
            continue
        if ALIASES.get(acc['id'], acc['id']) in ids:
            warn('%s already exists in the repo' % acc['id'])
        out += block(acc, ids, stats_known)
    print('\n'.join(out))


if __name__ == '__main__':
    main()
