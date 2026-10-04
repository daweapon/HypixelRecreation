import 'package:flutter/material.dart';

import 'api/control_api.dart';
import 'screens/dashboard_screen.dart';
import 'screens/enrol_screen.dart';
import 'state/app_state.dart';
import 'theme/reactor_theme.dart';

void main() {
  runApp(const ControlApp());
}

class ControlApp extends StatefulWidget {
  const ControlApp({super.key});

  @override
  State<ControlApp> createState() => _ControlAppState();
}

class _ControlAppState extends State<ControlApp> {
  final ControlApi _api = ControlApi();
  late final AppState _state = AppState(_api);
  bool _loaded = false;

  @override
  void initState() {
    super.initState();
    _api.load().then((_) {
      if (mounted) setState(() => _loaded = true);
    });
  }

  @override
  void dispose() {
    _state.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Hypixel Control',
      debugShowCheckedModeBanner: false,
      theme: ReactorTheme.build(),
      home: !_loaded
          ? const Scaffold(
              body: Center(
                child: SizedBox(
                  width: 18,
                  height: 18,
                  child: CircularProgressIndicator(strokeWidth: 1.5, color: ReactorTheme.cyan),
                ),
              ),
            )
          : _api.isEnrolled
              ? DashboardScreen(
                  state: _state,
                  api: _api,
                  onSignOut: () async {
                    await _api.signOut();
                    if (mounted) setState(() {});
                  },
                )
              : EnrolScreen(
                  api: _api,
                  onEnrolled: () => setState(() {}),
                ),
    );
  }
}
