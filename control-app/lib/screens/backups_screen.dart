import 'dart:async';

import 'package:flutter/material.dart';

import '../api/control_api.dart';
import '../api/ops_api.dart';
import '../models/fleet.dart';
import '../models/ops.dart';
import '../theme/reactor_theme.dart';
import '../widgets/chamfer.dart';
import '../widgets/hud_button.dart';
import '../widgets/job_banner.dart';
import '../widgets/second_factor.dart';

/// Scheduled Mongo + Redis backups: schedule, retention, history, back-up-now and restore.
///
/// Back up now needs OPERATOR. Settings and restore need OWNER; restore is two-step - the
/// server previews exactly what will happen and only a confirmation of that preview runs it.
///
/// Constructor: `BackupsScreen(api: ControlApi, role: 'READ_ONLY' | 'OPERATOR' | 'OWNER')`.
class BackupsScreen extends StatefulWidget {
  final ControlApi api;
  final String role;

  const BackupsScreen({super.key, required this.api, required this.role});

  @override
  State<BackupsScreen> createState() => _BackupsScreenState();
}

class _BackupsScreenState extends State<BackupsScreen> {
  late final OpsApi _ops = OpsApi(widget.api);
  BackupOverview? _overview;
  String? _error;
  bool _starting = false;
  OperationJob? _job;
  Timer? _poll;
  Timer? _jobTimer;

  bool get _canOperate => widget.role == 'OPERATOR' || widget.role == 'OWNER';
  bool get _isOwner => widget.role == 'OWNER';

  @override
  void initState() {
    super.initState();
    _load();
    _poll = Timer.periodic(const Duration(seconds: 5), (_) {
      if (_overview?.running != null) _load();
    });
  }

  @override
  void dispose() {
    _poll?.cancel();
    _jobTimer?.cancel();
    super.dispose();
  }

  Future<void> _load() async {
    try {
      final overview = await _ops.fetchBackups();
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

  void _notify(String message, {Color colour = ReactorTheme.cyan}) {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
      backgroundColor: ReactorTheme.panelRaised,
      content: Text(message, style: ReactorTheme.data(size: 11, color: colour)),
    ));
  }

  Future<void> _backupNow() async {
    setState(() => _starting = true);
    try {
      final started = await _ops.runBackup();
      _notify('Backup ${started.id} started');
      await _load();
    } catch (e) {
      _notify(e.toString(), colour: ReactorTheme.critical);
    } finally {
      if (mounted) setState(() => _starting = false);
    }
  }

