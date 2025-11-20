import org.mindrot.jbcrypt.BCrypt;

public class HashJBCrypt {
    public static void main(String[] args) {
        String raw = "1234567";

        String salt = BCrypt.gensalt(12);
        String hashed = BCrypt.hashpw(raw, salt);

        System.out.println("\n=== GEN JBCRYPT ===");
        System.out.println("Raw     : " + raw);
        System.out.println("Salt    : " + salt);
        System.out.println("Hashed  : " + hashed);
        System.out.println("Matches?: " + BCrypt.checkpw(raw, hashed));
        System.out.println("==================\n");
    }
}
