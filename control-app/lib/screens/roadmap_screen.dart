import 'package:flutter/material.dart';

import '../api/control_api.dart';
import '../theme/reactor_theme.dart';
import '../widgets/admin_kit.dart';
import '../widgets/chamfer.dart';

/// Read-only view of the tracker sheet's Roadmap, Bugs & Loose Ends and Done tabs. The server
/// caches each tab for five minutes; refresh bypasses that.
///
/// Constructor: `RoadmapScreen(api: controlApi)`.
class RoadmapScreen extends StatefulWidget {
  final ControlApi api;

  const RoadmapScreen({super.key, required this.api});

  @override
  State<RoadmapScreen> createState() => _RoadmapScreenState();
}

class _RoadmapScreenState extends State<RoadmapScreen> {
  static const _tabs = {'roadmap': 'ROADMAP', 'bugs': 'BUGS', 'done': 'DONE'};

  String _tab = 'roadmap';
  RoadmapTab? _data;
  String? _error;
  bool _hideDone = true;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load({bool refresh = false}) async {
    final tab = _tab;
    try {
      final data = await widget.api.fetchRoadmap(tab, refresh: refresh);
      if (mounted && tab == _tab) {
        setState(() {
          _data = data;
          _error = null;
        });
      }
    } catch (e) {
      if (mounted && tab == _tab) setState(() => _error = e.toString());
    }
  }

  void _select(String tab) {
    if (tab == _tab) return;
    setState(() {
      _tab = tab;
      _data = null;
      _error = null;
    });
    _load();
  }

  @override
  Widget build(BuildContext context) {
    return AdminScaffold(
      title: 'TRACKER',
      onRefresh: () => _load(refresh: true),
      body: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Wrap(
            spacing: 6,
            runSpacing: 6,
            children: [
              for (final entry in _tabs.entries)
                AdminChip(entry.value,
                    colour: entry.key == _tab ? ReactorTheme.cyan : ReactorTheme.textSecondary,
                    onTap: () => _select(entry.key)),
              if (_tab == 'bugs')
                AdminChip(_hideDone ? 'SHOW DONE' : 'HIDE DONE',
                    colour: ReactorTheme.amber, onTap: () => setState(() => _hideDone = !_hideDone)),
            ],
          ),
          const SizedBox(height: 10),
          Expanded(child: _body()),
        ],
      ),
    );
  }

  Widget _body() {
    if (_error != null) return AdminMessage(_error!, colour: ReactorTheme.critical);
    final data = _data;
    if (data == null) return const AdminSpinner();

    final rows = _tab == 'bugs' && _hideDone ? data.rows.where((row) => !row.isDone).toList() : data.rows;
    return HudPanel(
      title: '${rows.length} rows${data.fetchedAt == null ? '' : '  ·  fetched ${adminStamp(data.fetchedAt!)}'}',
      child: ListView.builder(
        itemCount: rows.length,
        itemBuilder: (context, index) => _RoadmapRowTile(row: rows[index]),
      ),
    );
  }
}

class _RoadmapRowTile extends StatefulWidget {
  final RoadmapRow row;

  const _RoadmapRowTile({required this.row});

  @override
  State<_RoadmapRowTile> createState() => _RoadmapRowTileState();
}

class _RoadmapRowTileState extends State<_RoadmapRowTile> {
  bool _expanded = false;

  Color get _statusColour {
    final status = widget.row.status.toLowerCase();
    if (status == 'done') return ReactorTheme.cyan;
    if (status.contains('progress')) return ReactorTheme.amber;
    if (status.contains('blocked')) return ReactorTheme.critical;
    return ReactorTheme.textSecondary;
  }

  @override
  Widget build(BuildContext context) {
    final row = widget.row;
    return InkWell(
      onTap: () => setState(() => _expanded = !_expanded),
      child: Padding(
        padding: const EdgeInsets.only(bottom: 8),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Container(width: 2, height: 30, color: _statusColour),
            const SizedBox(width: 8),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(children: [
                    SizedBox(
                      width: 34,
                      child: Text('#${row.number}', style: ReactorTheme.data(size: 10, color: ReactorTheme.textFaint)),
                    ),
                    Expanded(
                      child: Text(row.task,
                          maxLines: _expanded ? null : 2,
                          overflow: _expanded ? null : TextOverflow.ellipsis,
                          style: ReactorTheme.data(size: 12)),
                    ),
                  ]),
                  const SizedBox(height: 2),
                  Text(
                    [row.status, row.priority, row.effort, row.area].where((s) => s.isNotEmpty).join('  ·  '),
                    style: ReactorTheme.label(size: 9, color: _statusColour, spacing: 1.1),
                  ),
                  if (_expanded) ...[
                    if (row.scope.isNotEmpty) ...[
                      const SizedBox(height: 4),
                      Text(row.scope, style: ReactorTheme.data(size: 10, color: ReactorTheme.textSecondary)),
                    ],
                    if (row.notes.isNotEmpty) ...[
                      const SizedBox(height: 4),
                      Text(row.notes, style: ReactorTheme.data(size: 10, color: ReactorTheme.textFaint)),
                    ],
                  ],
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
