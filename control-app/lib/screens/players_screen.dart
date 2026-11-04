import 'package:flutter/material.dart';

import '../api/control_api.dart';
import '../theme/reactor_theme.dart';
import '../widgets/admin_kit.dart';
import '../widgets/chamfer.dart';

/// Everyone online across the game servers, plus lookup of any player who has ever joined.
///
/// Constructor: `PlayersScreen(api: controlApi)`.
class PlayersScreen extends StatefulWidget {
  final ControlApi api;

  const PlayersScreen({super.key, required this.api});

  @override
  State<PlayersScreen> createState() => _PlayersScreenState();
}

class _PlayersScreenState extends State<PlayersScreen> {
  final _search = TextEditingController();
  OnlineRoster? _roster;
  String? _error;

  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void dispose() {
    _search.dispose();
    super.dispose();
  }

  Future<void> _load({bool refresh = false}) async {
    try {
      final roster = await widget.api.fetchOnlinePlayers(refresh: refresh);
      if (mounted) {
        setState(() {
          _roster = roster;
          _error = null;
        });
      }
    } catch (e) {
      if (mounted) setState(() => _error = e.toString());
    }
  }

  void _open(String query) {
    if (query.trim().isEmpty) return;
    Navigator.of(context).push(MaterialPageRoute(
      builder: (_) => PlayerDetailScreen(api: widget.api, query: query.trim()),
    ));
  }

  @override
  Widget build(BuildContext context) {
    return AdminScaffold(
      title: 'PLAYERS',
      onRefresh: () => _load(refresh: true),
      body: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          TextField(
            controller: _search,
            style: ReactorTheme.data(size: 12),
            textInputAction: TextInputAction.search,
            onSubmitted: _open,
            decoration: InputDecoration(
              isDense: true,
              hintText: 'look up a player by name or UUID',
              hintStyle: ReactorTheme.data(size: 11, color: ReactorTheme.textFaint),
              suffixIcon: IconButton(
                iconSize: 16,
                icon: const Icon(Icons.search, color: ReactorTheme.textSecondary),
                onPressed: () => _open(_search.text),
              ),
            ),
          ),
          const SizedBox(height: 10),
          Expanded(child: _body()),
        ],
      ),
    );
  }

  Widget _body() {
    if (_error != null) return AdminMessage(_error!, colour: ReactorTheme.critical);
    final roster = _roster;
    if (roster == null) return const AdminSpinner();
    if (roster.servers.isEmpty) return const AdminMessage('no game server answered');

    return HudPanel(
      title: '${roster.totalPlayers} online  ·  ${roster.servers.length} servers',
      child: ListView(
        children: [
          for (final server in roster.servers) ..._server(server),
        ],
      ),
    );
  }

  List<Widget> _server(ServerRoster server) {
    return [
      Padding(
        padding: const EdgeInsets.only(top: 6, bottom: 4),
        child: Row(
          children: [
            Text(server.label,
                style: ReactorTheme.data(size: 12, color: ReactorTheme.cyan, weight: FontWeight.w600)),
            const SizedBox(width: 8),
            Text(server.serverType, style: ReactorTheme.label(size: 9, spacing: 1.2)),
            const Spacer(),
            Text('${server.players.length}', style: ReactorTheme.data(size: 11, color: ReactorTheme.textSecondary)),
          ],
        ),
      ),
      if (server.players.isEmpty)
        Padding(
          padding: const EdgeInsets.only(left: 10, bottom: 4),
          child: Text('empty', style: ReactorTheme.data(size: 10, color: ReactorTheme.textFaint)),
        ),
      for (final player in server.players)
        InkWell(
          onTap: () => _open(player.uuid),
          child: Padding(
            padding: const EdgeInsets.only(left: 10, top: 3, bottom: 3),
            child: Row(
              children: [
                Container(width: 2, height: 14, color: player.staff ? ReactorTheme.amber : ReactorTheme.hairline),
                const SizedBox(width: 8),
                Expanded(child: Text(player.name, style: ReactorTheme.data(size: 12))),
                if (player.staff)
                  Padding(
                    padding: const EdgeInsets.only(right: 8),
                    child: Text('STAFF', style: ReactorTheme.label(size: 9, color: ReactorTheme.amber, spacing: 1.2)),
                  ),
                Text(player.rank.replaceAll('_PLUS', '+'),
                    style: ReactorTheme.label(size: 9, color: ReactorTheme.textSecondary, spacing: 1.2)),
              ],
            ),
          ),
        ),
    ];
  }
}

