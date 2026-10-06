import 'package:flutter/material.dart';

import '../api/control_api.dart';
import '../models/fleet.dart';
import '../state/app_state.dart';
import '../theme/reactor_theme.dart';
import '../widgets/chamfer.dart';
import '../widgets/diagnosis_panel.dart';
import '../widgets/fleet_table.dart';
import '../widgets/job_banner.dart';
import '../widgets/reactor_ring.dart';
import '../widgets/telemetry_strip.dart';
import 'audit_screen.dart';
import 'devices_screen.dart';
import 'deploy_screen.dart';
import 'logs_screen.dart';

/// One adaptive dashboard: phone and desktop are the same language at different densities,
/// not two separate apps.
class DashboardScreen extends StatefulWidget {
  final AppState state;
  final ControlApi api;
  final VoidCallback onSignOut;

  const DashboardScreen({
    super.key,
    required this.state,
    required this.api,
    required this.onSignOut,
  });

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  static const double _desktopBreakpoint = 900;

  @override
  void initState() {
    super.initState();
    widget.state.addListener(_onChange);
    widget.state.loadVersion();
    widget.state.startPolling();
  }

  @override
  void dispose() {
    widget.state.removeListener(_onChange);
    widget.state.stopPolling();
    super.dispose();
  }

  void _onChange() {
    if (mounted) setState(() {});
  }

  void _openLogs(String container) {
    Navigator.of(context).push(MaterialPageRoute(
      builder: (_) => LogsScreen(api: widget.api, container: container),
    ));
  }