  Future<void> _editSettings(BackupOverview overview) async {
    final schedule = TextEditingController(text: overview.schedule);
    final retain = TextEditingController(text: '${overview.retain}');
    final save = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        backgroundColor: ReactorTheme.panel,
        shape: const RoundedRectangleBorder(borderRadius: BorderRadius.zero),
        title: Text('Backup settings', style: ReactorTheme.heading(size: 14)),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            TextField(
              controller: schedule,
              style: ReactorTheme.data(size: 13),
              decoration: const InputDecoration(
                  labelText: 'Schedule', helperText: 'off · daily@04:00 · every 6h'),
            ),
            const SizedBox(height: 10),
            TextField(
              controller: retain,
              keyboardType: TextInputType.number,
              style: ReactorTheme.data(size: 13),
              decoration: const InputDecoration(
                  labelText: 'Keep newest', helperText: 'older complete backups are deleted'),
            ),
            const SizedBox(height: 8),
            Text('Times are ${overview.timezone}.',
                style: ReactorTheme.data(size: 10, color: ReactorTheme.textFaint)),
          ],
        ),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(context, false),
              child: Text('CANCEL', style: ReactorTheme.label(size: 10))),
          TextButton(
              onPressed: () => Navigator.pop(context, true),
              child: Text('SAVE', style: ReactorTheme.label(size: 10, color: ReactorTheme.amber))),
        ],
      ),
    );
    if (save != true || !mounted) return;

    final keep = int.tryParse(retain.text.trim());
    if (keep == null || keep < 1) {
      _notify('Retention must be a whole number of at least 1', colour: ReactorTheme.critical);
      return;
    }
    try {
      final updated = await withSecondFactor(
          context,
          widget.api,
          () => _ops.updateBackupSettings(schedule: schedule.text.trim(), retain: keep));
      if (updated == null || !mounted) return;
      setState(() => _overview = updated);
      _notify('Saved: ${updated.schedule}, keeping ${updated.retain}');
    } catch (e) {
      _notify(e.toString(), colour: ReactorTheme.critical);
    }
  }

  Future<void> _restore(BackupEntry backup) async {
    final choice = await showDialog<_RestoreChoice>(
      context: context,
      builder: (context) => _RestoreChoiceDialog(backup: backup),
    );
    if (choice == null || !mounted) return;

    RestorePreview preview;
    try {
      preview = await _ops.previewRestore(backup.id, choice.parts, choice.mode);
    } catch (e) {
      _notify(e.toString(), colour: ReactorTheme.critical);
      return;
    }
    if (!mounted) return;

    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => _RestoreConfirmDialog(
          backup: backup, choice: choice, preview: preview),
    );
    if (confirmed != true || !mounted) return;

    try {
      final job = await withSecondFactor(
        context,
        widget.api,
        () => _ops.confirmRestore(backup.id, choice.parts, choice.mode, preview.confirmToken),
        reason: 'Restoring ${backup.id} needs the code from your authenticator app.',
      );
      if (job == null || !mounted) return;
      setState(() => _job = job);
      _follow(job.id);
    } catch (e) {
      _notify(e.toString(), colour: ReactorTheme.critical);
    }
  }

  void _follow(String id) {
    _jobTimer?.cancel();
    _jobTimer = Timer.periodic(const Duration(seconds: 2), (timer) async {
      try {
        final updated = await widget.api.fetchJob(id);
        if (!mounted) return timer.cancel();
        setState(() => _job = updated);
        if (!updated.isRunning) {
          timer.cancel();
          _load();
        }
      } catch (_) {
        timer.cancel();
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: hudAppBar('BACKUPS', actions: [
        IconButton(
          iconSize: 16,
          icon: const Icon(Icons.refresh, color: ReactorTheme.textSecondary),
          onPressed: _load,
        ),
      ]),
      body: Padding(padding: const EdgeInsets.all(10), child: _body()),
    );
  }

  Widget _body() {
    final overview = _overview;
    if (overview == null) {
      return _error != null
          ? Center(
              child: Text(_error!, style: ReactorTheme.data(size: 11, color: ReactorTheme.critical)))
          : hudSpinner();
    }

    return ListView(
      children: [
        if (_job != null) ...[
          JobBanner(job: _job!, onDismiss: () => setState(() => _job = null)),
          const SizedBox(height: 10),
        ],
        _statusPanel(overview),
        const SizedBox(height: 10),
        HudPanel(
          title: '${overview.backups.length} managed backups  ·  newest first',
          child: overview.backups.isEmpty
              ? Text('none yet', style: ReactorTheme.data(size: 11, color: ReactorTheme.textFaint))
              : Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: overview.backups.map(_backupRow).toList(),
                ),
        ),
        if (overview.unmanaged.isNotEmpty) ...[
          const SizedBox(height: 10),
          HudPanel(
            title: 'other dumps in ${overview.directory}  ·  not managed',
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: overview.unmanaged
                  .map((u) => Padding(
                        padding: const EdgeInsets.only(bottom: 4),
                        child: Row(children: [
                          Expanded(
                              child: Text('${u.name}${u.directory ? '/' : ''}',
                                  style: ReactorTheme.data(size: 11))),
                          Text('${formatBytes(u.bytes)}  ${formatStamp(u.modifiedAt)}',
                              style: ReactorTheme.data(size: 10, color: ReactorTheme.textFaint)),
                        ]),
                      ))
                  .toList(),
            ),
          ),
        ],
      ],
    );
  }

  Widget _statusPanel(BackupOverview overview) {
    final last = overview.lastResult;
    return HudPanel(
      title: 'schedule',
      borderColor: overview.available ? ReactorTheme.hairline : ReactorTheme.amber.withValues(alpha: 0.6),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          if (!overview.available)
            Padding(
              padding: const EdgeInsets.only(bottom: 8),
              child: Text(
                'The agent cannot write ${overview.directory}. Add '
                '"./backups:/app/backups" to service_control\'s volumes in '
                'docker-compose.override.yml and recreate it.',
                style: ReactorTheme.data(size: 11, color: ReactorTheme.amber),
              ),
            ),
          hudKeyValue('schedule', '${overview.schedule}  (${overview.timezone})'),
          hudKeyValue('next run', formatStamp(overview.nextRunAt)),
          hudKeyValue('keeping', '${overview.retain} newest complete backups'),
          if (overview.running != null)
            hudKeyValue('running', overview.running!.id, colour: ReactorTheme.amber),
          if (last != null)
            hudKeyValue('last', '${last.id}  ${last.status}', colour: _statusColour(last.status)),
          const SizedBox(height: 8),
          Row(children: [
            Expanded(
              child: HudButton(
                label: 'BACK UP NOW',
                busy: _starting || overview.running != null,
                onTap: _canOperate && overview.available ? _backupNow : null,
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              child: HudButton(
                label: 'SETTINGS',
                colour: ReactorTheme.amber,
                onTap: _isOwner ? () => _editSettings(overview) : null,
              ),
            ),
          ]),
        ],
      ),
    );
  }

  Widget _backupRow(BackupEntry backup) {
    final colour = _statusColour(backup.status);
    final restorable = _isOwner && backup.parts.any((p) => p.ok) && backup.status != 'RUNNING';
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(width: 2, height: 40, color: colour),
          const SizedBox(width: 8),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(children: [
                  Expanded(
                      child: Text(backup.id,
                          style: ReactorTheme.data(size: 12, weight: FontWeight.w600))),
                  Text(backup.status, style: ReactorTheme.label(size: 9, color: colour)),
                ]),
                Text(
                    '${backup.trigger.toLowerCase().replaceAll('_', '-')} · ${backup.requestedBy} · '
                    '${formatBytes(backup.totalBytes)}',
                    style: ReactorTheme.data(size: 10.5, color: ReactorTheme.textSecondary)),
                ...backup.parts.map((p) => Text(
                      '${p.kind}: ${p.ok ? formatBytes(p.bytes) : p.message}',
                      style: ReactorTheme.data(
                          size: 10, color: p.ok ? ReactorTheme.textFaint : ReactorTheme.critical),
                    )),
              ],
            ),
          ),
          const SizedBox(width: 8),
          HudButton(
            label: 'RESTORE',
            colour: ReactorTheme.amber,
            onTap: restorable ? () => _restore(backup) : null,
          ),
        ],
      ),
    );
  }

  static Color _statusColour(String status) {
    switch (status) {
      case 'COMPLETE':
        return ReactorTheme.cyan;
      case 'RUNNING':
      case 'PARTIAL':
        return ReactorTheme.amber;
      default:
        return ReactorTheme.critical;
    }
  }
}

