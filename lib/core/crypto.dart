import 'package:encrypt/encrypt.dart';

class CryptoData {
  String? base64IV;
  String base64Key;
  String data;
  
  CryptoData({this.base64IV, required this.data, required this.base64Key});
}

class Crypto {
  static CryptoData encrypt(CryptoData data) {
    final _key = Key.fromBase64(data.base64Key);
    final _iv = IV.fromLength(16);

    final _cipher = Encrypter(AES(_key));
    
    return CryptoData(
        base64IV: _iv.base64,
        data: _cipher.encrypt(data.data, iv: _iv).base64,
        base64Key: data.base64Key
    );
  }

  static String decrypt(CryptoData data) {
    final _key = Key.fromBase64(data.base64Key);
    final _iv = IV.fromBase64(data.base64IV!);

    final _cipher = Encrypter(AES(_key));
    return _cipher.decrypt(Encrypted.fromBase64(data.data), iv: _iv).toString();
  }

  static String generateKey(int keySize) {
    return Key.fromLength(keySize).base64;
  }
}