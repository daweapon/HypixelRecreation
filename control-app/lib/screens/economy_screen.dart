import 'package:flutter/material.dart';

import '../api/control_api.dart';
import '../theme/reactor_theme.dart';
import '../widgets/admin_kit.dart';
import '../widgets/chamfer.dart';

/// Read-only economy health: pending Bazaar deliveries, order and auction counts, the purse total
/// across every profile, and the Dark Auction.
///
/// Constructor: `EconomyScreen(api: controlApi)`.
class EconomyScreen extends StatefulWidget {
  final ControlApi api;

  const EconomyScreen({super.key, required this.api});

  @override
  State<EconomyScreen> createState() => _EconomyScreenState();
}

class _EconomyScreenState extends State<EconomyScreen> {
  EconomySnapshot? _snapshot;
  String? _error;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load({bool refresh = false}) async {
    try {
      final snapshot = await widget.api.fetchEconomy(refresh: refresh);
      if (mounted) {
        setState(() {
          _snapshot = snapshot;
          _error = null;
        });
      }
    } catch (e) {
      if (mounted) setState(() => _error = e.toString());
    }
  }

  @override
  Widget build(BuildContext context) {
    return AdminScaffold(title: 'ECONOMY', onRefresh: () => _load(refresh: true), body: _body());
  }

  Widget _body() {
    if (_error != null) return AdminMessage(_error!, colour: ReactorTheme.critical);
    final snapshot = _snapshot;
    if (snapshot == null) return const AdminSpinner();

    final dark = snapshot.darkAuction;
    return ListView(
      children: [
        HudPanel(
          title: 'markets',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              AdminField('bazaar orders', '${snapshot.bazaarOrders}'),
              AdminField('active auctions', '${snapshot.activeAuctions}'),
              AdminField('ended auctions', '${snapshot.endedAuctions}'),
              AdminField('purse total', '${adminCoins(snapshot.totalPurseCoins)} coins over ${snapshot.profilesScanned} profiles'),
              if (snapshot.coinsScannedAt != null)
                AdminField('scanned', adminStamp(snapshot.coinsScannedAt!), colour: ReactorTheme.textSecondary),
              AdminField(
                'dark auction',
                dark == null
                    ? 'service unreachable'
                    : dark.active
                        ? '${dark.phase ?? 'running'} · round ${dark.round} · ${dark.currentBid} coins'
                        : 'idle',
                colour: dark == null ? ReactorTheme.amber : ReactorTheme.textPrimary,
              ),
            ],
          ),
        ),
        const SizedBox(height: 10),
        HudPanel(
          title: '${snapshot.pendingTransactions} pending bazaar deliveries',
          borderColor: snapshot.pendingTransactions > 0 ? ReactorTheme.amber : ReactorTheme.hairline,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              for (final entry in snapshot.pendingByType.entries) AdminField(entry.key, '${entry.value}'),
              if (snapshot.oldestPending.isNotEmpty) ...[
                const SizedBox(height: 6),
                Text('OLDEST', style: ReactorTheme.label(size: 9, spacing: 1.2)),
                const SizedBox(height: 4),
                for (final pending in snapshot.oldestPending)
                  Padding(
                    padding: const EdgeInsets.only(bottom: 3),
                    child: Text(
                      '${pending.createdAt == null ? '?' : adminStamp(pending.createdAt!)}  ${pending.type}  ${pending.playerUuid}',
                      style: ReactorTheme.data(size: 10, color: ReactorTheme.textSecondary),
                    ),
                  ),
              ],
              if (snapshot.pendingTransactions == 0)
                Text('none waiting', style: ReactorTheme.data(size: 11, color: ReactorTheme.textFaint)),
            ],
          ),
        ),
      ],
    );
  }
}
