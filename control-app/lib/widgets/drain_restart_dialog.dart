import 'dart:async';

import 'package:flutter/material.dart';

import '../api/live_api.dart';
import '../theme/reactor_theme.dart';

/// Confirms, starts and follows a drain-and-restart of one game server.
///
/// Players are moved off first (SkyBlock worlds to the hub, the hub to islands, other games
/// to the lobby), then the container is restarted once empty or when the timeout passes.
Future<void> showDrainRestartDialog(BuildContext context, LiveApi live, String container) async {
  final confirmed = await showDialog<bool>(
    context: context,
    builder: (context) => AlertDialog(
      backgroundColor: ReactorTheme.panel,
      shape: const RoundedRectangleBorder(borderRadius: BorderRadius.zero),
      title: Text('Confirm DRAIN + RESTART',
          style: ReactorTheme.heading(size: 14, color: ReactorTheme.amber)),
      content: Text(
          'Players on $container are sent to another server first, then it restarts '
          'once empty or after 45 seconds.',
          style: ReactorTheme.data(size: 12, color: ReactorTheme.textSecondary)),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context, false),
          child: Text('CANCEL', style: ReactorTheme.label(size: 10)),
        ),
        TextButton(
          onPressed: () => Navigator.pop(context, true),
          child: Text('DRAIN + RESTART',
              style: ReactorTheme.label(size: 10, color: ReactorTheme.amber)),
        ),
      ],
    ),
  );
  if (confirmed != true || !context.mounted) return;

  await showDialog<void>(
    context: context,
    barrierDismissible: false,
    builder: (_) => _DrainProgressDialog(live: live, container: container),
  );
}

class _DrainProgressDialog extends StatefulWidget {
  final LiveApi live;
  final String container;

  const _DrainProgressDialog({required this.live, required this.container});

  @override
  State<_DrainProgressDialog> createState() => _DrainProgressDialogState();
}

class _DrainProgressDialogState extends State<_DrainProgressDialog> {
  static const Duration _poll = Duration(seconds: 2);

  DrainJob? _job;
  String? _error;
  Timer? _timer;

  @override
  void initState() {
    super.initState();
    _start();
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  Future<void> _start() async {
    try {
      final job = await widget.live.drainRestart(widget.container);
      if (!mounted) return;
      setState(() => _job = job);
      _timer = Timer.periodic(_poll, (_) => _refresh());
    } catch (e) {
      if (mounted) setState(() => _error = e.toString());
    }
  }

  Future<void> _refresh() async {
    final job = _job;
    if (job == null) return;
    try {
      final latest = await widget.live.fetchDrainJob(job.id);
      if (!mounted) return;
      setState(() {
        _job = latest;
        _error = null;
      });
      if (latest.isFinished) _timer?.cancel();
    } catch (e) {
      if (mounted) setState(() => _error = e.toString());
    }
  }

  @override
  Widget build(BuildContext context) {
    final job = _job;
    final finished = (_error != null && job == null) || (job?.isFinished ?? false);
    final colour = job == null
        ? (_error != null ? ReactorTheme.critical : ReactorTheme.cyan)
        : job.failed
            ? ReactorTheme.critical
            : job.isFinished
                ? ReactorTheme.cyan
                : ReactorTheme.amber;

    return AlertDialog(
      backgroundColor: ReactorTheme.panel,
      shape: const RoundedRectangleBorder(borderRadius: BorderRadius.zero),
      title: Text('${job?.phase ?? 'STARTING'}  ·  ${widget.container}',
          style: ReactorTheme.heading(size: 13, color: colour)),
      content: SizedBox(
        width: 380,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (job != null) ...[
              Text(
                  'players ${job.playersAtStart < 0 ? '?' : job.playersAtStart}'
                  ' → ${job.playersLeft < 0 ? '?' : job.playersLeft} left'
                  '${job.destination == null ? '' : '  ·  to ${job.destination}'}',
                  style: ReactorTheme.data(size: 11, color: ReactorTheme.textPrimary)),
              const SizedBox(height: 8),
              for (final line in job.log)
                Text('· $line', style: ReactorTheme.data(size: 10.5, color: ReactorTheme.textSecondary)),
            ],
            if (_error != null)
              Text(_error!, style: ReactorTheme.data(size: 11, color: ReactorTheme.critical)),
            if (!finished)
              const Padding(
                padding: EdgeInsets.only(top: 10),
                child: LinearProgressIndicator(
                    minHeight: 2, color: ReactorTheme.amber, backgroundColor: ReactorTheme.hairline),
              ),
          ],
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: Text(finished ? 'CLOSE' : 'HIDE', style: ReactorTheme.label(size: 10)),
        ),
      ],
    );
  }
}
