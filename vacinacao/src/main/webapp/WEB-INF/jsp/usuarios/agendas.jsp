<%@ include file="/WEB-INF/jsp/layout/header.jspf" %>
<%-- Item 6 (agendamentos do usuário) + Item 7B (controle de doses) --%>

<nav aria-label="breadcrumb">
    <ol class="breadcrumb">
        <li class="breadcrumb-item"><a href="${ctx}/usuarios">Usuários</a></li>
        <li class="breadcrumb-item"><a href="${ctx}/usuarios/detalhe?id=${usuario.id}"><c:out value="${usuario.nome}" /></a></li>
        <li class="breadcrumb-item active" aria-current="page">Cartão de vacinação</li>
    </ol>
</nav>

<div class="d-flex flex-wrap justify-content-between align-items-end mb-3 gap-2">
    <div>
        <h1 class="h3 mb-1"><i class="bi bi-journal-medical"></i> Cartão de vacinação</h1>
        <p class="text-body-secondary mb-0">
            <strong><c:out value="${usuario.nome}" /></strong> &middot; ${usuario.idade} anos &middot; ${usuario.sexo.rotulo}
            &middot; ${totalAgendas} agenda(s)
        </p>
        <c:if test="${not empty usuario.alergias}">
            <div class="mt-1 small">
                <i class="bi bi-exclamation-triangle text-warning"></i> Alergias:
                <c:forEach items="${usuario.alergias}" var="al" varStatus="s"><c:out value="${al.nome}" />${s.last ? '' : ', '}</c:forEach>
            </div>
        </c:if>
    </div>
    <a class="btn btn-primary" href="${ctx}/agendas/novo?usuario=${usuario.id}"><i class="bi bi-calendar-plus"></i> Agendar vacina</a>
</div>

<c:if test="${empty series}">
    <div class="card"><div class="card-body text-center text-body-secondary py-5">
        Nenhuma vacina agendada para este usuário.
        <a href="${ctx}/agendas/novo?usuario=${usuario.id}">Agendar a primeira</a>.
    </div></div>
</c:if>

