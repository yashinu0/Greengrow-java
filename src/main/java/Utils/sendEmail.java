package Utils;
import java.util.Properties;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class sendEmail {
    private static final String FROM_EMAIL = "greengrowfeed@gmail.com";
    private static final String EMAIL_PASSWORD = "cvopffpsysijqnqw"; // Utilisez un mot de passe d'application

    public static boolean sendVerificationEmail(String toEmail, String verificationCode) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");

        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(FROM_EMAIL, EMAIL_PASSWORD);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Réinitialisation de mot de passe - GreenGrow");

            String emailContent = "Bonjour,\n\n"
                    + "Votre code de vérification est : " + verificationCode + "\n\n"
                    + "Ce code expirera dans 15 minutes.\n\n"
                    + "Cordialement,\nL'équipe GreenGrow";

            message.setText(emailContent);

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            System.err.println("Erreur d'envoi d'email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}