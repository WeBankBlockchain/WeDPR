package com.webank.wedpr.components.user.helper;

import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.crypto.SM2Helper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bouncycastle.crypto.InvalidCipherTextException;

public class PasswordHelper {

    /** 强密码判断规则：验证一个至少包含8个字符，并且至少包含一个大写字母、一个小写字母、一个数字和一个特殊字符的密码 */
    private static final String STRONG_PASSWORD_PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{8,}$";

    // 解密前端传入密码
    public static String decryptPassword(String cipherText, String privateKey)
            throws WeDPRException {
        try {
            cipherText = "04" + cipherText; // js sm2加密结果后端必须加 "0x04"
            return SM2Helper.decrypt(privateKey, cipherText);
        } catch (InvalidCipherTextException e) {
            throw new WeDPRException(e.getMessage());
        }
    }

    // 加密传入密码
    public static String encryptPassword(String plainText, String publicKey) throws WeDPRException {
        try {
            return SM2Helper.encrypt(publicKey, plainText);
        } catch (InvalidCipherTextException e) {
            throw new WeDPRException(e.getMessage());
        }
    }

    // 判断是否是强密码
    public static boolean isStrongNonValid(String password) {
        Pattern pattern = Pattern.compile(STRONG_PASSWORD_PATTERN);
        Matcher matcher = pattern.matcher(password);
        return !matcher.matches();
    }
}
