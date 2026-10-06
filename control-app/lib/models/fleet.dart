/// Wire models mirroring service.control's JSON.
library;

class DependencyStatus {
  final String dependency;
  final String? containerName;
  final String state;
  final String statusText;
  final bool present;

  DependencyStatus({
    required this.dependency,
    required this.containerName,
    required this.state,
    required this.statusText,
    required this.present,
  });

  bool get isNominal => present && state == 'UP';

  String get displayName {
    switch (dependency) {
      case 'MONGO':
        return 'Mongo';
      case 'REDIS':
        return 'Redis';
      case 'PROXY':
        return 'Proxy';
      default:
        return dependency;
    }
  }

  factory DependencyStatus.fromJson(Map<String, dynamic> json) => DependencyStatus(
        dependency: json['dependency'] as String,
        containerName: json['containerName'] as String?,
        state: json['state'] as String? ?? 'DWN',
        statusText: json['statusText'] as String? ?? '',
        present: json['present'] as bool? ?? false,
      );
}

class ContainerInfo {
  final String id;
  final String name;
  final String image;
  final String group;
  final String state;
  final String dockerState;
  final String health;
  final int restartCount;
  final int exitCode;
  final DateTime? startedAt;
  final String statusText;

  ContainerInfo({
    required this.id,
    required this.name,
    required this.image,
    required this.group,
    required this.state,
    required this.dockerState,
    required this.health,
    required this.restartCount,
    required this.exitCode,
    required this.startedAt,
    required this.statusText,
  });

  bool get isNominal => state == 'UP';

  /// Compact uptime, e.g. "3d 4h", "12m".
  String get uptimeLabel {
    if (startedAt == null || !isNominal) return '--';
    final d = DateTime.now().toUtc().difference(startedAt!.toUtc());
    if (d.inDays > 0) return '${d.inDays}d ${d.inHours % 24}h';
    if (d.inHours > 0) return '${d.inHours}h ${d.inMinutes % 60}m';
    if (d.inMinutes > 0) return '${d.inMinutes}m';
    return '${d.inSeconds}s';
  }

  factory ContainerInfo.fromJson(Map<String, dynamic> json) => ContainerInfo(
        id: json['id'] as String? ?? '',
        name: json['name'] as String? ?? '?',
        image: json['image'] as String? ?? '',
        group: json['group'] as String? ?? 'UNKNOWN',
        state: json['state'] as String? ?? 'DWN',
        dockerState: json['dockerState'] as String? ?? '',
        health: json['health'] as String? ?? 'none',
        restartCount: (json['restartCount'] as num?)?.toInt() ?? 0,
        exitCode: (json['exitCode'] as num?)?.toInt() ?? 0,
        startedAt: json['startedAt'] == null ? null : DateTime.tryParse(json['startedAt'] as String),
        statusText: json['statusText'] as String? ?? '',
      );
}

class Diagnosis {
  final String severity;
  final String headline;
  final String detail;

  Diagnosis({required this.severity, required this.headline, required this.detail});

  factory Diagnosis.fromJson(Map<String, dynamic> json) => Diagnosis(
        severity: json['severity'] as String? ?? 'NOMINAL',
        headline: json['headline'] as String? ?? '',
        detail: json['detail'] as String? ?? '',
      );
}

class FleetSnapshot {
  final DateTime takenAt;
  final bool dockerReachable;
  final String? dockerError;
  final List<DependencyStatus> dependencies;
  final List<ContainerInfo> containers;
  final int integrity;
  final Diagnosis diagnosis;

  FleetSnapshot({
    required this.takenAt,
    required this.dockerReachable,
    required this.dockerError,
    required this.dependencies,
    required this.containers,
    required this.integrity,
    required this.diagnosis,
  });

  List<ContainerInfo> inGroup(String group) =>
      containers.where((c) => c.group == group).toList();

  int get runningCount => containers.where((c) => c.isNominal).length;

