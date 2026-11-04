part of 'control_api.dart';

/// Game-data admin calls: players, events and calendar, economy, orphans and the tracker sheet.
///
/// A part of [ControlApi]'s library so it shares the token exchange and error handling rather
/// than duplicating them. Every mutation here needs an OWNER device; the server enforces that.
extension AdminApi on ControlApi {
  bool get isOwner => role == 'OWNER';

  Future<OnlineRoster> fetchOnlinePlayers({bool refresh = false}) async {
    final data = await _authedGet('/control/admin/players', refresh ? {'refresh': 'true'} : null);
    return OnlineRoster.fromJson(data as Map<String, dynamic>);
  }

  Future<PlayerLookup> lookupPlayer(String query) async {
    final data = await _authedGet('/control/admin/player', {'query': query.trim()});
    return PlayerLookup.fromJson(data as Map<String, dynamic>);
  }

  /// [action] is one of set-rank, kick, ban, mute, unban, unmute, wipe, restore.
  Future<String> playerAction(String uuid, String action,
      {String? rank, String? reason, int? durationMinutes}) async {
    final body = <String, dynamic>{'uuid': uuid, 'action': action};
    if (rank != null) body['rank'] = rank;
    if (reason != null) body['reason'] = reason;
    if (durationMinutes != null) body['durationMinutes'] = durationMinutes;
    final data = await _authedPost('/control/admin/player/action', body);
    return _detail(data);
  }

  Future<EventsOverview> fetchEvents() async {
    final data = await _authedGet('/control/admin/events');
    return EventsOverview.fromJson(data as Map<String, dynamic>);
  }

  /// [kind] is one of dark-auction, force-mayor, special-mayor, calendar-skip, mining-event.
  Future<String> triggerEvent(String kind, {String? value}) async {
    final body = <String, dynamic>{'kind': kind};
    if (value != null) body['value'] = value;
    final data = await _authedPost('/control/admin/events/trigger', body);
    return _detail(data);
  }

  Future<InflationReport> fetchInflation({int days = 30, bool refresh = false}) async {
    final query = {'days': '$days'};
    if (refresh) query['refresh'] = 'true';
    final data = await _authedGet('/control/admin/economy/inflation', query);
    return InflationReport.fromJson(data as Map<String, dynamic>);
  }

  Future<EconomySnapshot> fetchEconomy({bool refresh = false}) async {
    final data = await _authedGet('/control/admin/economy', refresh ? {'refresh': 'true'} : null);
    return EconomySnapshot.fromJson(data as Map<String, dynamic>);
  }

  Future<OrphanReport> fetchOrphans({bool refresh = false}) async {
    final data = await _authedGet('/control/admin/orphans', refresh ? {'refresh': 'true'} : null);
    return OrphanReport.fromJson(data as Map<String, dynamic>);
  }

  /// The server re-scans and refuses unless the item is still an orphan; [confirm] must repeat [id].
  Future<String> deleteOrphan(String kind, String id, {required String confirm}) async {
    final data = await _authedPost(
        '/control/admin/orphans/delete', {'kind': kind, 'id': id, 'confirm': confirm});
    return _detail(data);
  }

  /// [tab] is roadmap, bugs or done.
  Future<RoadmapTab> fetchRoadmap(String tab, {bool refresh = false}) async {
    final query = {'tab': tab};
    if (refresh) query['refresh'] = 'true';
    final data = await _authedGet('/control/admin/roadmap', query);
    return RoadmapTab.fromJson(data as Map<String, dynamic>);
  }

  static String _detail(dynamic data) {
    if (data is Map && data['detail'] != null) return data['detail'].toString();
    return data?.toString() ?? 'done';
  }
}

int _int(dynamic value) => (value as num?)?.toInt() ?? 0;

double _double(dynamic value) => (value as num?)?.toDouble() ?? 0;

List<Map<String, dynamic>> _maps(dynamic value) =>
    ((value as List?) ?? const []).map((e) => e as Map<String, dynamic>).toList();

List<String> _strings(dynamic value) =>
    ((value as List?) ?? const []).map((e) => e.toString()).toList();

DateTime? _millis(dynamic value) =>
    value == null ? null : DateTime.fromMillisecondsSinceEpoch((value as num).toInt());

class OnlinePlayer {
  final String uuid;
  final String name;
  final String rank;
  final bool staff;

  OnlinePlayer({required this.uuid, required this.name, required this.rank, required this.staff});

  factory OnlinePlayer.fromJson(Map<String, dynamic> json) => OnlinePlayer(
        uuid: json['uuid'] as String? ?? '',
        name: json['name'] as String? ?? '?',
        rank: json['rank'] as String? ?? 'DEFAULT',
        staff: json['staff'] as bool? ?? false,
      );
}

