package test.mail;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;
 
public class Main {
    String  d_email = "jtexttiling@gmail.com",
            d_password = "jtexttiling1",
            d_host = "smtp.gmail.com",
            d_port  = "465",
            m_to = "danielfernandezaller@gmail.com",
            m_subject = "PROBANDO",
            m_text = "Mail de prueba";
    
    public Main() {
        Properties props = new Properties();
        props.put("mail.smtp.user", d_email);
        props.put("mail.smtp.host", d_host);
        props.put("mail.smtp.port", d_port);
        props.put("mail.smtp.starttls.enable","true");
        props.put("mail.smtp.auth", "true");
        //props.put("mail.smtp.debug", "true");
        props.put("mail.smtp.socketFactory.port", d_port);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
 
        //SecurityManager security = System.getSecurityManager();
 
        try {
            Authenticator auth = new SMTPAuthenticator();
            Session session = Session.getInstance(props, auth);
            //session.setDebug(true);
 
            MimeMessage msg = new MimeMessage(session);
            msg.setText(m_text);
            msg.setSubject(m_subject);
            msg.setFrom(new InternetAddress("no-reply@jtexttiling.com"));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(m_to));
            Transport.send(msg);
        }
        catch (Exception mex) {
            mex.printStackTrace();
        } 
    }
    
    public static void main(String[] args) {
    	System.out.println("a ver ho");
        Main blah = new Main();
        System.out.println("dale niño");
    }
 
    private class SMTPAuthenticator extends javax.mail.Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(d_email, d_password);
        }
    }
}
