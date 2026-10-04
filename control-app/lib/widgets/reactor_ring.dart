import 'dart:math' as math;

import 'package:flutter/material.dart';

import '../models/fleet.dart';
import '../theme/reactor_theme.dart';

/// The defining element of the console.
///
/// The three shared dependencies are drawn as three arcs of one ring with a single integrity
/// figure at its centre, so "lead with shared-dependency health" is literally the largest
/// object on screen rather than three tiles among many.
///
/// Their names sit in a legend beneath the ring rather than on the arcs. The annulus inside
/// the arcs is narrower than a two-line label, and the integrity figure scales with the ring,
/// so arc labels collided with it at every size the console actually uses.
///
/// Motion rule: movement means attention is needed. A nominal ring is almost still - only the
/// slow radar sweep turns, to show the view is live rather than a frozen capture. Faulted arcs
/// breathe.
class ReactorRing extends StatefulWidget {
  final List<DependencyStatus> dependencies;
  final int integrity;
  final String severity;
  final double size;

  const ReactorRing({
    super.key,
    required this.dependencies,
    required this.integrity,
    required this.severity,
    this.size = 220,
  });

  @override
  State<ReactorRing> createState() => _ReactorRingState();
}

class _ReactorRingState extends State<ReactorRing> with TickerProviderStateMixin {
  late final AnimationController _sweep;
  late final AnimationController _breathe;

  @override
  void initState() {
    super.initState();
    // Liveness: a 6s sweep says the view is live.
    _sweep = AnimationController(vsync: this, duration: const Duration(seconds: 6))..repeat();
    // Attention: faulted arcs breathe.
    _breathe = AnimationController(vsync: this, duration: const Duration(milliseconds: 1600))
      ..repeat(reverse: true);
  }

  @override
  void dispose() {
    _sweep.dispose();
    _breathe.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final reduceMotion = MediaQuery.maybeOf(context)?.disableAnimations ?? false;

    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        _ring(reduceMotion),
        SizedBox(height: widget.size * 0.05),
        _legend(),
      ],
    );
  }

  Widget _ring(bool reduceMotion) {
    return SizedBox(
      width: widget.size,
      height: widget.size,
      child: AnimatedBuilder(
        animation: Listenable.merge([_sweep, _breathe]),
        builder: (context, _) {
          return CustomPaint(
            painter: _ReactorPainter(
              dependencies: widget.dependencies,
              integrity: widget.integrity,
              sweep: reduceMotion ? 0 : _sweep.value,
              breathe: reduceMotion ? 1 : _breathe.value,
              showSweep: !reduceMotion,
            ),
            child: Center(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text('${widget.integrity}',
                      style: TextStyle(
                        fontFamily: ReactorTheme.dataFamily,
                        fontSize: widget.size * 0.26,
                        fontWeight: FontWeight.w600,
                        height: 1,
                        color: ReactorTheme.forSeverity(widget.severity),
                      )),
                  const SizedBox(height: 2),
                  Text('INTEGRITY', style: ReactorTheme.label(size: 9, spacing: 2.4)),
                ],
              ),
            ),
          );
        },
      ),
    );
  }

  /// Status is always colour *and* text code, so the legend repeats the state in words.
  Widget _legend() {
    return Wrap(
      alignment: WrapAlignment.center,
      spacing: 14,
      runSpacing: 6,
      children: [
        for (final dependency in widget.dependencies)
          Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(dependency.displayName.toUpperCase(),
                  style: ReactorTheme.label(
                      size: 9, color: ReactorTheme.textSecondary, spacing: 1.2)),
              const SizedBox(width: 5),
              Text(dependency.state,
                  style: TextStyle(
                    fontFamily: ReactorTheme.dataFamily,
                    fontSize: 11,
                    fontWeight: FontWeight.w600,
                    color: ReactorTheme.forStatus(dependency.state),
                  )),
            ],
          ),
      ],
    );
  }
}

class _ReactorPainter extends CustomPainter {
  final List<DependencyStatus> dependencies;
  final int integrity;
  final double sweep;
  final double breathe;
  final bool showSweep;

