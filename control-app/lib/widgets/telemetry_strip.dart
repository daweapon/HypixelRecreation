import 'package:flutter/material.dart';

import '../models/fleet.dart';
import '../theme/reactor_theme.dart';
import 'chamfer.dart';

/// Compact row of headline figures.
class TelemetryStrip extends StatelessWidget {
  final FleetSnapshot fleet;

  const TelemetryStrip({super.key, required this.fleet});

  @override
  Widget build(BuildContext context) {
    final gameServers = fleet.inGroup('GAME_SERVER');
    final services = fleet.inGroup('SERVICE');
    final looping = fleet.containers.where((c) => c.state == 'LOOP').length;

    return HudPanel(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
      child: Row(
        children: [
          _Metric(
            label: 'WORLDS UP',
            value: '${gameServers.where((c) => c.isNominal).length}/${gameServers.length}',
            colour: gameServers.any((c) => !c.isNominal)
                ? ReactorTheme.amber
                : ReactorTheme.cyan,
          ),
          _Metric(
            label: 'SERVICES UP',
            value: '${services.where((c) => c.isNominal).length}/${services.length}',
            colour: services.any((c) => !c.isNominal)
                ? ReactorTheme.amber
                : ReactorTheme.cyan,
          ),
          _Metric(
            label: 'LOOPING',
            value: '$looping',
            colour: looping > 0 ? ReactorTheme.critical : ReactorTheme.textFaint,
          ),
          _Metric(
            label: 'SNAPSHOT',
            value: _clock(fleet.takenAt),
            colour: ReactorTheme.textSecondary,
          ),
        ],
      ),
    );
  }

  static String _clock(DateTime t) {
    final local = t.toLocal();
    String two(int v) => v.toString().padLeft(2, '0');
    return '${two(local.hour)}:${two(local.minute)}:${two(local.second)}';
  }
}

class _Metric extends StatelessWidget {
  final String label;
  final String value;
  final Color colour;

  const _Metric({required this.label, required this.value, required this.colour});

  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(label, style: ReactorTheme.label(size: 9, spacing: 1.8)),
          const SizedBox(height: 3),
          Text(value,
              style: ReactorTheme.data(size: 17, color: colour, weight: FontWeight.w600)),
        ],
      ),
    );
  }
}
