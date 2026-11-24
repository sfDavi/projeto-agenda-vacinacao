<%@ include file="/WEB-INF/jsp/layout/header.jspf" %>
<%-- Próximas aplicações e doses atrasadas de todos os usuários --%>

<div class="d-flex flex-wrap justify-content-between align-items-end mb-3 gap-2">
    <div>
        <h1 class="h3 mb-1"><i class="bi bi-clock-history"></i> Próximas aplicações</h1>
        <p class="text-body-secondary mb-0">Doses atrasadas e agendadas para os próximos ${dias} dias (até ${ate}).</p>
    </div>
</div>

<div class="row g-4">
    <div class="col-12 col-lg-6">
        <div class="card border-danger h-100">
            <div class="card-header text-bg-danger">
                <i class="bi bi-alarm"></i> Atrasadas <span class="badge text-bg-light ms-1">${fn:length(atrasadas)}</span>
            </div>
            <c:choose>
                <c:when test="${empty atrasadas}">
                    <div class="card-body text-body-secondary">Nenhuma dose atrasada. 🎉</div>
                </c:when>
                <c:otherwise>
                    <ul class="list-group list-group-flush">
                        <c:forEach items="${atrasadas}" var="a">
                            <%@ include file="/WEB-INF/jsp/doses/_itemDose.jspf" %>
                        </c:forEach>
                    </ul>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
    <div class="col-12 col-lg-6">
        <div class="card h-100">
            <div class="card-header">
                <i class="bi bi-calendar-week"></i> Próximos ${dias} dias <span class="badge text-bg-primary ms-1">${fn:length(proximas)}</span>
            </div>
            <c:choose>
                <c:when test="${empty proximas}">
                    <div class="card-body text-body-secondary">Nenhuma dose agendada para os próximos ${dias} dias.</div>
                </c:when>
                <c:otherwise>
                    <ul class="list-group list-group-flush">
                        <c:forEach items="${proximas}" var="a">
                            <%@ include file="/WEB-INF/jsp/doses/_itemDose.jspf" %>
                        </c:forEach>
                    </ul>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/agendas/_modalBaixa.jspf" %>
<%@ include file="/WEB-INF/jsp/layout/footer.jspf" %>
