#!/usr/bin/env python3
"""Regenerate configuration/skyblock/accessories/families.yml from the A1 research data.

Usage: python3 -I tools/accessories/accessory_families.py <repo root> [A1_accessories_data.json]

Members are written with the ids this server uses (ALIASES maps a Hypixel id to an older repo id),
so a line can list accessories that don't exist yet; they join the line the moment their item YAML
is added. Lines Hypixel adds after the research snapshot are appended by hand at the end of the file.
"""
import json
import os
import sys

DATA = '/home/bradl/hypixel-dev/.claude/pets-acc-work/research/A1_accessories_data.json'

ALIASES = {
    'SCARF_STUDIES': 'SCARFS_STUDIES',
    'SCARF_THESIS': 'SCARFS_THESIS',
    'SCARF_GRIMOIRE': 'SCARFS_GRIMOIRE',
    'ABICASE_BLUE_BLUE': 'ACTUALLY_BLUE_ABICASE',
    'ABICASE_BLUE_GREEN': 'BLUE_BUT_GREEN_ABICASE',
    'ABICASE_BLUE_RED': 'BLUE_BUT_RED_ABICASE',
    'ABICASE_BLUE_YELLOW': 'BLUE_BUT_YELLOW_ABICASE',
    'ABICASE_BLUE_AQUA': 'LIGHTER_BLUE_ABICASE',
    'ABICASE_REZAR': 'REZAR_ABICASE',
    'ABICASE_SUMSUNG_1': 'SUMSUNG_G3_ABICASE',
    'ABICASE_SUMSUNG_2': 'SUMSUNG_GG_ABICASE',
    'MINE_TALISMAN': 'MINE_AFFINITY_TALISMAN',
    'VILLAGE_TALISMAN': 'VILLAGE_AFFINITY_TALISMAN',
}


def server_id(hypixel_id):
    return ALIASES.get(hypixel_id, hypixel_id)


def main():
    repo = sys.argv[1]
    data = json.load(open(sys.argv[2] if len(sys.argv) > 2 else DATA))
    out = [
        '# Accessory upgrade lines and "only one counts" groups. Within a family only the highest member',
        '# present counts (effects, stats and Accessory Power). Members are listed lowest to highest.',
        '# Generated from Hypixel data (NEU talisman_upgrades + wiki) by tools/accessories/accessory_families.py.',
        '# Add lines Hypixel releases later by hand at the end.',
        'families:',
    ]
    for line in sorted(data['lines'], key=lambda l: l['line_id']):
        kind = 'UPGRADE_LINE' if line['type'] == 'upgrade_line' else 'EXCLUSIVE_GROUP'
        members = [server_id(m) for m in line['members_low_to_high']]
        out.append('  - id: %s' % line['line_id'])
        out.append('    name: "%s"' % line['name'].replace('"', "'"))
        out.append('    type: %s' % kind)
        out.append('    members: [%s]' % ', '.join(members))
        if line.get('group_pick'):
            out.append('    pick: %s' % server_id(line['group_pick']))
    path = os.path.join(repo, 'configuration/skyblock/accessories/families.yml')
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w') as f:
        f.write('\n'.join(out) + '\n')
    print('wrote %d families to %s' % (len(data['lines']), path))


if __name__ == '__main__':
    main()
