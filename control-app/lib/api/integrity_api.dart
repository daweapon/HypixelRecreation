import '../models/fleet.dart';
import 'control_api.dart';

/// Fleet-integrity calls: version skew, proxy registration, config drift, capacity and
/// resource-pack health. Uses ControlApi's authenticated plumbing.
class IntegrityApi {
  final ControlApi api;

  IntegrityApi(this.api);

  Future<Background<SkewReport>> fetchSkew({bool refresh = false}) async {
    final data = await api.getJson('/control/integrity/skew', refresh ? {'refresh': 'true'} : null);
    return Background.fromJson(data as Map<String, dynamic>, SkewReport.fromJson);
  }

  Future<OperationJob> reconcile(String jar, {List<String>? names}) async {
    final body = <String, dynamic>{'jar': jar};
    if (names != null && names.isNotEmpty) body['names'] = names;
    final data = await api.postJson('/control/integrity/reconcile', body);
    return OperationJob.fromJson(data as Map<String, dynamic>);
  }

  Future<RegistrationReport> fetchRegistrations() async {
    final data = await api.getJson('/control/integrity/registrations');
    return RegistrationReport.fromJson(data as Map<String, dynamic>);
  }

  Future<OperationJob> reregister(List<String> names) async {
    final data = await api.postJson('/control/integrity/reregister', {'names': names});
    return OperationJob.fromJson(data as Map<String, dynamic>);
  }

  Future<Background<DriftReport>> fetchConfigDrift({bool refresh = false}) async {
    final data =
        await api.getJson('/control/integrity/config-drift', refresh ? {'refresh': 'true'} : null);
    return Background.fromJson(data as Map<String, dynamic>, DriftReport.fromJson);
  }

  Future<OperationJob> syncConfig({List<String>? names, bool restart = true}) async {
    final body = <String, dynamic>{'restart': restart};
    if (names != null && names.isNotEmpty) body['names'] = names;
    final data = await api.postJson('/control/integrity/config-sync', body);
    return OperationJob.fromJson(data as Map<String, dynamic>);
  }

  Future<CapacityReport> fetchCapacity({List<String> candidates = const []}) async {
    final data = await api.getJson('/control/integrity/capacity',
        candidates.isEmpty ? null : {'candidates': candidates.join(',')});
    return CapacityReport.fromJson(data as Map<String, dynamic>);
  }

  Future<PackReport> fetchPack({bool refresh = false, bool verify = false}) async {
    final query = <String, String>{};
    if (refresh) query['refresh'] = 'true';
    if (verify) query['verify'] = 'true';
    final data = await api.getJson('/control/integrity/pack', query.isEmpty ? null : query);
    return PackReport.fromJson(data as Map<String, dynamic>);
  }

  /// The capacity warning to show before starting [names], or null when the start fits.
  Future<String?> capacityWarning(List<String> names) async {
    if (names.isEmpty) return null;
    try {
      final report = await fetchCapacity(candidates: names);
      return report.overBudget ? report.summary : null;
    } catch (_) {
      return null;
    }
  }
}

List<String> _strings(dynamic value) =>
    ((value as List?) ?? const []).map((e) => e.toString()).toList();

int _int(dynamic value) => (value as num?)?.toInt() ?? 0;

/// A report the agent builds in the background; [report] is null until the first build ends.
class Background<T> {
  final bool refreshing;
  final DateTime? computedAt;
  final T? report;

  Background({required this.refreshing, required this.computedAt, required this.report});

  factory Background.fromJson(
          Map<String, dynamic> json, T Function(Map<String, dynamic>) parse) =>
      Background(
        refreshing: json['refreshing'] as bool? ?? false,
        computedAt: DateTime.tryParse(json['computedAt'] as String? ?? ''),
        report: json['report'] == null ? null : parse(json['report'] as Map<String, dynamic>),
      );
}

class SkewEntry {
  final String container;
  final String jar;
  final String state;
  final String? containerSha256;
  final String? referenceSha256;
  final String reason;

  SkewEntry({
    required this.container,
    required this.jar,
    required this.state,
    required this.containerSha256,
    required this.referenceSha256,
    required this.reason,
  });

  static String short(String? hash) =>
      hash == null ? '--' : (hash.length > 12 ? hash.substring(0, 12) : hash);

  factory SkewEntry.fromJson(Map<String, dynamic> json) => SkewEntry(
        container: json['container'] as String? ?? '?',
        jar: json['jar'] as String? ?? '?',
        state: json['state'] as String? ?? 'NO_REFERENCE',
        containerSha256: json['containerSha256'] as String?,
        referenceSha256: json['referenceSha256'] as String?,
        reason: json['reason'] as String? ?? '',
      );
}

class SkewReport {
  final String jarStore;
  final bool storeAvailable;
  final int skewed;
  final List<SkewEntry> entries;

  SkewReport({
    required this.jarStore,
    required this.storeAvailable,
    required this.skewed,
    required this.entries,
  });

