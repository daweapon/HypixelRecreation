import 'package:flutter/material.dart';

import '../api/integrity_api.dart';
import '../theme/reactor_theme.dart';
import 'chamfer.dart';

/// Memory budget: what running containers may claim against host RAM less the NAS reserve.
class CapacityPanel extends StatelessWidget {
  final CapacityReport? report;
  final bool showEntries;

  const CapacityPanel({super.key, required this.report, this.showEntries = false});

  @override
  Widget build(BuildContext context) {
    final r = report;
    if (r == null) {
      return HudPanel(
        title: 'memory budget',
        child: Text('waiting for the agent', style: ReactorTheme.data(size: 11, color: ReactorTheme.textFaint)),
      );
    }

    final fraction = r.committedFraction.clamp(0.0, 1.0);
    final colour = r.overBudget || r.committedFraction > 1
        ? ReactorTheme.critical
        : r.committedFraction > 0.85
            ? ReactorTheme.amber
            : ReactorTheme.cyan;

    return HudPanel(
      title: 'memory budget',
      borderColor: colour == ReactorTheme.cyan ? ReactorTheme.hairline : colour.withValues(alpha: 0.5),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        mainAxisSize: MainAxisSize.min,
        children: [
          Row(
            children: [
              Text(CapacityReport.gib(r.committedBytes),
                  style: ReactorTheme.data(size: 15, color: colour, weight: FontWeight.w600)),
              Text('  /  ${CapacityReport.gib(r.budgetBytes)}',
                  style: ReactorTheme.data(size: 11, color: ReactorTheme.textSecondary)),
              const Spacer(),
              Text('host ${CapacityReport.gib(r.hostBytes)}',
                  style: ReactorTheme.data(size: 10, color: ReactorTheme.textFaint)),
            ],
          ),
          const SizedBox(height: 6),
          SizedBox(
            height: 6,
            child: Stack(
              children: [
                Container(color: ReactorTheme.panelRaised),
                FractionallySizedBox(
                  alignment: Alignment.centerLeft,
                  widthFactor: fraction,
                  child: Container(color: colour.withValues(alpha: 0.7)),
                ),
              ],
            ),
          ),
          const SizedBox(height: 6),
          Text(
            'budget = host RAM less ${CapacityReport.gib(r.reserveBytes)} for the NAS'
            '${r.estimatedCount > 0 ? '  ·  ${r.estimatedCount} without mem_limit, estimated' : ''}',
            style: ReactorTheme.data(size: 10, color: ReactorTheme.textFaint),
          ),
          if (showEntries) ...[
            const SizedBox(height: 8),
            for (final entry in r.entries.where((e) => e.running || e.candidate))
              Row(
                children: [
                  Expanded(
                    child: Text(entry.name,
                        overflow: TextOverflow.ellipsis,
                        style: ReactorTheme.data(size: 10.5, color: ReactorTheme.textSecondary)),
                  ),
                  Text('${entry.estimated ? '~' : ''}${CapacityReport.gib(entry.bytes)}',
                      style: ReactorTheme.data(
                          size: 10.5,
                          color: entry.estimated ? ReactorTheme.textFaint : ReactorTheme.textSecondary)),
                ],
              ),
          ],
        ],
      ),
    );
  }
}
