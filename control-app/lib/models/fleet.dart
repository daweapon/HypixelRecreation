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
