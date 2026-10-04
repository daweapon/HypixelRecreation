import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:hypixel_control/models/fleet.dart';
import 'package:hypixel_control/widgets/reactor_ring.dart';

List<DependencyStatus> _dependencies() => [
      for (final name in ['MONGO', 'REDIS', 'PROXY'])
        DependencyStatus(
          dependency: name,
          containerName: 'hypixel_${name.toLowerCase()}',
          state: 'UP',
          statusText: 'running',
          present: true,
        ),
    ];

Future<void> _pump(WidgetTester tester, double size) => tester.pumpWidget(MaterialApp(
      home: Scaffold(
        body: Center(
          child: ReactorRing(
            dependencies: _dependencies(),
            integrity: 100,
            severity: 'NOMINAL',
            size: size,
          ),
        ),
      ),
    ));

void main() {
  // The integrity figure scales with the ring and the annulus inside the arcs is narrower
  // than a two-line label, so labels painted on the arcs overlapped the number at every size
  // the console uses - 210 on a phone, 240 on the desktop.
  for (final size in [210.0, 240.0]) {
    testWidgets('at ${size.toInt()}px the dependency names clear the integrity figure',
        (tester) async {
      await _pump(tester, size);

      final numeral = tester.getRect(find.text('100'));
      for (final name in ['MONGO', 'REDIS', 'PROXY']) {
        final label = tester.getRect(find.text(name));
        expect(numeral.overlaps(label), isFalse,
            reason: '$name overlaps the integrity figure at ${size.toInt()}px');
      }
    });
  }

  testWidgets('every dependency is named in words, not colour alone', (tester) async {
    await _pump(tester, 210);

    for (final name in ['MONGO', 'REDIS', 'PROXY']) {
      expect(find.text(name), findsOneWidget);
    }
    expect(find.text('UP'), findsNWidgets(3));
  });
}
