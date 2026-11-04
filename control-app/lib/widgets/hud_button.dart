import 'package:flutter/material.dart';

import '../theme/reactor_theme.dart';
import 'chamfer.dart';

/// Chamfered action button in the console style; greyed out and inert when [onTap] is null.
class HudButton extends StatelessWidget {
  final String label;
  final VoidCallback? onTap;
  final Color colour;
  final bool busy;

  const HudButton({
    super.key,
    required this.label,
    required this.onTap,
    this.colour = ReactorTheme.cyan,
    this.busy = false,
  });

  @override
  Widget build(BuildContext context) {
    final enabled = onTap != null && !busy;
    return GestureDetector(
      onTap: enabled ? onTap : null,
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 9, horizontal: 14),
        decoration: ShapeDecoration(
          color: enabled ? colour.withValues(alpha: 0.10) : ReactorTheme.panelRaised,
          shape: ChamferBorder(
            cut: 5,
            color: enabled ? colour.withValues(alpha: 0.55) : ReactorTheme.hairline,
          ),
        ),
        child: Center(
          widthFactor: 1,
          child: busy
              ? SizedBox(
                  width: 12,
                  height: 12,
                  child: CircularProgressIndicator(strokeWidth: 1.5, color: colour))
              : Text(label,
                  style: ReactorTheme.label(
                      size: 10, spacing: 1.6, color: enabled ? colour : ReactorTheme.textFaint)),
        ),
      ),
    );
  }
}

/// The console's app bar, shared by the operations screens.
PreferredSizeWidget hudAppBar(String title, {List<Widget> actions = const []}) => AppBar(
      backgroundColor: ReactorTheme.panel,
      elevation: 0,
      shape: const Border(bottom: BorderSide(color: ReactorTheme.hairline)),
      title: Text(title, style: ReactorTheme.heading(size: 13)),
      iconTheme: const IconThemeData(color: ReactorTheme.textSecondary, size: 18),
      actions: actions,
    );

Widget hudSpinner() => const Center(
    child: SizedBox(
        width: 16,
        height: 16,
        child: CircularProgressIndicator(strokeWidth: 1.5, color: ReactorTheme.cyan)));

Widget hudKeyValue(String key, String value, {Color? colour}) => Padding(
      padding: const EdgeInsets.only(bottom: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
              width: 96,
              child: Text(key.toUpperCase(),
                  style: ReactorTheme.label(size: 9, color: ReactorTheme.textFaint))),
          Expanded(
              child: Text(value,
                  style: ReactorTheme.data(size: 11, color: colour ?? ReactorTheme.textPrimary))),
        ],
      ),
    );
