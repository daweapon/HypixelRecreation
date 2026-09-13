# Agent Instructions

## Command documentation

- Whenever adding a command, update `COMMANDS.md` in the same task. Place it in the appropriate category and preserve alphabetical ordering.
- Include its syntax, aliases, purpose, permissions, console eligibility, and availability. Do not add source links or source columns.
- Keep existing entries accurate when changing command syntax, aliases, permissions, or behavior, and remove entries for deleted commands.

## Code Style

- **Minimal comments**: Do not add comments unless absolutely necessary. The codebase uses strong OOP, abstraction, and
  modular design - code should be self-explanatory through good naming and structure.
- Only add comments for truly complex algorithms or non-obvious business logic.

## Hypixel parity

- When adding or changing a player-facing feature, research the corresponding current Hypixel behavior before implementation. Always use [Hypixel SkyBlock Wiki](https://hypixelskyblock.minecraft.wiki/) as the primary cross-reference, document any uncertainty, and call out intentional differences.
- For SkyBlock Level/XP sources, Garden (crop profits, composter, pests, greenhouse), and Attributes (shard fusions, shard prices), also cross-reference the [Skyblock Things Spreadsheet by @Georik](https://docs.google.com/spreadsheets/d/1vOyMKoTGO-uZliJJ6jS4MTdoMKYXFozSf7bjRFysk2k/edit) — a community-maintained, actively updated sheet with numeric/mechanical data (prices, XP tables, fusion recipes) that's more granular than the wiki in these areas. Requires a signed-in Google account to load (unlike the Roadmap tracker sheet, it isn't publicly readable via a plain fetch).

## Local server startup

- Start the local stack with `/home/bradl/.hypixel-skyblock/start-servers.sh`. It staggers infrastructure, services, and game-server JVMs to avoid a simultaneous WSL CPU spike.