class _RestoreChoice {
  final List<String> parts;
  final String mode;

  _RestoreChoice(this.parts, this.mode);
}

class _RestoreChoiceDialog extends StatefulWidget {
  final BackupEntry backup;

  const _RestoreChoiceDialog({required this.backup});

  @override
  State<_RestoreChoiceDialog> createState() => _RestoreChoiceDialogState();
}

class _RestoreChoiceDialogState extends State<_RestoreChoiceDialog> {
  late bool _mongo = widget.backup.hasPart('mongo');
  bool _redis = false;
  String _mode = 'scratch';

  @override
  Widget build(BuildContext context) {
    final parts = [if (_mongo) 'mongo', if (_redis) 'redis'];
    return AlertDialog(
      backgroundColor: ReactorTheme.panel,
      shape: const RoundedRectangleBorder(borderRadius: BorderRadius.zero),
      title: Text('Restore ${widget.backup.id}', style: ReactorTheme.heading(size: 14)),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          CheckboxListTile(
            dense: true,
            value: _mongo,
            onChanged: widget.backup.hasPart('mongo') ? (v) => setState(() => _mongo = v ?? false) : null,
            title: Text('Mongo', style: ReactorTheme.data(size: 12)),
          ),
          CheckboxListTile(
            dense: true,
            value: _redis,
            onChanged: widget.backup.hasPart('redis') ? (v) => setState(() => _redis = v ?? false) : null,
            title: Text('Redis', style: ReactorTheme.data(size: 12)),
          ),
          const SizedBox(height: 6),
          RadioGroup<String>(
            groupValue: _mode,
            onChanged: (v) => setState(() => _mode = v ?? 'scratch'),
            child: Column(children: [
              RadioListTile<String>(
                dense: true,
                value: 'scratch',
                title: Text('Scratch (safe)', style: ReactorTheme.data(size: 12)),
                subtitle: Text('Mongo into restore_<db>; Redis verified only',
                    style: ReactorTheme.data(size: 10, color: ReactorTheme.textFaint)),
              ),
              RadioListTile<String>(
                dense: true,
                value: 'live',
                title: Text('Live (replaces production data)',
                    style: ReactorTheme.data(size: 12, color: ReactorTheme.critical)),
                subtitle: Text('a pre-restore backup is taken first',
                    style: ReactorTheme.data(size: 10, color: ReactorTheme.textFaint)),
              ),
            ]),
          ),
        ],
      ),
      actions: [
        TextButton(
            onPressed: () => Navigator.pop(context),
            child: Text('CANCEL', style: ReactorTheme.label(size: 10))),
        TextButton(
            onPressed: parts.isEmpty ? null : () => Navigator.pop(context, _RestoreChoice(parts, _mode)),
            child: Text('PREVIEW', style: ReactorTheme.label(size: 10, color: ReactorTheme.amber))),
      ],
    );
  }
}

