#!/usr/bin/env bash
set -euo pipefail

container="${PROXY_CONTAINER:-hypixel_proxy}"
mode="${1:-}"

case "$mode" in
    on)  online=false; via=true ;;
    off) online=true; via=false ;;
    *) echo "usage: $0 on|off   (on = offline accounts + ViaVersion so a 26.1 client can join, off = restore)" >&2; exit 2 ;;
esac

docker exec "$container" sed -i "s/^online-mode = .*/online-mode = $online/" /app/velocity.toml
docker exec "$container" sed -i "s/^\( *via-version:\) .*/\1 $via/" /app/configuration/config.yml
docker restart "$container" >/dev/null
until docker exec "$container" nc -z localhost 25565 2>/dev/null; do sleep 2; done
docker exec "$container" grep -n '^online-mode' /app/velocity.toml
docker exec "$container" grep -n 'via-version' /app/configuration/config.yml