  _ReactorPainter({
    required this.dependencies,
    required this.integrity,
    required this.sweep,
    required this.breathe,
    required this.showSweep,
  });

  static const double _gap = 0.12; // radians between arcs

  @override
  void paint(Canvas canvas, Size size) {
    final centre = Offset(size.width / 2, size.height / 2);
    final outer = size.shortestSide / 2;
    final arcRadius = outer - 16;

    _paintTicks(canvas, centre, outer);
    if (showSweep) _paintSweep(canvas, centre, outer);

    final count = dependencies.isEmpty ? 1 : dependencies.length;
    final segment = (2 * math.pi) / count;

    for (var i = 0; i < dependencies.length; i++) {
      final dependency = dependencies[i];
      final start = -math.pi / 2 + i * segment + _gap / 2;
      final extent = segment - _gap;
      final colour = ReactorTheme.forStatus(dependency.state);
      final faulted = !dependency.isNominal;

      // Track behind every arc, so a missing dependency still reads as a slot.
      canvas.drawArc(
        Rect.fromCircle(center: centre, radius: arcRadius),
        start,
        extent,
        false,
        Paint()
          ..color = ReactorTheme.hairline
          ..style = PaintingStyle.stroke
          ..strokeWidth = 10
          ..strokeCap = StrokeCap.butt,
      );

      if (faulted) {
        // Breathing glow: this is the attention signal.
        canvas.drawArc(
          Rect.fromCircle(center: centre, radius: arcRadius),
          start,
          extent,
          false,
          Paint()
            ..color = colour.withValues(alpha: 0.18 + 0.22 * breathe)
            ..style = PaintingStyle.stroke
            ..strokeWidth = 20
            ..strokeCap = StrokeCap.butt
            ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 6),
        );
      }

      canvas.drawArc(
        Rect.fromCircle(center: centre, radius: arcRadius),
        start,
        extent,
        false,
        Paint()
          ..color = faulted ? colour.withValues(alpha: 0.75 + 0.25 * breathe) : colour
          ..style = PaintingStyle.stroke
          ..strokeWidth = 10
          ..strokeCap = StrokeCap.butt,
      );

    }

    // Inner hairline circle.
    canvas.drawCircle(
      centre,
      arcRadius - 26,
      Paint()
        ..color = ReactorTheme.hairline
        ..style = PaintingStyle.stroke
        ..strokeWidth = 1,
    );
  }

  void _paintTicks(Canvas canvas, Offset centre, double outer) {
    final paint = Paint()..color = ReactorTheme.hairline;
    for (var i = 0; i < 60; i++) {
      final angle = (i / 60) * 2 * math.pi - math.pi / 2;
      final long = i % 5 == 0;
      final r1 = outer - (long ? 6 : 3);
      final p = Offset(centre.dx + r1 * math.cos(angle), centre.dy + r1 * math.sin(angle));
      canvas.drawRect(Rect.fromCenter(center: p, width: 1.5, height: long ? 4 : 2), paint);
    }
  }

  void _paintSweep(Canvas canvas, Offset centre, double outer) {
    final angle = sweep * 2 * math.pi - math.pi / 2;
    final shader = SweepGradient(
      startAngle: angle - 0.9,
      endAngle: angle,
      colors: [ReactorTheme.cyan.withValues(alpha: 0.0), ReactorTheme.cyan.withValues(alpha: 0.20)],
      transform: GradientRotation(0),
    ).createShader(Rect.fromCircle(center: centre, radius: outer));

    canvas.drawArc(
      Rect.fromCircle(center: centre, radius: outer - 3),
      angle - 0.9,
      0.9,
      false,
      Paint()
        ..shader = shader
        ..style = PaintingStyle.stroke
        ..strokeWidth = 5,
    );
  }

  @override
  bool shouldRepaint(covariant _ReactorPainter old) =>
      old.sweep != sweep ||
      old.breathe != breathe ||
      old.integrity != integrity ||
      old.dependencies != dependencies;
}
