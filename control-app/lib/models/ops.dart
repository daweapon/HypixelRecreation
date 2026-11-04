DateTime? _time(dynamic value) => value is String ? DateTime.tryParse(value) : null;

int _int(dynamic value) => (value as num?)?.toInt() ?? 0;

String formatBytes(int bytes) {
  if (bytes < 0) return '?';
  if (bytes < 1024) return '$bytes B';
  if (bytes < 1024 * 1024) return '${(bytes / 1024).toStringAsFixed(1)} KB';
  if (bytes < 1024 * 1024 * 1024) return '${(bytes / (1024 * 1024)).toStringAsFixed(1)} MB';
  return '${(bytes / (1024 * 1024 * 1024)).toStringAsFixed(2)} GB';
}

String formatStamp(DateTime? at) {
  if (at == null) return '-';
  final local = at.toLocal();
  String two(int v) => v.toString().padLeft(2, '0');
  return '${local.year}-${two(local.month)}-${two(local.day)} ${two(local.hour)}:${two(local.minute)}';
}

class BackupPart {
  final String kind;
  final String? container;
  final int bytes;
  final bool ok;
  final String message;

  BackupPart({
    required this.kind,
    required this.container,
    required this.bytes,
    required this.ok,
    required this.message,
  });

  factory BackupPart.fromJson(Map<String, dynamic> json) => BackupPart(
        kind: json['kind'] as String? ?? '',
        container: json['container'] as String?,
        bytes: _int(json['bytes']),
        ok: json['ok'] as bool? ?? false,
        message: json['message'] as String? ?? '',
      );
}

class BackupEntry {
  final String id;
  final DateTime? createdAt;
  final DateTime? finishedAt;
  final String trigger;
  final String requestedBy;
  final String status;
  final List<BackupPart> parts;

  BackupEntry({
    required this.id,
    required this.createdAt,
    required this.finishedAt,
    required this.trigger,
    required this.requestedBy,
    required this.status,
    required this.parts,
  });

  int get totalBytes => parts.fold(0, (sum, p) => sum + p.bytes);
  bool hasPart(String kind) => parts.any((p) => p.kind == kind && p.ok);

  factory BackupEntry.fromJson(Map<String, dynamic> json) => BackupEntry(
        id: json['id'] as String? ?? '',
        createdAt: _time(json['createdAt']),
        finishedAt: _time(json['finishedAt']),
        trigger: json['trigger'] as String? ?? '',
        requestedBy: json['requestedBy'] as String? ?? '',
        status: json['status'] as String? ?? '',
        parts: ((json['parts'] as List?) ?? [])
            .map((e) => BackupPart.fromJson(e as Map<String, dynamic>))
            .toList(),
      );
}

class UnmanagedBackup {
  final String name;
  final bool directory;
  final int bytes;
  final DateTime? modifiedAt;

  UnmanagedBackup({
    required this.name,
    required this.directory,
    required this.bytes,
    required this.modifiedAt,
  });

  factory UnmanagedBackup.fromJson(Map<String, dynamic> json) => UnmanagedBackup(
        name: json['name'] as String? ?? '',
        directory: json['directory'] as bool? ?? false,
        bytes: _int(json['bytes']),
        modifiedAt: _time(json['modifiedAt']),
      );
}

class BackupOverview {
  final String directory;
  final bool available;
  final String schedule;
  final String timezone;
  final int retain;
  final DateTime? nextRunAt;
  final BackupEntry? running;
  final BackupEntry? lastResult;
  final List<BackupEntry> backups;
  final List<UnmanagedBackup> unmanaged;

  BackupOverview({
    required this.directory,
    required this.available,
    required this.schedule,
    required this.timezone,
    required this.retain,
    required this.nextRunAt,
    required this.running,
    required this.lastResult,
    required this.backups,
    required this.unmanaged,
  });

