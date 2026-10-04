import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:package_info_plus/package_info_plus.dart';

import '../api/control_api.dart';
import '../models/fleet.dart';

/// Polls the control plane and holds everything the screens render.
class AppState extends ChangeNotifier {
  final ControlApi api;

  AppState(this.api);

  static const Duration pollInterval = Duration(seconds: 5);

  FleetSnapshot? fleet;
  List<AlertEntry> alerts = [];
  String? error;
  bool loading = false;
  OperationJob? activeJob;

  Timer? _timer;
  Timer? _jobTimer;

  /// Read from the installed package rather than a hand-maintained constant, so it cannot
  /// drift from what is actually running. Surfaced in the UI because this app is sideloaded:
  /// the filename never changes, so "am I on the current build?" is otherwise unanswerable
  /// without digging through Android settings.
  String appVersion = '';

  Future<void> loadVersion() async {
    try {
      final info = await PackageInfo.fromPlatform();
      appVersion = 'v${info.version}';
      notifyListeners();
    } catch (_) {
      // Version display is a convenience; never let it break startup.
    }
  }

  bool get isEnrolled => api.isEnrolled;
  String get role => api.role ?? 'READ_ONLY';
  bool get canOperate => role == 'OPERATOR' || role == 'OWNER';

  void startPolling() {
    _timer?.cancel();
    refresh();
    _timer = Timer.periodic(pollInterval, (_) => refresh());
  }

  void stopPolling() {
    _timer?.cancel();
    _timer = null;
    _jobTimer?.cancel();
    _jobTimer = null;
  }

  Future<void> refresh({bool force = false}) async {
    if (!api.isEnrolled) return;
    try {
      final snapshot = await api.fetchFleet(refresh: force);
      fleet = snapshot;
      error = null;
    } catch (e) {
      error = e.toString();
    }
    notifyListeners();
  }

  Future<void> refreshAlerts() async {
    try {
      alerts = await api.fetchAlerts();
      notifyListeners();
    } catch (_) {
      // Alerts are supplementary; a failure here must not blank the dashboard.
    }
  }

  /// Runs a lifecycle action and follows the resulting job to completion.
  Future<void> runAction(String action, {List<String>? names, int? stagger}) async {
    loading = true;
    error = null;
    notifyListeners();
    try {
      activeJob = await api.act(action, names: names, stagger: stagger);
      _followJob();
    } catch (e) {
      error = e.toString();
    } finally {
      loading = false;
      notifyListeners();
    }
  }

  void _followJob() {
    _jobTimer?.cancel();
    final job = activeJob;
    if (job == null) return;
    _jobTimer = Timer.periodic(const Duration(seconds: 2), (timer) async {
      try {
        final updated = await api.fetchJob(job.id);
        activeJob = updated;
        notifyListeners();
        if (!updated.isRunning) {
          timer.cancel();
          await refresh(force: true);
        }
      } catch (_) {
        timer.cancel();
      }
    });
  }

  void dismissJob() {
    _jobTimer?.cancel();
    activeJob = null;
    notifyListeners();
  }

  @override
  void dispose() {
    stopPolling();
    super.dispose();
  }
}
