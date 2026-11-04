/// Live game data: capacity, the orchestrator queue, per-server metrics and drain-and-restart.
library;

import 'control_api.dart';

int _int(dynamic value) => (value as num?)?.toInt() ?? 0;
double _double(dynamic value) => (value as num?)?.toDouble() ?? 0;

class TypeCapacity {
  final String type;
  final int servers;
  final int instances;
  final int freeInstanceSlots;
  final int onlinePlayers;
  final int maxPlayers;
  final int queued;

  TypeCapacity({
    required this.type,
    required this.servers,
    required this.instances,
    required this.freeInstanceSlots,
    required this.onlinePlayers,
    required this.maxPlayers,
    required this.queued,
  });

  factory TypeCapacity.fromJson(Map<String, dynamic> json) => TypeCapacity(
        type: json['type'] as String? ?? '?',
        servers: _int(json['servers']),
        instances: _int(json['instances']),
        freeInstanceSlots: _int(json['freeInstanceSlots']),
        onlinePlayers: _int(json['onlinePlayers']),
        maxPlayers: _int(json['maxPlayers']),
        queued: _int(json['queued']),
      );
}

class WaitingTicket {
  final String type;
  final int position;
  final int length;
  final String? mode;
  final String? originServer;
  final List<String> players;
  final int waitedMillis;

  WaitingTicket({
    required this.type,
    required this.position,
    required this.length,
    required this.mode,
    required this.originServer,
    required this.players,
    required this.waitedMillis,
  });

  String get waitedLabel {
    final seconds = waitedMillis ~/ 1000;
    return '${seconds ~/ 60}:${(seconds % 60).toString().padLeft(2, '0')}';
  }

  factory WaitingTicket.fromJson(Map<String, dynamic> json) => WaitingTicket(
        type: json['type'] as String? ?? '?',
        position: _int(json['position']),
        length: _int(json['length']),
        mode: json['mode'] as String?,
        originServer: json['originServer'] as String?,
        players: ((json['players'] as List?) ?? []).map((e) => e.toString()).toList(),
        waitedMillis: _int(json['waitedMillis']),
      );
}

class CapacityReport {
  final DateTime takenAt;
  final bool orchestratorOnline;
  final List<TypeCapacity> types;
  final List<WaitingTicket> waiting;

  CapacityReport({
    required this.takenAt,
    required this.orchestratorOnline,
    required this.types,
    required this.waiting,
  });

  int get totalQueued => types.fold(0, (sum, t) => sum + t.queued);

  factory CapacityReport.fromJson(Map<String, dynamic> json) => CapacityReport(
        takenAt: DateTime.tryParse(json['takenAt'] as String? ?? '') ?? DateTime.now(),
        orchestratorOnline: json['orchestratorOnline'] as bool? ?? false,
        types: ((json['types'] as List?) ?? [])
            .map((e) => TypeCapacity.fromJson(e as Map<String, dynamic>))
            .toList(),
        waiting: ((json['waiting'] as List?) ?? [])
            .map((e) => WaitingTicket.fromJson(e as Map<String, dynamic>))
            .toList(),
      );
}

/// One game server's latest heartbeat. Health is NOMINAL, LOADED (amber) or FAILING (red).
class ServerMetric {
  final String serverUuid;
  final String serverName;
  final String shortName;
  final String type;
  final String host;
  final double tps;
  final double mspt;
  final int heapUsedMb;
  final int heapMaxMb;
  final int players;
  final int maxPlayers;
  final int ageMillis;
  final String health;

  ServerMetric({
    required this.serverUuid,
    required this.serverName,
    required this.shortName,
    required this.type,
    required this.host,
    required this.tps,
    required this.mspt,
    required this.heapUsedMb,
    required this.heapMaxMb,
    required this.players,
    required this.maxPlayers,
    required this.ageMillis,
    required this.health,
  });

  bool get isLoaded => health == 'LOADED';
  bool get isFailing => health == 'FAILING';

  /// Docker names a container's host after the first 12 characters of its id.
  bool belongsTo(String containerId, String containerName) =>
      host.isNotEmpty && (host == containerName || containerId.startsWith(host));

  factory ServerMetric.fromJson(Map<String, dynamic> json) => ServerMetric(
        serverUuid: json['serverUuid'] as String? ?? '',
        serverName: json['serverName'] as String? ?? '?',
        shortName: json['shortName'] as String? ?? '?',
        type: json['type'] as String? ?? '?',
        host: json['host'] as String? ?? '',
        tps: _double(json['tps']),
        mspt: _double(json['mspt']),
        heapUsedMb: _int(json['heapUsedMb']),
        heapMaxMb: _int(json['heapMaxMb']),
        players: _int(json['players']),
        maxPlayers: _int(json['maxPlayers']),
        ageMillis: _int(json['ageMillis']),
        health: json['health'] as String? ?? 'FAILING',
      );
}

