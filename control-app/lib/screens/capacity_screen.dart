import 'dart:async';

import 'package:flutter/material.dart';

import '../api/control_api.dart';
import '../api/live_api.dart';
import '../models/fleet.dart';
import '../theme/reactor_theme.dart';
import '../widgets/chamfer.dart';
import '../widgets/drain_restart_dialog.dart';

/// Instance capacity, the orchestrator's waiting queue and per-server load.
///
/// Capacity and queue are the same numbers `/orchestrator status` prints in game; server load
/// comes from the heartbeat each game server publishes every five seconds.
class CapacityScreen extends StatefulWidget {
  final ControlApi api;

  const CapacityScreen({super.key, required this.api});

  @override
  State<CapacityScreen> createState() => _CapacityScreenState();
}

class _CapacityScreenState extends State<CapacityScreen> {
  static const Duration _interval = Duration(seconds: 5);

  late final LiveApi _live = LiveApi(widget.api);
  CapacityReport? _capacity;
  MetricsReport? _metrics;
  FleetSnapshot? _fleet;
  String? _error;
  Timer? _timer;

  @override
  void initState() {
    super.initState();
    _load();
    _timer = Timer.periodic(_interval, (_) => _load());
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  Future<void> _load() async {
    try {
      final results = await Future.wait([
        _live.fetchCapacity(),
        _live.fetchMetrics(),
        widget.api.fetchFleet(),
      ]);
      if (!mounted) return;
      setState(() {
        _capacity = results[0] as CapacityReport;
        _metrics = results[1] as MetricsReport;
        _fleet = results[2] as FleetSnapshot;
        _error = null;
      });
    } catch (e) {
      if (mounted) setState(() => _error = e.toString());
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: ReactorTheme.panel,
        elevation: 0,
        shape: const Border(bottom: BorderSide(color: ReactorTheme.hairline)),
        title: Text('CAPACITY', style: ReactorTheme.heading(size: 13)),
        iconTheme: const IconThemeData(color: ReactorTheme.textSecondary, size: 18),
        actions: [
          IconButton(
            iconSize: 16,
            icon: const Icon(Icons.refresh, color: ReactorTheme.textSecondary),
            onPressed: _load,
          ),
        ],
      ),
      body: _body(),
    );
  }

  Widget _body() {
    final capacity = _capacity;
    final metrics = _metrics;
    if (capacity == null || metrics == null) {
      return Center(
        child: _error != null
            ? Text(_error!, style: ReactorTheme.data(size: 11, color: ReactorTheme.critical))
            : const SizedBox(
                width: 16,
                height: 16,
                child: CircularProgressIndicator(strokeWidth: 1.5, color: ReactorTheme.cyan)),
      );
    }

    return ListView(
      padding: const EdgeInsets.all(10),
      children: [
        if (_error != null)
          Padding(
            padding: const EdgeInsets.only(bottom: 8),
            child: Text('last refresh failed: $_error',
                style: ReactorTheme.data(size: 10.5, color: ReactorTheme.amber)),
          ),
        _capacityPanel(capacity),
        const SizedBox(height: 10),
        _queuePanel(capacity),
        const SizedBox(height: 10),
        _serversPanel(metrics),
      ],
    );
  }

  Widget _capacityPanel(CapacityReport capacity) {
    if (!capacity.orchestratorOnline) {
      return HudPanel(
        title: 'instances',
        borderColor: ReactorTheme.critical,
        child: Text('The orchestrator did not answer. Matchmaking is down or Redis is unreachable.',
            style: ReactorTheme.data(size: 11, color: ReactorTheme.critical)),
      );
    }
    return HudPanel(
      title: 'instances  ·  ${capacity.types.length} server types',
      child: Column(
        children: [
          _headerRow(const ['TYPE', 'SRV', 'RUNS', 'FREE', 'PLAYERS', 'QUEUE']),
          for (final type in capacity.types)
            _dataRow([
              _typeLabel(type.type),
              '${type.servers}',
              '${type.instances}',
              '${type.freeInstanceSlots}',
              '${type.onlinePlayers}/${type.maxPlayers}',
              '${type.queued}',
            ], highlight: type.queued > 0 ? ReactorTheme.amber : null),
        ],
      ),
    );
  }