  factory BackupOverview.fromJson(Map<String, dynamic> json) => BackupOverview(
        directory: json['directory'] as String? ?? '',
        available: json['available'] as bool? ?? false,
        schedule: json['schedule'] as String? ?? 'off',
        timezone: json['timezone'] as String? ?? 'UTC',
        retain: _int(json['retain']),
        nextRunAt: _time(json['nextRunAt']),
        running: json['running'] is Map
            ? BackupEntry.fromJson(json['running'] as Map<String, dynamic>)
            : null,
        lastResult: json['lastResult'] is Map
            ? BackupEntry.fromJson(json['lastResult'] as Map<String, dynamic>)
            : null,
        backups: ((json['backups'] as List?) ?? [])
            .map((e) => BackupEntry.fromJson(e as Map<String, dynamic>))
            .toList(),
        unmanaged: ((json['unmanaged'] as List?) ?? [])
            .map((e) => UnmanagedBackup.fromJson(e as Map<String, dynamic>))
            .toList(),
      );
}

class RestorePreview {
  final String confirmToken;
  final int expiresInSeconds;
  final bool secondFactorRequired;
  final List<String> steps;
  final List<String> warnings;

  RestorePreview({
    required this.confirmToken,
    required this.expiresInSeconds,
    required this.secondFactorRequired,
    required this.steps,
    required this.warnings,
  });

  factory RestorePreview.fromJson(Map<String, dynamic> json) {
    final preview = (json['preview'] as Map?)?.cast<String, dynamic>() ?? const {};
    return RestorePreview(
      confirmToken: json['confirmToken'] as String? ?? '',
      expiresInSeconds: _int(json['expiresInSeconds']),
      secondFactorRequired: json['secondFactorRequired'] as bool? ?? false,
      steps: ((preview['steps'] as List?) ?? []).map((e) => e.toString()).toList(),
      warnings: ((preview['warnings'] as List?) ?? []).map((e) => e.toString()).toList(),
    );
  }
}

class AlertRecord {
  final DateTime? at;
  final String severity;
  final String key;
  final String title;
  final String body;

  AlertRecord({
    required this.at,
    required this.severity,
    required this.key,
    required this.title,
    required this.body,
  });

  factory AlertRecord.fromJson(Map<String, dynamic> json) => AlertRecord(
        at: _time(json['at']),
        severity: json['severity'] as String? ?? 'WARNING',
        key: json['key'] as String? ?? '',
        title: json['title'] as String? ?? '',
        body: json['body'] as String? ?? '',
      );
}

class AlertOverview {
  final bool enabled;
  final DateTime? lastPollAt;
  final int pollSeconds;
  final List<String> transports;
  final List<String> active;
  final List<AlertRecord> recent;

  AlertOverview({
    required this.enabled,
    required this.lastPollAt,
    required this.pollSeconds,
    required this.transports,
    required this.active,
    required this.recent,
  });

  factory AlertOverview.fromJson(Map<String, dynamic> json) => AlertOverview(
        enabled: json['enabled'] as bool? ?? false,
        lastPollAt: _time(json['lastPollAt']),
        pollSeconds: _int(json['pollSeconds']),
        transports: ((json['transports'] as List?) ?? []).map((e) => e.toString()).toList(),
        active: ((json['active'] as List?) ?? []).map((e) => e.toString()).toList(),
        recent: ((json['recent'] as List?) ?? [])
            .map((e) => AlertRecord.fromJson(e as Map<String, dynamic>))
            .toList(),
      );
}

class ExceptionGroup {
  final String signature;
  final String kind;
  final String? exceptionClass;
  final String? frame;
  final String? message;
  final int count;
  final Map<String, int> perContainer;
  final DateTime? firstSeen;
  final DateTime? lastSeen;
  final String? lastContainer;
  final List<String> sample;

  ExceptionGroup({
    required this.signature,
    required this.kind,
    required this.exceptionClass,
    required this.frame,
    required this.message,
    required this.count,
    required this.perContainer,
    required this.firstSeen,
    required this.lastSeen,
    required this.lastContainer,
    required this.sample,
  });