/// Step two. A live restore additionally needs the backup id typed back, so it cannot be
/// confirmed by a reflexive tap.
class _RestoreConfirmDialog extends StatefulWidget {
  final BackupEntry backup;
  final _RestoreChoice choice;
  final RestorePreview preview;

  const _RestoreConfirmDialog({required this.backup, required this.choice, required this.preview});

  @override
  State<_RestoreConfirmDialog> createState() => _RestoreConfirmDialogState();
}

class _RestoreConfirmDialogState extends State<_RestoreConfirmDialog> {
  final _typed = TextEditingController();

  bool get _live => widget.choice.mode == 'live';

  @override
  Widget build(BuildContext context) {
    final ready = !_live || _typed.text.trim() == widget.backup.id;
    return AlertDialog(
      backgroundColor: ReactorTheme.panel,
      shape: const RoundedRectangleBorder(borderRadius: BorderRadius.zero),
      title: Text(_live ? 'Confirm LIVE restore' : 'Confirm scratch restore',
          style: ReactorTheme.heading(
              size: 14, color: _live ? ReactorTheme.critical : ReactorTheme.amber)),
      content: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            ...widget.preview.steps.map((s) => Padding(
                  padding: const EdgeInsets.only(bottom: 4),
                  child: Text('• $s', style: ReactorTheme.data(size: 11)),
                )),
            ...widget.preview.warnings.map((w) => Padding(
                  padding: const EdgeInsets.only(top: 4),
                  child: Text('! $w', style: ReactorTheme.data(size: 11, color: ReactorTheme.amber)),
                )),
            const SizedBox(height: 8),
            Text('Confirmation expires in ${widget.preview.expiresInSeconds}s.',
                style: ReactorTheme.data(size: 10, color: ReactorTheme.textFaint)),
            if (_live) ...[
              const SizedBox(height: 10),
              Text('Type the backup id to confirm:',
                  style: ReactorTheme.data(size: 11, color: ReactorTheme.textSecondary)),
              TextField(
                controller: _typed,
                style: ReactorTheme.data(size: 12),
                decoration: InputDecoration(hintText: widget.backup.id),
                onChanged: (_) => setState(() {}),
              ),
            ],
          ],
        ),
      ),
      actions: [
        TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: Text('CANCEL', style: ReactorTheme.label(size: 10))),
        TextButton(
            onPressed: ready ? () => Navigator.pop(context, true) : null,
            child: Text('RESTORE',
                style: ReactorTheme.label(
                    size: 10, color: ready ? ReactorTheme.critical : ReactorTheme.textFaint))),
      ],
    );
  }
}
