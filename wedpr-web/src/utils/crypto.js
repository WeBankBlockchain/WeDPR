/* eslint-disable */
import { defaultPublicKey } from './config'
const encrypt = (data) => {
  const ascii = data
    .toString()
    .split('')
    .map((i) => `3${i}`)
    .join('')
  const plaintext = KeyouCryptography.util.Hex.parse(ascii)
  const publicKey = KeyouCryptography.util.Hex.parse(defaultPublicKey)
  const encrypted = KeyouCryptography.algorithm.SM2.encrypt(plaintext, publicKey)
  return KeyouCryptography.util.Hex.stringify(encrypted)
}

export { encrypt }
