import { loginManageServer } from 'Api'
import { encrypt } from 'Utils/crypto.js'
export const passwordHanle = {
  data () {
    return {}
  },
  methods: {
    async encodePassword (password) {
      const factorRes = await loginManageServer.getRandom()
      if (factorRes.code === '0' && factorRes.data) {
        const { factor } = factorRes.data
        const handledPassword = encrypt(password + factor)
        return Promise.resolve({ code: '0', encodedPassword: handledPassword, factor })
      } else {
        return Promise.resolve({ code: 'error' })
      }
    }
  }
}
