import 'dart:async';

import 'package:flutter/foundation.dart';

import '../api/live_api.dart';

/// Polls per-server metrics so the fleet view can show TPS and heap beside each container.
///
/// A failed poll keeps the last report rather than blanking the view: the metrics are
/// supplementary, and the fleet table already reports a server that is actually down.
class LiveMetricsController extends ChangeNotifier {
  static const Duration interval = Duration(seconds: 5);

  final LiveApi live;
  Timer? _timer;
  MetricsReport? report;
  String? error;
  bool _disposed = false;

  LiveMetricsController(this.live);

  void start() {
    _timer?.cancel();
    refresh();
    _timer = Timer.periodic(interval, (_) => refresh());
  }

  void stop() {
    _timer?.cancel();
    _timer = null;
  }

  Future<void> refresh() async {
    try {
      report = await live.fetchMetrics();
      error = null;
    } catch (e) {
      error = e.toString();
    }
    if (!_disposed) notifyListeners();
  }

  @override
  void dispose() {
    _disposed = true;
    stop();
    super.dispose();
  }
}
