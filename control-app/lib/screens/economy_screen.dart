import 'package:flutter/material.dart';

import '../api/control_api.dart';
import '../theme/reactor_theme.dart';
import '../widgets/admin_kit.dart';
import '../widgets/chamfer.dart';
import '../widgets/inflation_view.dart';

/// Read-only economy health: pending Bazaar deliveries, order and auction counts, the purse total
/// across every profile, the Dark Auction, and the inflation view (coin sources against sinks).
///
/// Constructor: `EconomyScreen(api: controlApi)`.
class EconomyScreen extends StatefulWidget {
  final ControlApi api;

  const EconomyScreen({super.key, required this.api});

  @override
  State<EconomyScreen> createState() => _EconomyScreenState();
}

class _EconomyScreenState extends State<EconomyScreen> {
  final GlobalKey<InflationViewState> _inflation = GlobalKey<InflationViewState>();
  EconomySnapshot? _snapshot;
  String? _error;
  bool _showInflation = false;

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
    return AdminScaffold(
      title: 'ECONOMY',
      onRefresh: () => _showInflation ? _inflation.currentState?.load(refresh: true) : _load(refresh: true),
      body: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Row(
            children: [
              _tab('OVERVIEW', !_showInflation, () => setState(() => _showInflation = false)),
              _tab('INFLATION', _showInflation, () => setState(() => _showInflation = true)),
            ],
          ),
          const SizedBox(height: 8),
          Expanded(child: _showInflation ? InflationView(key: _inflation, api: widget.api) : _body()),
        ],
      ),
    );
  }

  Widget _tab(String label, bool selected, VoidCallback onTap) {
    return Padding(
      padding: const EdgeInsets.only(right: 8),
      child: TextButton(
        onPressed: selected ? null : onTap,
        child: Text(label,
            style: ReactorTheme.label(size: 10, spacing: 1.2, color: selected ? ReactorTheme.cyan : ReactorTheme.textSecondary)),
      ),
    );
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
