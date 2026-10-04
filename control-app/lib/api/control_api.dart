import 'dart:convert';
import 'dart:math';

import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:http/http.dart' as http;

import '../models/fleet.dart';

class ApiException implements Exception {
  final String message;
  final int? status;

  ApiException(this.message, {this.status});

  @override
  String toString() => message;
}

/// Client for service.api's control endpoints.
///
/// Holds the long-lived device token in secure storage and exchanges it for a short-lived
/// access token on demand, transparently re-exchanging when one expires.
class ControlApi {
  static const _storage = FlutterSecureStorage();
  static const _keyBaseUrl = 'control.baseUrl';
  static const _keyDeviceToken = 'control.deviceToken';

  String? _baseUrl;
  String? _deviceToken;
  String? _accessToken;
  DateTime? _accessTokenExpiry;

  String? role;
  String? playerName;

  String? get baseUrl => _baseUrl;
  bool get isEnrolled => _deviceToken != null && _baseUrl != null;

  Future<void> load() async {
    _baseUrl = await _storage.read(key: _keyBaseUrl);
    _deviceToken = await _storage.read(key: _keyDeviceToken);
  }

  Future<void> setBaseUrl(String url) async {
    var trimmed = url.trim();
    if (trimmed.endsWith('/')) trimmed = trimmed.substring(0, trimmed.length - 1);
    if (!trimmed.startsWith('http')) trimmed = 'http://$trimmed';
    _baseUrl = trimmed;
    await _storage.write(key: _keyBaseUrl, value: trimmed);
  }

  Future<void> signOut() async {
    _deviceToken = null;
    _accessToken = null;
    _accessTokenExpiry = null;
    role = null;
    await _storage.delete(key: _keyDeviceToken);
  }

  /// Generates the 8-character code the operator types into /verifyapi in game.
  static String generateAuthCode() {
    const alphabet = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
    final random = Random.secure();
    return List.generate(8, (_) => alphabet[random.nextInt(alphabet.length)]).join();
  }

  static String generateSessionId() {
    final random = Random.secure();
    final bytes = List<int>.generate(16, (_) => random.nextInt(256));
    return base64Url.encode(bytes).replaceAll('=', '');
  }

  Uri _uri(String path, [Map<String, String>? query]) {
    if (_baseUrl == null) throw ApiException('No server address configured');
    return Uri.parse('$_baseUrl/api$path').replace(queryParameters: query);
  }

  /// Step 1 of enrolment: register the code against a new session.
  Future<void> initiateAuth(String sessionId, String authCode) async {
    final response = await http.get(
      _uri('/initiateAuth'),
      headers: {'X-Auth-Code': authCode, 'X-Session-ID': sessionId},
    ).timeout(const Duration(seconds: 15));
    _ensureOk(response);
  }

  /// Step 2: once /verifyapi has been run in game, trade the session for a device token.
  Future<Map<String, dynamic>> enrol(String sessionId, String deviceName) async {
    final response = await http
        .post(
          _uri('/control/enrol'),
          headers: {'Content-Type': 'application/json'},
          body: jsonEncode({'sessionId': sessionId, 'deviceName': deviceName}),
        )
        .timeout(const Duration(seconds: 15));

    final payload = _decodeData(_ensureOk(response));
    _deviceToken = payload['deviceToken'] as String;
    role = payload['role'] as String?;
    playerName = payload['playerName'] as String?;
    await _storage.write(key: _keyDeviceToken, value: _deviceToken);
    return payload;
  }

  Future<void> _ensureAccessToken() async {
    final expiry = _accessTokenExpiry;
    if (_accessToken != null && expiry != null && DateTime.now().isBefore(expiry)) {
      return;
    }
    if (_deviceToken == null) throw ApiException('Not enrolled');

    final response = await http
        .post(
          _uri('/control/token'),
          headers: {'Content-Type': 'application/json'},
          body: jsonEncode({'deviceToken': _deviceToken}),
        )
        .timeout(const Duration(seconds: 15));

    final payload = _decodeData(_ensureOk(response));
    _accessToken = payload['accessToken'] as String;
    role = payload['role'] as String?;
    playerName = payload['playerName'] as String?;
    final ttl = (payload['expiresInSeconds'] as num?)?.toInt() ?? 900;
    // Refresh a minute early so a call never races the expiry.
    _accessTokenExpiry = DateTime.now().add(Duration(seconds: ttl - 60));
  }

