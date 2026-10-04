import 'package:flutter/material.dart';

import '../models/fleet.dart';
import '../theme/reactor_theme.dart';
import 'chamfer.dart';
import 'status_pill.dart';

/// Dense full-fleet table, grouped CORE / GAME SERVERS / SERVICES with a live filter.
class FleetTable extends StatefulWidget {
  final FleetSnapshot fleet;
  final void Function(ContainerInfo container) onSelect;
  final bool canOperate;
  final void Function(String action, List<String> names)? onAction;

  const FleetTable({
    super.key,
    required this.fleet,
    required this.onSelect,
    required this.canOperate,
    this.onAction,
  });

  @override
  State<FleetTable> createState() => _FleetTableState();
}

class _FleetTableState extends State<FleetTable> {
  String _filter = '';

  static const _groupLabels = {
    'CORE': 'CORE',
    'GAME_SERVER': 'GAME SERVERS',
    'SERVICE': 'SERVICES',
    'UNKNOWN': 'OTHER',
  };

  @override
  Widget build(BuildContext context) {
    final groups = ['CORE', 'GAME_SERVER', 'SERVICE', 'UNKNOWN'];

    return HudPanel(
      title: 'fleet  ·  ${widget.fleet.runningCount}/${widget.fleet.containers.length} up',
      actions: [SizedBox(width: 170, child: _filterField())],
      child: ListView(
        shrinkWrap: true,
        children: [
          for (final group in groups) ..._buildGroup(group),
        ],
      ),
    );
  }

  Widget _filterField() {
    return SizedBox(
      height: 26,
      child: TextField(
        onChanged: (value) => setState(() => _filter = value.toLowerCase()),
        style: ReactorTheme.data(size: 11),
        cursorColor: ReactorTheme.cyan,
        cursorWidth: 6,
        cursorHeight: 12,
        decoration: InputDecoration(
          isDense: true,
          contentPadding: const EdgeInsets.symmetric(horizontal: 8, vertical: 6),
          hintText: 'filter',
          hintStyle: ReactorTheme.data(size: 11, color: ReactorTheme.textFaint),
          filled: true,
          fillColor: ReactorTheme.ground,
          border: OutlineInputBorder(
            borderRadius: BorderRadius.zero,
            borderSide: BorderSide(color: ReactorTheme.hairline),
          ),
          enabledBorder: OutlineInputBorder(
            borderRadius: BorderRadius.zero,
            borderSide: BorderSide(color: ReactorTheme.hairline),
          ),
          focusedBorder: OutlineInputBorder(
            borderRadius: BorderRadius.zero,
            borderSide: BorderSide(color: ReactorTheme.cyan.withValues(alpha: 0.6)),
          ),
        ),
      ),
    );
  }

  List<Widget> _buildGroup(String group) {
    final rows = widget.fleet
        .inGroup(group)
        .where((c) => _filter.isEmpty || c.name.toLowerCase().contains(_filter))
        .toList();
    if (rows.isEmpty) return [];

    return [
      Padding(
        padding: const EdgeInsets.only(top: 12, bottom: 4),
        child: Row(
          children: [
            Text(_groupLabels[group] ?? group,
                style: ReactorTheme.label(size: 9, color: ReactorTheme.textFaint, spacing: 2)),
            const SizedBox(width: 8),
            Expanded(child: Container(height: 1, color: ReactorTheme.hairline)),
          ],
        ),
      ),
      for (final container in rows) _FleetRow(
        container: container,
        onTap: () => widget.onSelect(container),
        canOperate: widget.canOperate,
        onAction: widget.onAction,
      ),
    ];
  }
}

class _FleetRow extends StatefulWidget {
  final ContainerInfo container;
  final VoidCallback onTap;
  final bool canOperate;
  final void Function(String action, List<String> names)? onAction;

  const _FleetRow({
    required this.container,
    required this.onTap,
    required this.canOperate,
    this.onAction,
  });

  @override
  State<_FleetRow> createState() => _FleetRowState();
}

class _FleetRowState extends State<_FleetRow> {
  bool _hovered = false;

  @override
  Widget build(BuildContext context) {
    final c = widget.container;
    return MouseRegion(
      onEnter: (_) => setState(() => _hovered = true),
      onExit: (_) => setState(() => _hovered = false),
      child: GestureDetector(
        onTap: widget.onTap,
        behavior: HitTestBehavior.opaque,
        child: Container(
          height: 28,
          color: _hovered ? ReactorTheme.panelRaised : Colors.transparent,
          padding: const EdgeInsets.symmetric(horizontal: 4),
          child: Row(
            children: [
              StatusPill(code: c.state),
              const SizedBox(width: 10),
              Expanded(
                flex: 4,
                child: Text(c.name,
                    overflow: TextOverflow.ellipsis,
                    style: ReactorTheme.data(
                        size: 11.5,
                        color: c.isNominal ? ReactorTheme.textPrimary : ReactorTheme.textSecondary)),
              ),
              Expanded(
                flex: 2,
                child: Text(c.statusText,
                    overflow: TextOverflow.ellipsis,
                    style: ReactorTheme.data(size: 10.5, color: ReactorTheme.textFaint)),
              ),
              SizedBox(
                width: 62,
                child: Text(c.uptimeLabel,
                    textAlign: TextAlign.right,
                    style: ReactorTheme.data(size: 10.5, color: ReactorTheme.textFaint)),
              ),
              SizedBox(
                width: 34,
                child: Text(c.restartCount > 0 ? '↻${c.restartCount}' : '',
                    textAlign: TextAlign.right,
                    style: ReactorTheme.data(
                        size: 10.5,
                        color: c.restartCount > 3
                            ? ReactorTheme.amber
                            : ReactorTheme.textFaint)),
              ),
              if (widget.canOperate && widget.onAction != null)
                SizedBox(
                  width: 30,
                  child: _hovered
                      ? IconButton(
                          padding: EdgeInsets.zero,
                          constraints: const BoxConstraints(),
                          iconSize: 14,
                          icon: const Icon(Icons.refresh, color: ReactorTheme.cyan),
                          tooltip: 'Restart ${c.name}',
                          onPressed: () => widget.onAction!('restart', [c.name]),
                        )
                      : const SizedBox.shrink(),
                )
              else
                const SizedBox(width: 30),
            ],
          ),
        ),
      ),
    );
  }
}
