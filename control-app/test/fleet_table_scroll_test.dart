import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:hypixel_control/models/fleet.dart';
import 'package:hypixel_control/widgets/fleet_table.dart';

FleetSnapshot _snapshot(int containers) => FleetSnapshot.fromJson({
      'takenAt': DateTime.now().toIso8601String(),
      'dockerReachable': true,
      'dependencies': <Map<String, dynamic>>[],
      'containers': [
        for (var i = 0; i < containers; i++)
          {'id': 'id$i', 'name': 'service_$i', 'group': 'SERVICE', 'state': 'UP'},
      ],
      'integrity': 100,
      'diagnosis': {'severity': 'NOMINAL', 'headline': 'ok', 'detail': ''},
    });

Widget _wrap(Widget child) => MaterialApp(home: Scaffold(body: child));

ListView _innerList(WidgetTester tester) =>
    tester.widget<ListView>(find.descendant(
      of: find.byType(FleetTable),
      matching: find.byType(ListView),
    ));

void main() {
  testWidgets('nested in a page, the table yields the drag to the page', (tester) async {
    await tester.pumpWidget(_wrap(ListView(children: [
      FleetTable(
        fleet: _snapshot(30),
        scrollable: false,
        canOperate: false,
        onSelect: (_) {},
      ),
    ])));

    expect(_innerList(tester).physics, isA<NeverScrollableScrollPhysics>(),
        reason: 'a nested scrollable claims the drag even with nothing left to scroll, '
            'which strands the page');
  });

  testWidgets('given its own bounded space, the table scrolls itself', (tester) async {
    await tester.pumpWidget(_wrap(Column(children: [
      Expanded(
        child: FleetTable(
          fleet: _snapshot(30),
          canOperate: false,
          onSelect: (_) {},
        ),
      ),
    ])));

    expect(_innerList(tester).physics, isNot(isA<NeverScrollableScrollPhysics>()));
  });

  testWidgets('a page containing the table can scroll back up after scrolling down',
      (tester) async {
    await tester.pumpWidget(_wrap(ListView(children: [
      const SizedBox(height: 400, child: Placeholder()),
      FleetTable(
        fleet: _snapshot(40),
        scrollable: false,
        canOperate: false,
        onSelect: (_) {},
      ),
    ])));

    final page = find.byType(ListView).first;
    await tester.drag(page, const Offset(0, -600));
    await tester.pumpAndSettle();
    final scrolledTo = tester.widget<Scrollable>(find.byType(Scrollable).first);
    expect(scrolledTo.controller?.offset ?? tester.getTopLeft(find.byType(Placeholder)).dy,
        isNot(0));

    await tester.drag(page, const Offset(0, 900));
    await tester.pumpAndSettle();
    expect(tester.getTopLeft(find.byType(Placeholder)).dy, closeTo(0, 1),
        reason: 'the page must return to the top');
  });
}
