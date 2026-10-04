import 'dart:async';

import 'package:flutter/material.dart';

import '../api/control_api.dart';
import '../models/fleet.dart';
import '../theme/reactor_theme.dart';
import '../widgets/chamfer.dart';
import '../widgets/job_banner.dart';

/// Deploys a jar staged in the drop folder.
///
/// The operator copies a built jar to the drop share (which needs no privilege on the NAS),
/// and the agent does the privileged part: verify, persist, push into each container, restart
/// staggered, confirm. Deploying requires OWNER - pushing arbitrary new code is a bigger grant
/// than restarting existing code.
class DeployScreen extends StatefulWidget {
  final ControlApi api;
  final bool canDeploy;

  const DeployScreen({super.key, required this.api, required this.canDeploy});

  @override
  State<DeployScreen> createState() => _DeployScreenState();
}

class _DeployScreenState extends State<DeployScreen> {
  DeployCandidates? _candidates;
  String? _error;
  OperationJob? _job;
  Timer? _jobTimer;

  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void dispose() {
    _jobTimer?.cancel();
    super.dispose();
  }

  Future<void> _load() async {
    try {
      final result = await widget.api.fetchDeployCandidates();
      if (mounted) {
        setState(() {
          _candidates = result;
          _error = null;
        });
      }
    } catch (e) {
      if (mounted) setState(() => _error = e.toString());
    }
  }

  Future<void> _deploy(JarCandidate jar) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        backgroundColor: ReactorTheme.panel,
        shape: const RoundedRectangleBorder(borderRadius: BorderRadius.zero),
        title: Text('Deploy ${jar.name}?',
            style: ReactorTheme.heading(size: 14, color: ReactorTheme.amber)),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              jar.defaultTargets.isEmpty
                  ? 'No target containers resolved for this jar. Deploy will be refused.'
                  : 'This restarts ${jar.defaultTargets.length} container(s):\n'
                      '${jar.defaultTargets.join('\n')}',
              style: ReactorTheme.data(size: 11.5, color: ReactorTheme.textSecondary),
            ),
            const SizedBox(height: 10),
            Text('sha256 ${jar.shortHash}  ·  ${jar.sizeLabel}',
                style: ReactorTheme.data(size: 10, color: ReactorTheme.textFaint)),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: Text('CANCEL', style: ReactorTheme.label(size: 10)),
          ),
          TextButton(
            onPressed: () => Navigator.pop(context, true),
            child: Text('DEPLOY',
                style: ReactorTheme.label(size: 10, color: ReactorTheme.amber)),
          ),
        ],
      ),
    );
    if (confirmed != true) return;

    try {
      final job = await widget.api.deploy(jar.name);
      if (!mounted) return;
      setState(() => _job = job);
      _follow(job.id);
    } catch (e) {
      if (mounted) setState(() => _error = e.toString());
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
      appBar: AppBar(
        backgroundColor: ReactorTheme.panel,
        elevation: 0,
        shape: const Border(bottom: BorderSide(color: ReactorTheme.hairline)),
        title: Text('DEPLOY', style: ReactorTheme.heading(size: 13)),
        iconTheme: const IconThemeData(color: ReactorTheme.textSecondary, size: 18),
        actions: [
          IconButton(
            iconSize: 16,
            icon: const Icon(Icons.refresh, color: ReactorTheme.textSecondary),
            onPressed: _load,
          ),
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(10),
        child: Column(
          children: [
            if (_job != null) ...[
              JobBanner(job: _job!, onDismiss: () => setState(() => _job = null)),
              const SizedBox(height: 10),
            ],
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
    final result = _candidates;
    if (result == null) {
      return const Center(
          child: SizedBox(
              width: 16,
              height: 16,
              child: CircularProgressIndicator(strokeWidth: 1.5, color: ReactorTheme.cyan)));
    }

    if (!result.dropAvailable) {
      return HudPanel(
        title: 'drop folder unavailable',
        child: Text(
          'The agent cannot see ${result.dropDirectory}. Check the DEPLOY_DROP_DIR mount on '
          'service_control.',
          style: ReactorTheme.data(size: 11, color: ReactorTheme.textSecondary),
        ),
      );
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Text('DROP  ${result.dropDirectory}',
            style: ReactorTheme.label(size: 9, color: ReactorTheme.textFaint)),
        const SizedBox(height: 8),
        if (!widget.canDeploy)
          Padding(
            padding: const EdgeInsets.only(bottom: 8),
            child: Text(
              'Deploying requires OWNER. This device can view staged jars only.',
              style: ReactorTheme.data(size: 11, color: ReactorTheme.amber),
            ),
          ),
        Expanded(
          child: result.candidates.isEmpty
              ? Center(
                  child: Text('no jars staged',
                      style: ReactorTheme.data(size: 11, color: ReactorTheme.textFaint)))
              : ListView.builder(
                  itemCount: result.candidates.length,
                  itemBuilder: (context, index) => _row(result.candidates[index]),
                ),
        ),
      ],
    );
  }

  Widget _row(JarCandidate jar) {
    final deployable = widget.canDeploy && jar.defaultTargets.isNotEmpty;

    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: HudPanel(
        padding: const EdgeInsets.all(10),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
            Row(
              children: [
                Expanded(
                  child: Text(jar.name,
                      style: ReactorTheme.data(
                          size: 13, color: ReactorTheme.textPrimary, weight: FontWeight.w600)),
                ),
                if (jar.matchesStaged)
                  Text('ALREADY DEPLOYED',
                      style: ReactorTheme.label(size: 8, color: ReactorTheme.textFaint)),
              ],
            ),
            const SizedBox(height: 4),
            Text('${jar.sizeLabel}  ·  sha256 ${jar.shortHash}',
                style: ReactorTheme.data(size: 10, color: ReactorTheme.textFaint)),
            const SizedBox(height: 6),
            Text(
              jar.defaultTargets.isEmpty
                  ? 'no target containers resolved'
                  : '→ ${jar.defaultTargets.join(', ')}',
              style: ReactorTheme.data(
                  size: 10.5,
                  color: jar.defaultTargets.isEmpty
                      ? ReactorTheme.amber
                      : ReactorTheme.textSecondary),
            ),
            const SizedBox(height: 8),
            GestureDetector(
              onTap: deployable ? () => _deploy(jar) : null,
              child: Container(
                padding: const EdgeInsets.symmetric(vertical: 9),
                decoration: ShapeDecoration(
                  color: deployable
                      ? ReactorTheme.amber.withValues(alpha: 0.10)
                      : ReactorTheme.panelRaised,
                  shape: ChamferBorder(
                    cut: 5,
                    color: deployable
                        ? ReactorTheme.amber.withValues(alpha: 0.55)
                        : ReactorTheme.hairline,
                  ),
                ),
                child: Center(
                  child: Text(
                    jar.matchesStaged ? 'REDEPLOY' : 'DEPLOY',
                    style: ReactorTheme.label(
                        size: 10,
                        spacing: 1.6,
                        color: deployable ? ReactorTheme.amber : ReactorTheme.textFaint),
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
