package installer

import (
	"fmt"
	"strings"
)

func ComposeYAML(cfg Config) string {
	var out strings.Builder
	mongoPorts := ""
	redisPorts := ""
	if cfg.ExposePorts {
		mongoPorts = `    ports:
      - "27017:27017"
`
		redisPorts = `    ports:
      - "6379:6379"
`
	}

	fmt.Fprintf(&out, `x-forwarding-env: &forwarding_env
  FORWARDING_SECRET: ${FORWARDING_SECRET:-change-me}
services:
  mongodb:
    image: mongo:8.0.9
    container_name: hypixel_mongo
    environment:
      MONGO_INITDB_DATABASE: Minestom
      MONGO_URL: hypixel_mongo
    volumes:
      - mongodb-data:/data/db
%s    networks:
      - hypixel_network
    healthcheck:
      test: |
        mongosh --eval "db.adminCommand('ping').ok" --quiet > /dev/null 2>&1 || exit 1
      interval: 10s
      timeout: 5s
      retries: 5
    command: ["mongod", "--quiet"]
    mem_limit: 768m

  redis:
    image: redis:latest
    container_name: hypixel_redis
    command: ["redis-server", "--appendonly", "yes"]
    volumes:
      - redis-data:/data
%s    networks:
      - hypixel_network
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
    mem_limit: 512m

  proxy:
    build:
      context: .
      dockerfile: "./DockerFiles/Dockerfile.proxy"
    container_name: hypixel_proxy
    environment:
      <<: *forwarding_env
      JAVA_TOOL_OPTIONS: -Xms256m -Xmx512m
    ports:
      - "25565:25565"
    depends_on:
      mongodb:
        condition: service_healthy
      redis:
        condition: service_healthy
    volumes:
      - ./configuration:/app/configuration_files
    networks:
      - hypixel_network
    healthcheck:
      test: ["CMD", "nc", "-z", "localhost", "25565"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 10s
    mem_limit: 768m

  game_server_builder:
    build:
      context: .
      dockerfile: "./DockerFiles/Dockerfile.game_server"
    image: game_server_prepared
    container_name: game_server_builder
    volumes:
      - ./configuration:/app/configuration_files
    networks:
      - hypixel_network

  pico_limbo:
    image: ghcr.io/quozul/picolimbo:v1.12.2-mc26.1.2
    container_name: pico_limbo
    restart: unless-stopped
    environment:
      <<: *forwarding_env
    depends_on:
      proxy:
        condition: service_healthy
      game_server_builder:
        condition: service_started
    volumes:
      - ./configuration:/app/configuration_files
    networks:
      - hypixel_network
    mem_limit: 256m

  pack_server:
    image: game_server_prepared
    container_name: hypixel_pack_server
    restart: "unless-stopped"
    environment:
      <<: *forwarding_env
      SERVICE_CMD: java -jar net.swofty.packer.HypixelPackServer.jar
      JAVA_TOOL_OPTIONS: -Xms128m -Xmx384m
    ports:
      - "7270:7270"
    depends_on:
      game_server_builder:
        condition: service_started
    volumes:
      - ./configuration:/app/configuration_files
    networks:
      - hypixel_network
    mem_limit: 512m

`, mongoPorts, redisPorts)

	for _, server := range Deduplicate(cfg.Servers) {
		name := strings.ToLower(server)
		fmt.Fprintf(&out, `  hypixelcore_%[1]s:
    image: game_server_prepared
    container_name: hypixelcore_%[1]s
    restart: "unless-stopped"
    environment:
      <<: *forwarding_env
      SERVICE_CMD: java -jar HypixelCore.jar %[2]s
      JAVA_TOOL_OPTIONS: -Xms256m -Xmx1536m
    depends_on:
      proxy:
        condition: service_healthy
      game_server_builder:
        condition: service_started
    volumes:
      - ./configuration:/app/configuration_files
    networks:
      - hypixel_network
    mem_limit: 2g

`, name, server)
	}

	for _, service := range Deduplicate(cfg.Services) {
		name := strings.ToLower(strings.TrimPrefix(service, "Service"))
		cmd := "java -jar " + service + ".jar"
		ports := ""
		if service == "ServiceAPI" && cfg.APIPort != "" {
			cmd += " --port=" + cfg.APIPort
			ports = fmt.Sprintf("    ports:\n      - %q\n", cfg.APIPort+":"+cfg.APIPort)
		}
		fmt.Fprintf(&out, `  service_%[1]s:
    image: game_server_prepared
    container_name: service_%[1]s
    restart: "unless-stopped"
    environment:
      <<: *forwarding_env
      SERVICE_CMD: %[2]s
      JAVA_TOOL_OPTIONS: -Xms128m -Xmx384m
    depends_on:
      proxy:
        condition: service_healthy
      game_server_builder:
        condition: service_started
    volumes:
      - ./configuration:/app/configuration_files
%[3]s    networks:
      - hypixel_network
    mem_limit: 512m

`, name, cmd, ports)
	}

	out.WriteString(`volumes:
  mongodb-data:
    driver: local
    name: mongo-data
  redis-data:
    driver: local
    name: redis-data

networks:
  hypixel_network:
    driver: bridge
`)
	return out.String()
}
