package setxpro.email.dtos;

public record EmailRequestDto(
        String to,
        String from,
        String html,
        String subject,
        String base64Attachment,
        String base64AttachmentName,
        String message
) {
}
