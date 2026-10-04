import 'package:flutter/material.dart';

import '../theme/reactor_theme.dart';

/// Clips to a chamfered rectangle - corners cut at 45 degrees rather than rounded.
class ChamferBorder extends ShapeBorder {
  final double cut;
  final Color color;
  final double width;

  const ChamferBorder({this.cut = 8, this.color = ReactorTheme.hairline, this.width = 1});

  @override
  EdgeInsetsGeometry get dimensions => EdgeInsets.all(width);

  @override
  Path getInnerPath(Rect rect, {TextDirection? textDirection}) =>
      _path(rect.deflate(width), cut);

  @override
  Path getOuterPath(Rect rect, {TextDirection? textDirection}) => _path(rect, cut);

  static Path _path(Rect r, double cut) {
    final c = cut.clamp(0.0, r.shortestSide / 2);
    return Path()
      ..moveTo(r.left + c, r.top)
      ..lineTo(r.right - c, r.top)
      ..lineTo(r.right, r.top + c)
      ..lineTo(r.right, r.bottom - c)
      ..lineTo(r.right - c, r.bottom)
      ..lineTo(r.left + c, r.bottom)
      ..lineTo(r.left, r.bottom - c)
      ..lineTo(r.left, r.top + c)
      ..close();
  }

  @override
  void paint(Canvas canvas, Rect rect, {TextDirection? textDirection}) {
    final paint = Paint()
      ..color = color
      ..style = PaintingStyle.stroke
      ..strokeWidth = width;
    canvas.drawPath(_path(rect.deflate(width / 2), cut), paint);
  }

  @override
  ShapeBorder scale(double t) => ChamferBorder(cut: cut * t, color: color, width: width * t);
}

/// Standard panel: chamfered, hairline-bordered, optional small-caps title with tick marks.
class HudPanel extends StatelessWidget {
  final String? title;
  final Widget child;
  final EdgeInsets padding;
  final Color borderColor;
  final Color background;
  final List<Widget> actions;

  const HudPanel({
    super.key,
    this.title,
    required this.child,
    this.padding = const EdgeInsets.all(14),
    this.borderColor = ReactorTheme.hairline,
    this.background = ReactorTheme.panel,
    this.actions = const [],
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: ShapeDecoration(
        color: background,
        shape: ChamferBorder(color: borderColor),
      ),
      padding: padding,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        mainAxisSize: MainAxisSize.min,
        children: [
          if (title != null) ...[
            Row(
              children: [
                const _TickMark(),
                const SizedBox(width: 6),
                Expanded(
                  child: Text(title!.toUpperCase(),
                      style: ReactorTheme.label(size: 10, spacing: 1.8)),
                ),
                ...actions,
              ],
            ),
            const SizedBox(height: 10),
          ],
          Flexible(child: child),
        ],
      ),
    );
  }
}

class _TickMark extends StatelessWidget {
  const _TickMark();

  @override
  Widget build(BuildContext context) =>
      Container(width: 5, height: 5, color: ReactorTheme.cyan);
}