  Map<String, List<SkewEntry>> get byJar {
    final groups = <String, List<SkewEntry>>{};
    for (final entry in entries) {
      groups.putIfAbsent(entry.jar, () => []).add(entry);
    }
    return groups;
  }

  factory SkewReport.fromJson(Map<String, dynamic> json) => SkewReport(
        jarStore: json['jarStore'] as String? ?? '',
        storeAvailable: json['storeAvailable'] as bool? ?? false,
        skewed: _int(json['skewed']),
        entries: ((json['entries'] as List?) ?? [])
            .map((e) => SkewEntry.fromJson(e as Map<String, dynamic>))
            .toList(),
      );
}

class RegistrationRow {
  final String container;
  final String? serverType;
  final String containerState;
  final String state;
  final String? proxyName;
  final int players;
  final String detail;

  RegistrationRow({
    required this.container,
    required this.serverType,
    required this.containerState,
    required this.state,
    required this.proxyName,
    required this.players,
    required this.detail,
  });

  factory RegistrationRow.fromJson(Map<String, dynamic> json) => RegistrationRow(
        container: json['container'] as String? ?? '?',
        serverType: json['serverType'] as String?,
        containerState: json['containerState'] as String? ?? 'DWN',
        state: json['state'] as String? ?? 'UNKNOWN',
        proxyName: json['proxyName'] as String?,
        players: _int(json['players']),
        detail: json['detail'] as String? ?? '',
      );
}

class RegistrationReport {
  final bool proxyReachable;
  final String? proxyError;
  final String severity;
  final String headline;
  final String detail;
  final List<RegistrationRow> rows;
  final int unmatched;
  final List<String> delisted;

  RegistrationReport({
    required this.proxyReachable,
    required this.proxyError,
    required this.severity,
    required this.headline,
    required this.detail,
    required this.rows,
    required this.unmatched,
    required this.delisted,
  });

  factory RegistrationReport.fromJson(Map<String, dynamic> json) => RegistrationReport(
        proxyReachable: json['proxyReachable'] as bool? ?? false,
        proxyError: json['proxyError'] as String?,
        severity: json['severity'] as String? ?? 'WARNING',
        headline: json['headline'] as String? ?? '',
        detail: json['detail'] as String? ?? '',
        rows: ((json['rows'] as List?) ?? [])
            .map((e) => RegistrationRow.fromJson(e as Map<String, dynamic>))
            .toList(),
        unmatched: ((json['unmatched'] as List?) ?? []).length,
        delisted: _strings(json['delisted']),
      );
}

class FileDrift {
  final String path;
  final String kind;

  FileDrift({required this.path, required this.kind});

  bool get syncable => kind != 'EXTRA_IN_CONTAINER';

  String get kindLabel {
    switch (kind) {
      case 'CHANGED':
        return 'CHG';
      case 'MISSING_IN_CONTAINER':
        return 'NEW';
      default:
        return 'DEL';
    }
  }

  factory FileDrift.fromJson(Map<String, dynamic> json) => FileDrift(
        path: json['path'] as String? ?? '?',
        kind: json['kind'] as String? ?? 'CHANGED',
      );
}

class ContainerDrift {
  final String container;
  final String status;
  final String scope;
  final int changed;
  final int missing;
  final int extra;
  final List<FileDrift> files;
  final String? error;

  ContainerDrift({
    required this.container,
    required this.status,
    required this.scope,
    required this.changed,
    required this.missing,
    required this.extra,
    required this.files,
    required this.error,
  });

  factory ContainerDrift.fromJson(Map<String, dynamic> json) => ContainerDrift(
        container: json['container'] as String? ?? '?',
        status: json['status'] as String? ?? 'UNREADABLE',
        scope: json['scope'] as String? ?? '',
        changed: _int(json['changed']),
        missing: _int(json['missing']),
        extra: _int(json['extra']),
        files: ((json['files'] as List?) ?? [])
            .map((e) => FileDrift.fromJson(e as Map<String, dynamic>))
            .toList(),
        error: json['error'] as String?,
      );
}

class DriftReport {
  final bool hostMounted;
  final String hostDirectory;
  final String composeLine;
  final int hostFiles;
  final List<ContainerDrift> containers;

  DriftReport({
    required this.hostMounted,
    required this.hostDirectory,
    required this.composeLine,
    required this.hostFiles,
    required this.containers,
  });

  bool get anyDrift => containers.any((c) => c.status == 'DRIFTED');

  factory DriftReport.fromJson(Map<String, dynamic> json) => DriftReport(
        hostMounted: json['hostMounted'] as bool? ?? false,
        hostDirectory: json['hostDirectory'] as String? ?? '',
        composeLine: json['composeLine'] as String? ?? '',
        hostFiles: _int(json['hostFiles']),
        containers: ((json['containers'] as List?) ?? [])
            .map((e) => ContainerDrift.fromJson(e as Map<String, dynamic>))
            .toList(),
      );
}

class CapacityEntry {
  final String name;
  final bool running;
  final bool candidate;
  final int bytes;
  final bool estimated;

