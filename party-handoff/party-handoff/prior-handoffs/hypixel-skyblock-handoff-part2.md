# HypixelRecreation SkyBlock — Handoff Update (Part 2)

Continuation of `hypixel-skyblock-handoff.md`. Covers everything since switching primary dev work to Claude Code (VS Code extension), including several serious infrastructure issues discovered and fixed. Read the original handoff first for project structure, deploy loop, and the first two features (Personal Deletor, Personal Compactor v1).

## Work completed via Claude Code since the switch

- **Dark Auction issues — fully fixed.** (Prior handoff left this as diagnosed-but-unfixed: the `NullPointerException` in `DarkAuctionHandler.handleRoundStart` from a missing null check, and the Sirius NPC never being instantiated anywhere in the codebase.) Confirm exact fix details by checking recent git history / diffs in `~/hypixel-dev` — not verified in detail from this side, but user confirmed "fully fixed all of the dark auction issues."
- **Personal Compactor — GUI added, feature completed.** Built to the same full spec as Personal Deletor: toggle GUI, `INTERACTABLE` right-click handler, tiered speed scaling. See prior handoff for the exact file locations and architecture pattern (`type.skyblockgeneric/.../compactor/PersonalCompactorService.java`, `.../gui/inventories/compactor/GUIPersonalCompactor.java`, new `DatapointToggles.Toggles.ToggleType` entries: `PERSONAL_COMPACTOR_IRON/GOLD/DIAMOND/EMERALD/COAL/REDSTONE/LAPIS`).
- **Color/MiniMessage gotcha found and fixed in both GUIs**: `Text.of(markup, args)` (used by `ItemStacks.item(...)`) parses color tags only in the *static* markup string, before argument substitution — a color tag embedded inside a substituted `{}` argument renders as literal text (e.g. `<a>ENABLED` shows literally instead of green). Fix: choose between two full static templates (each with embedded color tags) via a ternary, substituting only plain uncolored values.

## Serious infrastructure issues hit tonight (all now resolved, but good to know if they recur)

### 1. WSL DNS breaks repeatedly, breaking Claude Code
- **Symptom:** `curl`/any network call fails with "Could not resolve host." Eventually also caused Claude Code (which needs network access to reach Anthropic's API) to hang completely unresponsive, even to simple messages.
- **Root cause:** WSL auto-regenerates `/etc/resolv.conf` on its own (pointing to a WSL-internal nameserver, `172.23.208.1`, that intermittently doesn't resolve), overwriting any manual fix — this happened **repeatedly** (3+ times in under 2 hours) whenever WSL restarted for any reason.
- **Permanent fix applied:**
  1. Added to `/etc/wsl.conf`:
     ```
     [network]
     generateResolvConf = false
     ```
  2. Rewrote `/etc/resolv.conf`:
     ```
     nameserver 8.8.8.8
     nameserver 8.8.4.4
     ```
  3. Made it immutable so nothing can silently overwrite it again: `sudo chattr +i /etc/resolv.conf`
  4. Required a full WSL restart (`wsl --shutdown` from **Windows** PowerShell, not from inside WSL) for the `wsl.conf` change to take effect.
- **This confirmed to survive** a full WSL shutdown/restart and later a full Windows restart. If DNS breaks again despite this, check `lsattr /etc/resolv.conf` first — if the immutable flag (`i`) is missing, something explicitly removed it, which would be a new, different problem worth investigating rather than just reapplying the fix again.

### 2. Mongo/Redis don't reliably auto-restart after a WSL shutdown
- **Symptom:** After a `wsl --shutdown` + restart cycle, most containers came back up automatically (Docker Desktop's own restore-on-restart behavior), but `hypixel_mongo`, `hypixel_redis`, and `hypixel_proxy` specifically stayed `Exited (255)` and did **not** auto-restart.
- **Consequence — this is the important part:** every game server container, unable to reach Mongo/Redis, got stuck in a hard connection-retry loop, driving CPU to **170–228% per container** across 6+ servers simultaneously. This in turn overloaded the whole WSL environment enough to cause VS Code's remote connection to drop ("Disconnected" prompts) and Claude Code to hang — it looked at first like a resource/memory problem, but the real fix was much simpler.
- **Fix:** `docker start hypixel_mongo hypixel_redis`, wait ~5s, then `docker start hypixel_proxy`. CPU across every container dropped from 170-228% back to a normal 3-9% within seconds of Mongo/Redis coming back.
- **Lesson for next time:** if CPU looks insanely high across *many* different game servers simultaneously after any WSL/Docker restart, check `docker ps -a | grep -E "mongo|redis|proxy"` for anything `Exited` **before** assuming it's a new code-level performance bug. A uniform spike across every server type points to a shared dependency (DB) being down, not a bug in any one server's code.

### 3. WSL itself became unstable / crashed outright
- After several `wsl --shutdown` cycles in succession (for the DNS fix, then again after the Mongo/Redis incident), WSL eventually failed to restart cleanly with `Catastrophic failure — Error code: Wsl/Service/E_UNEXPECTED`.
- Ruled out: disk space (70GB free on C:, plenty), `wsl --status` responded normally (core service wasn't fundamentally broken), `.wslconfig` syntax (was valid).
- **What actually fixed it:** a full **Windows restart** (not just another WSL shutdown). This is the most thorough reset available — clears Hyper-V/WSL VM/virtual networking state all at once. After the restart, WSL, the DNS fix, and Docker all came back up clean.
- **Lesson:** if WSL gets into a bad state after repeated shutdown/restart cycles in one session, don't keep trying more `wsl --shutdown` cycles — go straight to a full Windows restart.

### 4. WSL2 memory limit increased
- Machine has 16GB total physical RAM; WSL2 was capped at its default ~7.75GB, which is genuinely tight given the full Docker stack (proxy + 13 services + up to 8 game world servers) plus Gradle builds plus VS Code Server plus Claude Code all sharing that one allocation.
- Bumped to 12GB via `.wslconfig`:
  ```
  [wsl2]
  memory=12GB
  ```
  (Note: **edit this file carefully** — Notepad edits didn't save correctly twice in a row during this session, once landing empty and once reverting to a stale `memory=8GB` from some earlier default. Always verify with `Get-Content "$env:USERPROFILE\.wslconfig"` from PowerShell before restarting WSL, not just trusting that Notepad's save succeeded.)
- Requires `wsl --shutdown` from PowerShell to take effect, which also stops the whole Docker stack (restart via the manager afterward).

## Loose ends / not yet done

- **A checklist of desired next features** was requested but not yet built — candidates discussed: connecting Google Drive (an MCP connector is available but not yet connected) to pull from an existing notes doc, or just building the list directly from the remaining `NOT_FINISHED_YET` items in the codebase (see original handoff — several dozen still exist, grep `configuration/skyblock/items/` for the marker).
- Public server exposure (port forwarding, firewall) was set up but the resource pack's `server-url` in `config.yml` still needs updating from `127.0.0.1` to a real public IP/DDNS hostname before it's actually usable by outside players — unchanged since the original handoff, still outstanding.
