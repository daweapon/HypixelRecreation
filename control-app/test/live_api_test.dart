import 'package:flutter_test/flutter_test.dart';
import 'package:hypixel_control/api/live_api.dart';

void main() {
  test('capacity report parses types and the waiting queue', () {
    final report = CapacityReport.fromJson({
      'takenAt': '2026-11-03T10:00:00Z',
      'orchestratorOnline': true,
      'types': [
        {
          'type': 'SKYBLOCK_KUUDRA',
          'servers': 2,
          'instances': 3,
          'freeInstanceSlots': 1,
          'onlinePlayers': 8,
          'maxPlayers': 40,
          'queued': 2,
        }
      ],
      'waiting': [
        {
          'type': 'SKYBLOCK_KUUDRA',
          'position': 2,
          'length': 2,
          'mode': 'HOT',
          'originServer': 'abc',
          'players': ['11111111-2222-3333-4444-555555555555'],
          'waitedMillis': 75000,
        }
      ],
    });

    expect(report.orchestratorOnline, isTrue);
    expect(report.totalQueued, 2);
    expect(report.types.single.instances, 3);
    expect(report.waiting.single.position, 2);
    expect(report.waiting.single.waitedLabel, '1:15');
  });

  test('a heartbeat matches its container by the id prefix Docker uses as hostname', () {
    final metrics = MetricsReport.fromJson({
      'takenAt': '2026-11-03T10:00:00Z',
      'servers': [
        {
          'serverName': 'mini1A',
          'host': '0123456789ab',
          'tps': 17.2,
          'mspt': 38,
          'heapUsedMb': 1800,
          'heapMaxMb': 4096,
          'players': 3,
          'maxPlayers': 20,
          'health': 'LOADED',
        }
      ],
      'summary': {'servers': 1, 'lowestTps': 17.2, 'worstHealth': 'LOADED'},
    });

    expect(metrics.forContainer('0123456789abcdef', 'hypixelcore_hub')?.serverName, 'mini1A');
    expect(metrics.forContainer('ffffffffffff0000', 'hypixelcore_island'), isNull);
    expect(metrics.servers.single.isLoaded, isTrue);
    expect(metrics.summary.lowestTps, 17.2);
    expect(metrics.summary.worstHealth, 'LOADED');
  });

  test('a drain job is finished only once DONE or FAILED', () {
    expect(DrainJob.fromJson({'phase': 'DRAINING'}).isFinished, isFalse);
    expect(DrainJob.fromJson({'phase': 'RESTARTING'}).isFinished, isFalse);
    expect(DrainJob.fromJson({'phase': 'DONE'}).isFinished, isTrue);
    expect(DrainJob.fromJson({'phase': 'FAILED'}).failed, isTrue);
  });
}
