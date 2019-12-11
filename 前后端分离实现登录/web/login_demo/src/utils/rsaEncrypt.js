import JSEncrypt from 'jsencrypt/bin/jsencrypt'

// 密钥对生成 http://web.chacuo.net/netrsakeypair

const publicKey = 'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDGum1HWUcKktac1q7hwWUpczk76catb3Koq3mKjRPvDA89UXPhugGQ4vcGmZCclZtqu40Ae2mUaGcwC6u8PL/H8q+4IIw2vuoiYAONc8RXRZ0CHHznc6dx37PbnznfDVWuTH+lXvSl7/ocSB/imsz8ivj/hTamOQGK8wSN3W4DjwIDAQAB'

const privateKey =''
// 加密
export function encrypt(txt) {
  const encryptor = new JSEncrypt()
  encryptor.setPublicKey(publicKey) // 设置公钥
  return encryptor.encrypt(txt) // 对需要加密的数据进行加密
}

// 解密
export function decrypt(txt) {
  const encryptor = new JSEncrypt()
  encryptor.setPrivateKey(privateKey)
  return encryptor.decrypt(txt)
}

