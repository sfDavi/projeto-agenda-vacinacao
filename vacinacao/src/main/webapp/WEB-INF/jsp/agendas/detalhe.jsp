<%@ include file="/WEB-INF/jsp/layout/header.jspf" %>

<nav aria-label="breadcrumb">
    <ol class="breadcrumb">
        <li class="breadcrumb-item"><a href="${ctx}/agendas">Agendas</a></li>
        <li class="breadcrumb-item active" aria-current="page">Agenda ${agenda.id}</li>
    </ol>
</nav>

<div class="row g-4">
    <div class="col-12 col-lg-6">
        <div class="card h-100">
            <div class="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
                <h1 class="h5 mb-0"><i class="bi bi-calendar-event"></i> <c:out value="${agenda.vacina.titulo}" />
                    &ndash; dose ${agenda.dose}/${agenda.vacina.doses}</h1>
                <span class="badge ${agenda.situacao.classeCss} fs-6">${agenda.situacao.rotulo}</span>
            </div>
            <div class="card-body">
                <dl class="row mb-0">
                    <dt class="col-sm-5">Código</dt>
                    <dd class="col-sm-7">${agenda.id}</dd>
                    <dt class="col-sm-5">Usuário</dt>
                    <dd class="col-sm-7"><a href="${ctx}/usuarios/detalhe?id=${agenda.usuario.id}">
                        <c:out value="${agenda.usuario.nome}" /></a></dd>
                    <dt class="col-sm-5">Vacina</dt>
                    <dd class="col-sm-7"><a href="${ctx}/vacinas/detalhe?id=${agenda.vacina.id}">
                        <c:out value="${agenda.vacina.titulo}" /></a>
                        <div class="small text-body-secondary"><c:out value="${agenda.vacina.esquema}" /></div></dd>
                    <dt class="col-sm-5">Data e hora</dt>
                    <dd class="col-sm-7">${agenda.dataFormatada} às ${agenda.hora}</dd>
                    <dt class="col-sm-5">Dose</dt>
                    <dd class="col-sm-7">${agenda.dose} de ${agenda.vacina.doses}</dd>
                    <dt class="col-sm-5">Situação</dt>
                    <dd class="col-sm-7">${agenda.situacao.rotulo}</dd>
                    <dt class="col-sm-5">Data da situação</dt>
                    <dd class="col-sm-7">
                        <c:choose>
                            <c:when test="${empty agenda.dataSituacao}"><span class="text-body-secondary">&mdash;</span></c:when>
                            <c:otherwise>${agenda.dataSituacaoFormatada}</c:otherwise>
                        </c:choose>
                    </dd>
                    <dt class="col-sm-5">Observações</dt>
                    <dd class="col-sm-7">
                        <c:choose>
                            <c:when test="${empty agenda.observacoes}"><span class="text-body-secondary">&mdash;</span></c:when>
                            <c:otherwise><c:out value="${agenda.observacoes}" /></c:otherwise>
                        </c:choose>
                    </dd>
                </dl>
            </div>
            <div class="card-footer d-flex justify-content-between flex-wrap gap-2">
                <a href="${ctx}/agendas" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> Voltar</a>
                <div class="d-flex flex-wrap gap-2 align-items-center">
                    <c:set var="a" value="${agenda}" />
                    <%@ include file="/WEB-INF/jsp/agendas/_botoesBaixa.jspf" %>
                    <form method="post" action="${ctx}/agendas/excluir" class="d-inline"
                          data-confirmar="Excluir somente esta agenda (dose ${agenda.dose} em ${agenda.dataFormatada})?">
                        <input type="hidden" name="id" value="${agenda.id}">
                        <input type="hidden" name="escopo" value="agenda">
                        <button type="submit" class="btn btn-sm btn-outline-danger"><i class="bi bi-trash"></i> Excluir esta</button>
                    </form>
                    <c:if test="${agenda.situacao.nome == 'AGENDADO' and dosesSeguintesAgendadas > 0}">
                        <form method="post" action="${ctx}/agendas/excluir" class="d-inline"
                              data-confirmar="Excluir esta dose e as ${dosesSeguintesAgendadas} dose(s) seguinte(s) ainda agendada(s)?">
                            <input type="hidden" name="id" value="${agenda.id}">
                            <input type="hidden" name="escopo" value="seguintes">
                            <button type="submit" class="btn btn-sm btn-danger"><i class="bi bi-trash3"></i>
                                Excluir esta e as seguintes (${dosesSeguintesAgendadas})</button>
                        </form>
                    </c:if>
                </div>
            </div>
        </div>
    </div>

    <div class="col-12 col-lg-6">
        <div class="card h-100">
            <div class="card-header"><i class="bi bi-list-ol"></i>
                Doses de <c:out value="${agenda.vacina.titulo}" /> para <c:out value="${agenda.usuario.nome}" /></div>
            <div class="table-responsive">
                <table class="table align-middle mb-0">
                    <thead class="table-light">
                    <tr><th scope="col">Dose</th><th scope="col">Data</th><th scope="col">Hora</th><th scope="col">Situação</th></tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${serie}" var="s">
                        <tr class="${s.id == agenda.id ? 'table-primary' : ''}">
                            <td>${s.dose}/${s.vacina.doses}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${s.id == agenda.id}"><strong>${s.dataFormatada}</strong></c:when>
                                    <c:otherwise><a href="${ctx}/agendas/detalhe?id=${s.id}">${s.dataFormatada}</a></c:otherwise>
                                </c:choose>
                            </td>
                            <td>${s.hora}</td>
                            <td><span class="badge ${s.situacao.classeCss}">${s.situacao.rotulo}</span></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/agendas/_modalBaixa.jspf" %>
<%@ include file="/WEB-INF/jsp/layout/footer.jspf" %>
