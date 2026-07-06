package com.smartteach.tools;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码哈希生成器：用于生成 BCrypt 加密后的密码字符串。
 * 用法：在项目根目录执行
 *   mvn -q exec:java -Dexec.mainClass=com.smartteach.tools.PasswordHashGen -Dexec.args="admin123"
 * 或者写个测试类调用 main() 即可。
 *
 * 输出示例：admin123 -> $2a$10$xxxxxx...（将该值直接 update 到 sys_user.password 即可）
 */
public class PasswordHashGen {

    public static void main(String[] args) {
        String raw = args.length > 0 ? args[0] : "admin";
        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
        String hash = enc.encode(raw);
        System.out.println("明文: " + raw);
        System.out.println("BCrypt 哈希: " + hash);
        System.out.println();
        System.out.println("对应 SQL 更新语句:");
        System.out.println("UPDATE sys_user SET password='" + hash + "' WHERE username='admin';");
    }
}
