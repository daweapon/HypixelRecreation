package installer

import "os"

const (
	Version           = "2.1.0"
	GitHubRepo        = "daweapon/HypixelRecreation"
	LimboAssetsURL    = "https://files.catbox.moe/flri48.zip"
	StateFileName     = ".state.json"
	DefaultInstallSub = ".hypixel-skyblock"
)

var SkyBlockServers = []string{
	"SKYBLOCK_HUB",
	"SKYBLOCK_ISLAND",
	"SKYBLOCK_SPIDERS_DEN",
	"SKYBLOCK_THE_END",
	"SKYBLOCK_CRIMSON_ISLE",
	"SKYBLOCK_DUNGEON_HUB",
	"SKYBLOCK_THE_FARMING_ISLANDS",
	"SKYBLOCK_GOLD_MINE",
	"SKYBLOCK_DEEP_CAVERNS",
	"SKYBLOCK_DWARVEN_MINES",
	"SKYBLOCK_CRYSTAL_HOLLOWS",
	"SKYBLOCK_THE_PARK",
	"SKYBLOCK_GALATEA",
	"SKYBLOCK_TORRHUS_CANYON",
	"SKYBLOCK_BACKWATER_BAYOU",
	"SKYBLOCK_JERRYS_WORKSHOP",
	"SKYBLOCK_THE_GARDEN",
	"SKYBLOCK_KUUDRA",
	"SKYBLOCK_CATACOMBS",
}

var RequiredServers = []string{"PROTOTYPE_LOBBY"}

var MinigameServers = []string{
	"MAIN_LOBBY",
	"BEDWARS_LOBBY",
	"BEDWARS_GAME",
	"BEDWARS_CONFIGURATOR",
	"MURDER_MYSTERY_LOBBY",
	"MURDER_MYSTERY_GAME",
	"MURDER_MYSTERY_CONFIGURATOR",
	"SKYWARS_LOBBY",
	"SKYWARS_GAME",
	"SKYWARS_CONFIGURATOR",
	"RAVENGARD_LOBBY",
	"RAVENGARD_DUNGEON",
	"REPLAY_VIEWER",
}

var AllServices = []string{
	"ServiceParty",
	"ServiceAPI",
	"ServiceAuctionHouse",
	"ServiceBazaar",
	"ServiceItemTracker",
	"ServiceDarkAuction",
	"ServiceOrchestrator",
	"ServiceFriend",
	"ServiceStore",
	"ServiceElection",
	"ServiceGuild",
	"ServicePunishment",
	"ServiceReplay",
}

var RequiredServices = []string{"ServiceParty"}

var DefaultServices = []string{
	"ServiceParty",
	"ServiceAPI",
	"ServiceAuctionHouse",
	"ServiceBazaar",
	"ServiceItemTracker",
}

func AllServers() []string {
	servers := make([]string, 0, len(RequiredServers)+len(SkyBlockServers)+len(MinigameServers))
	servers = append(servers, RequiredServers...)
	servers = append(servers, SkyBlockServers...)
	servers = append(servers, MinigameServers...)
	return servers
}

func Repo() string {
	if repo := os.Getenv("SKYBLOCK_INSTALLER_REPO"); repo != "" {
		return repo
	}
	return GitHubRepo
}