class ServerRoster {
  final String serverId;
  final String serverType;
  final String? serverName;
  final List<OnlinePlayer> players;

  ServerRoster({
    required this.serverId,
    required this.serverType,
    required this.serverName,
    required this.players,
  });

  String get label => serverName ?? serverType;

  factory ServerRoster.fromJson(Map<String, dynamic> json) => ServerRoster(
        serverId: json['serverId'] as String? ?? '',
        serverType: json['serverType'] as String? ?? '?',
        serverName: json['serverName'] as String?,
        players: _maps(json['players']).map(OnlinePlayer.fromJson).toList(),
      );
}

class OnlineRoster {
  final int totalPlayers;
  final List<ServerRoster> servers;

  OnlineRoster({required this.totalPlayers, required this.servers});

  factory OnlineRoster.fromJson(Map<String, dynamic> json) => OnlineRoster(
        totalPlayers: _int(json['totalPlayers']),
        servers: _maps(json['servers']).map(ServerRoster.fromJson).toList(),
      );
}

class ProfileSummary {
  final String id;
  final String? name;
  final bool selected;
  final double coins;
  final String? coopId;
  final int coopMembers;

  ProfileSummary({
    required this.id,
    required this.name,
    required this.selected,
    required this.coins,
    required this.coopId,
    required this.coopMembers,
  });

  factory ProfileSummary.fromJson(Map<String, dynamic> json) => ProfileSummary(
        id: json['id'] as String? ?? '',
        name: json['name'] as String?,
        selected: json['selected'] as bool? ?? false,
        coins: _double(json['coins']),
        coopId: json['coopId'] as String?,
        coopMembers: _int(json['coopMembers']),
      );
}

class PunishmentSummary {
  final String type;
  final String id;
  final String reason;
  final DateTime? expiresAt;

  PunishmentSummary({required this.type, required this.id, required this.reason, required this.expiresAt});

  bool get permanent => expiresAt == null;

  factory PunishmentSummary.fromJson(Map<String, dynamic> json) {
    final expires = _int(json['expiresAt']);
    return PunishmentSummary(
      type: json['type'] as String? ?? '?',
      id: json['id'] as String? ?? '',
      reason: json['reason'] as String? ?? '',
      expiresAt: expires > 0 ? DateTime.fromMillisecondsSinceEpoch(expires) : null,
    );
  }
}

class AdminOptions {
  final List<String> ranks;
  final List<String> banReasons;
  final List<String> muteReasons;

  AdminOptions({required this.ranks, required this.banReasons, required this.muteReasons});

  factory AdminOptions.fromJson(Map<String, dynamic> json) => AdminOptions(
        ranks: _strings(json['ranks']),
        banReasons: _strings(json['banReasons']),
        muteReasons: _strings(json['muteReasons']),
      );
}

class PlayerLookup {
  final String uuid;
  final String name;
  final String rank;
  final bool staff;
  final String? onlineServer;
  final String? onlineServerType;
  final DateTime? lastSeen;
  final List<ProfileSummary> profiles;
  final List<PunishmentSummary> punishments;
  final DateTime? wipeSnapshotAt;
  final DateTime? restorableUntil;
  final AdminOptions options;

  PlayerLookup({
    required this.uuid,
    required this.name,
    required this.rank,
    required this.staff,
    required this.onlineServer,
    required this.onlineServerType,
    required this.lastSeen,
    required this.profiles,
    required this.punishments,
    required this.wipeSnapshotAt,
    required this.restorableUntil,
    required this.options,
  });

  bool get isOnline => onlineServer != null || onlineServerType != null;

  bool get canRestore => restorableUntil != null && DateTime.now().isBefore(restorableUntil!);

  factory PlayerLookup.fromJson(Map<String, dynamic> json) {
    final player = (json['player'] as Map<String, dynamic>?) ?? const {};
    return PlayerLookup(
      uuid: player['uuid'] as String? ?? '',
      name: player['name'] as String? ?? '?',
      rank: player['rank'] as String? ?? 'DEFAULT',
      staff: player['staff'] as bool? ?? false,
      onlineServer: player['onlineServer'] as String?,
      onlineServerType: player['onlineServerType'] as String?,
      lastSeen: _millis(player['lastSeen']),
      profiles: _maps(player['profiles']).map(ProfileSummary.fromJson).toList(),
      punishments: _maps(player['punishments']).map(PunishmentSummary.fromJson).toList(),
      wipeSnapshotAt: _millis(player['wipeSnapshotAt']),
      restorableUntil: _millis(player['restorableUntil']),
      options: AdminOptions.fromJson((json['options'] as Map<String, dynamic>?) ?? const {}),
    );
  }
}

