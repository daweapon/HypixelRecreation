import '../models/fleet.dart';
import '../models/ops.dart';
import 'control_api.dart';

/// Backups, alerts, exception triage and the second factor.
///
/// Kept apart from [ControlApi] so the operations features do not all edit one file; it reuses
/// that client's token handling through its public authed calls.
class OpsApi {
  final ControlApi api;

  OpsApi(this.api);

  static TotpStatus? _cachedTotp;
  static DateTime? _cachedTotpAt;

  Future<BackupOverview> fetchBackups() async {
    final data = await api.authedGet('/control/backups');
    return BackupOverview.fromJson(data as Map<String, dynamic>);
  }

  /// OPERATOR. Starts a backup in the background; poll [fetchBackups] for its result.
  Future<BackupEntry> runBackup() async {
    final data = await api.authedPost('/control/backups/run', {});
    return BackupEntry.fromJson(data as Map<String, dynamic>);
  }

  /// OWNER. [schedule] is `off`, `daily@HH:MM` or `every <n>h|m`.
  Future<BackupOverview> updateBackupSettings({String? schedule, int? retain}) async {
    final body = <String, dynamic>{};
    if (schedule != null) body['schedule'] = schedule;
    if (retain != null) body['retain'] = retain;
    final data = await api.authedPost('/control/backups/settings', body);
    return BackupOverview.fromJson(data as Map<String, dynamic>);
  }

  /// OWNER, step one of two: nothing is touched; returns what would happen and a token.
  Future<RestorePreview> previewRestore(String id, List<String> parts, String mode) async {
    final data = await api.authedPost('/control/backups/restore', {
      'id': id,
      'parts': parts,
      'mode': mode,
    });
    return RestorePreview.fromJson(data as Map<String, dynamic>);
  }

  /// OWNER, step two: runs the restore previewed with [confirmToken].
  Future<OperationJob> confirmRestore(
      String id, List<String> parts, String mode, String confirmToken) async {
    final data = await api.authedPost('/control/backups/restore', {
      'id': id,
      'parts': parts,
      'mode': mode,
      'confirmToken': confirmToken,
    });
    return OperationJob.fromJson(data as Map<String, dynamic>);
  }

  Future<AlertOverview> fetchAlerts() async {
    final data = await api.authedGet('/control/alerts');
    return AlertOverview.fromJson(data as Map<String, dynamic>);
  }

  /// OWNER. Returns each transport's outcome: `ok` or the error it raised.
  Future<Map<String, String>> sendTestAlert({String? message}) async {
    final data = await api.authedPost('/control/alerts/test', {
      if (message != null && message.isNotEmpty) 'message': message,
    }) as Map<String, dynamic>;
    return ((data['results'] as Map?) ?? {}).map((k, v) => MapEntry(k.toString(), v.toString()));
  }

  Future<ExceptionReport> fetchExceptions(
      {int tail = 2000, bool includeNoise = false, bool refresh = false}) async {
    final data = await api.authedGet('/control/exceptions', {
      'tail': '$tail',
      'noise': '$includeNoise',
      'refresh': '$refresh',
    });
    return ExceptionReport.fromJson(data as Map<String, dynamic>);
  }

  /// Cached for a minute, since it is checked before every OWNER action.
  Future<TotpStatus> fetchTotpStatus({bool force = false}) async {
    final cached = _cachedTotp;
    final at = _cachedTotpAt;
    if (!force && cached != null && at != null && DateTime.now().difference(at).inSeconds < 60) {
      return cached;
    }
    final data = await api.authedGet('/control/totp');
    final status = TotpStatus.fromJson(data as Map<String, dynamic>);
    _cachedTotp = status;
    _cachedTotpAt = DateTime.now();
    return status;
  }

  Future<TotpEnrolment> startTotpEnrolment() async {
    final data = await api.authedPost('/control/totp/enrol', {});
    return TotpEnrolment.fromJson(data as Map<String, dynamic>);
  }

  Future<void> confirmTotp(String code) async {
    await api.authedPost('/control/totp/confirm', {'code': code.replaceAll(' ', '')});
    _cachedTotp = null;
  }

  Future<void> disableTotp() async {
    await api.authedPost('/control/totp/disable', {});
    _cachedTotp = null;
  }
}
