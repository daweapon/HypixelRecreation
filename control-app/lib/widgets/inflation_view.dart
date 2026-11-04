import 'package:flutter/material.dart';

import '../api/control_api.dart';
import '../theme/reactor_theme.dart';
import 'admin_kit.dart';
import 'chamfer.dart';

/// Coins created (sources) against coins destroyed (sinks), per day and per category, plus the
/// player-to-player channels and staff tools that move coins without counting as inflation.
class InflationView extends StatefulWidget {
  final ControlApi api;

  const InflationView({super.key, required this.api});

  @override
  State<InflationView> createState() => InflationViewState();
}

class InflationViewState extends State<InflationView> {
  static const _windows = [7, 30, 90];

  int _days = 30;
  InflationReport? _report;
  String? _error;

  @override
  void initState() {
    super.initState();
    load();
  }

  Future<void> load({bool refresh = false}) async {
    try {
      final report = await widget.api.fetchInflation(days: _days, refresh: refresh);
      if (mounted) {
        setState(() {
          _report = report;
          _error = null;
        });
      }
    } catch (e) {
      if (mounted) setState(() => _error = e.toString());
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_error != null) return AdminMessage(_error!, colour: ReactorTheme.critical);
    final report = _report;
    if (report == null) return const AdminSpinner();

    return ListView(
      children: [
        _windowPicker(),
        const SizedBox(height: 10),
        _summary(report),
        const SizedBox(height: 10),
        _dailyChart(report),
        const SizedBox(height: 10),
        _lines('coin sources', report.sources, ReactorTheme.amber, 'nothing minted in this window'),
        const SizedBox(height: 10),
        _lines('coin sinks', report.sinks, ReactorTheme.cyan, 'nothing destroyed in this window'),
        const SizedBox(height: 10),
        _transfers(report),
        if (report.staff.isNotEmpty) ...[
          const SizedBox(height: 10),
          _lines('staff tools (not counted)', report.staff, ReactorTheme.textSecondary, ''),
        ],
      ],
    );
  }

  Widget _windowPicker() {
    return Row(
      children: [
        for (final days in _windows)
          Padding(
            padding: const EdgeInsets.only(right: 6),
            child: TextButton(
              onPressed: days == _days
                  ? null
                  : () {
                      setState(() {
                        _days = days;
                        _report = null;
                      });
                      load();
                    },
              child: Text('${days}D',
                  style: ReactorTheme.label(
                      size: 10, color: days == _days ? ReactorTheme.cyan : ReactorTheme.textSecondary)),
            ),
          ),
      ],
    );
  }

  Widget _summary(InflationReport report) {
    final inflating = report.net > 0;
    return HudPanel(
      title: 'inflation · last ${report.days} days',
      borderColor: inflating ? ReactorTheme.amber : ReactorTheme.hairline,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          AdminField('coins created', adminCoins(report.minted), colour: ReactorTheme.amber),
          AdminField('coins destroyed', adminCoins(report.burned), colour: ReactorTheme.cyan),
          AdminField('net change', _signed(report.net),
              colour: inflating ? ReactorTheme.amber : ReactorTheme.textPrimary),
          AdminField('average per day', _signed(report.averageDailyNet)),
          AdminField('sinks cover', '${(report.sinkToSourceRatio * 100).toStringAsFixed(1)}% of sources'),
          if (report.netAsPercentOfSupply != null)
            AdminField('net vs money supply', '${_percent(report.netAsPercentOfSupply!)} of the supply'),
          AdminField('money supply', report.supplyNow == null ? 'not measured yet' : '${adminCoins(report.supplyNow!)} coins (purses + banks)'),
          if (report.supplyChangePercent != null)
            AdminField('supply change', '${_percent(report.supplyChangePercent!)} since ${adminCoins(report.supplyAtStart!)}'),
          if (report.staffNet != 0)
            AdminField('staff tools', '${_signed(report.staffNet)} (left out of the totals above)',
                colour: ReactorTheme.textSecondary),
        ],
      ),
    );
  }

  Widget _dailyChart(InflationReport report) {
    final biggest = report.daily.fold<double>(0, (max, day) => day.net.abs() > max ? day.net.abs() : max);
    return HudPanel(
      title: 'net coins per day',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          for (final day in report.daily.reversed)
            Padding(
              padding: const EdgeInsets.only(bottom: 3),
              child: Row(
                children: [
                  SizedBox(
                    width: 52,
                    child: Text(day.date.length >= 10 ? day.date.substring(5) : day.date,
                        style: ReactorTheme.data(size: 10, color: ReactorTheme.textSecondary)),
                  ),
                  Expanded(
                    child: LayoutBuilder(
                      builder: (context, constraints) {
                        final width = biggest == 0 ? 0.0 : constraints.maxWidth * day.net.abs() / biggest;
                        return Align(
                          alignment: Alignment.centerLeft,
                          child: Container(
                            height: 8,
                            width: width,
                            color: day.net >= 0 ? ReactorTheme.amber : ReactorTheme.cyan,
                          ),
                        );
                      },
                    ),
                  ),
                  SizedBox(
                    width: 84,
                    child: Text(_signed(day.net),
                        textAlign: TextAlign.right, style: ReactorTheme.data(size: 10)),
                  ),
                ],
              ),
            ),
          const SizedBox(height: 4),
          Text('amber = more coins created than destroyed', style: ReactorTheme.data(size: 9, color: ReactorTheme.textFaint)),
        ],
      ),
    );
  }

  Widget _lines(String title, List<FlowLine> lines, Color colour, String empty) {
    return HudPanel(
      title: title,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          if (lines.isEmpty && empty.isNotEmpty)
            Text(empty, style: ReactorTheme.data(size: 11, color: ReactorTheme.textFaint)),
          for (final line in lines)
            AdminField(line.category,
                '${adminCoins(line.amount.abs())}${line.share > 0 ? ' · ${line.share.toStringAsFixed(1)}%' : ''} · ${line.count}x',
                colour: colour),
        ],
      ),
    );
  }

  Widget _transfers(InflationReport report) {
    return HudPanel(
      title: 'player-to-player channels',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          if (report.transfers.isEmpty)
            Text('no transfers in this window', style: ReactorTheme.data(size: 11, color: ReactorTheme.textFaint)),
          for (final channel in report.transfers)
            AdminField(channel.channel,
                'in ${adminCoins(channel.received)} · out ${adminCoins(channel.paid)} · net ${_signed(channel.net)}'),
          const SizedBox(height: 4),
          Text('these move coins between players; a negative net is tax or coins still held in escrow',
              style: ReactorTheme.data(size: 9, color: ReactorTheme.textFaint)),
        ],
      ),
    );
  }

  String _signed(double value) => '${value < 0 ? '-' : '+'}${adminCoins(value.abs())}';

  String _percent(double value) => '${value >= 0 ? '+' : ''}${value.toStringAsFixed(2)}%';
}
