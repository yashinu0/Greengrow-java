package Services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailService {
    private final String username = "greengrowfeed@gmail.com";
    private final String password = "ldobildswprpnola";
    
    public void sendFeedbackEmail(String fromEmail, String name, String subject, String message) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        
        try {
            Message mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(username));
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(username));
            mimeMessage.setSubject("Nouveau feedback de " + name + ": " + subject);
            
            String emailContent = String.format("""
                Nouveau feedback reçu :
                
                De : %s
                Email : %s
                Sujet : %s
                
                Message :
                %s
                """, name, fromEmail, subject, message);
            
            mimeMessage.setText(emailContent);
            
            System.out.println("Tentative d'envoi de l'email avec Jakarta Mail...");
            Transport.send(mimeMessage);
            System.out.println("Email envoyé avec succès à " + username);
            
        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
} 