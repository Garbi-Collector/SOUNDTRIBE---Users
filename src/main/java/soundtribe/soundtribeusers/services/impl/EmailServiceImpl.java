package soundtribe.soundtribeusers.services.impl;

import soundtribe.soundtribeusers.dtos.notification.NotificationEmail;
import soundtribe.soundtribeusers.exceptions.SoundtribeUserEmailException;
import soundtribe.soundtribeusers.exceptions.SoundtribeUserException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@Slf4j
public class EmailServiceImpl {

    @Autowired
    JavaMailSender javaMailSender;

    @Autowired
    TemplateEngine templateEngine;

    @Async
    public void enviarMail(NotificationEmail email) throws MessagingException {
        try {
            // 1. Crear el Helper para enviar un mensaje por correo.
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            //2. Construir el correo con su destinatario y su asunto.
            helper.setTo(email.getDestinatario());
            helper.setSubject(email.getAsunto());

            //3. Procesar el HTML para el Body.
            Context context = new Context();
            context.setVariable("mensaje", email.getMensaje());
            String contentHTML = templateEngine.process("email", context);

            //4. Contruir el correo con su mensaje.
            helper.setText(contentHTML,true);

            //5. Enviar.
            javaMailSender.send(message);
        }catch (SoundtribeUserException e){
            throw new SoundtribeUserEmailException("Error al enviar el correo: "+ e.getMessage() +", "+ e);
        }
    }




    @Async
    public void enviarMailRecuperacionContraseña(String destinatario, String urlReset) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject("Recuperación de contraseña - SoundTribe");

            // Creamos el contexto con la variable que espera el template
            Context context = new Context();
            context.setVariable("urlReset", urlReset);

            // Procesamos el template `getPassword.html`
            String contentHTML = templateEngine.process("getPassword", context);

            helper.setText(contentHTML, true);

            javaMailSender.send(message);
        } catch (Exception e) {
            throw new SoundtribeUserEmailException("Error al enviar el correo de recuperación: " + e.getMessage());
        }
    }



}