  /// Short class name for the list row; the full signature is shown when expanded.
  String get headline {
    final cls = exceptionClass;
    if (cls == null) return message ?? signature;
    return cls.split('.').last;
  }

  factory ExceptionGroup.fromJson(Map<String, dynamic> json) => ExceptionGroup(
        signature: json['signature'] as String? ?? '',
        kind: json['kind'] as String? ?? 'ERROR',
        exceptionClass: json['exceptionClass'] as String?,
        frame: json['frame'] as String?,
        message: json['message'] as String?,
        count: _int(json['count']),
        perContainer: ((json['perContainer'] as Map?) ?? {})
            .map((k, v) => MapEntry(k.toString(), _int(v))),
        firstSeen: _time(json['firstSeen']),
        lastSeen: _time(json['lastSeen']),
        lastContainer: json['lastContainer'] as String?,
        sample: ((json['sample'] as List?) ?? []).map((e) => e.toString()).toList(),
      );
}

class ExceptionReport {
  final DateTime? scannedAt;
  final int linesScanned;
  final int totalEvents;
  final int tail;
  final Map<String, int> perContainer;
  final List<ExceptionGroup> groups;
  final List<String> containersScanned;
  final Map<String, String> errors;

  ExceptionReport({
    required this.scannedAt,
    required this.linesScanned,
    required this.totalEvents,
    required this.tail,
    required this.perContainer,
    required this.groups,
    required this.containersScanned,
    required this.errors,
  });

  factory ExceptionReport.fromJson(Map<String, dynamic> json) {
    final report = (json['report'] as Map?)?.cast<String, dynamic>() ?? const {};
    return ExceptionReport(
      scannedAt: _time(report['scannedAt']),
      linesScanned: _int(report['linesScanned']),
      totalEvents: _int(report['totalEvents']),
      tail: _int(json['tail']),
      perContainer: ((report['perContainer'] as Map?) ?? {})
          .map((k, v) => MapEntry(k.toString(), _int(v))),
      groups: ((report['groups'] as List?) ?? [])
          .map((e) => ExceptionGroup.fromJson(e as Map<String, dynamic>))
          .toList(),
      containersScanned:
          ((json['containersScanned'] as List?) ?? []).map((e) => e.toString()).toList(),
      errors: ((json['errors'] as Map?) ?? {}).map((k, v) => MapEntry(k.toString(), v.toString())),
    );
  }
}

class TotpStatus {
  final bool enrolled;
  final String? enrolledBy;
  final DateTime? enrolledAt;
  final bool pending;

  TotpStatus({
    required this.enrolled,
    required this.enrolledBy,
    required this.enrolledAt,
    required this.pending,
  });

  factory TotpStatus.fromJson(Map<String, dynamic> json) => TotpStatus(
        enrolled: json['enrolled'] as bool? ?? false,
        enrolledBy: json['enrolledBy'] as String?,
        enrolledAt: _time(json['enrolledAt']),
        pending: json['pending'] as bool? ?? false,
      );
}

class TotpEnrolment {
  final String secret;
  final String otpauthUri;
  final String account;

  TotpEnrolment({required this.secret, required this.otpauthUri, required this.account});

  /// The secret in groups of four, which is how authenticator apps ask for it to be typed.
  String get groupedSecret {
    final buffer = StringBuffer();
    for (var i = 0; i < secret.length; i += 4) {
      if (i > 0) buffer.write(' ');
      buffer.write(secret.substring(i, i + 4 > secret.length ? secret.length : i + 4));
    }
    return buffer.toString();
  }

  factory TotpEnrolment.fromJson(Map<String, dynamic> json) => TotpEnrolment(
        secret: json['secret'] as String? ?? '',
        otpauthUri: json['otpauthUri'] as String? ?? '',
        account: json['account'] as String? ?? '',
      );
}
