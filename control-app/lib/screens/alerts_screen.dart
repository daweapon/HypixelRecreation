import 'package:flutter/material.dart';

import '../api/control_api.dart';
import '../api/ops_api.dart';
import '../models/ops.dart';
import '../theme/reactor_theme.dart';
import '../widgets/chamfer.dart';
import '../widgets/hud_button.dart';

/// Watchdog state, recent alerts, and a test push.
///
/// The test button exists because an alert channel nobody has seen deliver is not one to
/// trust: it sends through every configured transport (log, webhook, ntfy) and shows what each
/// one answered. Sending needs OWNER.
///
/// Constructor: `AlertsScreen(api: ControlApi, canTest: bool)` - pass `role == 'OWNER'`.
class AlertsScreen extends StatefulWidget {
  final ControlApi api;
  final bool canTest;

  const AlertsScreen({super.key, required this.api, required this.canTest});

  @override
  State<AlertsScreen> createState() => _AlertsScreenState();
}

class _AlertsScreenState extends State<AlertsScreen> {
  late final OpsApi _ops = OpsApi(widget.api);
  AlertOverview? _overview;
  String? _error;
  bool _sending = false;
  Map<String, String>? _testResults;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    try {
      final overview = await _ops.fetchAlerts();
      if (mounted) {
        setState(() {
          _overview = overview;
          _error = null;
        });
      }
    } catch (e) {
      if (mounted) setState(() => _error = e.toString());
    }
  }

  Future<void> _sendTest() async {
    setState(() {
      _sending = true;
      _testResults = null;
    });
    try {
      final results = await _ops.sendTestAlert();
      if (mounted) setState(() => _testResults = results);
      await _load();
    } catch (e) {
      if (mounted) setState(() => _testResults = {'error': e.toString()});
    } finally {
      if (mounted) setState(() => _sending = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: hudAppBar('ALERTS', actions: [
        IconButton(
          iconSize: 16,
          icon: const Icon(Icons.refresh, color: ReactorTheme.textSecondary),
          onPressed: _load,
        ),
      ]),
      body: Padding(padding: const EdgeInsets.all(10), child: _body()),
    );
  }

  Widget _body() {
    final overview = _overview;
    if (overview == null) {
      return _error != null
          ? Center(
              child: Text(_error!, style: ReactorTheme.data(size: 11, color: ReactorTheme.critical)))
          : hudSpinner();
    }
    final pushConfigured = overview.transports.any((t) => t != 'log');

    return ListView(
      children: [
        HudPanel(
          title: 'watchdog',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              hudKeyValue('state', overview.enabled ? 'watching' : 'muted',
                  colour: overview.enabled ? ReactorTheme.cyan : ReactorTheme.amber),
              hudKeyValue('poll', 'every ${overview.pollSeconds}s · last ${formatStamp(overview.lastPollAt)}'),
              hudKeyValue('channels', overview.transports.join(', '),
                  colour: pushConfigured ? ReactorTheme.textPrimary : ReactorTheme.amber),
              if (!pushConfigured)
                Padding(
                  padding: const EdgeInsets.only(bottom: 6),
                  child: Text(
                    'Alerts only reach the log. Set ALERT_NTFY_URL in the stack .env to get them '
                    'on a phone.',
                    style: ReactorTheme.data(size: 10.5, color: ReactorTheme.amber),
                  ),
                ),
              hudKeyValue('active', overview.active.isEmpty ? 'none' : overview.active.join(', '),
                  colour: overview.active.isEmpty ? ReactorTheme.cyan : ReactorTheme.critical),
              const SizedBox(height: 8),
              HudButton(
                label: 'SEND TEST ALERT',
                colour: ReactorTheme.amber,
                busy: _sending,
                onTap: widget.canTest ? _sendTest : null,
              ),
              if (!widget.canTest)
                Padding(
                  padding: const EdgeInsets.only(top: 6),
                  child: Text('Sending a test alert requires OWNER.',
                      style: ReactorTheme.data(size: 10, color: ReactorTheme.textFaint)),
                ),
              if (_testResults != null) ...[
                const SizedBox(height: 8),
                ..._testResults!.entries.map((e) => hudKeyValue(e.key, e.value,
                    colour: e.value == 'ok' ? ReactorTheme.cyan : ReactorTheme.critical)),
              ],
            ],
          ),
        ),
        const SizedBox(height: 10),
        HudPanel(
          title: '${overview.recent.length} recent alerts  ·  newest first',
          child: overview.recent.isEmpty
              ? Text('nothing has alerted since the agent started',
                  style: ReactorTheme.data(size: 11, color: ReactorTheme.textFaint))
              : Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: overview.recent.map(_row).toList(),
                ),
        ),
      ],
    );
  }

  Widget _row(AlertRecord alert) {
    final colour = ReactorTheme.forSeverity(alert.severity);
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(width: 2, height: 30, color: colour),
          const SizedBox(width: 8),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(children: [
                  Text(alert.severity, style: ReactorTheme.label(size: 9, color: colour)),
                  const SizedBox(width: 8),
                  Expanded(
                      child: Text(alert.title,
                          style: ReactorTheme.data(size: 12, weight: FontWeight.w600))),
                  Text(formatStamp(alert.at),
                      style: ReactorTheme.data(size: 10, color: ReactorTheme.textFaint)),
                ]),
                if (alert.body.isNotEmpty)
                  Text(alert.body,
                      style: ReactorTheme.data(size: 10.5, color: ReactorTheme.textSecondary)),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
