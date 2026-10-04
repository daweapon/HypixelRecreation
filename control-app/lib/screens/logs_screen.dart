import 'dart:async';

import 'package:flutter/material.dart';

import '../api/control_api.dart';
import '../models/fleet.dart';
import '../theme/reactor_theme.dart';
import '../widgets/chamfer.dart';

/// Log view, boot-race noise suppressed by default behind a FILTERED / RAW segmented control
/// rather than a toggle switch.
class LogsScreen extends StatefulWidget {
  final ControlApi api;
  final String container;

  const LogsScreen({super.key, required this.api, required this.container});

  @override
  State<LogsScreen> createState() => _LogsScreenState();
}

class _LogsScreenState extends State<LogsScreen> {
  LogResult? _result;
  String _mode = 'filtered';
  String? _error;
  Timer? _timer;

  @override
  void initState() {
    super.initState();
    _load();
    _timer = Timer.periodic(const Duration(seconds: 5), (_) => _load());
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  Future<void> _load() async {
    try {
      final result = await widget.api.fetchLogs(widget.container, tail: 400, mode: _mode);
      if (mounted) {
        setState(() {
          _result = result;
          _error = null;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() => _error = e.toString());
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: ReactorTheme.panel,
        elevation: 0,
        shape: const Border(bottom: BorderSide(color: ReactorTheme.hairline)),
        title: Text(widget.container, style: ReactorTheme.data(size: 13)),
        iconTheme: const IconThemeData(color: ReactorTheme.textSecondary, size: 18),
      ),
      body: Padding(
        padding: const EdgeInsets.all(10),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              children: [
                _segment('FILTERED', 'filtered'),
                _segment('RAW', 'raw'),
                const Spacer(),
                if (_result != null)
                  Text(
                    _mode == 'filtered'
                        ? '${_result!.suppressedLines} noise lines hidden'
                        : '${_result!.totalLines} lines',
                    style: ReactorTheme.data(size: 10, color: ReactorTheme.textFaint),
                  ),
              ],
            ),
            const SizedBox(height: 10),
            Expanded(child: _body()),
          ],
        ),
      ),
    );
  }

  Widget _body() {
    if (_error != null) {
      return Center(
          child: Text(_error!, style: ReactorTheme.data(size: 11, color: ReactorTheme.critical)));
    }
    final result = _result;
    if (result == null) {
      return const Center(
          child: SizedBox(
              width: 16,
              height: 16,
              child: CircularProgressIndicator(strokeWidth: 1.5, color: ReactorTheme.cyan)));
    }
    if (result.lines.isEmpty) {
      return Center(
          child: Text('no output', style: ReactorTheme.data(size: 11, color: ReactorTheme.textFaint)));
    }

    return HudPanel(
      padding: const EdgeInsets.all(8),
      child: SelectionArea(
        child: ListView.builder(
          reverse: true,
          itemCount: result.lines.length,
          itemBuilder: (context, index) {
            final line = result.lines[result.lines.length - 1 - index];
            return Padding(
              padding: const EdgeInsets.only(bottom: 1),
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  if (line.timestamp != null)
                    SizedBox(
                      width: 62,
                      child: Text(_shortTime(line.timestamp!),
                          style: ReactorTheme.data(size: 10, color: ReactorTheme.textFaint)),
                    ),
                  Expanded(
                    child: Text(
                      line.message,
                      style: ReactorTheme.data(
                        size: 11,
                        color: line.noise
                            ? ReactorTheme.textFaint
                            : _lineColour(line.message),
                      ),
                    ),
                  ),
                ],
              ),
            );
          },
        ),
      ),
    );
  }

  static Color _lineColour(String message) {
    final upper = message.toUpperCase();
    if (upper.contains('ERROR') || upper.contains('SEVERE') || upper.contains('EXCEPTION')) {
      return ReactorTheme.critical;
    }
    if (upper.contains('WARN')) return ReactorTheme.amber;
    return ReactorTheme.textSecondary;
  }

  static String _shortTime(String timestamp) {
    final parsed = DateTime.tryParse(timestamp);
    if (parsed == null) return '';
    final local = parsed.toLocal();
    String two(int v) => v.toString().padLeft(2, '0');
    return '${two(local.hour)}:${two(local.minute)}:${two(local.second)}';
  }

  Widget _segment(String label, String value) {
    final selected = _mode == value;
    return GestureDetector(
      onTap: () {
        setState(() => _mode = value);
        _load();
      },
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
        decoration: BoxDecoration(
          color: selected ? ReactorTheme.cyan.withValues(alpha: 0.12) : Colors.transparent,
          border: Border.all(
              color: selected ? ReactorTheme.cyan.withValues(alpha: 0.6) : ReactorTheme.hairline),
        ),
        child: Text(label,
            style: ReactorTheme.label(
                size: 9,
                spacing: 1.6,
                color: selected ? ReactorTheme.cyan : ReactorTheme.textFaint)),
      ),
    );
  }
}
