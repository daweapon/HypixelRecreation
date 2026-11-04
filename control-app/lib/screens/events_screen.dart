import 'package:flutter/material.dart';

import '../api/control_api.dart';
import '../theme/reactor_theme.dart';
import '../widgets/admin_kit.dart';
import '../widgets/chamfer.dart';

/// SkyBlock calendar per server against the shared clock, the mayor and Dark Auction, and the
/// OWNER-only event triggers.
///
/// Constructor: `EventsScreen(api: controlApi)`.
class EventsScreen extends StatefulWidget {
  final ControlApi api;

  const EventsScreen({super.key, required this.api});

  @override
  State<EventsScreen> createState() => _EventsScreenState();
}

class _EventsScreenState extends State<EventsScreen> {
  EventsOverview? _overview;
  String? _error;
  bool _busy = false;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    try {
      final overview = await widget.api.fetchEvents();
      if (mounted) {
        setState(() {
          _overview = overview;
          _error = null;
        });
      }
    } catch (e) {
      if (mounted) setState(() => _error = e.toString());
    }
  }

  Future<void> _trigger(String kind, String title, {List<String>? choices, String? body}) async {
    String? value;
    if (choices != null) {
      if (choices.isEmpty) {
        adminNotify(context, 'no SkyBlock server reported the options', colour: ReactorTheme.amber);
        return;
      }
      value = await adminPick(context, title, choices);
      if (value == null || !mounted) return;
    }
    if (!await adminConfirm(context, title, body ?? (value == null ? 'Fire this trigger now?' : 'Run with $value?'),
        'FIRE')) {
      return;
    }

    setState(() => _busy = true);
    try {
      final detail = await widget.api.triggerEvent(kind, value: value);
      if (mounted) adminNotify(context, detail);
      await _load();
    } catch (e) {
      if (mounted) adminNotify(context, e.toString(), colour: ReactorTheme.critical);
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return AdminScaffold(title: 'EVENTS & CALENDAR', onRefresh: _load, body: _body());
  }

  Widget _body() {
    if (_error != null) return AdminMessage(_error!, colour: ReactorTheme.critical);
    final overview = _overview;
    if (overview == null) return const AdminSpinner();

    return ListView(
      children: [
        _calendar(overview.calendar),
        const SizedBox(height: 10),
        HudPanel(
          title: 'mayor and dark auction',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              AdminField('mayor', overview.currentMayor ?? 'unknown'),
              AdminField('minister', overview.currentMinister ?? 'none'),
              AdminField('election', overview.electionOpen ? 'open' : 'closed'),
              _darkAuction(overview.darkAuction),
            ],
          ),
        ),
        const SizedBox(height: 10),
        _triggers(overview),
      ],
    );
  }

  Widget _calendar(CalendarOverview calendar) {
    final colour = calendar.diverged ? ReactorTheme.critical : ReactorTheme.cyan;
    return HudPanel(
      title: 'skyblock calendar',
      borderColor: calendar.diverged ? ReactorTheme.critical : ReactorTheme.hairline,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          AdminField('shared clock', calendar.reference?.label ?? 'not set - servers run their own clocks',
              colour: calendar.sharedClock ? ReactorTheme.textPrimary : ReactorTheme.amber),
          AdminField('state',
              calendar.servers.isEmpty
                  ? 'no SkyBlock server answered'
                  : calendar.diverged
                      ? 'DIVERGED · ${calendar.spread} ticks apart'
                      : 'in sync · ${calendar.spread} ticks apart',
              colour: colour),
          const SizedBox(height: 6),
          for (final server in calendar.servers)
            Padding(
              padding: const EdgeInsets.only(bottom: 4),
              child: Row(
                children: [
                  Container(
                      width: 2,
                      height: 26,
                      color: server.offset.abs() > 200 ? ReactorTheme.critical : ReactorTheme.hairline),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(server.label, style: ReactorTheme.data(size: 12)),
                        Text(server.date.label,
                            style: ReactorTheme.data(size: 10, color: ReactorTheme.textSecondary)),
                      ],
                    ),
                  ),
                  Text(server.shared ? 'SHARED' : 'LOCAL',
                      style: ReactorTheme.label(
                          size: 9, color: server.shared ? ReactorTheme.cyan : ReactorTheme.amber, spacing: 1.2)),
                  const SizedBox(width: 8),
                  SizedBox(
                    width: 56,
                    child: Text('${server.offset >= 0 ? '+' : ''}${server.offset}',
                        textAlign: TextAlign.right,
                        style: ReactorTheme.data(size: 10, color: ReactorTheme.textFaint)),
                  ),
                ],
              ),
            ),
        ],
      ),
    );
  }

  Widget _darkAuction(DarkAuctionState? state) {
    if (state == null) return const AdminField('dark auction', 'service unreachable', colour: ReactorTheme.amber);
    if (!state.active) return const AdminField('dark auction', 'idle');
    return AdminField('dark auction',
        '${state.phase ?? 'running'} · round ${state.round} · ${state.currentItem ?? '?'} · '
        '${state.currentBid} coins${state.highestBidder == null ? '' : ' (${state.highestBidder})'}',
        colour: ReactorTheme.cyan);
  }

  Widget _triggers(EventsOverview overview) {
    if (!widget.api.isOwner) return const AdminMessage('event triggers need an OWNER device');
    if (_busy) return const AdminSpinner();
    return HudPanel(
      title: 'triggers',
      child: Wrap(
        spacing: 6,
        runSpacing: 6,
        children: [
          AdminChip('DARK AUCTION', onTap: () => _trigger('dark-auction', 'Start a Dark Auction',
              body: 'Starts a Dark Auction now, like /force darkauction.')),
          AdminChip('FORCE MAYOR', colour: ReactorTheme.amber,
              onTap: () => _trigger('force-mayor', 'Force-elect a mayor', choices: overview.mayors)),
          AdminChip('SPECIAL MAYOR', colour: ReactorTheme.amber,
              onTap: () => _trigger('special-mayor', 'Force-elect the next special mayor',
                  body: 'Installs the next special mayor in rotation on every server.')),
          AdminChip('MINING EVENT',
              onTap: () => _trigger('mining-event', 'Start a mining event', choices: overview.miningEvents)),
          AdminChip('SKIP CALENDAR', colour: ReactorTheme.amber,
              onTap: () => _trigger('calendar-skip', 'Skip to a calendar event', choices: overview.calendarEvents)),
        ],
      ),
    );
  }
}
