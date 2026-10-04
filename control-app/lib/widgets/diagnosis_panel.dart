import 'package:flutter/material.dart';

import '../models/fleet.dart';
import '../theme/reactor_theme.dart';
import 'chamfer.dart';

/// Plain-language reading of what is wrong and what to do about it.
///
/// This is the panel that turns "13 containers are red" into "Mongo is down, that is why
/// everything else is looping, start it first".
class DiagnosisPanel extends StatelessWidget {
  final Diagnosis diagnosis;
  final bool compact;

  const DiagnosisPanel({super.key, required this.diagnosis, this.compact = false});

  @override
  Widget build(BuildContext context) {
    final colour = ReactorTheme.forSeverity(diagnosis.severity);

    return HudPanel(
      borderColor: diagnosis.severity == 'NOMINAL'
          ? ReactorTheme.hairline
          : colour.withValues(alpha: 0.45),
      title: 'diagnosis',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(width: 3, height: compact ? 16 : 20, color: colour),
              const SizedBox(width: 8),
              Expanded(
                child: Text(
                  diagnosis.headline,
                  style: ReactorTheme.heading(size: compact ? 14 : 16, color: colour),
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Text(
            diagnosis.detail,
            style: ReactorTheme.data(size: 11.5, color: ReactorTheme.textSecondary),
            maxLines: compact ? 4 : 8,
            overflow: TextOverflow.ellipsis,
          ),
        ],
      ),
    );
  }
}
