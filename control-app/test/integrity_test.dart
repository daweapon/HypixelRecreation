import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:hypixel_control/api/integrity_api.dart';
import 'package:hypixel_control/models/fleet.dart';
import 'package:hypixel_control/widgets/capacity_panel.dart';
import 'package:hypixel_control/widgets/fleet_table.dart';

const _gib = 1024 * 1024 * 1024;

SkewReport _skew() => SkewReport.fromJson({
      'jarStore': '/app/jar-store',
      'storeAvailable': true,
      'skewed': 1,
      'entries': [
        {'container': 'hypixelcore_hub', 'jar': 'HypixelCore.jar', 'state': 'MATCH', 'reason': 'ok'},
        {
          'container': 'hypixelcore_island',
          'jar': 'HypixelCore.jar',
          'state': 'SKEW',
          'containerSha256': 'b' * 64,
          'referenceSha256': 'a' * 64,
          'reason': 'differs from the jar store copy of HypixelCore.jar',
        },
      ],
    });

RegistrationReport _registrations() => RegistrationReport.fromJson({
      'proxyReachable': true,
      'severity': 'CRITICAL',
      'headline': '1 game server(s) running but not registered with the proxy',
      'detail': '',
      'rows': [
        {'container': 'hypixelcore_hub', 'containerState': 'UP', 'state': 'DELISTED', 'players': 0, 'detail': 'gone'},
        {'container': 'hypixelcore_island', 'containerState': 'UP', 'state': 'REGISTERED', 'proxyName': 'mini1A', 'players': 2, 'detail': 'ok'},
      ],
      'unmatched': [],
      'delisted': ['hypixelcore_hub'],
    });

FleetSnapshot _fleet() => FleetSnapshot.fromJson({
      'takenAt': DateTime.now().toIso8601String(),
      'dockerReachable': true,
      'dependencies': <Map<String, dynamic>>[],
      'containers': [
        {'id': '1', 'name': 'hypixelcore_hub', 'group': 'GAME_SERVER', 'state': 'UP'},
        {'id': '2', 'name': 'hypixelcore_island', 'group': 'GAME_SERVER', 'state': 'UP'},
      ],
      'integrity': 100,
      'diagnosis': {'severity': 'NOMINAL', 'headline': 'ok', 'detail': ''},
    });

void main() {
  test('badges carry DELISTED and SKEW to the right rows', () {
    final badges = buildRowBadges(_skew(), _registrations());
    expect(badges['hypixelcore_hub']!.map((b) => b.code), ['DELISTED']);
    expect(badges['hypixelcore_island']!.map((b) => b.code), ['REG', 'SKEW']);
  });

  test('a background report still being built parses with no report', () {
    final view = Background.fromJson({'refreshing': true, 'computedAt': null, 'report': null},
        SkewReport.fromJson);
    expect(view.refreshing, isTrue);
    expect(view.report, isNull);
  });

  testWidgets('the fleet table shows integrity tags beside the state', (tester) async {
    await tester.pumpWidget(MaterialApp(
      home: Scaffold(
        body: FleetTable(
          fleet: _fleet(),
          canOperate: false,
          onSelect: (_) {},
          badges: buildRowBadges(_skew(), _registrations()),
        ),
      ),
    ));

    expect(find.text('DELISTED'), findsOneWidget);
    expect(find.text('SKEW'), findsOneWidget);
  });

  testWidgets('the capacity panel shows committed against budget', (tester) async {
    final report = CapacityReport.fromJson({
      'hostBytes': 32 * _gib,
      'reserveBytes': 4 * _gib,
      'budgetBytes': 28 * _gib,
      'committedBytes': 20 * _gib,
      'candidateBytes': 0,
      'projectedBytes': 20 * _gib,
      'overBudget': false,
      'estimatedCount': 2,
      'entries': [],
      'summary': '20.0 GiB of 28.0 GiB committed',
    });
    await tester.pumpWidget(MaterialApp(home: Scaffold(body: CapacityPanel(report: report))));

    expect(find.text('20.0 GiB'), findsOneWidget);
    expect(find.textContaining('28.0 GiB'), findsOneWidget);
  });
}
