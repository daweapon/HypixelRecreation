import csv
import json
import re
import sys

RARITIES = ["Common", "Uncommon", "Rare", "Epic", "Legendary"]
CATEGORIES = {"Forest", "Water", "Combat"}
SKILLS = {"Mining"}
ALIASES = {"Cocoaleach": "Cocoaleech"}
ID_PATTERN = re.compile(r"[CUREL]\d+")


def load(csv_path, shards_path):
    rows = [r for r in csv.reader(open(csv_path, encoding="utf-8"))
            if len(r) > 69 and ID_PATTERN.fullmatch(r[5].strip())]
    known = {s["id"] for s in json.load(open(shards_path, encoding="utf-8"))["shards"]}
    return rows, known


def resolve(token, names, families):
    token = re.sub(r" Shard$", "", token.strip())
    token = ALIASES.get(token, token)
    if token == "Any":
        return {"any": True}
    match = re.fullmatch(r"(Common|Uncommon|Rare|Epic|Legendary)(\+?)(?: (.+))?", token)
    if match:
        alt = {"minRarity" if match.group(2) else "rarity": match.group(1).upper()}
        if match.group(3):
            alt.update(resolve(match.group(3), names, families))
        return alt
    if token in names:
        return {"shard": names[token]}
    if token in families:
        return {"family": token}
    if token in CATEGORIES:
        return {"category": token.upper()}
    if token in SKILLS:
        return {"skill": token.upper()}
    raise SystemExit("unresolved token: " + token)


def clause(text, names, families):
    parts = [p for p in re.split(r",\s*(?:or\s+)?|\s+or\s+", text) if p.strip()]
    return [resolve(p, names, families) for p in parts]


def main(csv_path, shards_path, out_path):
    rows, known = load(csv_path, shards_path)
    ids = {r[5].strip() for r in rows}
    if ids != known:
        raise SystemExit("sheet and shards.yml disagree on ids")
    names = {r[6].strip(): r[5].strip() for r in rows}
    families = {r[12].strip() for r in rows if r[12].strip()}
    shards = {}
    special = []
    for r in rows:
        sid = r[5].strip()
        entry = {}
        idr = r[17].strip()
        if idr in ids:
            entry["id_result"] = idr
        chameleon = [c.strip() for c in r[20:23] if c.strip() in ids]
        if chameleon:
            entry["chameleon"] = chameleon
        if r[12].strip():
            entry["family"] = r[12].strip()
        shards[sid] = entry
        text = r[69].strip()
        if text:
            left, right = text.split(" + ")
            special.append({"output": sid, "inputs": [clause(left, names, families), clause(right, names, families)]})
    json.dump({"shards": shards, "special": special}, open(out_path, "w", encoding="utf-8"), indent=1, ensure_ascii=False)
    print(len(shards), "shards", len(special), "special recipes")


main(*sys.argv[1:4])
