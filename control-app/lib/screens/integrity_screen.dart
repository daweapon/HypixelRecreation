import 'dart:async';

import 'package:flutter/material.dart';

import '../api/control_api.dart';
import '../api/integrity_api.dart';
import '../models/fleet.dart';
import '../theme/reactor_theme.dart';
import '../widgets/capacity_panel.dart';
import '../widgets/chamfer.dart';
import '../widgets/job_banner.dart';
import '../widgets/row_badge_tag.dart';

/// Fleet integrity: is every server registered with the proxy, running the intended jar and
/// the current configuration, is the resource pack being served, and does the fleet fit in RAM.
///
/// Re-register needs OPERATOR; Reconcile and Sync replace running code or content and need
/// OWNER, the same grant as a deploy.
class IntegrityScreen extends StatefulWidget {
  final ControlApi api;

  const IntegrityScreen({super.key, required this.api});

  @override
  State<IntegrityScreen> createState() => _IntegrityScreenState();
}

class _IntegrityScreenState extends State<IntegrityScreen> {
  static const double _wideBreakpoint = 900;

  late final IntegrityApi _integrity = IntegrityApi(widget.api);

  RegistrationReport? _registrations;
  Background<SkewReport>? _skew;
  Background<DriftReport>? _drift;
  PackReport? _pack;
  CapacityReport? _capacity;
  final Map<String, String> _errors = {};

  OperationJob? _job;
  Timer? _jobTimer;
  Timer? _pendingTimer;
  bool _restartAfterSync = true;
  bool _verifying = false;
  final Set<String> _expanded = {};

  String get _role => widget.api.role ?? 'READ_ONLY';
  bool get _isOwner => _role == 'OWNER';
  bool get _canOperate => _role == 'OPERATOR' || _role == 'OWNER';
  bool get _busy => _job?.isRunning == true;

  @override
  void initState() {
    super.initState();
    _loadAll();
  }

  @override
  void dispose() {
    _jobTimer?.cancel();
    _pendingTimer?.cancel();
    super.dispose();
  }

  Future<void> _loadAll({bool refresh = false}) async {
    await Future.wait([
      _load('registrations', () async => _registrations = await _integrity.fetchRegistrations()),
      _load('skew', () async => _skew = await _integrity.fetchSkew(refresh: refresh)),
      _load('drift', () async => _drift = await _integrity.fetchConfigDrift(refresh: refresh)),
      _load('pack', () async => _pack = await _integrity.fetchPack(refresh: refresh)),
      _load('capacity', () async => _capacity = await _integrity.fetchCapacity()),
    ]);
    _schedulePendingReload();
  }

  Future<void> _load(String key, Future<Object?> Function() fetch) async {
    try {
      await fetch();
      _errors.remove(key);
    } catch (e) {
      _errors[key] = e.toString();
    }
    if (mounted) setState(() {});
  }

  /// Skew and drift are built in the background on the agent; poll until they settle.
  void _schedulePendingReload() {
    _pendingTimer?.cancel();
    if (_skew?.refreshing != true && _drift?.refreshing != true) return;
    _pendingTimer = Timer(const Duration(seconds: 4), () async {
      await Future.wait([
        if (_skew?.refreshing == true) _load('skew', () async => _skew = await _integrity.fetchSkew()),
        if (_drift?.refreshing == true)
          _load('drift', () async => _drift = await _integrity.fetchConfigDrift()),
      ]);
      _schedulePendingReload();
    });
  }

