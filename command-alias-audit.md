# Hypixel command alias audit

Scope: aliases for commands already implemented in the remake. Checked 2026-09-07. Roadmap item 7.

The official wiki [closed in July 2026](https://hypixel.net/threads/end-of-the-official-hypixel-wiki-july-2026.6112020/). The mappings below use Hypixel support and firsthand player documentation on the Hypixel forums. Forum evidence documents observed behavior, rather than guaranteeing that every alias remains available on every live Hypixel server.

| Existing command | Added aliases / equivalent forms | Evidence |
| --- | --- | --- |
| `/msg <player> <message>` | `/m`, `/w`, `/t`, `/tell`, `/whisper` with identical arguments | [Messaging aliases](https://hypixel.net/threads/list-commands-on-the-server.331180/), [party/guild guide](https://hypixel.net/threads/guide-all-party-and-guild-commands.1061767/) |
| `/chat <type>` | `/ch <type>` | [Official quality-of-life update](https://hypixel.net/threads/quality-of-life-update.1407703/) |
| `/party chat <message>` | `/pc`, `/pchat` | [Official party support](https://support.hypixel.net/hc/en-us/articles/360019551320-How-to-Create-a-Party-on-Hypixel), [player command guide](https://hypixel.net/threads/guide-commonly-used-commands.4100375/) |
| `/guild chat <message>` | `/gc`, `/gchat` | [Player command guide](https://hypixel.net/threads/guide-commonly-used-commands.4100375/) |
| `/guild officerchat <message>` | `/oc`, `/ochat` | [Player command guide](https://hypixel.net/threads/guide-commonly-used-commands.4100375/) |
| `/party list` | `/pl` | [Command shortcuts](https://hypixel.net/threads/in-game-commands-you-might-not-know.2005657/), [2023 firsthand usage](https://hypixel.net/threads/internal-server-error.5442763/post-38565037) |
| `/guild list` | `/gl` | [Command shortcuts](https://hypixel.net/threads/in-game-commands-you-might-not-know.2005657/) |
| `/party invite <player>` | `/party add <player>` (also `/p add`) | [Party guide](https://hypixel.net/threads/guide-hypixel-server-basics-features-statistics-and-more.4285518/) |
| `/party kick <player>` | `/party remove <player>` (also `/p remove`) | [Party command guide](https://hypixel.net/threads/guide-all-party-and-guild-commands.1061767/) |
| `/party kickoffline` | `/party removeoffline` (also `/p removeoffline`) | [Official quality-of-life update](https://hypixel.net/threads/quality-of-life-update.1407703/) |
| `/huntingbox` | `/hb` | [Hunting box usage](https://hypixel.net/threads/huntingbox-improvements.6141909/) |
| `/attributes`, `/attributesmenu` | `/am`, `/attributemenu` | [Attribute menu usage](https://hypixel.net/threads/how-to-see-attribute-shards-missing.6028856/), [2025 firsthand behavior](https://hypixel.net/threads/random-little-things.5981418/post-41229467) |

Already present: `/p` and `/party`, `/f` and `/friend`, `/g` and `/guild`, `/fl`, `/wd`, `/is`, `/cf`, `/cc` and `/cchat`, plus the existing short forms for view commands. Existing remake-specific labels are preserved; their presence is not a claim of Hypixel parity. The old `/whipser` typo remains accepted for compatibility alongside the correct `/whisper`.

Chat and list shortcuts forward through the registered full command, retaining its service availability, party/guild membership, role checks, and command handling. They do not change the selected chat channel. Menu and private-message aliases share the original command object and syntax.

Not expanded into new systems: `/reply`/`/r` require reply-target tracking that the existing messaging command lacks; persistent guild/officer/coop chat modes are absent from the existing `/chat` implementation. `/ch` supports the same modes as `/chat`. GUI-only features without a corresponding implemented command are outside this command-alias pass. Unverified one-letter subcommand abbreviations are not guessed.

Validation: command registration collision scan, targeted Minestom command routing tests, module compilation, and a fresh `:loader:shadowJar --no-build-cache` build. Live deployment status is recorded in roadmap item 7.

Deployment artifact: `HypixelCore.jar`, SHA-256 `037d79eb22e9f002983a5145d2f4f130930fa8cc7bfb396ad1807960b193c7c7`. All 36,700 ZIP entries passed integrity checks. The previous persisted jar is backed up at `/tmp/hypixel-before-aliases/HypixelCore.jar`.
