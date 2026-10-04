import 'package:flutter/material.dart';

import '../theme/reactor_theme.dart';

/// Status as colour AND a short text code, never colour alone.
///
/// Faulted pills breathe; nominal ones are completely still.
class StatusPill extends StatefulWidget {
  final String code;
  final double width;

  const StatusPill({super.key, required this.code, this.width = 46});

  @override
  State<StatusPill> createState() => _StatusPillState();
}

class _StatusPillState extends State<StatusPill> with SingleTickerProviderStateMixin {
  AnimationController? _pulse;

  bool get _isFault => widget.code != 'UP';

  @override
  void initState() {
    super.initState();
    if (_isFault) {
      _pulse = AnimationController(vsync: this, duration: const Duration(milliseconds: 1500))
        ..repeat(reverse: true);
    }
  }

  @override
  void didUpdateWidget(covariant StatusPill oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (_isFault && _pulse == null) {
      _pulse = AnimationController(vsync: this, duration: const Duration(milliseconds: 1500))
        ..repeat(reverse: true);
    } else if (!_isFault && _pulse != null) {
      _pulse!.dispose();
      _pulse = null;
    }
  }

  @override
  void dispose() {
    _pulse?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final reduceMotion = MediaQuery.maybeOf(context)?.disableAnimations ?? false;
    final colour = ReactorTheme.forStatus(widget.code);

    Widget pill(double alpha) => Container(
          width: widget.width,
          padding: const EdgeInsets.symmetric(vertical: 2),
          decoration: BoxDecoration(
            color: colour.withValues(alpha: 0.10 * alpha),
            border: Border.all(color: colour.withValues(alpha: 0.55 * alpha)),
          ),
          child: Text(
            widget.code,
            textAlign: TextAlign.center,
            style: TextStyle(
              fontFamily: ReactorTheme.dataFamily,
              fontSize: 10,
              fontWeight: FontWeight.w600,
              color: colour,
              letterSpacing: 0.5,
            ),
          ),
        );

    if (_pulse == null || reduceMotion) return pill(1);

    return AnimatedBuilder(
      animation: _pulse!,
      builder: (context, _) => pill(0.6 + 0.4 * _pulse!.value),
    );
  }
}
