package com.sga.unemi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Servicio de notificaciones por correo electrónico (sección "Seguridad"
 * del documento de diseño: "Bloqueo por intentos fallidos... se notifica
 * al usuario por correo electrónico").
 * <p>
 * El envío de correo es de mejor esfuerzo (best-effort): si el servidor
 * SMTP no está configurado o no responde, el error se registra en el log
 * pero <b>no</b> interrumpe el flujo de negocio que lo invoca (por ejemplo,
 * el bloqueo de la cuenta en {@code AuthService} debe completarse aunque la
 * notificación por correo falle). Esto evita que una dependencia externa
 * no crítica se convierta en un punto único de fallo para una operación de
 * seguridad importante.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Notifica a un usuario que su cuenta fue bloqueada temporalmente tras
     * varios intentos fallidos de inicio de sesión.
     *
     * @param email          dirección de correo del usuario afectado
     * @param minutosBloqueo duración del bloqueo, para informar en el mensaje
     */
    public void notificarBloqueoCuenta(String email, long minutosBloqueo) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(email);
            mensaje.setSubject("Alerta de seguridad: cuenta bloqueada temporalmente - SGA Durán");
            mensaje.setText(
                    "Hola,\n\n" +
                    "Tu cuenta en el Sistema de Gestión Académica de la Unidad Educativa Fiscal \"Durán\" " +
                    "fue bloqueada temporalmente por " + minutosBloqueo + " minutos, debido a varios intentos " +
                    "fallidos de inicio de sesión.\n\n" +
                    "Si fuiste tú quien intentó ingresar, simplemente espera el tiempo indicado e " +
                    "intenta de nuevo.\n\n" +
                    "Si no reconoces esta actividad, te recomendamos cambiar tu contraseña tan pronto " +
                    "el bloqueo se levante, o contactar al administrador del sistema.\n\n" +
                    "Este es un mensaje automático, por favor no respondas a este correo."
            );
            mailSender.send(mensaje);
        } catch (Exception e) {
            // Best-effort: el bloqueo de la cuenta ya se aplico antes de llegar aqui;
            // un fallo de SMTP no debe revertir esa proteccion de seguridad.
            log.warn("No se pudo enviar el correo de notificacion de bloqueo a {}: {}", email, e.getMessage());
        }
    }
}