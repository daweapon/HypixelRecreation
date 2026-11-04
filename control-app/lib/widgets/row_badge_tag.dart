import 'package:flutter/material.dart';

import '../api/integrity_api.dart';
import '../theme/reactor_theme.dart';

/// A compact integrity tag on a fleet row: colour plus text, never colour alone.
class RowBadgeTag extends StatelessWidget {
  final RowBadge badge;

  const RowBadgeTag({super.key, required this.badge});

  static Color colourFor(String code) {
    switch (code) {
      case 'DELISTED':
      case 'BAD':
        return ReactorTheme.critical;
      case 'SKEW':
      case 'ERR':
        return ReactorTheme.amber;
      case 'REG':
      case 'OK':
        return ReactorTheme.cyan;
      default:
        return ReactorTheme.textFaint;
    }
  }

  @override
  Widget build(BuildContext context) {
    final colour = colourFor(badge.code);
    final quiet = badge.code == 'REG' || badge.code == 'OK';
    return Tooltip(
      message: badge.tooltip,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 1),
        decoration: BoxDecoration(
          color: quiet ? Colors.transparent : colour.withValues(alpha: 0.10),
          border: Border.all(color: colour.withValues(alpha: quiet ? 0.25 : 0.55)),
        ),
        child: Text(
          badge.code,
          style: TextStyle(
            fontFamily: ReactorTheme.dataFamily,
            fontSize: 9,
            fontWeight: FontWeight.w600,
            color: quiet ? colour.withValues(alpha: 0.6) : colour,
            letterSpacing: 0.4,
          ),
        ),
      ),
    );
  }
}
