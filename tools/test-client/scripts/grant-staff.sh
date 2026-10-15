#!/usr/bin/env bash
set -euo pipefail

name="${1:?usage: $0 <username> [RANK]}"
rank="${2:-STAFF}"
container="${REDIS_CONTAINER:-hypixel_redis}"

uuid="$(python3 -I - "$name" <<'PY'
import hashlib, sys, uuid
digest = bytearray(hashlib.md5(("OfflinePlayer:" + sys.argv[1]).encode()).digest())
digest[6] = (digest[6] & 0x0F) | 0x30
digest[8] = (digest[8] & 0x3F) | 0x80
print(uuid.UUID(bytes=bytes(digest)))
PY
)"
key="hsb:acct:players:$uuid"

current="$(docker exec "$container" redis-cli get "$key")"
if [[ -z "$current" ]]; then
    echo "no account yet for $name ($uuid): join once as that user, disconnect, then rerun" >&2
    exit 1
fi

printf '%s' "$current" | python3 -I -c '
import json, sys
data = json.load(sys.stdin)
data["hypixel:rank"] = json.dumps(sys.argv[1])
print(json.dumps(data, indent=2))
' "$rank" | docker exec -i "$container" redis-cli -x set "$key" >/dev/null

echo "$name ($uuid) -> $rank"