  Widget _queuePanel(CapacityReport capacity) {
    final waiting = capacity.waiting;
    return HudPanel(
      title: 'queue  ·  ${waiting.length} waiting',
      borderColor: waiting.isEmpty ? ReactorTheme.hairline : ReactorTheme.amber,
      child: waiting.isEmpty
          ? Text('Nobody is waiting for an instance.',
              style: ReactorTheme.data(size: 11, color: ReactorTheme.textFaint))
          : Column(
              children: [
                _headerRow(const ['ACTIVITY', 'POS', 'MODE', 'PARTY', 'WAITED', 'FROM']),
                for (final ticket in waiting)
                  _dataRow([
                    _typeLabel(ticket.type),
                    '#${ticket.position}/${ticket.length}',
                    ticket.mode ?? '-',
                    ticket.players.isEmpty
                        ? '-'
                        : ticket.players.map((p) => p.substring(0, p.length < 8 ? p.length : 8)).join(' '),
                    ticket.waitedLabel,
                    _serverLabel(ticket.originServer),
                  ]),
              ],
            ),
    );
  }

  Widget _serversPanel(MetricsReport metrics) {
    final canOperate = _live.canOperate;
    return HudPanel(
      title: 'servers  ·  ${metrics.servers.length} reporting',
      child: metrics.servers.isEmpty
          ? Text('No game server has sent a heartbeat in the last 30 seconds.',
              style: ReactorTheme.data(size: 11, color: ReactorTheme.textFaint))
          : Column(
              children: [
                for (final server in metrics.servers) _serverRow(server, canOperate),
              ],
            ),
    );
  }

  Widget _serverRow(ServerMetric server, bool canOperate) {
    final colour = server.isFailing
        ? ReactorTheme.critical
        : server.isLoaded
            ? ReactorTheme.amber
            : ReactorTheme.cyan;
    final container = _containerFor(server);

    return Padding(
      padding: const EdgeInsets.only(bottom: 6),
      child: Row(
        children: [
          Container(width: 2, height: 30, color: colour),
          const SizedBox(width: 8),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('${server.serverName}  ·  ${_typeLabel(server.type)}',
                    overflow: TextOverflow.ellipsis,
                    style: ReactorTheme.data(size: 11.5, color: ReactorTheme.textPrimary)),
                Text(
                    'TPS ${server.tps.toStringAsFixed(1)}  ·  ${server.mspt.toStringAsFixed(1)} ms'
                    '  ·  heap ${server.heapUsedMb}/${server.heapMaxMb} MB'
                    '  ·  ${server.players}/${server.maxPlayers} online',
                    overflow: TextOverflow.ellipsis,
                    style: ReactorTheme.data(size: 10.5, color: colour)),
              ],
            ),
          ),
          if (canOperate && container != null)
            TextButton(
              onPressed: () => showDrainRestartDialog(context, _live, container.name),
              child: Text('DRAIN', style: ReactorTheme.label(size: 9.5, color: ReactorTheme.amber)),
            ),
        ],
      ),
    );
  }

  ContainerInfo? _containerFor(ServerMetric server) {
    final fleet = _fleet;
    if (fleet == null) return null;
    for (final container in fleet.containers) {
      if (server.belongsTo(container.id, container.name)) return container;
    }
    return null;
  }

  String _serverLabel(String? uuid) {
    if (uuid == null) return '-';
    for (final server in _metrics?.servers ?? const <ServerMetric>[]) {
      if (server.serverUuid == uuid) return server.shortName;
    }
    return uuid.length > 8 ? uuid.substring(0, 8) : uuid;
  }

  static String _typeLabel(String type) =>
      type.replaceFirst('SKYBLOCK_', '').replaceAll('_', ' ').toLowerCase();

  static const List<int> _flex = [4, 2, 2, 2, 3, 2];

  Widget _headerRow(List<String> labels) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 4),
      child: Row(
        children: [
          for (var i = 0; i < labels.length; i++)
            Expanded(
              flex: _flex[i],
              child: Text(labels[i],
                  overflow: TextOverflow.ellipsis,
                  style: ReactorTheme.label(size: 9, color: ReactorTheme.textFaint, spacing: 1.4)),
            ),
        ],
      ),
    );
  }

  Widget _dataRow(List<String> values, {Color? highlight}) {
    return SizedBox(
      height: 22,
      child: Row(
        children: [
          for (var i = 0; i < values.length; i++)
            Expanded(
              flex: _flex[i],
              child: Text(values[i],
                  overflow: TextOverflow.ellipsis,
                  style: ReactorTheme.data(
                      size: 11,
                      color: i == values.length - 1 && highlight != null
                          ? highlight
                          : i == 0
                              ? ReactorTheme.textPrimary
                              : ReactorTheme.textSecondary)),
            ),
        ],
      ),
    );
  }
}
