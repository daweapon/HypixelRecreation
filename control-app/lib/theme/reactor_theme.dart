import 'package:flutter/material.dart';

/// The Reactor HUD palette and type scale.
///
/// Dense dark operations console: small type, high information density, status always shown as
/// colour *and* a short text code so it never depends on colour alone. No rounded cards -
/// panels are chamfered with hairline borders.
class ReactorTheme {
  ReactorTheme._();

  static const Color ground = Color(0xFF06090D);
  static const Color panel = Color(0xFF0B1118);
  static const Color panelRaised = Color(0xFF101922);
  static const Color hairline = Color(0xFF1E2B36);

  static const Color cyan = Color(0xFF49D4E8);
  static const Color amber = Color(0xFFE8A33D);
  static const Color critical = Color(0xFFFF6F5E);

  static const Color textPrimary = Color(0xFFD6E4EC);
  static const Color textSecondary = Color(0xFF7D93A3);
  static const Color textFaint = Color(0xFF475A68);

  /// Status colours, keyed by the short code the UI also prints.
  static Color forStatus(String code) {
    switch (code) {
      case 'UP':
        return cyan;
      case 'WRN':
        return amber;
      case 'LOOP':
        return amber;
      case 'EXIT':
      case 'DWN':
        return critical;
      default:
        return textFaint;
    }
  }

  static Color forSeverity(String severity) {
    switch (severity) {
      case 'CRITICAL':
        return critical;
      case 'WARNING':
        return amber;
      case 'RECOVERY':
      case 'NOMINAL':
        return cyan;
      default:
        return textSecondary;
    }
  }

  // Labels and headings.
  static const String labelFamily = 'ChakraPetch';

  // All data: numbers, container names, log lines, timestamps.
  static const String dataFamily = 'IBMPlexMono';

  static TextStyle label({
    double size = 11,
    Color color = textSecondary,
    FontWeight weight = FontWeight.w500,
    double spacing = 1.4,
  }) =>
      TextStyle(
        fontFamily: labelFamily,
        fontSize: size,
        color: color,
        fontWeight: weight,
        letterSpacing: spacing,
      );

  static TextStyle heading({
    double size = 15,
    Color color = textPrimary,
    FontWeight weight = FontWeight.w600,
  }) =>
      TextStyle(
        fontFamily: labelFamily,
        fontSize: size,
        color: color,
        fontWeight: weight,
        letterSpacing: 0.8,
      );

  static TextStyle data({
    double size = 12,
    Color color = textPrimary,
    FontWeight weight = FontWeight.w400,
  }) =>
      TextStyle(
        fontFamily: dataFamily,
        fontSize: size,
        color: color,
        fontWeight: weight,
        height: 1.35,
      );

  static ThemeData build() {
    return ThemeData(
      useMaterial3: true,
      brightness: Brightness.dark,
      scaffoldBackgroundColor: ground,
      canvasColor: ground,
      colorScheme: const ColorScheme.dark(
        surface: panel,
        primary: cyan,
        secondary: amber,
        error: critical,
        onSurface: textPrimary,
      ),
      textTheme: TextTheme(
        bodyMedium: data(),
        bodySmall: data(size: 11, color: textSecondary),
        titleMedium: heading(),
      ),
      dividerColor: hairline,
      splashFactory: NoSplash.splashFactory,
      highlightColor: Colors.transparent,
    );
  }
}
