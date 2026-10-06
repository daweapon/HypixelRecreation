import 'package:flutter/material.dart';

import '../models/fleet.dart';
import '../theme/reactor_theme.dart';
import 'chamfer.dart';

/// Progress of a running staggered operation. Staggered starts take minutes, so the operator
/// needs to see which step is in flight rather than a spinner.
class JobBanner extends StatelessWidget {
  final OperationJob job;
  final VoidCallback onDismiss;

  const JobBanner({super.key, required this.job, required this.onDismiss});

  @override
  Widget build(BuildContext context) {
    final failed = job.steps.where((s) => !s.ok).length;
    // REJECTED means nothing was attempted - the operator pressed twice. Worth showing, but
    // it is not an outage and should not read like one.
    final colour = job.status == 'REJECTED'
        ? ReactorTheme.textSecondary
        : job.status == 'FAILED' || failed > 0
            ? ReactorTheme.amber
            : ReactorTheme.cyan;

    return HudPanel(
      borderColor: colour.withValues(alpha: 0.45),
      title: 'operation  ·  ${job.operation}',
      actions: [
        if (!job.isRunning)
          GestureDetector(
            onTap: onDismiss,
            child: Text('DISMISS',
                style: ReactorTheme.label(size: 9, color: ReactorTheme.textSecondary)),
          ),
      ],
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          Row(
            children: [
              if (job.isRunning)
                const SizedBox(
                  width: 10,
                  height: 10,
                  child: CircularProgressIndicator(strokeWidth: 1.5, color: ReactorTheme.cyan),
                ),
              if (job.isRunning) const SizedBox(width: 8),
              Expanded(
                child: Text(
                  job.isRunning ? job.currentStep : '${job.status}  ·  ${job.steps.length} steps',
                  style: ReactorTheme.data(size: 12, color: colour),
                ),
              ),
            ],
          ),
          if (job.steps.isNotEmpty) ...[
            const SizedBox(height: 8),
            ConstrainedBox(
              constraints: const BoxConstraints(maxHeight: 120),
              child: ListView(
                shrinkWrap: true,
                reverse: true,
                children: [
                  for (final step in job.steps.reversed)
                    Padding(
                      padding: const EdgeInsets.only(bottom: 2),
                      child: Row(
                        children: [
                          Text(step.ok ? '✓' : '✗',
                              style: ReactorTheme.data(
                                  size: 11,
                                  color: step.ok ? ReactorTheme.cyan : ReactorTheme.critical)),
                          const SizedBox(width: 8),
                          Expanded(
                            child: Text('${step.container}  ${step.action}  ${step.message}',
                                overflow: TextOverflow.ellipsis,
                                style: ReactorTheme.data(
                                    size: 10.5, color: ReactorTheme.textFaint)),
                          ),
                        ],
                      ),
                    ),
                ],
              ),
            ),
          ],
        ],
      ),
    );
  }
}