  Future<bool> _confirm(String title, String body) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        backgroundColor: ReactorTheme.panel,
        shape: const RoundedRectangleBorder(borderRadius: BorderRadius.zero),
        title: Text(title, style: ReactorTheme.heading(size: 14, color: ReactorTheme.amber)),
        content: Text(body, style: ReactorTheme.data(size: 12, color: ReactorTheme.textSecondary)),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: Text('CANCEL', style: ReactorTheme.label(size: 10)),
          ),
          TextButton(
            onPressed: () => Navigator.pop(context, true),
            child: Text('CONFIRM', style: ReactorTheme.label(size: 10, color: ReactorTheme.amber)),
          ),
        ],
      ),
    );
    return confirmed == true;
  }

  Future<void> _run(Future<OperationJob> Function() start) async {
    try {
      final job = await start();
      if (!mounted) return;
      setState(() {
        _job = job;
        _errors.remove('action');
      });
      _follow(job);
    } catch (e) {
      if (mounted) setState(() => _errors['action'] = e.toString());
    }
  }

  void _follow(OperationJob job) {
    _jobTimer?.cancel();
    _jobTimer = Timer.periodic(const Duration(seconds: 2), (timer) async {
      try {
        final updated = await widget.api.fetchJob(job.id);
        if (!mounted) return;
        setState(() => _job = updated);
        if (!updated.isRunning) {
          timer.cancel();
          await _loadAll(refresh: true);
        }
      } catch (_) {
        timer.cancel();
      }
    });
  }

  Future<void> _reregister(List<String> names) async {
    if (!await _confirm('RE-REGISTER',
        'Restarts ${names.join(', ')} so it registers with the proxy again. '
        'Players on it are disconnected.')) {
      return;
    }
    await _run(() => _integrity.reregister(names));
  }

  Future<void> _reconcile(String jar, List<String> names) async {
    if (!await _confirm('RECONCILE $jar',
        'Pushes the jar store copy of $jar into ${names.join(', ')} and restarts them, staggered.')) {
      return;
    }
    await _run(() => _integrity.reconcile(jar, names: names));
  }

  Future<void> _sync(List<String> names) async {
    final restart = _restartAfterSync;
    if (!await _confirm('SYNC CONFIGURATION',
        'Copies the host configuration files that differ into ${names.join(', ')}'
        '${restart ? ' and restarts them, the pack server first' : '. Running servers keep the values they loaded until restarted'}.')) {
      return;
    }
    await _run(() => _integrity.syncConfig(names: names, restart: restart));
  }

  Future<void> _verifyPack() async {
    setState(() => _verifying = true);
    await _load('pack', () async => _pack = await _integrity.fetchPack(verify: true));
    if (mounted) setState(() => _verifying = false);
  }

  @override
  Widget build(BuildContext context) {
    final wide = MediaQuery.of(context).size.width >= _wideBreakpoint;
    final left = [_registrationPanel(), _skewPanel()];
    final right = [_packPanel(), CapacityPanel(report: _capacity, showEntries: true), _driftPanel()];

    return Scaffold(
      appBar: AppBar(
        backgroundColor: ReactorTheme.panel,
        elevation: 0,
        shape: const Border(bottom: BorderSide(color: ReactorTheme.hairline)),
        title: Text('INTEGRITY', style: ReactorTheme.heading(size: 13)),
        iconTheme: const IconThemeData(color: ReactorTheme.textSecondary, size: 18),
        actions: [
          IconButton(
            iconSize: 16,
            tooltip: 'Re-check everything',
            icon: const Icon(Icons.refresh, color: ReactorTheme.textSecondary),
            onPressed: () => _loadAll(refresh: true),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(10),
        children: [
          if (_errors['action'] != null) ...[
            _errorText(_errors['action']!),
            const SizedBox(height: 10),
          ],
          if (_job != null) ...[
            JobBanner(job: _job!, onDismiss: () => setState(() => _job = null)),
            const SizedBox(height: 10),
          ],
          if (wide)
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(child: _column(left)),
                const SizedBox(width: 10),
                Expanded(child: _column(right)),
              ],
            )
          else
            _column([...left, ...right]),
        ],
      ),
    );
  }

  Widget _column(List<Widget> panels) => Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          for (final panel in panels) ...[panel, const SizedBox(height: 10)],
        ],
      );

  // --- proxy registration ---

  Widget _registrationPanel() {
    final report = _registrations;
    final error = _errors['registrations'];
    if (report == null) return _placeholder('proxy registration', error);

    final colour = ReactorTheme.forSeverity(report.severity);
    return HudPanel(
      title: 'proxy registration',
      borderColor: report.severity == 'NOMINAL' ? ReactorTheme.hairline : colour.withValues(alpha: 0.5),
      actions: [
        if (_canOperate && report.delisted.isNotEmpty)
          _smallButton('RE-REGISTER ALL', ReactorTheme.critical,
              _busy ? null : () => _reregister(report.delisted)),
      ],
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(report.headline, style: ReactorTheme.data(size: 12, color: colour)),
          const SizedBox(height: 4),
          Text(report.detail, style: ReactorTheme.data(size: 10.5, color: ReactorTheme.textSecondary)),
          const SizedBox(height: 8),
          for (final row in report.rows)
            _line(
              tag: _stateTag(row.state),
              name: row.container,
              trailing: row.state == 'REGISTERED'
                  ? '${row.proxyName ?? ''}  ${row.players}p'
                  : row.detail,
              action: row.state == 'DELISTED' && _canOperate
                  ? _iconAction(Icons.link, 'Re-register ${row.container}',
                      _busy ? null : () => _reregister([row.container]))
                  : null,
            ),
          if (error != null) _errorText(error),
        ],
      ),
    );
  }

  Widget _stateTag(String state) {
    final code = switch (state) {
      'REGISTERED' => 'REG',
      'BOOTING' => 'BOOT',
      'NOT_RUNNING' => 'DOWN',
      _ => state,
    };
    return RowBadgeTag(badge: RowBadge(code, state));
  }

  // --- version skew ---

  Widget _skewPanel() {
    final view = _skew;
    final error = _errors['skew'];
    final report = view?.report;
    if (report == null) {
      return _placeholder('version skew', error,
          waiting: view?.refreshing == true ? 'hashing every jar in the fleet...' : null);
    }

    return HudPanel(
      title: 'version skew${view!.refreshing ? '  ·  re-checking' : ''}',
      borderColor: report.skewed > 0 ? ReactorTheme.amber.withValues(alpha: 0.5) : ReactorTheme.hairline,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(
            report.skewed > 0
                ? '${report.skewed} container(s) running a different jar than the store'
                : 'Every checked container matches its reference',
            style: ReactorTheme.data(
                size: 12, color: report.skewed > 0 ? ReactorTheme.amber : ReactorTheme.cyan),
          ),
          if (!report.storeAvailable)
            Text('Jar store ${report.jarStore} is not mounted; containers are compared with each other.',
                style: ReactorTheme.data(size: 10.5, color: ReactorTheme.textFaint)),
          for (final group in report.byJar.entries) ..._skewGroup(group.key, group.value),
          if (error != null) _errorText(error),
        ],
      ),
    );
  }

  List<Widget> _skewGroup(String jar, List<SkewEntry> entries) {
    final skewed = entries.where((e) => e.state == 'SKEW').map((e) => e.container).toList();
    return [
      const SizedBox(height: 10),
      Row(
        children: [
          Expanded(
            child: Text(jar, style: ReactorTheme.label(size: 10, color: ReactorTheme.textSecondary)),
          ),
          if (_isOwner && skewed.isNotEmpty)
            _smallButton('RECONCILE', ReactorTheme.amber, _busy ? null : () => _reconcile(jar, skewed)),
        ],
      ),
      for (final entry in entries)
        _line(
          tag: RowBadgeTag(badge: RowBadge(_skewCode(entry.state), entry.reason)),
          name: entry.container,
          trailing: entry.state == 'SKEW'
              ? '${SkewEntry.short(entry.containerSha256)} != ${SkewEntry.short(entry.referenceSha256)}'
              : SkewEntry.short(entry.containerSha256),
        ),
    ];
  }

  static String _skewCode(String state) => switch (state) {
        'MATCH' => 'OK',
        'NO_REFERENCE' => 'NOREF',
        'UNREADABLE' => 'ERR',
        'STOPPED' => 'DOWN',
        _ => state,
      };

  // --- config drift ---

  Widget _driftPanel() {
    final view = _drift;
    final error = _errors['drift'];
    final report = view?.report;
    if (report == null) {
      return _placeholder('config drift', error,
          waiting: view?.refreshing == true ? 'hashing the host and every game server...' : null);
    }

    if (!report.hostMounted) {
      return HudPanel(
        title: 'config drift',
        borderColor: ReactorTheme.amber.withValues(alpha: 0.5),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          mainAxisSize: MainAxisSize.min,
          children: [
            Text('The host configuration is not mounted into service_control.',
                style: ReactorTheme.data(size: 12, color: ReactorTheme.amber)),
            const SizedBox(height: 6),
            Text('Add this to service_control\'s volumes in docker-compose.override.yml, then recreate it:',
                style: ReactorTheme.data(size: 10.5, color: ReactorTheme.textSecondary)),
            const SizedBox(height: 4),
            SelectableText(report.composeLine,
                style: ReactorTheme.data(size: 11, color: ReactorTheme.textPrimary)),
          ],
        ),
      );
    }

    final drifted = report.containers.where((c) => c.status == 'DRIFTED').map((c) => c.container).toList();
    return HudPanel(
      title: 'config drift${view!.refreshing ? '  ·  re-checking' : ''}',
      borderColor: drifted.isNotEmpty ? ReactorTheme.amber.withValues(alpha: 0.5) : ReactorTheme.hairline,
      actions: [
        if (_isOwner && drifted.isNotEmpty)
          _smallButton('SYNC ALL', ReactorTheme.amber, _busy ? null : () => _sync(drifted)),
      ],
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(
            drifted.isEmpty
                ? 'Every running game server matches the host (${report.hostFiles} files)'
                : '${drifted.length} container(s) differ from the host configuration',
            style: ReactorTheme.data(
                size: 12, color: drifted.isEmpty ? ReactorTheme.cyan : ReactorTheme.amber),
          ),
          if (_isOwner && drifted.isNotEmpty)
            Row(
              children: [
                Checkbox(
                  value: _restartAfterSync,
                  visualDensity: VisualDensity.compact,
                  activeColor: ReactorTheme.amber,
                  onChanged: (value) => setState(() => _restartAfterSync = value ?? true),
                ),
                Expanded(
                  child: Text('restart after sync (a running server keeps the values it loaded)',
                      style: ReactorTheme.data(size: 10.5, color: ReactorTheme.textSecondary)),
                ),
              ],
            ),
          for (final container in report.containers) ..._driftRows(container),
          if (error != null) _errorText(error),
        ],
      ),
    );
  }

  List<Widget> _driftRows(ContainerDrift drift) {
    final expanded = _expanded.contains(drift.container);
    final summary = switch (drift.status) {
      'DRIFTED' => '${drift.changed} changed, ${drift.missing} new, ${drift.extra} deleted on host',
      'CLEAN' => drift.scope.isEmpty ? 'matches' : 'matches (${drift.scope}/ only)',
      _ => drift.error ?? drift.status.toLowerCase(),
    };
    return [
      GestureDetector(
        behavior: HitTestBehavior.opaque,
        onTap: drift.files.isEmpty
            ? null
            : () => setState(() {
                  if (!_expanded.remove(drift.container)) _expanded.add(drift.container);
                }),
        child: _line(
          tag: RowBadgeTag(badge: RowBadge(_driftCode(drift.status), summary)),
          name: drift.container,
          trailing: summary,
          action: _isOwner && drift.status == 'DRIFTED'
              ? _iconAction(Icons.sync, 'Sync ${drift.container}', _busy ? null : () => _sync([drift.container]))
              : null,
        ),
      ),
      if (expanded)
        Padding(
          padding: const EdgeInsets.only(left: 54, bottom: 6),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              for (final file in drift.files.take(40))
                Text('${file.kindLabel}  ${file.path}',
                    overflow: TextOverflow.ellipsis,
                    style: ReactorTheme.data(
                        size: 10,
                        color: file.syncable ? ReactorTheme.textSecondary : ReactorTheme.textFaint)),
              if (drift.files.length > 40)
                Text('... and ${drift.files.length - 40} more',
                    style: ReactorTheme.data(size: 10, color: ReactorTheme.textFaint)),
              if (drift.extra > 0)
                Text('DEL files were removed on the host; only a container recreate clears them.',
                    style: ReactorTheme.data(size: 10, color: ReactorTheme.textFaint)),
            ],
          ),
        ),
    ];
  }

  static String _driftCode(String status) => switch (status) {
        'CLEAN' => 'OK',
        'DRIFTED' => 'SKEW',
        'STOPPED' => 'DOWN',
        _ => 'ERR',
      };

  // --- resource pack ---

  Widget _packPanel() {
    final report = _pack;
    final error = _errors['pack'];
    if (report == null) return _placeholder('resource pack', error);

    final colour = switch (report.state) {
      'LIVE' => ReactorTheme.cyan,
      'STALE' => ReactorTheme.amber,
      _ => ReactorTheme.critical,
    };
    return HudPanel(
      title: 'resource pack',
      borderColor: report.state == 'LIVE' ? ReactorTheme.hairline : colour.withValues(alpha: 0.5),
      actions: [
        _smallButton(_verifying ? 'VERIFYING' : 'VERIFY DOWNLOAD', ReactorTheme.cyan,
            _verifying ? null : _verifyPack),
      ],
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        mainAxisSize: MainAxisSize.min,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                decoration: BoxDecoration(
                  color: colour.withValues(alpha: 0.10),
                  border: Border.all(color: colour.withValues(alpha: 0.55)),
                ),
                child: Text(report.state,
                    style: ReactorTheme.data(size: 12, color: colour, weight: FontWeight.w600)),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: Text(report.headline, style: ReactorTheme.data(size: 11.5, color: colour)),
              ),
            ],
          ),
          for (final problem in report.problems)
            Padding(
              padding: const EdgeInsets.only(top: 4),
              child: Text(problem, style: ReactorTheme.data(size: 10.5, color: ReactorTheme.textSecondary)),
            ),
          const SizedBox(height: 8),
          for (final entry in report.advertised.entries)
            _line(
              tag: RowBadgeTag(badge: RowBadge(
                  report.verified[entry.value] == false ? 'BAD' : 'OK', 'advertised by ${entry.key}')),
              name: entry.key,
              trailing: entry.value.substring(0, entry.value.length > 12 ? 12 : entry.value.length),
            ),
          for (final probe in report.external)
            _line(
              tag: RowBadgeTag(badge: RowBadge(probe.status >= 200 && probe.status < 300 ? 'OK' : 'ERR', probe.url)),
              name: Uri.tryParse(probe.url)?.host ?? probe.url,
              trailing: '${probe.label}  (reported only)',
            ),
          if (error != null) _errorText(error),
        ],
      ),
    );
  }

  // --- shared bits ---

  Widget _placeholder(String title, String? error, {String? waiting}) => HudPanel(
        title: title,
        child: error != null
            ? _errorText(error)
            : Text(waiting ?? 'loading...',
                style: ReactorTheme.data(size: 11, color: ReactorTheme.textFaint)),
      );

  Widget _errorText(String error) => Padding(
        padding: const EdgeInsets.only(top: 4),
        child: Text(error, style: ReactorTheme.data(size: 10.5, color: ReactorTheme.critical)),
      );

  Widget _line({required Widget tag, required String name, required String trailing, Widget? action}) {
    return SizedBox(
      height: 24,
      child: Row(
        children: [
          SizedBox(width: 54, child: Align(alignment: Alignment.centerLeft, child: tag)),
          Expanded(
            flex: 3,
            child: Text(name,
                overflow: TextOverflow.ellipsis,
                style: ReactorTheme.data(size: 11, color: ReactorTheme.textPrimary)),
          ),
          Expanded(
            flex: 4,
            child: Text(trailing,
                overflow: TextOverflow.ellipsis,
                textAlign: TextAlign.right,
                style: ReactorTheme.data(size: 10, color: ReactorTheme.textFaint)),
          ),
          SizedBox(width: 28, child: action),
        ],
      ),
    );
  }

  Widget _iconAction(IconData icon, String tooltip, VoidCallback? onPressed) => IconButton(
        padding: EdgeInsets.zero,
        constraints: const BoxConstraints(),
        iconSize: 14,
        tooltip: tooltip,
        icon: Icon(icon, color: onPressed == null ? ReactorTheme.textFaint : ReactorTheme.cyan),
        onPressed: onPressed,
      );

  Widget _smallButton(String label, Color colour, VoidCallback? onTap) {
    final enabled = onTap != null;
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
        decoration: ShapeDecoration(
          color: enabled ? colour.withValues(alpha: 0.10) : ReactorTheme.panelRaised,
          shape: ChamferBorder(cut: 4, color: enabled ? colour.withValues(alpha: 0.55) : ReactorTheme.hairline),
        ),
        child: Text(label,
            style: ReactorTheme.label(size: 9, spacing: 1.4, color: enabled ? colour : ReactorTheme.textFaint)),
      ),
    );
  }
}
