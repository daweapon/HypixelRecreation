import 'package:flutter/material.dart';

import '../api/control_api.dart';
import '../models/fleet.dart';
import '../theme/reactor_theme.dart';
import '../widgets/chamfer.dart';

/// Append-only record of every action taken through the app.
///
/// Shows denied attempts alongside successful ones - a log of successes only cannot answer
/// "who tried to stop the fleet and was refused", which is the question that matters once
/// more than one person holds a paired device.
class AuditScreen extends StatefulWidget {
  final ControlApi api;

  const AuditScreen({super.key, required this.api});

  @override
  State<AuditScreen> createState() => _AuditScreenState();
}

class _AuditScreenState extends State<AuditScreen> {
  List<AuditEntry>? _entries;
  String? _error;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    try {
      final entries = await widget.api.fetchAudit(limit: 200);
      if (mounted) {
        setState(() {
          _entries = entries;
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
        title: Text('AUDIT', style: ReactorTheme.heading(size: 13)),
        iconTheme: const IconThemeData(color: ReactorTheme.textSecondary, size: 18),
        actions: [
          IconButton(
            iconSize: 16,
            icon: const Icon(Icons.refresh, color: ReactorTheme.textSecondary),
            onPressed: _load,
          ),
        ],
      ),
      body: Padding(padding: const EdgeInsets.all(10), child: _body()),
    );
  }

  Widget _body() {
    if (_error != null) {
      return Center(
          child: Text(_error!, style: ReactorTheme.data(size: 11, color: ReactorTheme.critical)));
    }
    final entries = _entries;
    if (entries == null) {
      return const Center(
          child: SizedBox(
              width: 16,
              height: 16,
              child: CircularProgressIndicator(strokeWidth: 1.5, color: ReactorTheme.cyan)));
    }
    if (entries.isEmpty) {
      return Center(
          child: Text('no recorded actions yet',
              style: ReactorTheme.data(size: 11, color: ReactorTheme.textFaint)));
    }

    return HudPanel(
      title: '${entries.length} entries  ·  newest first',
      child: ListView.builder(
        itemCount: entries.length,
        itemBuilder: (context, index) => _row(entries[index]),
      ),
    );
  }

  Widget _row(AuditEntry entry) {
    final colour = entry.wasAccepted
        ? ReactorTheme.cyan
        : entry.wasDenied
            ? ReactorTheme.critical
            : ReactorTheme.amber;

    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(width: 2, height: 30, color: colour),
          const SizedBox(width: 8),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Text(entry.action,
                        style: ReactorTheme.data(
                            size: 12, color: colour, weight: FontWeight.w600)),
                    const SizedBox(width: 8),
                    if (!entry.wasAccepted)
                      Text(entry.outcome,
                          style: ReactorTheme.label(size: 9, color: colour, spacing: 1.2)),
                    const Spacer(),
                    Text(_stamp(entry.at),
                        style: ReactorTheme.data(size: 10, color: ReactorTheme.textFaint)),
                  ],
                ),
                const SizedBox(height: 2),
                Text(
                  '${entry.actorName}'
                  '${entry.role == null ? '' : ' · ${entry.role}'}'
                  '${entry.targets.isEmpty ? '' : ' → ${entry.targets.join(', ')}'}',
                  style: ReactorTheme.data(size: 11, color: ReactorTheme.textSecondary),
                ),
                if (entry.detail != null && entry.detail!.isNotEmpty)
                  Text(entry.detail!,
                      style: ReactorTheme.data(size: 10, color: ReactorTheme.textFaint)),
              ],
            ),
          ),
        ],
      ),
    );
  }

  static String _stamp(DateTime at) {
    final local = at.toLocal();
    String two(int v) => v.toString().padLeft(2, '0');
    return '${two(local.month)}/${two(local.day)} ${two(local.hour)}:${two(local.minute)}:${two(local.second)}';
  }
}
