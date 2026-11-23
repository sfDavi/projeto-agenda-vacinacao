<%@ include file="/WEB-INF/jsp/layout/header.jspf" %>

<div class="row justify-content-center">
    <div class="col-12 col-md-10 col-lg-7">
        <div class="card text-center shadow-sm">
            <div class="card-body py-5 px-4">
                <i class="bi ${iconeErro} display-4 text-danger"></i>
                <h1 class="h3 mt-3"><c:out value="${tituloErro}" /></h1>
                <p class="text-body-secondary mb-4"><c:out value="${mensagemErro}" /></p>
                <div class="d-flex flex-wrap justify-content-center gap-2">
                    <button type="button" class="btn btn-outline-secondary" onclick="history.back()">
                        <i class="bi bi-arrow-left"></i> Voltar</button>
                    <a href="${ctx}/home" class="btn btn-primary"><i class="bi bi-house"></i> Ir para o início</a>
                </div>
                <p class="small text-body-secondary mt-4 mb-0">Código ${codigoErro}</p>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/layout/footer.jspf" %>
