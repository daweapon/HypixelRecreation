import 'package:flutter/material.dart';

import '../api/control_api.dart';
import '../theme/reactor_theme.dart';
import '../widgets/admin_kit.dart';
import '../widgets/chamfer.dart';

/// Profiles nobody owns, islands nothing points at, and co-ops pointing at missing profiles.
/// Owners can delete an orphaned island or profile after a typed second confirmation.
///
/// Constructor: `OrphansScreen(api: controlApi)`.
class OrphansScreen extends StatefulWidget {
  final ControlApi api;

  const OrphansScreen({super.key, required this.api});

  @override
  State<OrphansScreen> createState() => _OrphansScreenState();
}

class _OrphansScreenState extends State<OrphansScreen> {
  OrphanReport? _report;
  String? _error;
  String? _busyId;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load({bool refresh = false}) async {
    try {
      final report = await widget.api.fetchOrphans(refresh: refresh);
      if (mounted) {
        setState(() {
          _report = report;
          _error = null;
        });
      }
    } catch (e) {
      if (mounted) setState(() => _error = e.toString());
    }
  }

  Future<void> _delete(Orphan orphan) async {
    final what = orphan.kind.toLowerCase();
    if (!await adminConfirm(context, 'Delete orphaned $what?',
        '${orphan.id}\n${orphan.reason}\n\nThe server re-scans first and refuses if anything now points at it.',
        'CONTINUE')) {
      return;
    }
    if (!mounted) return;
    final shortId = orphan.id.length > 8 ? orphan.id.substring(0, 8) : orphan.id;
    if (!await adminConfirmByTyping(context, 'Second confirmation', 'Deleting cannot be undone.', shortId)) {
      return;
    }

    setState(() => _busyId = orphan.id);
    try {
      final detail = await widget.api.deleteOrphan(orphan.kind, orphan.id, confirm: orphan.id);
      if (mounted) adminNotify(context, detail);
      await _load(refresh: true);
    } catch (e) {
      if (mounted) adminNotify(context, e.toString(), colour: ReactorTheme.critical);
    } finally {
      if (mounted) setState(() => _busyId = null);
    }
  }

  @override
  Widget build(BuildContext context) {
    return AdminScaffold(title: 'ORPHANS', onRefresh: () => _load(refresh: true), body: _body());
  }

  Widget _body() {
    if (_error != null) return AdminMessage(_error!, colour: ReactorTheme.critical);
    final report = _report;
    if (report == null) return const AdminSpinner();

    final counts = ['PROFILE', 'ISLAND', 'COOP'].map((kind) => '${report.counts[kind] ?? 0} ${kind.toLowerCase()}').join('  ·  ');
    return HudPanel(
      title: counts,
      borderColor: report.orphans.isEmpty ? ReactorTheme.hairline : ReactorTheme.amber,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Text(
            'scanned ${report.profiles} profiles, ${report.accounts} accounts, ${report.islands} islands, '
            '${report.coops} co-ops${report.scannedAt == null ? '' : ' at ${adminStamp(report.scannedAt!)}'}',
            style: ReactorTheme.data(size: 10, color: ReactorTheme.textFaint),
          ),
          const SizedBox(height: 8),
          Flexible(
            child: report.orphans.isEmpty
                ? Text('nothing orphaned', style: ReactorTheme.data(size: 11, color: ReactorTheme.cyan))
                : ListView.builder(
                    shrinkWrap: true,
                    itemCount: report.orphans.length,
                    itemBuilder: (context, index) => _row(report.orphans[index]),
                  ),
          ),
        ],
      ),
    );
  }

  Widget _row(Orphan orphan) {
    final colour = orphan.kind == 'COOP' ? ReactorTheme.amber : ReactorTheme.critical;
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(width: 2, height: 34, color: colour),
          const SizedBox(width: 8),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(children: [
                  Text(orphan.kind, style: ReactorTheme.label(size: 9, color: colour, spacing: 1.2)),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(orphan.id,
                        overflow: TextOverflow.ellipsis, style: ReactorTheme.data(size: 11)),
                  ),
                ]),
                Text(orphan.reason, style: ReactorTheme.data(size: 10, color: ReactorTheme.textSecondary)),
                if (orphan.related != null)
                  Text(orphan.related!, style: ReactorTheme.data(size: 9, color: ReactorTheme.textFaint)),
              ],
            ),
          ),
          if (widget.api.isOwner && orphan.deletable)
            _busyId == orphan.id
                ? const SizedBox(
                    width: 12,
                    height: 12,
                    child: CircularProgressIndicator(strokeWidth: 1.5, color: ReactorTheme.cyan))
                : AdminChip('DELETE', colour: ReactorTheme.critical, onTap: () => _delete(orphan)),
        ],
      ),
    );
  }
}