  factory FleetSnapshot.fromJson(Map<String, dynamic> json) => FleetSnapshot(
        takenAt: DateTime.tryParse(json['takenAt'] as String? ?? '') ?? DateTime.now(),
        dockerReachable: json['dockerReachable'] as bool? ?? false,
        dockerError: json['dockerError'] as String?,
        dependencies: ((json['dependencies'] as List?) ?? [])
            .map((e) => DependencyStatus.fromJson(e as Map<String, dynamic>))
            .toList(),
        containers: ((json['containers'] as List?) ?? [])
            .map((e) => ContainerInfo.fromJson(e as Map<String, dynamic>))
            .toList(),
        integrity: (json['integrity'] as num?)?.toInt() ?? 0,
        diagnosis: Diagnosis.fromJson(
            (json['diagnosis'] as Map<String, dynamic>?) ?? <String, dynamic>{}),
      );
}

class LogLine {
  final String? timestamp;
  final String message;
  final bool noise;

  LogLine({required this.timestamp, required this.message, required this.noise});

  factory LogLine.fromJson(Map<String, dynamic> json) => LogLine(
        timestamp: json['timestamp'] as String?,
        message: json['message'] as String? ?? '',
        noise: json['noise'] as bool? ?? false,
      );
}

class LogResult {
  final String container;
  final String mode;
  final int totalLines;
  final int suppressedLines;
  final List<LogLine> lines;

  LogResult({
    required this.container,
    required this.mode,
    required this.totalLines,
    required this.suppressedLines,
    required this.lines,
  });

  factory LogResult.fromJson(Map<String, dynamic> json) => LogResult(
        container: json['container'] as String? ?? '',
        mode: json['mode'] as String? ?? 'FILTERED',
        totalLines: (json['totalLines'] as num?)?.toInt() ?? 0,
        suppressedLines: (json['suppressedLines'] as num?)?.toInt() ?? 0,
        lines: ((json['lines'] as List?) ?? [])
            .map((e) => LogLine.fromJson(e as Map<String, dynamic>))
            .toList(),
      );
}

class AlertEntry {
  final DateTime at;
  final String severity;
  final String title;
  final String body;

  AlertEntry({required this.at, required this.severity, required this.title, required this.body});

  factory AlertEntry.fromJson(Map<String, dynamic> json) => AlertEntry(
        at: DateTime.tryParse(json['at'] as String? ?? '') ?? DateTime.now(),
        severity: json['severity'] as String? ?? 'WARNING',
        title: json['title'] as String? ?? '',
        body: json['body'] as String? ?? '',
      );
}

class OperationStep {
  final String container;
  final String action;
  final bool ok;
  final String message;

  OperationStep({
    required this.container,
    required this.action,
    required this.ok,
    required this.message,
  });

  factory OperationStep.fromJson(Map<String, dynamic> json) => OperationStep(
        container: json['container'] as String? ?? '',
        action: json['action'] as String? ?? '',
        ok: json['ok'] as bool? ?? false,
        message: json['message'] as String? ?? '',
      );
}

class OperationJob {
  final String id;
  final String operation;
  final String status;
  final String currentStep;
  final List<OperationStep> steps;

  OperationJob({
    required this.id,
    required this.operation,
    required this.status,
    required this.currentStep,
    required this.steps,
  });

  bool get isRunning => status == 'RUNNING';

  factory OperationJob.fromJson(Map<String, dynamic> json) => OperationJob(
        id: json['id'] as String? ?? '',
        operation: json['operation'] as String? ?? '',
        status: json['status'] as String? ?? '',
        currentStep: json['currentStep'] as String? ?? '',
        steps: ((json['steps'] as List?) ?? [])
            .map((e) => OperationStep.fromJson(e as Map<String, dynamic>))
            .toList(),
      );
}

class AuditEntry {
  final String id;
  final DateTime at;
  final String actorName;
  final String? deviceName;
  final String? role;
  final String action;
  final List<String> targets;
  final String outcome;
  final String? detail;