  Future<void> _confirmAndRun(String action, {List<String>? names, String? what}) async {
    final target = what ?? names?.join(', ') ?? 'the fleet';
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        backgroundColor: ReactorTheme.panel,
        shape: const RoundedRectangleBorder(borderRadius: BorderRadius.zero),
        title: Text('Confirm ${action.toUpperCase()}',
            style: ReactorTheme.heading(size: 14, color: ReactorTheme.amber)),
        content: Text('This will $action $target.',
            style: ReactorTheme.data(size: 12, color: ReactorTheme.textSecondary)),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: Text('CANCEL', style: ReactorTheme.label(size: 10)),
          ),
          TextButton(
            onPressed: () => Navigator.pop(context, true),
            child: Text('CONFIRM',
                style: ReactorTheme.label(size: 10, color: ReactorTheme.amber)),
          ),
        ],
      ),
    );
    if (confirmed == true) {
      await widget.state.runAction(action, names: names);
    }
  }

  @override
  Widget build(BuildContext context) {
    final fleet = widget.state.fleet;
    final isDesktop = MediaQuery.of(context).size.width >= _desktopBreakpoint;

    return Scaffold(
      body: SafeArea(
        child: Column(
          children: [
            _commandBar(fleet),
            if (widget.state.error != null) _errorBar(widget.state.error!),
            Expanded(
              child: fleet == null
                  ? const Center(
                      child: SizedBox(
                          width: 18,
                          height: 18,
                          child: CircularProgressIndicator(
                              strokeWidth: 1.5, color: ReactorTheme.cyan)))
                  : (isDesktop ? _desktopLayout(fleet) : _phoneLayout(fleet)),
            ),
          ],
        ),
      ),
    );
  }

  Widget _commandBar(FleetSnapshot? fleet) {
    // The identity block is Expanded and the actions are fixed width, so the actions always
    // get their space and the text gives way instead. With a Spacer and an unconstrained
    // title the reverse happened: on a phone the right-hand icons were pushed off-screen
    // entirely and could not be reached.
    final narrow = MediaQuery.of(context).size.width < 520;

    return Container(
      height: 46,
      decoration: const BoxDecoration(
        color: ReactorTheme.panel,
        border: Border(bottom: BorderSide(color: ReactorTheme.hairline)),
      ),
      padding: const EdgeInsets.only(left: 14, right: 4),
      child: Row(
        children: [
          Container(width: 6, height: 6, color: ReactorTheme.cyan),
          const SizedBox(width: 8),
          Expanded(
            child: Row(
              children: [
                Flexible(
                  child: Text(
                    narrow ? 'HYPIXEL' : 'HYPIXEL CONTROL',
                    overflow: TextOverflow.ellipsis,
                    style: ReactorTheme.heading(size: 13, color: ReactorTheme.textPrimary),
                  ),
                ),
                // The server address is reference information, not something to act on, so
                // it is the first thing dropped when space is short.
                if (!narrow) ...[
                  const SizedBox(width: 14),
                  Flexible(
                    child: Text(
                      widget.api.baseUrl ?? '',
                      overflow: TextOverflow.ellipsis,
                      style: ReactorTheme.data(size: 10, color: ReactorTheme.textFaint),
                    ),
                  ),
                ],
              ],
            ),
          ),
          const SizedBox(width: 8),
          if (widget.state.appVersion.isNotEmpty) ...[
            Text(widget.state.appVersion,
                style: ReactorTheme.data(size: 10, color: ReactorTheme.textFaint)),
            const SizedBox(width: 8),
          ],
          Text(widget.state.role,
              style: ReactorTheme.label(
                  size: 9,
                  spacing: 1.2,
                  color: widget.state.canOperate
                      ? ReactorTheme.cyan
                      : ReactorTheme.textFaint)),
          const SizedBox(width: 6),
          _action(Icons.upload_file, 'Deploy', () => Navigator.of(context).push(
              MaterialPageRoute(builder: (_) => DeployScreen(
                    api: widget.api,
                    canDeploy: widget.state.role == 'OWNER',
                  )))),
          _action(Icons.receipt_long, 'Audit log', () => Navigator.of(context).push(
              MaterialPageRoute(builder: (_) => AuditScreen(api: widget.api)))),
          if (widget.state.role == 'OWNER')
            _action(Icons.devices_other, 'Paired devices', () => Navigator.of(context).push(
                MaterialPageRoute(builder: (_) => DevicesScreen(api: widget.api)))),
          _action(Icons.refresh, 'Refresh now', () => widget.state.refresh(force: true)),
          _action(Icons.logout, 'Unpair this device', widget.onSignOut),
        ],
      ),
    );
  }

  Widget _action(IconData icon, String tooltip, VoidCallback onPressed) {
    return SizedBox(
      width: 32,
      child: IconButton(
        iconSize: 16,
        padding: EdgeInsets.zero,
        constraints: const BoxConstraints(),
        icon: Icon(icon, color: ReactorTheme.textSecondary),
        tooltip: tooltip,
        onPressed: onPressed,
      ),
    );
  }

  Widget _errorBar(String error) {
    return Container(
      width: double.infinity,
      color: ReactorTheme.critical.withValues(alpha: 0.12),
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
      child: Text(error,
          style: ReactorTheme.data(size: 11, color: ReactorTheme.critical),
          maxLines: 2,
          overflow: TextOverflow.ellipsis),
    );
  }

  Widget _desktopLayout(FleetSnapshot fleet) {
    return Padding(
      padding: const EdgeInsets.all(12),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 340,
            child: Column(
              children: [
                HudPanel(
                  title: 'shared dependencies',
                  child: Center(
                    child: ReactorRing(
                      dependencies: fleet.dependencies,
                      integrity: fleet.integrity,
                      severity: fleet.diagnosis.severity,
                      size: 240,
                    ),
                  ),
                ),
                const SizedBox(height: 10),
                DiagnosisPanel(diagnosis: fleet.diagnosis),
                const SizedBox(height: 10),
                _fleetControls(),
                if (widget.state.activeJob != null) ...[
                  const SizedBox(height: 10),
                  JobBanner(
                      job: widget.state.activeJob!,
                      onDismiss: widget.state.dismissJob),
                ],
              ],
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              children: [
                TelemetryStrip(fleet: fleet),
                const SizedBox(height: 10),
                Expanded(
                  child: FleetTable(
                    fleet: fleet,
                    canOperate: widget.state.canOperate,
                    onSelect: (c) => _openLogs(c.name),
                    onAction: (action, names) =>
                        _confirmAndRun(action, names: names, what: names.join(', ')),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _phoneLayout(FleetSnapshot fleet) {
    return ListView(
      padding: const EdgeInsets.all(10),
      children: [
        Center(
          child: ReactorRing(
            dependencies: fleet.dependencies,
            integrity: fleet.integrity,
            severity: fleet.diagnosis.severity,
            size: 210,
          ),
        ),
        const SizedBox(height: 12),
        DiagnosisPanel(diagnosis: fleet.diagnosis, compact: true),
        const SizedBox(height: 10),
        TelemetryStrip(fleet: fleet),
        if (widget.state.activeJob != null) ...[
          const SizedBox(height: 10),
          JobBanner(job: widget.state.activeJob!, onDismiss: widget.state.dismissJob),
        ],
        const SizedBox(height: 10),
        _fleetControls(),
        const SizedBox(height: 10),
        FleetTable(
          fleet: fleet,
          scrollable: false,
          canOperate: widget.state.canOperate,
          onSelect: (c) => _openLogs(c.name),
          onAction: (action, names) =>
              _confirmAndRun(action, names: names, what: names.join(', ')),
        ),
      ],
    );
  }

  Widget _fleetControls() {
    if (!widget.state.canOperate) {
      return HudPanel(
        title: 'lifecycle',
        child: Text(
          'This device is read-only. An owner can promote it from another paired device.',
          style: ReactorTheme.data(size: 11, color: ReactorTheme.textFaint),
        ),
      );
    }

    final busy = widget.state.activeJob?.isRunning == true;

    return HudPanel(
      title: 'lifecycle',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        mainAxisSize: MainAxisSize.min,
        children: [
          Row(
            children: [
              Expanded(
                child: _actionButton('START FLEET', ReactorTheme.cyan,
                    busy ? null : () => _confirmAndRun('fleet-start', what: 'the whole fleet')),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: _actionButton('STOP FLEET', ReactorTheme.critical,
                    busy ? null : () => _confirmAndRun('fleet-stop', what: 'the whole fleet')),
              ),
            ],
          ),
          const SizedBox(height: 6),
          Text('Starts are staggered and never run two JVMs up at once.',
              style: ReactorTheme.data(size: 10, color: ReactorTheme.textFaint)),
        ],
      ),
    );
  }

  Widget _actionButton(String label, Color colour, VoidCallback? onTap) {
    final enabled = onTap != null;
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 10),
        decoration: ShapeDecoration(
          color: enabled ? colour.withValues(alpha: 0.10) : ReactorTheme.panelRaised,
          shape: ChamferBorder(
            cut: 5,
            color: enabled ? colour.withValues(alpha: 0.55) : ReactorTheme.hairline,
          ),
        ),
        child: Center(
          child: Text(label,
              style: ReactorTheme.label(
                  size: 10,
                  spacing: 1.6,
                  color: enabled ? colour : ReactorTheme.textFaint)),
        ),
      ),
    );
  }
}
