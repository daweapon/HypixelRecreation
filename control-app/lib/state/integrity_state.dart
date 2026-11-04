import 'dart:async';

import 'package:flutter/foundation.dart';

import '../api/control_api.dart';
import '../api/integrity_api.dart';

/// Slow-moving integrity facts the dashboard shows beside the fleet: per-row SKEW and
/// proxy-registration badges, and the memory budget.
///
/// Polled far less often than the fleet itself, because each skew check hashes every jar in
/// the fleet; a failure leaves the last good values in place rather than blanking them.
class IntegrityState extends ChangeNotifier {
  final IntegrityApi api;

  IntegrityState(ControlApi controlApi) : api = IntegrityApi(controlApi);

  static const Duration pollInterval = Duration(seconds: 30);

  SkewReport? skew;
  RegistrationReport? registrations;
  CapacityReport? capacity;
  Map<String, List<RowBadge>> badges = const {};

  Timer? _timer;
  bool _disposed = false;

  void startPolling() {
    _timer?.cancel();
    refresh();
    _timer = Timer.periodic(pollInterval, (_) => refresh());
  }

  void stopPolling() {
    _timer?.cancel();
    _timer = null;
  }

  Future<void> refresh() async {
    await Future.wait([
      _guard(() async {
        skew = (await api.fetchSkew()).report ?? skew;
      }),
      _guard(() async {
        registrations = await api.fetchRegistrations();
      }),
      _guard(() async {
        capacity = await api.fetchCapacity();
      }),
    ]);
    badges = buildRowBadges(skew, registrations);
    if (!_disposed) notifyListeners();
  }

  static Future<void> _guard(Future<void> Function() load) async {
    try {
      await load();
    } catch (_) {
      // Supplementary to the fleet view; an older backend without these routes is expected.
    }
  }

  @override
  void dispose() {
    _disposed = true;
    stopPolling();
    super.dispose();
  }
}
