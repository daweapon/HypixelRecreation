import 'package:flutter/material.dart';

import '../api/control_api.dart';
import '../api/ops_api.dart';
import '../models/ops.dart';
import '../theme/reactor_theme.dart';
import '../widgets/chamfer.dart';
import '../widgets/hud_button.dart';

/// Exceptions and ERROR lines from every container's recent logs, grouped by signature
/// (exception class + first project stack frame). Read-only; any paired device can view it.
///
/// Constructor: `ExceptionsScreen(api: ControlApi)`.
class ExceptionsScreen extends StatefulWidget {
  final ControlApi api;

  const ExceptionsScreen({super.key, required this.api});

  @override
  State<ExceptionsScreen> createState() => _ExceptionsScreenState();
}

class _ExceptionsScreenState extends State<ExceptionsScreen> {
  late final OpsApi _ops = OpsApi(widget.api);
  ExceptionReport? _report;
  String? _error;
  bool _loading = false;
  bool _includeNoise = false;
  final Set<String> _expanded = {};

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load({bool refresh = false}) async {
    setState(() => _loading = true);
    try {
      final report = await _ops.fetchExceptions(includeNoise: _includeNoise, refresh: refresh);
      if (mounted) {
        setState(() {
          _report = report;
          _error = null;
        });
      }
    } catch (e) {
      if (mounted) setState(() => _error = e.toString());
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: hudAppBar('EXCEPTIONS', actions: [
        IconButton(
          iconSize: 16,
          icon: const Icon(Icons.refresh, color: ReactorTheme.textSecondary),
          onPressed: _loading ? null : () => _load(refresh: true),
        ),
      ]),
      body: Padding(padding: const EdgeInsets.all(10), child: _body()),
    );
  }

  Widget _body() {
    final report = _report;
    if (report == null) {
      return _error != null
          ? Center(
              child: Text(_error!, style: ReactorTheme.data(size: 11, color: ReactorTheme.critical)))
          : hudSpinner();
    }

    return ListView(
      children: [
        HudPanel(
          title: 'scan',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              hudKeyValue('scanned', '${report.containersScanned.length} containers · last '
                  '${report.tail} lines each · ${formatStamp(report.scannedAt)}'),
              hudKeyValue('events', '${report.totalEvents} in ${report.groups.length} groups',
                  colour: report.totalEvents == 0 ? ReactorTheme.cyan : ReactorTheme.amber),
              if (report.perContainer.isNotEmpty)
                hudKeyValue(
                    'by container',
                    (report.perContainer.entries.toList()..sort((a, b) => b.value.compareTo(a.value)))
                        .map((e) => '${e.key} ${e.value}')
                        .join(' · ')),
              ...report.errors.entries.map((e) =>
                  hudKeyValue('unread', '${e.key}: ${e.value}', colour: ReactorTheme.critical)),
              const SizedBox(height: 6),
              Row(children: [
                Expanded(
                  child: HudButton(
                    label: _includeNoise ? 'HIDING NOTHING' : 'BOOT NOISE HIDDEN',
                    colour: ReactorTheme.textSecondary,
                    busy: _loading,
                    onTap: () {
                      setState(() => _includeNoise = !_includeNoise);
                      _load();
                    },
                  ),
                ),
              ]),
            ],
          ),
        ),
        const SizedBox(height: 10),
        if (report.groups.isEmpty)
          Center(
              child: Padding(
            padding: const EdgeInsets.all(20),
            child: Text('no exceptions in the scanned window',
                style: ReactorTheme.data(size: 11, color: ReactorTheme.textFaint)),
          ))
        else
          ...report.groups.map(_group),
      ],
    );
  }

  Widget _group(ExceptionGroup group) {
    final open = _expanded.contains(group.signature);
    final colour = group.kind == 'EXCEPTION' ? ReactorTheme.critical : ReactorTheme.amber;
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: GestureDetector(
        onTap: () => setState(() => open ? _expanded.remove(group.signature) : _expanded.add(group.signature)),
        child: HudPanel(
          padding: const EdgeInsets.all(10),
          borderColor: open ? colour.withValues(alpha: 0.5) : ReactorTheme.hairline,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            mainAxisSize: MainAxisSize.min,
            children: [
              Row(children: [
                Text('${group.count}x',
                    style: ReactorTheme.data(size: 13, color: colour, weight: FontWeight.w600)),
                const SizedBox(width: 10),
                Expanded(
                  child: Text(group.headline,
                      maxLines: open ? null : 1,
                      overflow: open ? null : TextOverflow.ellipsis,
                      style: ReactorTheme.data(size: 12, weight: FontWeight.w600)),
                ),
              ]),
              if (group.frame != null)
                Text('@ ${group.frame}',
                    maxLines: open ? null : 1,
                    overflow: open ? null : TextOverflow.ellipsis,
                    style: ReactorTheme.data(size: 10.5, color: ReactorTheme.textSecondary)),
              Text(
                  '${group.perContainer.length} container(s) · last ${formatStamp(group.lastSeen)}'
                  '${group.lastContainer == null ? '' : ' on ${group.lastContainer}'}',
                  style: ReactorTheme.data(size: 10, color: ReactorTheme.textFaint)),
              if (open) ...[
                const SizedBox(height: 8),
                hudKeyValue('signature', group.signature),
                hudKeyValue('first seen', formatStamp(group.firstSeen)),
                hudKeyValue('per container',
                    group.perContainer.entries.map((e) => '${e.key} ${e.value}').join(' · ')),
                const SizedBox(height: 6),
                Container(
                  color: ReactorTheme.ground,
                  padding: const EdgeInsets.all(8),
                  child: SingleChildScrollView(
                    scrollDirection: Axis.horizontal,
                    child: SelectableText(group.sample.join('\n'),
                        style: ReactorTheme.data(size: 10, color: ReactorTheme.textSecondary)),
                  ),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}