  Future<dynamic> _authedGet(String path, [Map<String, String>? query]) async {
    await _ensureAccessToken();
    final response = await http.get(
      _uri(path, query),
      headers: {'Authorization': 'Bearer $_accessToken'},
    ).timeout(const Duration(seconds: 20));

    if (response.statusCode == 401) {
      // Access token rejected - drop it and retry once with a fresh one.
      _accessToken = null;
      await _ensureAccessToken();
      final retry = await http.get(
        _uri(path, query),
        headers: {'Authorization': 'Bearer $_accessToken'},
      ).timeout(const Duration(seconds: 20));
      return _decodeData(_ensureOk(retry));
    }
    return _decodeData(_ensureOk(response));
  }

  Future<dynamic> _authedPost(String path, Map<String, dynamic> body) async {
    await _ensureAccessToken();
    final response = await http
        .post(
          _uri(path),
          headers: {
            'Authorization': 'Bearer $_accessToken',
            'Content-Type': 'application/json',
          },
          body: jsonEncode(body),
        )
        .timeout(const Duration(seconds: 30));
    return _decodeData(_ensureOk(response));
  }

  Future<FleetSnapshot> fetchFleet({bool refresh = false}) async {
    final data = await _authedGet('/control/fleet', refresh ? {'refresh': 'true'} : null);
    return FleetSnapshot.fromJson(data as Map<String, dynamic>);
  }

  Future<LogResult> fetchLogs(String container, {int tail = 200, String mode = 'filtered'}) async {
    final data = await _authedGet('/control/logs', {
      'container': container,
      'tail': '$tail',
      'mode': mode,
    });
    return LogResult.fromJson(data as Map<String, dynamic>);
  }

  Future<List<AlertEntry>> fetchAlerts() async {
    final data = await _authedGet('/control/alerts') as Map<String, dynamic>;
    return ((data['recent'] as List?) ?? [])
        .map((e) => AlertEntry.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<OperationJob> act(String action, {List<String>? names, int? stagger}) async {
    final body = <String, dynamic>{'action': action};
    if (names != null) body['names'] = names;
    if (stagger != null) body['stagger'] = stagger;
    final data = await _authedPost('/control/action', body);
    return OperationJob.fromJson(data as Map<String, dynamic>);
  }

  Future<OperationJob> fetchJob(String id) async {
    final data = await _authedGet('/control/jobs', {'id': id});
    return OperationJob.fromJson(data as Map<String, dynamic>);
  }

  /// The API wraps everything in {status, message, data}; message carries JSON for some
  /// endpoints and a plain string for others.
  http.Response _ensureOk(http.Response response) {
    if (response.statusCode >= 500) {
      throw ApiException('Server error ${response.statusCode}', status: response.statusCode);
    }
    final decoded = _tryDecode(response.body);
    if (decoded is Map && decoded['status'] == 'ERROR') {
      throw ApiException(_describe(decoded['message']), status: response.statusCode);
    }
    if (response.statusCode >= 400) {
      throw ApiException('Request failed (${response.statusCode})', status: response.statusCode);
    }
    return response;
  }

  static String _describe(dynamic message) {
    if (message == null) return 'Request failed';
    final text = message.toString();
    final inner = _tryDecode(text);
    if (inner is Map && inner['error'] != null) return inner['error'].toString();
    return text;
  }

  dynamic _decodeData(http.Response response) {
    final decoded = _tryDecode(response.body);
    if (decoded is! Map) throw ApiException('Unexpected response shape');
    if (decoded['data'] != null) return decoded['data'];
    final message = decoded['message'];
    if (message is String) {
      final inner = _tryDecode(message);
      if (inner != null) return inner;
      return message;
    }
    return message;
  }

  static dynamic _tryDecode(String? body) {
    if (body == null || body.isEmpty) return null;
    try {
      return jsonDecode(body);
    } catch (_) {
      return null;
    }
  }
}