class SkyBlockDate {
  final int elapsed;
  final int year;
  final String monthName;
  final int day;
  final int hour;

  SkyBlockDate({
    required this.elapsed,
    required this.year,
    required this.monthName,
    required this.day,
    required this.hour,
  });

  String get label => '$monthName ${_ordinal(day)}, Year $year · ${hour.toString().padLeft(2, '0')}:00';

  static String _ordinal(int n) {
    if (n % 100 >= 11 && n % 100 <= 13) return '${n}th';
    switch (n % 10) {
      case 1:
        return '${n}st';
      case 2:
        return '${n}nd';
      case 3:
        return '${n}rd';
      default:
        return '${n}th';
    }
  }

  factory SkyBlockDate.fromJson(Map<String, dynamic> json) => SkyBlockDate(
        elapsed: _int(json['elapsed']),
        year: _int(json['year']),
        monthName: json['monthName'] as String? ?? '?',
        day: _int(json['day']),
        hour: _int(json['hour']),
      );
}

class ServerCalendar {
  final String serverId;
  final String serverType;
  final String? serverName;
  final SkyBlockDate date;
  final bool shared;
  final int offset;

  ServerCalendar({
    required this.serverId,
    required this.serverType,
    required this.serverName,
    required this.date,
    required this.shared,
    required this.offset,
  });

  String get label => serverName ?? serverType;

  factory ServerCalendar.fromJson(Map<String, dynamic> json) => ServerCalendar(
        serverId: json['serverId'] as String? ?? '',
        serverType: json['serverType'] as String? ?? '?',
        serverName: json['serverName'] as String?,
        date: SkyBlockDate.fromJson((json['date'] as Map<String, dynamic>?) ?? const {}),
        shared: json['shared'] as bool? ?? false,
        offset: _int(json['offset']),
      );
}

class CalendarOverview {
  final SkyBlockDate? reference;
  final bool sharedClock;
  final List<ServerCalendar> servers;
  final int spread;
  final bool diverged;

  CalendarOverview({
    required this.reference,
    required this.sharedClock,
    required this.servers,
    required this.spread,
    required this.diverged,
  });

  factory CalendarOverview.fromJson(Map<String, dynamic> json) {
    final divergence = (json['divergence'] as Map<String, dynamic>?) ?? const {};
    final reference = json['reference'] as Map<String, dynamic>?;
    return CalendarOverview(
      reference: reference == null ? null : SkyBlockDate.fromJson(reference),
      sharedClock: json['sharedClock'] as bool? ?? false,
      servers: _maps(json['servers']).map(ServerCalendar.fromJson).toList(),
      spread: _int(divergence['spread']),
      diverged: divergence['diverged'] as bool? ?? false,
    );
  }
}

class DarkAuctionState {
  final bool active;
  final String? phase;
  final int round;
  final String? currentItem;
  final int currentBid;
  final String? highestBidder;
  final bool reachable;

  DarkAuctionState({
    required this.active,
    required this.phase,
    required this.round,
    required this.currentItem,
    required this.currentBid,
    required this.highestBidder,
    required this.reachable,
  });

  static DarkAuctionState? fromNullable(dynamic json) {
    if (json is! Map<String, dynamic>) return null;
    return DarkAuctionState(
      active: json['auctionActive'] as bool? ?? false,
      phase: json['phase'] as String?,
      round: _int(json['currentRound']),
      currentItem: json['currentItemType'] as String?,
      currentBid: _int(json['currentBid']),
      highestBidder: json['highestBidderName'] as String?,
      reachable: json['success'] as bool? ?? true,
    );
  }
}

class EventsOverview {
  final CalendarOverview calendar;
  final String? currentMayor;
  final String? currentMinister;
  final bool electionOpen;
  final DarkAuctionState? darkAuction;
  final List<String> mayors;
  final List<String> miningEvents;
  final List<String> calendarEvents;

  EventsOverview({
    required this.calendar,
    required this.currentMayor,
    required this.currentMinister,
    required this.electionOpen,
    required this.darkAuction,
    required this.mayors,
    required this.miningEvents,
    required this.calendarEvents,
  });