  AuditEntry({
    required this.id,
    required this.at,
    required this.actorName,
    required this.deviceName,
    required this.role,
    required this.action,
    required this.targets,
    required this.outcome,
    required this.detail,
  });

  bool get wasDenied => outcome == 'DENIED';
  bool get wasAccepted => outcome == 'ACCEPTED';

  factory AuditEntry.fromJson(Map<String, dynamic> json) => AuditEntry(
        id: json['id'] as String? ?? '',
        at: DateTime.tryParse(json['at'] as String? ?? '') ?? DateTime.now(),
        actorName: json['actorName'] as String? ?? 'unknown',
        deviceName: json['deviceName'] as String?,
        role: json['role'] as String?,
        action: json['action'] as String? ?? '?',
        targets: ((json['targets'] as List?) ?? []).map((e) => e.toString()).toList(),
        outcome: json['outcome'] as String? ?? 'FAILED',
        detail: json['detail'] as String?,
      );
}

class JarCandidate {
  final String name;
  final int sizeBytes;
  final String sha256;
  final DateTime? modifiedAt;
  final List<String> defaultTargets;
  final bool bindMounted;
  final bool matchesStaged;

  JarCandidate({
    required this.name,
    required this.sizeBytes,
    required this.sha256,
    required this.modifiedAt,
    required this.defaultTargets,
    required this.bindMounted,
    required this.matchesStaged,
  });

  String get sizeLabel => '${(sizeBytes / 1048576).toStringAsFixed(1)} MB';
  String get shortHash => sha256.length > 12 ? sha256.substring(0, 12) : sha256;

  factory JarCandidate.fromJson(Map<String, dynamic> json) => JarCandidate(
        name: json['name'] as String? ?? '?',
        sizeBytes: (json['sizeBytes'] as num?)?.toInt() ?? 0,
        sha256: json['sha256'] as String? ?? '',
        modifiedAt: DateTime.tryParse(json['modifiedAt'] as String? ?? ''),
        defaultTargets:
            ((json['defaultTargets'] as List?) ?? []).map((e) => e.toString()).toList(),
        bindMounted: json['bindMounted'] as bool? ?? false,
        matchesStaged: json['matchesStaged'] as bool? ?? false,
      );
}

class DeployCandidates {
  final String dropDirectory;
  final bool dropAvailable;
  final List<JarCandidate> candidates;

  DeployCandidates({
    required this.dropDirectory,
    required this.dropAvailable,
    required this.candidates,
  });

  factory DeployCandidates.fromJson(Map<String, dynamic> json) => DeployCandidates(
        dropDirectory: json['dropDirectory'] as String? ?? '',
        dropAvailable: json['dropAvailable'] as bool? ?? false,
        candidates: ((json['candidates'] as List?) ?? [])
            .map((e) => JarCandidate.fromJson(e as Map<String, dynamic>))
            .toList(),
      );
}

/// An enrolled device: who may control the stack, and at what level.
class DeviceEntry {
  final String deviceId;
  final String deviceName;
  final String playerName;
  final String role;
  final DateTime? createdAt;
  final DateTime? lastSeenAt;
  final bool revoked;

  DeviceEntry({
    required this.deviceId,
    required this.deviceName,
    required this.playerName,
    required this.role,
    required this.createdAt,
    required this.lastSeenAt,
    required this.revoked,
  });

  bool get isOwner => role == 'OWNER';
  bool get isReadOnly => role == 'READ_ONLY';

  factory DeviceEntry.fromJson(Map<String, dynamic> json) => DeviceEntry(
        deviceId: json['deviceId'] as String? ?? '',
        deviceName: json['deviceName'] as String? ?? 'unnamed device',
        playerName: json['playerName'] as String? ?? 'unknown',
        role: json['role'] as String? ?? 'READ_ONLY',
        createdAt: DateTime.tryParse(json['createdAt'] as String? ?? ''),
        lastSeenAt: DateTime.tryParse(json['lastSeenAt'] as String? ?? ''),
        revoked: json['revoked'] as bool? ?? false,
      );
}
