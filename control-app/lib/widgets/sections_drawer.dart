import 'package:flutter/material.dart';

import '../api/control_api.dart';
import '../screens/alerts_screen.dart';
import '../screens/audit_screen.dart';
import '../screens/backups_screen.dart';
import '../screens/capacity_screen.dart';
import '../screens/deploy_screen.dart';
import '../screens/devices_screen.dart';
import '../screens/economy_screen.dart';
import '../screens/events_screen.dart';
import '../screens/exceptions_screen.dart';
import '../screens/integrity_screen.dart';
import '../screens/orphans_screen.dart';
import '../screens/players_screen.dart';
import '../screens/roadmap_screen.dart';
import '../screens/second_factor_screen.dart';
import '../theme/reactor_theme.dart';

class SectionsDrawer extends StatelessWidget {
  final ControlApi api;
  final String role;

  const SectionsDrawer({super.key, required this.api, required this.role});

  bool get _owner => role == 'OWNER';

  @override
  Widget build(BuildContext context) {
    return Drawer(
      backgroundColor: ReactorTheme.panel,
      child: SafeArea(
        child: ListView(
          children: [
            _heading('Fleet'),
            _entry(context, Icons.hub_outlined, 'Integrity', () => IntegrityScreen(api: api)),
            _entry(context, Icons.stacked_bar_chart, 'Capacity and queue', () => CapacityScreen(api: api)),
            _entry(context, Icons.upload_file, 'Deploy', () => DeployScreen(api: api, canDeploy: _owner)),
            _entry(context, Icons.bug_report_outlined, 'Exceptions', () => ExceptionsScreen(api: api)),
            _entry(context, Icons.notifications_outlined, 'Alerts', () => AlertsScreen(api: api, canTest: _owner)),
            _entry(context, Icons.backup_outlined, 'Backups', () => BackupsScreen(api: api, role: role)),
            _heading('Game'),
            _entry(context, Icons.people_outline, 'Players', () => PlayersScreen(api: api)),
            _entry(context, Icons.event_outlined, 'Events and calendar', () => EventsScreen(api: api)),
            _entry(context, Icons.payments_outlined, 'Economy', () => EconomyScreen(api: api)),
            _entry(context, Icons.link_off, 'Orphans', () => OrphansScreen(api: api)),
            _heading('Admin'),
            _entry(context, Icons.map_outlined, 'Roadmap', () => RoadmapScreen(api: api)),
            _entry(context, Icons.receipt_long, 'Audit log', () => AuditScreen(api: api)),
            _entry(context, Icons.password, 'Second factor', () => SecondFactorScreen(api: api, isOwner: _owner)),
            if (_owner) _entry(context, Icons.devices_other, 'Paired devices', () => DevicesScreen(api: api)),
          ],
        ),
      ),
    );
  }

  Widget _heading(String text) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 18, 16, 6),
      child: Text(text.toUpperCase(),
          style: ReactorTheme.label(size: 9, spacing: 1.4, color: ReactorTheme.textFaint)),
    );
  }

  Widget _entry(BuildContext context, IconData icon, String label, Widget Function() screen) {
    return ListTile(
      dense: true,
      leading: Icon(icon, size: 18, color: ReactorTheme.textSecondary),
      title: Text(label, style: ReactorTheme.data(size: 12)),
      onTap: () {
        Navigator.of(context).pop();
        Navigator.of(context).push(MaterialPageRoute(builder: (_) => screen()));
      },
    );
  }
}