class MetricsSummary {
  final int servers;
  final int players;
  final int heapUsedMb;
  final int heapMaxMb;
  final double? lowestTps;
  final String? lowestTpsServer;
  final double? highestMspt;
  final String worstHealth;

  MetricsSummary({
    required this.servers,
    required this.players,
    required this.heapUsedMb,
    required this.heapMaxMb,
    required this.lowestTps,
    required this.lowestTpsServer,
    required this.highestMspt,
    required this.worstHealth,
  });

  factory MetricsSummary.fromJson(Map<String, dynamic> json) => MetricsSummary(
        servers: _int(json['servers']),
        players: _int(json['players']),
        heapUsedMb: _int(json['heapUsedMb']),
        heapMaxMb: _int(json['heapMaxMb']),
        lowestTps: (json['lowestTps'] as num?)?.toDouble(),
        lowestTpsServer: json['lowestTpsServer'] as String?,
        highestMspt: (json['highestMspt'] as num?)?.toDouble(),
        worstHealth: json['worstHealth'] as String? ?? 'NOMINAL',
      );
}

class MetricsReport {
  final DateTime takenAt;
  final List<ServerMetric> servers;
  final MetricsSummary summary;

  MetricsReport({required this.takenAt, required this.servers, required this.summary});

  ServerMetric? forContainer(String containerId, String containerName) {
    for (final server in servers) {
      if (server.belongsTo(containerId, containerName)) return server;
    }
    return null;
  }

  factory MetricsReport.fromJson(Map<String, dynamic> json) => MetricsReport(
        takenAt: DateTime.tryParse(json['takenAt'] as String? ?? '') ?? DateTime.now(),
        servers: ((json['servers'] as List?) ?? [])
            .map((e) => ServerMetric.fromJson(e as Map<String, dynamic>))
            .toList(),
        summary: MetricsSummary.fromJson(
            (json['summary'] as Map<String, dynamic>?) ?? <String, dynamic>{}),
      );
}

class DrainJob {
  final String id;
  final String container;
  final String phase;
  final int timeoutSeconds;
  final int playersAtStart;
  final int playersLeft;
  final String? destination;
  final String? agentJobId;
  final List<String> log;

  DrainJob({
    required this.id,
    required this.container,
    required this.phase,
    required this.timeoutSeconds,
    required this.playersAtStart,
    required this.playersLeft,
    required this.destination,
    required this.agentJobId,
    required this.log,
  });

  bool get isFinished => phase == 'DONE' || phase == 'FAILED';
  bool get failed => phase == 'FAILED';

  factory DrainJob.fromJson(Map<String, dynamic> json) => DrainJob(
        id: json['id'] as String? ?? '',
        container: json['container'] as String? ?? '',
        phase: json['phase'] as String? ?? 'FAILED',
        timeoutSeconds: _int(json['timeoutSeconds']),
        playersAtStart: _int(json['playersAtStart']),
        playersLeft: _int(json['playersLeft']),
        destination: json['destination'] as String?,
        agentJobId: json['agentJobId'] as String?,
        log: ((json['log'] as List?) ?? []).map((e) => e.toString()).toList(),
      );
}

/// Client for the live-data control endpoints, sharing ControlApi's authentication.
class LiveApi {
  final ControlApi api;

  LiveApi(this.api);

  bool get canOperate => api.role == 'OPERATOR' || api.role == 'OWNER';

  Future<CapacityReport> fetchCapacity() async {
    final data = await api.authedGet('/control/capacity');
    return CapacityReport.fromJson(data as Map<String, dynamic>);
  }

  Future<MetricsReport> fetchMetrics() async {
    final data = await api.authedGet('/control/metrics');
    return MetricsReport.fromJson(data as Map<String, dynamic>);
  }

  Future<DrainJob> drainRestart(String container, {int? timeoutSeconds}) async {
    final body = <String, dynamic>{'name': container};
    if (timeoutSeconds != null) body['timeoutSeconds'] = timeoutSeconds;
    final data = await api.authedPost('/control/drain-restart', body);
    return DrainJob.fromJson(data as Map<String, dynamic>);
  }

  Future<DrainJob> fetchDrainJob(String id) async {
    final data = await api.authedGet('/control/drain-restart/jobs', {'id': id});
    return DrainJob.fromJson(data as Map<String, dynamic>);
  }
}