<div class="row g-3">
    <c:forEach items="${series}" var="serie">
        <div class="col-12 col-xl-6">
            <div class="card h-100 ${serie.status == 'ATRASADA' ? 'border-danger' : ''}">
                <div class="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
                    <div>
                        <h2 class="h5 mb-0"><a href="${ctx}/vacinas/detalhe?id=${serie.vacina.id}" class="text-decoration-none">
                            <c:out value="${serie.vacina.titulo}" /></a></h2>
                        <small class="text-body-secondary"><c:out value="${serie.vacina.esquema}" /></small>
                    </div>
                    <span class="badge ${serie.status.classeCss}">${serie.status.rotulo}</span>
                </div>
                <div class="card-body">
                    <%-- Progresso (7B) --%>
                    <div class="d-flex justify-content-between small mb-1">
                        <span><strong>${serie.realizadas} de ${serie.total}</strong> dose(s) realizada(s)</span>
                        <span>${serie.percentual}%</span>
                    </div>
                    <div class="progress mb-3" role="progressbar" aria-label="Progresso da vacinação"
                         aria-valuenow="${serie.percentual}" aria-valuemin="0" aria-valuemax="100" style="height: .6rem">
                        <div class="progress-bar ${serie.status == 'CONCLUIDA' ? 'bg-success' : ''}" style="width: ${serie.percentual}%"></div>
                    </div>

                    <%-- Próxima aplicação (7B) --%>
                    <c:if test="${not empty serie.proxima}">
                        <div class="alert ${serie.proxima.atrasada ? 'alert-danger' : 'alert-info'} py-2 mb-3">
                            <i class="bi ${serie.proxima.atrasada ? 'bi-alarm' : 'bi-calendar-event'}"></i>
                            Próxima: <strong>dose ${serie.proxima.dose}</strong> em
                            <strong>${serie.proxima.dataFormatada}</strong> às ${serie.proxima.hora}
                            (<strong>${serie.proxima.prazo}</strong>)
                        </div>
                    </c:if>
                    <c:if test="${serie.precisaRecalcular}">
                        <div class="alert alert-warning py-2 mb-3">
                            <i class="bi bi-exclamation-triangle"></i>
                            A dose ${serie.ultimaRealizada.dose} foi aplicada em ${serie.ultimaRealizada.dataSituacaoFormatada};
                            pelo intervalo da vacina, a dose ${serie.proxima.dose} só pode ser aplicada a partir de
                            <strong>${serie.dataMinimaProximaFormatada}</strong>, mas está marcada para ${serie.proxima.dataFormatada}.
                            <form method="post" action="${ctx}/doses/recalcular" class="mt-2">
                                <input type="hidden" name="usuario" value="${usuario.id}">
                                <input type="hidden" name="vacina" value="${serie.vacina.id}">
                                <input type="hidden" name="voltar" value="${urlAtual}">
                                <button type="submit" class="btn btn-sm btn-warning">
                                    <i class="bi bi-arrow-repeat"></i> Recalcular próximas doses</button>
                            </form>
                        </div>
                    </c:if>

                    <%-- Linha do tempo das doses (Item 6) --%>
                    <ol class="list-group list-group-numbered-none">
                        <c:forEach items="${serie.doses}" var="a">
                            <li class="list-group-item ${a.atrasada ? 'list-group-item-danger' : ''}">
                                <div class="d-flex flex-wrap justify-content-between align-items-center gap-2">
                                    <div>
                                        <span class="fw-semibold">Dose ${a.dose}/${serie.total}</span>
                                        <span class="ms-2">${a.dataFormatada} às ${a.hora}</span>
                                        <span class="badge ${a.situacao.classeCss} ms-2">${a.situacao.rotulo}</span>
                                        <c:choose>
                                            <c:when test="${a.situacao.nome == 'AGENDADO'}">
                                                <small class="ms-1 ${a.atrasada ? 'text-danger fw-semibold' : 'text-body-secondary'}">${a.prazo}</small>
                                            </c:when>
                                            <c:otherwise>
                                                <small class="text-body-secondary ms-1">em ${a.dataSituacaoFormatada}</small>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="d-flex align-items-center gap-1">
                                        <%@ include file="/WEB-INF/jsp/agendas/_botoesBaixa.jspf" %>
                                        <a href="${ctx}/agendas/detalhe?id=${a.id}" class="btn btn-sm btn-outline-primary" title="Detalhes">
                                            <i class="bi bi-eye"></i></a>
                                    </div>
                                </div>
                                <c:if test="${not empty a.observacoes}">
                                    <div class="small text-body-secondary mt-1"><i class="bi bi-chat-left-text"></i>
                                        <c:out value="${a.observacoes}" /></div>
                                </c:if>
                                <c:if test="${serie.isReagendavel(a)}">
                                    <c:set var="minima" value="${serie.dataMinima(a.dose)}" />
                                    <form method="post" action="${ctx}/doses/reagendar" class="row g-2 align-items-end mt-1">
                                        <input type="hidden" name="id" value="${a.id}">
                                        <input type="hidden" name="voltar" value="${urlAtual}">
                                        <div class="col-auto">
                                            <label class="form-label small mb-0" for="data${a.id}">Reagendar dose ${a.dose} para</label>
                                            <input type="date" class="form-control form-control-sm" id="data${a.id}" name="data" required
                                                   min="${empty minima or minima.toString() lt hojeIso ? hojeIso : minima}">
                                        </div>
                                        <div class="col-auto">
                                            <input type="time" class="form-control form-control-sm" name="hora" required
                                                   value="${a.hora}" aria-label="Hora">
                                        </div>
                                        <div class="col-auto">
                                            <button type="submit" class="btn btn-sm btn-outline-primary">
                                                <i class="bi bi-calendar-plus"></i> Reagendar</button>
                                        </div>
                                    </form>
                                </c:if>
                            </li>
                        </c:forEach>
                    </ol>
                </div>
            </div>
        </div>
    </c:forEach>
</div>

<%@ include file="/WEB-INF/jsp/agendas/_modalBaixa.jspf" %>
<%@ include file="/WEB-INF/jsp/layout/footer.jspf" %>