  CapacityEntry({
    required this.name,
    required this.running,
    required this.candidate,
    required this.bytes,
    required this.estimated,
  });

  factory CapacityEntry.fromJson(Map<String, dynamic> json) => CapacityEntry(
        name: json['name'] as String? ?? '?',
        running: json['running'] as bool? ?? false,
        candidate: json['candidate'] as bool? ?? false,
        bytes: _int(json['bytes']),
        estimated: json['estimated'] as bool? ?? false,
      );
}

class CapacityReport {
  final int hostBytes;
  final int reserveBytes;
  final int budgetBytes;
  final int committedBytes;
  final int candidateBytes;
  final int projectedBytes;
  final bool overBudget;
  final int estimatedCount;
  final List<CapacityEntry> entries;
  final String summary;

  CapacityReport({
    required this.hostBytes,
    required this.reserveBytes,
    required this.budgetBytes,
    required this.committedBytes,
    required this.candidateBytes,
    required this.projectedBytes,
    required this.overBudget,
    required this.estimatedCount,
    required this.entries,
    required this.summary,
  });

  static String gib(int bytes) => '${(bytes / (1024 * 1024 * 1024)).toStringAsFixed(1)} GiB';

  double get committedFraction => budgetBytes == 0 ? 1 : committedBytes / budgetBytes;

  factory CapacityReport.fromJson(Map<String, dynamic> json) => CapacityReport(
        hostBytes: _int(json['hostBytes']),
        reserveBytes: _int(json['reserveBytes']),
        budgetBytes: _int(json['budgetBytes']),
        committedBytes: _int(json['committedBytes']),
        candidateBytes: _int(json['candidateBytes']),
        projectedBytes: _int(json['projectedBytes']),
        overBudget: json['overBudget'] as bool? ?? false,
        estimatedCount: _int(json['estimatedCount']),
        entries: ((json['entries'] as List?) ?? [])
            .map((e) => CapacityEntry.fromJson(e as Map<String, dynamic>))
            .toList(),
        summary: json['summary'] as String? ?? '',
      );
}

class ProbeOutcome {
  final String url;
  final int status;
  final String? error;

  ProbeOutcome({required this.url, required this.status, required this.error});

  String get label => status > 0 ? 'HTTP $status' : (error ?? 'no answer');

  factory ProbeOutcome.fromJson(Map<String, dynamic> json) => ProbeOutcome(
        url: json['url'] as String? ?? '',
        status: _int(json['status']),
        error: json['error'] as String?,
      );
}

class PackReport {
  final String state;
  final String headline;
  final List<String> problems;
  final String container;
  final bool running;
  final List<String> served;
  final Map<String, String> advertised;
  final List<ProbeOutcome> probes;
  final List<ProbeOutcome> external;
  final Map<String, bool> verified;

  PackReport({
    required this.state,
    required this.headline,
    required this.problems,
    required this.container,
    required this.running,
    required this.served,
    required this.advertised,
    required this.probes,
    required this.external,
    required this.verified,
  });

  factory PackReport.fromJson(Map<String, dynamic> json) => PackReport(
        state: json['state'] as String? ?? 'DEAD',
        headline: json['headline'] as String? ?? '',
        problems: _strings(json['problems']),
        container: json['container'] as String? ?? '',
        running: json['running'] as bool? ?? false,
        served: _strings(json['served']),
        advertised: ((json['advertised'] as Map?) ?? const {})
            .map((k, v) => MapEntry(k.toString(), v.toString())),
        probes: ((json['probes'] as List?) ?? [])
            .map((e) => ProbeOutcome.fromJson(e as Map<String, dynamic>))
            .toList(),
        external: ((json['external'] as List?) ?? [])
            .map((e) => ProbeOutcome.fromJson(e as Map<String, dynamic>))
            .toList(),
        verified: ((json['verified'] as Map?) ?? const {})
            .map((k, v) => MapEntry(k.toString(), v == true)),
      );
}

/// A short tag drawn on a fleet-table row beside its container state.
class RowBadge {
  final String code;
  final String tooltip;

  const RowBadge(this.code, this.tooltip);
}

/// Per-container badges from the latest skew and registration reports.
Map<String, List<RowBadge>> buildRowBadges(SkewReport? skew, RegistrationReport? registrations) {
  final badges = <String, List<RowBadge>>{};
  void add(String container, RowBadge badge) =>
      badges.putIfAbsent(container, () => []).add(badge);

  for (final row in registrations?.rows ?? const <RegistrationRow>[]) {
    switch (row.state) {
      case 'REGISTERED':
        add(row.container, RowBadge('REG', row.detail));
      case 'DELISTED':
        add(row.container, RowBadge('DELISTED', row.detail));
      case 'BOOTING':
        add(row.container, RowBadge('BOOT', row.detail));
    }
  }
  for (final entry in skew?.entries ?? const <SkewEntry>[]) {
    if (entry.state == 'SKEW') add(entry.container, RowBadge('SKEW', entry.reason));
  }
  return badges;
}
