package setxpro.email.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import setxpro.email.dtos.EmailRequestDto;
import setxpro.email.dtos.MessageEmailDto;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.util.Properties;

@Service
public class EmailService {

    @Value("${email.username}")
    private String username;

    @Value("${email.password}")
    private String password;

    public MessageEmailDto sentEmail(EmailRequestDto emailRequestDto) {
        try {
            Message message = new MimeMessage(session());
            message.setFrom(new InternetAddress(emailRequestDto.from()));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(emailRequestDto.to()));
            message.setSubject(emailRequestDto.subject());
            message.setText(emailRequestDto.message());

            if (!emailRequestDto.html().isEmpty()) {
                // Configura o conteúdo da mensagem como HTML
                String htmlContent = emailRequestDto.html();
                message.setContent(htmlContent, "text/html");
            }

            MimeBodyPart textoParte = new MimeBodyPart();
            textoParte.setText(emailRequestDto.message());

            // Cria uma parte para o anexo, se houver
            if (!emailRequestDto.base64Attachment().isEmpty()) {
                byte[] arquivoBytes = java.util.Base64.getDecoder().decode(emailRequestDto.base64Attachment());
                MimeBodyPart anexoParte = new MimeBodyPart();

                DataHandler dataHandler = new DataHandler(new ByteArrayDataSource(arquivoBytes, "application/pdf"));
                anexoParte.setDataHandler(dataHandler);
                anexoParte.setFileName(emailRequestDto.base64AttachmentName());

                // Adiciona as partes à mensagem
                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(textoParte);
                multipart.addBodyPart(anexoParte);

                // Configura a mensagem com partes múltiplas
                message.setContent(multipart);
            }

            Transport.send(message);
            return new MessageEmailDto("Email successfully sent!");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "topics.email.request.topic", groupId = "group_id")
    public void consumerEmail(EmailRequestDto emailRequestDto) {
        sentEmail(emailRequestDto);
    }

    public Session session() {
        Properties props = new Properties();
        // Configurações para envio de e-mails usando Amazon SES
        props.put("mail.smtp.host", "email-smtp.us-east-2.amazonaws.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.trust", "email-smtp.us-east-2.amazonaws.com");
        props.put("mail.smtp.from", "patrickpqdt87289@gmail.com");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        return session;
    }

}
