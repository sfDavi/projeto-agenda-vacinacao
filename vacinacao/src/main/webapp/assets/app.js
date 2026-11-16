// Comportamentos comuns a todas as páginas (JavaScript puro).
document.addEventListener('DOMContentLoaded', () => {
    // Confirmação antes de enviar formulários marcados com data-confirmar="mensagem"
    document.querySelectorAll('form[data-confirmar]').forEach((form) => {
        form.addEventListener('submit', (evento) => {
            if (!window.confirm(form.dataset.confirmar)) {
                evento.preventDefault();
            }
        });
    });

    // Fecha automaticamente alertas de sucesso após alguns segundos
    document.querySelectorAll('.alert-success.alert-dismissible').forEach((alerta) => {
        setTimeout(() => bootstrap.Alert.getOrCreateInstance(alerta).close(), 5000);
    });
});
