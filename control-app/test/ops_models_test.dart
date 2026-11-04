import 'package:flutter_test/flutter_test.dart';
import 'package:hypixel_control/models/ops.dart';

void main() {
  test('backup overview parses the agent payload', () {
    final overview = BackupOverview.fromJson({
      'directory': '/app/backups',
      'available': true,
      'schedule': 'daily@04:00',
      'timezone': 'UTC',
      'retain': 14,
      'nextRunAt': '2026-11-04T04:00:00Z',
      'running': null,
      'lastResult': null,
      'backups': [
        {
          'id': '20261103-040000-scheduled',
          'createdAt': '2026-11-03T04:00:00Z',
          'finishedAt': '2026-11-03T04:02:00Z',
          'trigger': 'SCHEDULED',
          'requestedBy': 'schedule',
          'status': 'PARTIAL',
          'parts': [
            {'kind': 'mongo', 'container': 'hypixel_mongo', 'bytes': 2048, 'ok': true, 'message': 'ok'},
            {'kind': 'redis', 'container': null, 'bytes': 0, 'ok': false, 'message': 'Redis container is not running'},
          ],
        }
      ],
      'unmanaged': [
        {'name': 'mongo-2026-10-01', 'directory': true, 'bytes': 4096, 'modifiedAt': '2026-10-01T00:00:00Z'}
      ],
    });
    expect(overview.backups.single.hasPart('mongo'), isTrue);
    expect(overview.backups.single.hasPart('redis'), isFalse);
    expect(overview.backups.single.totalBytes, 2048);
    expect(overview.unmanaged.single.directory, isTrue);
  });

  test('exception report parses groups and per-container counts', () {
    final report = ExceptionReport.fromJson({
      'report': {
        'scannedAt': '2026-11-03T10:00:00Z',
        'linesScanned': 4000,
        'totalEvents': 3,
        'perContainer': {'hypixelcore_hub': 2, 'hypixelcore_island': 1},
        'groups': [
          {
            'signature': 'java.lang.IllegalStateException @ dev.daweapon.Profile.load',
            'kind': 'EXCEPTION',
            'exceptionClass': 'java.lang.IllegalStateException',
            'frame': 'dev.daweapon.Profile.load',
            'count': 3,
            'perContainer': {'hypixelcore_hub': 2, 'hypixelcore_island': 1},
            'sample': ['java.lang.IllegalStateException: profile missing'],
          }
        ],
      },
      'tail': 2000,
      'includeNoise': false,
      'containersScanned': ['hypixelcore_hub', 'hypixelcore_island'],
      'errors': {},
    });
    expect(report.groups.single.headline, 'IllegalStateException');
    expect(report.groups.single.perContainer['hypixelcore_hub'], 2);
    expect(report.totalEvents, 3);
  });

  test('totp secret is grouped for typing', () {
    final enrolment = TotpEnrolment.fromJson(
        {'secret': 'JBSWY3DPEHPK3PXPJBSWY3DPEHPK3PXP', 'otpauthUri': 'otpauth://x', 'account': 'a'});
    expect(enrolment.groupedSecret, 'JBSW Y3DP EHPK 3PXP JBSW Y3DP EHPK 3PXP');
  });
}