  factory EventsOverview.fromJson(Map<String, dynamic> json) {
    final mayor = (json['mayor'] as Map<String, dynamic>?) ?? const {};
    final options = (json['options'] as Map<String, dynamic>?) ?? const {};
    return EventsOverview(
      calendar: CalendarOverview.fromJson((json['calendar'] as Map<String, dynamic>?) ?? const {}),
      currentMayor: mayor['currentMayor'] as String?,
      currentMinister: mayor['currentMinister'] as String?,
      electionOpen: mayor['electionOpen'] as bool? ?? false,
      darkAuction: DarkAuctionState.fromNullable(json['darkAuction']),
      mayors: _strings(options['mayors']),
      miningEvents: _strings(options['miningEvents']),
      calendarEvents: _strings(options['calendarEvents']),
    );
  }
}

class PendingTransaction {
  final String id;
  final String playerUuid;
  final String type;
  final DateTime? createdAt;

  PendingTransaction({required this.id, required this.playerUuid, required this.type, required this.createdAt});

  factory PendingTransaction.fromJson(Map<String, dynamic> json) => PendingTransaction(
        id: json['id'] as String? ?? '',
        playerUuid: json['playerUuid'] as String? ?? '',
        type: json['type'] as String? ?? '?',
        createdAt: DateTime.tryParse(json['createdAt'] as String? ?? ''),
      );
}

class EconomySnapshot {
  final int pendingTransactions;
  final Map<String, int> pendingByType;
  final List<PendingTransaction> oldestPending;
  final int bazaarOrders;
  final int activeAuctions;
  final int endedAuctions;
  final double totalPurseCoins;
  final int profilesScanned;
  final DateTime? coinsScannedAt;
  final DarkAuctionState? darkAuction;

  EconomySnapshot({
    required this.pendingTransactions,
    required this.pendingByType,
    required this.oldestPending,
    required this.bazaarOrders,
    required this.activeAuctions,
    required this.endedAuctions,
    required this.totalPurseCoins,
    required this.profilesScanned,
    required this.coinsScannedAt,
    required this.darkAuction,
  });

  factory EconomySnapshot.fromJson(Map<String, dynamic> json) => EconomySnapshot(
        pendingTransactions: _int(json['pendingTransactions']),
        pendingByType: ((json['pendingByType'] as Map<String, dynamic>?) ?? const {})
            .map((key, value) => MapEntry(key, _int(value))),
        oldestPending: _maps(json['oldestPending']).map(PendingTransaction.fromJson).toList(),
        bazaarOrders: _int(json['bazaarOrders']),
        activeAuctions: _int(json['activeAuctions']),
        endedAuctions: _int(json['endedAuctions']),
        totalPurseCoins: _double(json['totalPurseCoins']),
        profilesScanned: _int(json['profilesScanned']),
        coinsScannedAt: DateTime.tryParse(json['coinsScannedAt'] as String? ?? ''),
        darkAuction: DarkAuctionState.fromNullable(json['darkAuction']),
      );
}

class InflationDay {
  final String date;
  final double minted;
  final double burned;
  final double net;
  final double staff;
  final double? supply;

  InflationDay({
    required this.date,
    required this.minted,
    required this.burned,
    required this.net,
    required this.staff,
    required this.supply,
  });

  factory InflationDay.fromJson(Map<String, dynamic> json) => InflationDay(
        date: json['date'] as String? ?? '',
        minted: _double(json['minted']),
        burned: _double(json['burned']),
        net: _double(json['net']),
        staff: _double(json['staff']),
        supply: (json['supply'] as num?)?.toDouble(),
      );
}

class FlowLine {
  final String category;
  final double amount;
  final int count;
  final double share;

  FlowLine({required this.category, required this.amount, required this.count, required this.share});

  factory FlowLine.fromJson(Map<String, dynamic> json) => FlowLine(
        category: json['category'] as String? ?? '?',
        amount: _double(json['amount']),
        count: _int(json['count']),
        share: _double(json['share']),
      );
}

class TransferChannel {
  final String channel;
  final double received;
  final double paid;
  final double net;
  final int count;

  TransferChannel({required this.channel, required this.received, required this.paid, required this.net, required this.count});

  factory TransferChannel.fromJson(Map<String, dynamic> json) => TransferChannel(
        channel: json['channel'] as String? ?? '?',
        received: _double(json['received']),
        paid: _double(json['paid']),
        net: _double(json['net']),
        count: _int(json['count']),
      );
}

class InflationReport {
  final int days;
  final double minted;
  final double burned;
  final double net;
  final double staffNet;
  final double averageDailyNet;
  final double? supplyNow;
  final double? supplyAtStart;
  final double? supplyChangePercent;
  final double? netAsPercentOfSupply;
  final double sinkToSourceRatio;
  final List<InflationDay> daily;
  final List<FlowLine> sources;
  final List<FlowLine> sinks;
  final List<TransferChannel> transfers;
  final List<FlowLine> staff;

