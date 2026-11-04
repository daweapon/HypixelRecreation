import 'package:flutter_test/flutter_test.dart';
import 'package:hypixel_control/api/control_api.dart';

void main() {
  test('player lookup reads the service.api payload', () {
    final lookup = PlayerLookup.fromJson({
      'player': {
        'uuid': 'u-1',
        'name': 'Swofty',
        'rank': 'STAFF',
        'staff': true,
        'onlineServerType': 'SKYBLOCK_HUB',
        'lastSeen': 1762000000000,
        'profiles': [
          {'id': 'p-1', 'name': 'Apple', 'selected': true, 'coins': 1250.5, 'coopMembers': 0},
          {'id': 'p-2', 'selected': false, 'coins': 0, 'coopId': 'c-1', 'coopMembers': 3},
        ],
        'punishments': [
          {'type': 'MUTE', 'id': 'X1', 'reason': 'Being rude', 'expiresAt': -1},
        ],
        'wipeSnapshotAt': 1762000000000,
        'restorableUntil': 1762003600000,
      },
      'options': {
        'ranks': ['STAFF', 'DEFAULT'],
        'banReasons': ['EXPLOITING'],
        'muteReasons': ['RUDE'],
      },
    });

    expect(lookup.name, 'Swofty');
    expect(lookup.isOnline, isTrue);
    expect(lookup.profiles, hasLength(2));
    expect(lookup.profiles.first.coins, 1250.5);
    expect(lookup.profiles.last.coopMembers, 3);
    expect(lookup.punishments.single.permanent, isTrue);
    expect(lookup.canRestore, isFalse);
    expect(lookup.options.ranks, ['STAFF', 'DEFAULT']);
  });

  test('events overview flags divergence and reads options', () {
    final overview = EventsOverview.fromJson({
      'calendar': {
        'reference': {'elapsed': 24000, 'year': 1, 'monthName': 'Early Spring', 'day': 2, 'hour': 0},
        'sharedClock': true,
        'servers': [
          {
            'serverId': 's-1',
            'serverType': 'SKYBLOCK_HUB',
            'serverName': 'mini1A',
            'date': {'elapsed': 0, 'year': 1, 'monthName': 'Early Spring', 'day': 1, 'hour': 0},
            'shared': false,
            'offset': -24000,
          },
        ],
        'divergence': {'spread': 24000, 'diverged': true, 'reporting': 1},
      },
      'mayor': {'currentMayor': 'DIANA', 'electionOpen': false},
      'options': {
        'mayors': ['DIANA'],
        'miningEvents': ['RAFFLE', 'END'],
        'calendarEvents': ['dark_auction'],
      },
    });

    expect(overview.calendar.diverged, isTrue);
    expect(overview.calendar.servers.single.label, 'mini1A');
    expect(overview.calendar.servers.single.date.label, 'Early Spring 1st, Year 1 · 00:00');
    expect(overview.calendar.reference!.label, 'Early Spring 2nd, Year 1 · 00:00');
    expect(overview.currentMayor, 'DIANA');
    expect(overview.darkAuction, isNull);
    expect(overview.miningEvents, contains('END'));
  });

  test('orphans, economy and roadmap payloads parse', () {
    final orphans = OrphanReport.fromJson({
      'scan': {
        'scannedAt': '2026-11-03T10:00:00Z',
        'profiles': 4,
        'orphans': [
          {'kind': 'ISLAND', 'id': 'i-1', 'reason': 'no profile or co-op points at this island'},
          {'kind': 'COOP', 'id': 'c-1', 'reason': 'points at 1 missing profile(s)', 'related': 'p-9'},
        ],
      },
      'counts': {'COOP': 1, 'ISLAND': 1, 'PROFILE': 0},
    });
    expect(orphans.orphans.first.deletable, isTrue);
    expect(orphans.orphans.last.deletable, isFalse);
    expect(orphans.counts['ISLAND'], 1);

    final economy = EconomySnapshot.fromJson({
      'pendingTransactions': 2,
      'pendingByType': {'SuccessfulBazaarTransaction': 2},
      'oldestPending': [
        {'id': 't', 'playerUuid': 'u', 'type': 'SuccessfulBazaarTransaction', 'createdAt': '2026-11-01T00:00:00Z'},
      ],
      'totalPurseCoins': 1.5e6,
      'darkAuction': {'auctionActive': true, 'phase': 'BIDDING', 'currentRound': 2, 'currentBid': 70000, 'success': true},
    });
    expect(economy.pendingByType['SuccessfulBazaarTransaction'], 2);
    expect(economy.darkAuction!.active, isTrue);
    expect(economy.totalPurseCoins, 1.5e6);

    final roadmap = RoadmapTab.fromJson({
      'tab': 'BUGS',
      'rows': [
        {'number': '7', 'task': 'Bug', 'status': 'Done'},
      ],
    });
    expect(roadmap.rows.single.isDone, isTrue);
  });
}