/// One player's rank, location, profiles, punishments and wipe state, with OWNER-only actions.
///
/// Constructor: `PlayerDetailScreen(api: controlApi, query: nameOrUuid)`.
class PlayerDetailScreen extends StatefulWidget {
  final ControlApi api;
  final String query;

  const PlayerDetailScreen({super.key, required this.api, required this.query});

  @override
  State<PlayerDetailScreen> createState() => _PlayerDetailScreenState();
}

class _PlayerDetailScreenState extends State<PlayerDetailScreen> {
  PlayerLookup? _player;
  String? _error;
  bool _busy = false;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    try {
      final player = await widget.api.lookupPlayer(_player?.uuid ?? widget.query);
      if (mounted) {
        setState(() {
          _player = player;
          _error = null;
        });
      }
    } catch (e) {
      if (mounted) setState(() => _error = e.toString());
    }
  }

  Future<void> _act(String action, {String? rank, String? reason, int? durationMinutes}) async {
    final player = _player;
    if (player == null) return;
    setState(() => _busy = true);
    try {
      final detail = await widget.api.playerAction(player.uuid, action,
          rank: rank, reason: reason, durationMinutes: durationMinutes);
      if (mounted) adminNotify(context, detail);
      await _load();
    } catch (e) {
      if (mounted) adminNotify(context, e.toString(), colour: ReactorTheme.critical);
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  Future<void> _setRank(PlayerLookup player) async {
    final rank = await adminPick(context, 'Set rank of ${player.name}', player.options.ranks);
    if (rank == null || !mounted) return;
    if (!await adminConfirm(context, 'Set rank to $rank?',
        player.isOnline
            ? '${player.name} is online; the rank changes in their live session now.'
            : '${player.name} is offline; the rank is written to their account and shows on next login.',
        'SET RANK')) {
      return;
    }
    await _act('set-rank', rank: rank);
  }

  Future<void> _kick(PlayerLookup player) async {
    final reason = await adminPrompt(context, 'Kick ${player.name}', 'reason shown to the player');
    if (reason == null) return;
    await _act('kick', reason: reason);
  }

  Future<void> _punish(PlayerLookup player, String type) async {
    final reasons = type == 'ban' ? player.options.banReasons : player.options.muteReasons;
    final reason = await adminPick(context, '${type == 'ban' ? 'Ban' : 'Mute'} reason', reasons);
    if (reason == null || !mounted) return;
    final duration = await adminPick(context, 'Duration', const ['1 hour', '1 day', '7 days', '30 days', 'Permanent']);
    if (duration == null || !mounted) return;
    final minutes = switch (duration) {
      '1 hour' => 60,
      '1 day' => 1440,
      '7 days' => 10080,
      '30 days' => 43200,
      _ => 0,
    };
    if (!await adminConfirm(context, '${type.toUpperCase()} ${player.name}?', '$reason · $duration',
        type.toUpperCase())) {
      return;
    }
    await _act(type, reason: reason, durationMinutes: minutes);
  }

  Future<void> _wipe(PlayerLookup player) async {
    if (!await adminConfirm(context, 'Wipe ${player.name}?',
        'Deletes every profile, island and co-op membership this player owns. A snapshot is kept '
            'for one hour so it can be restored.',
        'CONTINUE')) {
      return;
    }
    if (!mounted) return;
    if (!await adminConfirmByTyping(context, 'Second confirmation', 'This cannot be undone after an hour.',
        player.name)) {
      return;
    }
    await _act('wipe');
  }

  Future<void> _restore(PlayerLookup player) async {
    if (!await adminConfirm(context, 'Restore ${player.name}?',
        'Writes the wipe snapshot back over their current data.', 'RESTORE')) {
      return;
    }
    await _act('restore');
  }

  @override
  Widget build(BuildContext context) {
    return AdminScaffold(
      title: _player?.name.toUpperCase() ?? 'PLAYER',
      onRefresh: _load,
      body: _body(),
    );
  }

  Widget _body() {
    if (_error != null) return AdminMessage(_error!, colour: ReactorTheme.critical);
    final player = _player;
    if (player == null) return const AdminSpinner();

    return ListView(
      children: [
        HudPanel(
          title: 'account',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              AdminField('name', player.name),
              AdminField('uuid', player.uuid, colour: ReactorTheme.textSecondary),
              AdminField('rank', player.rank, colour: player.staff ? ReactorTheme.amber : ReactorTheme.textPrimary),
              AdminField('status',
                  player.isOnline ? 'online · ${player.onlineServer ?? player.onlineServerType}' : 'offline',
                  colour: player.isOnline ? ReactorTheme.cyan : ReactorTheme.textSecondary),
              if (player.lastSeen != null) AdminField('last seen', adminStamp(player.lastSeen!)),
              if (player.wipeSnapshotAt != null)
                AdminField('wiped', '${adminStamp(player.wipeSnapshotAt!)}'
                    '${player.canRestore ? ' · restorable until ${adminStamp(player.restorableUntil!)}' : ' · expired'}',
                    colour: ReactorTheme.amber),
            ],
          ),
        ),
        const SizedBox(height: 10),
        HudPanel(
          title: '${player.profiles.length} profiles',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              if (player.profiles.isEmpty)
                Text('none', style: ReactorTheme.data(size: 11, color: ReactorTheme.textFaint)),
              for (final profile in player.profiles)
                Padding(
                  padding: const EdgeInsets.only(bottom: 6),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(children: [
                        Text(profile.name ?? 'unnamed',
                            style: ReactorTheme.data(
                                size: 12,
                                color: profile.selected ? ReactorTheme.cyan : ReactorTheme.textPrimary,
                                weight: FontWeight.w600)),
                        const SizedBox(width: 8),
                        if (profile.selected)
                          Text('SELECTED', style: ReactorTheme.label(size: 9, color: ReactorTheme.cyan, spacing: 1.2)),
                        if (profile.coopId != null)
                          Padding(
                            padding: const EdgeInsets.only(left: 8),
                            child: Text('CO-OP ×${profile.coopMembers}',
                                style: ReactorTheme.label(size: 9, color: ReactorTheme.amber, spacing: 1.2)),
                          ),
                        const Spacer(),
                        Text('${adminCoins(profile.coins)} coins',
                            style: ReactorTheme.data(size: 11, color: ReactorTheme.textSecondary)),
                      ]),
                      Text(profile.id, style: ReactorTheme.data(size: 9, color: ReactorTheme.textFaint)),
                    ],
                  ),
                ),
            ],
          ),
        ),
        const SizedBox(height: 10),
        HudPanel(
          title: 'punishments',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              if (player.punishments.isEmpty)
                Text('none active', style: ReactorTheme.data(size: 11, color: ReactorTheme.textFaint)),
              for (final punishment in player.punishments)
                AdminField(
                  punishment.type,
                  '${punishment.reason}\n${punishment.permanent ? 'permanent' : 'until ${adminStamp(punishment.expiresAt!)}'}'
                  ' · ${punishment.id}',
                  colour: ReactorTheme.critical,
                ),
            ],
          ),
        ),
        const SizedBox(height: 10),
        _actions(player),
      ],
    );
  }

  Widget _actions(PlayerLookup player) {
    if (!widget.api.isOwner) {
      return const AdminMessage('player actions need an OWNER device');
    }
    if (_busy) return const AdminSpinner();
    final banned = player.punishments.any((p) => p.type == 'BAN');
    final muted = player.punishments.any((p) => p.type == 'MUTE');
    return HudPanel(
      title: 'actions',
      child: Wrap(
        spacing: 6,
        runSpacing: 6,
        children: [
          AdminChip('SET RANK', onTap: () => _setRank(player)),
          AdminChip('KICK', colour: ReactorTheme.amber, onTap: player.isOnline ? () => _kick(player) : null),
          if (banned)
            AdminChip('UNBAN', onTap: () => _act('unban'))
          else
            AdminChip('BAN', colour: ReactorTheme.critical, onTap: () => _punish(player, 'ban')),
          if (muted)
            AdminChip('UNMUTE', onTap: () => _act('unmute'))
          else
            AdminChip('MUTE', colour: ReactorTheme.amber, onTap: () => _punish(player, 'mute')),
          AdminChip('WIPE', colour: ReactorTheme.critical, onTap: player.isOnline ? null : () => _wipe(player)),
          AdminChip('RESTORE', onTap: player.canRestore && !player.isOnline ? () => _restore(player) : null),
        ],
      ),
    );
  }
}