  InflationReport({
    required this.days,
    required this.minted,
    required this.burned,
    required this.net,
    required this.staffNet,
    required this.averageDailyNet,
    required this.supplyNow,
    required this.supplyAtStart,
    required this.supplyChangePercent,
    required this.netAsPercentOfSupply,
    required this.sinkToSourceRatio,
    required this.daily,
    required this.sources,
    required this.sinks,
    required this.transfers,
    required this.staff,
  });

  factory InflationReport.fromJson(Map<String, dynamic> json) => InflationReport(
        days: _int(json['days']),
        minted: _double(json['minted']),
        burned: _double(json['burned']),
        net: _double(json['net']),
        staffNet: _double(json['staffNet']),
        averageDailyNet: _double(json['averageDailyNet']),
        supplyNow: (json['supplyNow'] as num?)?.toDouble(),
        supplyAtStart: (json['supplyAtStart'] as num?)?.toDouble(),
        supplyChangePercent: (json['supplyChangePercent'] as num?)?.toDouble(),
        netAsPercentOfSupply: (json['netAsPercentOfSupply'] as num?)?.toDouble(),
        sinkToSourceRatio: _double(json['sinkToSourceRatio']),
        daily: _maps(json['daily']).map(InflationDay.fromJson).toList(),
        sources: _maps(json['sources']).map(FlowLine.fromJson).toList(),
        sinks: _maps(json['sinks']).map(FlowLine.fromJson).toList(),
        transfers: _maps(json['transfers']).map(TransferChannel.fromJson).toList(),
        staff: _maps(json['staff']).map(FlowLine.fromJson).toList(),
      );
}

class Orphan {
  final String kind;
  final String id;
  final String reason;
  final String? related;

  Orphan({required this.kind, required this.id, required this.reason, required this.related});

  bool get deletable => kind == 'ISLAND' || kind == 'PROFILE';

  factory Orphan.fromJson(Map<String, dynamic> json) => Orphan(
        kind: json['kind'] as String? ?? '?',
        id: json['id'] as String? ?? '',
        reason: json['reason'] as String? ?? '',
        related: json['related'] as String?,
      );
}

class OrphanReport {
  final DateTime? scannedAt;
  final int profiles;
  final int accounts;
  final int islands;
  final int coops;
  final List<Orphan> orphans;
  final Map<String, int> counts;

  OrphanReport({
    required this.scannedAt,
    required this.profiles,
    required this.accounts,
    required this.islands,
    required this.coops,
    required this.orphans,
    required this.counts,
  });

  factory OrphanReport.fromJson(Map<String, dynamic> json) {
    final scan = (json['scan'] as Map<String, dynamic>?) ?? const {};
    return OrphanReport(
      scannedAt: DateTime.tryParse(scan['scannedAt'] as String? ?? ''),
      profiles: _int(scan['profiles']),
      accounts: _int(scan['accounts']),
      islands: _int(scan['islands']),
      coops: _int(scan['coops']),
      orphans: _maps(scan['orphans']).map(Orphan.fromJson).toList(),
      counts: ((json['counts'] as Map<String, dynamic>?) ?? const {})
          .map((key, value) => MapEntry(key, _int(value))),
    );
  }
}

class RoadmapRow {
  final String number;
  final String task;
  final String scope;
  final String area;
  final String priority;
  final String effort;
  final String status;
  final String notes;

  RoadmapRow({
    required this.number,
    required this.task,
    required this.scope,
    required this.area,
    required this.priority,
    required this.effort,
    required this.status,
    required this.notes,
  });

  bool get isDone => status.toLowerCase() == 'done';

  factory RoadmapRow.fromJson(Map<String, dynamic> json) => RoadmapRow(
        number: json['number'] as String? ?? '',
        task: json['task'] as String? ?? '',
        scope: json['scope'] as String? ?? '',
        area: json['area'] as String? ?? '',
        priority: json['priority'] as String? ?? '',
        effort: json['effort'] as String? ?? '',
        status: json['status'] as String? ?? '',
        notes: json['notes'] as String? ?? '',
      );
}

class RoadmapTab {
  final String tab;
  final List<RoadmapRow> rows;
  final DateTime? fetchedAt;

  RoadmapTab({required this.tab, required this.rows, required this.fetchedAt});

  factory RoadmapTab.fromJson(Map<String, dynamic> json) => RoadmapTab(
        tab: json['tab'] as String? ?? '',
        rows: _maps(json['rows']).map(RoadmapRow.fromJson).toList(),
        fetchedAt: DateTime.tryParse(json['fetchedAt'] as String? ?? ''),
      );
}
