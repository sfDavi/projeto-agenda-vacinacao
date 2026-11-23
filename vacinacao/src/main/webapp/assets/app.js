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
    // (não mexe quando o esquema está bloqueado - edição de vacina que já tem agendas)
    if (campoDoses && camposMultidose.length && !campoDoses.matches(':disabled')) {
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

    // Formulário de agendamento: prévia das doses que serão geradas (GET /agendas/previa)
    const formAgenda = document.getElementById('formAgenda');
    const painelPrevia = document.getElementById('previa');
    if (formAgenda && painelPrevia) {
        const escapar = (texto) => String(texto)
            .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
        let ultimaRequisicao = 0;

        const atualizarPrevia = async () => {
            const params = new URLSearchParams({
                vacina: formAgenda.vacina.value,
                data: formAgenda.data.value,
                usuario: formAgenda.usuario.value,
            });
            const hora = formAgenda.hora.value;
            const requisicao = ++ultimaRequisicao;
            try {
                const resposta = await fetch(`${formAgenda.dataset.previaUrl}?${params}`);
                const dados = await resposta.json();
                if (requisicao !== ultimaRequisicao) {
                    return; // chegou fora de ordem
                }
                if (!dados.ok) {
                    painelPrevia.innerHTML = `<p class="text-body-secondary mb-0">${escapar(dados.erro)}</p>`;
                    return;
                }
                const itens = dados.doses.map((d) => `
                    <li class="list-group-item d-flex justify-content-between align-items-center">
                        <span><span class="badge badge-agendado me-2">Dose ${d.dose}</span>${escapar(d.data)}
                            <small class="text-body-secondary">(${escapar(d.diaSemana)})</small></span>
                        <span class="text-body-secondary">${escapar(hora || '--:--')}</span>
                    </li>`).join('');
                const aviso = dados.aviso
                    ? `<div class="alert alert-warning py-2 mb-2"><i class="bi bi-exclamation-triangle"></i> ${escapar(dados.aviso)}</div>`
                    : '';
                painelPrevia.innerHTML = `${aviso}
                    <p class="mb-2"><strong>${escapar(dados.vacina)}</strong>
                        <span class="text-body-secondary">&mdash; ${escapar(dados.esquema)}</span></p>
                    <ul class="list-group">${itens}</ul>
                    <p class="small text-body-secondary mt-2 mb-0">${dados.doses.length} agenda(s) serão criadas.</p>`;
            } catch (erro) {
                painelPrevia.innerHTML = '<p class="text-danger mb-0">Não foi possível carregar a prévia.</p>';
            }
        };

        ['usuario', 'vacina', 'data', 'hora'].forEach((campo) => {
            formAgenda[campo].addEventListener('change', atualizarPrevia);
        });
        atualizarPrevia();

        // 7A: marca com ⚠ as vacinas contraindicadas ao usuário e mostra o alerta da vacina escolhida
        const alerta = document.getElementById('alertaContraindicacao');
        if (alerta) {
            const nomesConflito = alerta.querySelector('[data-alergias-conflito]');
            const caixaCiente = alerta.querySelector('#ciente');
            const opcoesVacina = Array.from(formAgenda.vacina.options).filter((o) => o.value);
            opcoesVacina.forEach((o) => { o.dataset.textoOriginal = o.textContent.trim(); });
            let contraindicadas = {};

            const mostrarAlerta = () => {
                const conflitos = contraindicadas[formAgenda.vacina.value];
                if (conflitos && conflitos.length) {
                    nomesConflito.textContent = conflitos.join(', ');
                    alerta.hidden = false;
                } else {
                    alerta.hidden = true;
                    caixaCiente.checked = false;
                }
            };

            const carregarContraindicacoes = async () => {
                contraindicadas = {};
                if (formAgenda.usuario.value) {
                    try {
                        const params = new URLSearchParams({ usuario: formAgenda.usuario.value });
                        const resposta = await fetch(`${alerta.dataset.contraindicacoesUrl}?${params}`);
                        contraindicadas = (await resposta.json()).vacinas || {};
                    } catch (erro) {
                        contraindicadas = {}; // o servidor valida de qualquer forma ao salvar
                    }
                }
                opcoesVacina.forEach((o) => {
                    const conflitos = contraindicadas[o.value];
                    o.textContent = conflitos
                        ? `⚠ ${o.dataset.textoOriginal} (contraindicada: ${conflitos.join(', ')})`
                        : o.dataset.textoOriginal;
                });
                mostrarAlerta();
            };

            formAgenda.usuario.addEventListener('change', carregarContraindicacoes);
            formAgenda.vacina.addEventListener('change', mostrarAlerta);
            carregarContraindicacoes();
        }
    }

    // Modal de baixa (Item 5): preenche com os dados do botão que o abriu
    const modalBaixa = document.getElementById('modalBaixa');
    if (modalBaixa) {
        modalBaixa.addEventListener('show.bs.modal', (evento) => {
            const botao = evento.relatedTarget;
            const cancelar = botao.dataset.acao === 'CANCELADO';
            const form = modalBaixa.querySelector('form');
            form.elements.namedItem('id').value = botao.dataset.id;
            form.elements.namedItem('acao').value = botao.dataset.acao;
            form.elements.namedItem('motivo').value = '';
            modalBaixa.querySelector('.modal-title').textContent = cancelar ? 'Cancelar agenda' : 'Registrar aplicação';
            modalBaixa.querySelector('[data-baixa-pergunta]').textContent = cancelar
                ? 'Confirma o cancelamento desta agenda?'
                : 'Confirma que esta dose foi aplicada?';
            modalBaixa.querySelector('[data-baixa-descricao]').textContent = botao.dataset.descricao;
            modalBaixa.querySelector('[data-baixa-motivo]').hidden = !cancelar;
            const confirmar = modalBaixa.querySelector('[data-baixa-confirmar]');
            confirmar.className = `btn ${cancelar ? 'btn-danger' : 'btn-success'}`;
            confirmar.innerHTML = cancelar
                ? '<i class="bi bi-x-lg"></i> Cancelar agenda'
                : '<i class="bi bi-check-lg"></i> Marcar como realizada';
        });
        modalBaixa.addEventListener('shown.bs.modal', () => {
            const motivo = modalBaixa.querySelector('[name="motivo"]');
            if (!motivo.closest('[data-baixa-motivo]').hidden) {
                motivo.focus();
            }
        });
    }

    // Fecha automaticamente alertas de sucesso após alguns segundos
    document.querySelectorAll('.alert-success.alert-dismissible').forEach((alerta) => {
        setTimeout(() => bootstrap.Alert.getOrCreateInstance(alerta).close(), 5000);
    });
});
