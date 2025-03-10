package br.unb.sds.gds2ephem.infrastructure;

public interface NotificationService {
    void notificarErro(String assunto, String mensagem, Throwable erro);
}