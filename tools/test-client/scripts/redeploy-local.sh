#!/usr/bin/env bash
set -euo pipefail

repo="$(cd "$(dirname "$0")/../../.." && pwd)"
stack="${HYPIXEL_LOCAL_STACK:-$HOME/.hypixel-skyblock}"
backup="$stack/DockerFiles/jars/HypixelCore.jar.bak-pre-testclient"

cd "$stack"
echo "Stopping the local stack to free memory for Gradle..."
docker compose stop >/dev/null

cd "$repo"
echo "Building loader jar..."
./gradlew :loader:shadowJar -q

[[ -f "$backup" ]] || cp "$stack/DockerFiles/jars/HypixelCore.jar" "$backup"
cp loader/build/libs/HypixelCore.jar "$stack/DockerFiles/jars/HypixelCore.jar"

echo "Syncing tracked configuration files (keeping the local config.yml, velocity.toml, server.toml)..."
git ls-files -z configuration \
    | grep -zv '^configuration/[^/]*$' \
    | rsync -a --from0 --files-from=- "$repo/" "$stack/"

cd "$stack"
echo "Rebuilding the game server image..."
docker compose build game_server_builder
docker builder prune -af >/dev/null
docker compose up -d --force-recreate game_server_builder >/dev/null

echo "Starting the proxy in test mode before the game servers register..."
docker compose up -d mongodb redis proxy >/dev/null
"$repo/tools/test-client/scripts/offline-proxy.sh" on

echo "Starting the stack..."
./start-servers.sh
