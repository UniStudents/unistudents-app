import 'package:encrypt/encrypt.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class Crypto {

  static const _ss = FlutterSecureStorage();
  static late String _encKey;

  static Future<void> init() async {
    String? key = await _ss.read(key: "storage_encryption_key");
    if(key != null) {
      _encKey = key;
      return;
    }

    // Generate key
    _encKey = Key.fromLength(16).base64;
    await _ss.write(key: "storage_encryption_key", value: _encKey);
  }

  static String encrypt(String data) {
    final _key = Key.fromBase64(_encKey.split('-')[0]);
    final _iv = IV.fromLength(16);

    final _cipher = Encrypter(AES(_key));
    return _cipher.encrypt(data, iv: _iv).base64 + '-' + _iv.base64;
  }

  static String decrypt(String cipher) {
    final _key = Key.fromBase64(_encKey);
    final _iv = IV.fromBase64(cipher.split('-')[1]);

    final _cipher = Encrypter(AES(_key));
    return _cipher.decrypt(Encrypted.fromBase64(cipher.split('-')[0]), iv: _iv).toString();
  }

  static String generateKey(int keySize) {
    return Key.fromLength(keySize).base64;
  }
}