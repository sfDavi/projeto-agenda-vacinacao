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

    // Formulário de vacina: com dose única, periodicidade e intervalo não se aplicam
    const campoDoses = document.getElementById('doses');
    const camposMultidose = document.querySelectorAll('[data-multidose]');
    if (campoDoses && camposMultidose.length) {
        const atualizar = () => {
            const multidose = Number(campoDoses.value) > 1;
            camposMultidose.forEach((campo) => {
                campo.disabled = !multidose;
                campo.required = multidose;
                if (!multidose) {
                    campo.value = '';
                }
            });
        };
        campoDoses.addEventListener('input', atualizar);
        atualizar();
    }

    // Fecha automaticamente alertas de sucesso após alguns segundos
    document.querySelectorAll('.alert-success.alert-dismissible').forEach((alerta) => {
        setTimeout(() => bootstrap.Alert.getOrCreateInstance(alerta).close(), 5000);
    });
});
