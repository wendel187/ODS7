import { api } from "./api.js";

const root = document.querySelector("#view-root");
const titleNode = document.querySelector("#page-title");
const sidebar = document.querySelector("#sidebar");
const mobileMenu = document.querySelector("#mobile-menu");
const navBackdrop = document.querySelector("#nav-backdrop");
const titleMap = {
  dashboard: "Visão geral",
  aparelhos: "Meus aparelhos",
  novo: "Cadastrar aparelho",
  editar: "Editar aparelho",
  calculadora: "Calculadora",
  simulador: "Simulador",
  solar: "Simulador Solar",
  quiz: "Quiz Energético",
  dicas: "Dicas de economia",
  sobre: "Sobre o projeto"
};
const brNumber = new Intl.NumberFormat("pt-BR", { minimumFractionDigits: 0, maximumFractionDigits: 2 });
const brCurrency = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });
const brDecimal = new Intl.NumberFormat("pt-BR", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
let charts = [];
let currentView = "dashboard";
let applianceCache = [];
let bootstrapModal;
let quizRound = null;
let quizAnswers = [];
let quizSelections = [];
let quizIndex = 0;
let quizResult = null;
let quizSubmitting = false;

function setMobileNavigation(open, returnFocus = false) {
  const isMobile = window.matchMedia("(max-width: 760px)").matches;
  const expanded = isMobile && open;
  sidebar.classList.toggle("open", expanded);
  sidebar.inert = isMobile && !expanded;
  mobileMenu.setAttribute("aria-expanded", String(expanded));
  mobileMenu.setAttribute("aria-label", expanded ? "Fechar menu" : "Abrir menu");
  navBackdrop.hidden = !expanded;
  if (expanded) sidebar.querySelector("a[href]")?.focus();
  else if (returnFocus) mobileMenu.focus();
}

const iconFor = (name = "") => {
  const value = name.toLocaleLowerCase("pt-BR");
  if (value.includes("geladeira") || value.includes("freezer")) return "bi-snow";
  if (value.includes("chuveiro")) return "bi-droplet";
  if (value.includes("ar-condicionado") || value.includes("ar condicionado")) return "bi-wind";
  if (value.includes("televis") || value.includes("computador") || value.includes("notebook")) return "bi-tv";
  if (value.includes("lâmpada") || value.includes("lampada") || value.includes("luz")) return "bi-lightbulb";
  if (value.includes("máquina") || value.includes("maquina")) return "bi-water";
  return "bi-plug";
};

function escapeHtml(value = "") {
  return String(value).replace(/[&<>"']/g, (char) => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
  })[char]);
}

function money(value) {
  return value == null ? "—" : brCurrency.format(Number(value));
}

function number(value, digits = 2) {
  return value == null ? "—" : new Intl.NumberFormat("pt-BR", {
    minimumFractionDigits: digits,
    maximumFractionDigits: digits
  }).format(Number(value));
}

function heading(kicker, title, description, action = "") {
  return `<div class="page-heading"><div><span class="eyebrow">${kicker}</span><h1>${title}</h1><p>${description}</p></div>${action}</div>`;
}

function showToast(message, type = "success") {
  const id = `toast-${Date.now()}`;
  const icon = type === "error" ? "bi-exclamation-circle" : "bi-check-circle";
  const label = type === "error" ? "Atenção" : "Tudo certo";
  document.querySelector("#toast-container").insertAdjacentHTML("beforeend", `
    <div id="${id}" class="toast ${type}" role="status" aria-live="${type === "error" ? "assertive" : "polite"}">
      <div class="toast-header"><i class="bi ${icon} me-2" aria-hidden="true"></i><strong class="me-auto">${label}</strong>
      <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Fechar"></button></div>
      <div class="toast-body">${escapeHtml(message)}</div>
    </div>`);
  const element = document.getElementById(id);
  const toast = new bootstrap.Toast(element, { delay: type === "error" ? 6000 : 3500 });
  element.addEventListener("hidden.bs.toast", () => element.remove(), { once: true });
  toast.show();
}

function showError(error) {
  showToast(error instanceof Error ? error.message : "Ocorreu um erro inesperado.", "error");
}

function setLoading() {
  root.innerHTML = '<div class="spinner-wrap"><div class="spinner-border spinner-border-sm" role="status"><span class="visually-hidden">Carregando...</span></div></div>';
}

function destroyCharts() {
  charts.forEach((chart) => chart.destroy());
  charts = [];
}

function updateNavigation(view) {
  document.querySelectorAll(".side-nav a").forEach((link) => {
    const active = link.dataset.view === view || (view === "novo" || view === "editar") && link.dataset.view === "aparelhos";
    link.classList.toggle("active", active);
    if (active) link.setAttribute("aria-current", "page");
    else link.removeAttribute("aria-current");
  });
  titleNode.textContent = titleMap[view] || titleMap.dashboard;
}

async function navigate() {
  const [route, query = ""] = location.hash.slice(1).split("?", 2);
  const view = route || "dashboard";
  const navigationFromSidebar = sidebar.contains(document.activeElement);
  currentView = Object.hasOwn(titleMap, view) ? view : "dashboard";
  updateNavigation(currentView);
  setMobileNavigation(false);
  destroyCharts();
  setLoading();
  try {
    if (currentView === "dashboard") await renderDashboard();
    else if (currentView === "aparelhos") await renderAppliances();
    else if (currentView === "novo") renderApplianceForm();
    else if (currentView === "editar") {
      const id = new URLSearchParams(query).get("id");
      if (!id || !/^[1-9]\d*$/.test(id)) {
        throw new Error("O identificador do aparelho para edição é inválido.");
      }
      await renderApplianceForm(id);
    }
    else if (currentView === "calculadora") renderCalculator();
    else if (currentView === "simulador") await renderSimulator();
    else if (currentView === "solar") renderSolarSimulator();
    else if (currentView === "quiz") await renderQuiz();
    else if (currentView === "dicas") renderTips();
    else renderAbout();
  } catch (error) {
    root.innerHTML = `<section class="content-card empty-state" role="alert"><div class="empty-illustration"><i class="bi bi-cloud-slash" aria-hidden="true"></i></div><h2>Não foi possível carregar esta página</h2><p>${escapeHtml(error.message)}</p><button class="btn btn-outline-secondary" type="button" data-action="retry"><i class="bi bi-arrow-clockwise" aria-hidden="true"></i>Tentar novamente</button></section>`;
  }
  if (navigationFromSidebar) document.querySelector("#main-content").focus({ preventScroll: true });
}

function emptyState(title, message, actionLabel = "", action = "") {
  return `<div class="empty-state"><span class="empty-illustration"><i class="bi bi-plug" aria-hidden="true"></i></span><h2>${title}</h2><p>${message}</p>${actionLabel ? `<a class="btn btn-primary" href="#${action}"><i class="bi bi-plus-lg" aria-hidden="true"></i>${actionLabel}</a>` : ""}</div>`;
}

async function renderDashboard() {
  const data = await api.dashboard();
  applianceCache = data.aparelhos || [];
  const noDevices = data.quantidadeAparelhos === 0;
  root.innerHTML = `
    ${heading("ACOMPANHAMENTO DE ENERGIA", "Seu consumo, suas escolhas.", "Uma visão simples do consumo estimado dos aparelhos cadastrados.",
      '<a class="btn btn-primary" href="#novo"><i class="bi bi-plus-lg" aria-hidden="true"></i>Novo aparelho</a>')}
    <section class="stat-grid" aria-label="Resumo do consumo">
      <article class="stat-card"><div class="stat-top"><span>Consumo estimado</span><span class="stat-icon"><i class="bi bi-lightning-charge" aria-hidden="true"></i></span></div><div class="stat-value">${number(data.consumoMensalTotalKwh)}<span class="stat-unit">kWh/mês</span></div><div class="stat-caption">Soma dos aparelhos cadastrados</div></article>
      <article class="stat-card"><div class="stat-top"><span>Custo estimado</span><span class="stat-icon"><i class="bi bi-currency-dollar" aria-hidden="true"></i></span></div><div class="stat-value" style="font-size:${data.custoMensalEstimado == null ? "17px" : "24px"}">${money(data.custoMensalEstimado)}</div><div class="stat-caption">${data.tarifaConfigurada ? "Com base na tarifa informada" : '<button class="btn btn-link p-0 text-decoration-none" type="button" data-action="tarifa">Configure sua tarifa</button>'}</div></article>
      <article class="stat-card"><div class="stat-top"><span>Aparelhos</span><span class="stat-icon"><i class="bi bi-house-gear" aria-hidden="true"></i></span></div><div class="stat-value">${data.quantidadeAparelhos}<span class="stat-unit">${data.quantidadeAparelhos === 1 ? "cadastrado" : "cadastrados"}</span></div><div class="stat-caption">No perfil de demonstração</div></article>
      <article class="stat-card"><div class="stat-top"><span>Maior consumo</span><span class="stat-icon"><i class="bi bi-graph-up-arrow" aria-hidden="true"></i></span></div><div class="stat-value" style="font-size:${data.maiorConsumidor ? "17px" : "24px"}">${data.maiorConsumidor ? escapeHtml(data.maiorConsumidor.nome) : "—"}</div><div class="stat-caption">${data.maiorConsumidor ? `${number(data.maiorConsumidor.consumoMensalKwh)} kWh/mês` : "Aguardando cadastro"}</div></article>
    </section>
    ${noDevices ? `<section class="panel">${emptyState("Comece cadastrando um aparelho", "Seus indicadores e gráficos aparecerão aqui com base apenas nos dados que você informar.", "Cadastrar primeiro aparelho", "novo")}</section>` : `
      ${!data.tarifaConfigurada ? '<div class="notice mb-3"><i class="bi bi-info-circle" aria-hidden="true"></i><span>O custo não está disponível porque nenhuma tarifa foi configurada. <button class="btn btn-link p-0 align-baseline text-decoration-none" type="button" data-action="tarifa">Informar tarifa por kWh</button>. Os valores são estimativas e não representam necessariamente a conta final.</span></div>' : '<div class="notice mb-3"><i class="bi bi-info-circle" aria-hidden="true"></i><span>Valores estimados com base nos dados cadastrados e na tarifa informada. A conta final pode incluir outros encargos.</span></div>'}
      <section class="dashboard-grid">
        <article class="panel"><div class="panel-heading"><div><h2>Consumo por aparelho</h2><p>Estimativa mensal em kWh</p></div></div><div class="chart-area"><canvas id="consumption-chart" aria-label="Gráfico de consumo por aparelho" role="img"></canvas></div></article>
        <article class="panel"><div class="panel-heading"><div><h2>Distribuição do consumo</h2><p>Participação de cada aparelho</p></div></div><div class="chart-area donut-area"><canvas id="share-chart" aria-label="Gráfico de distribuição do consumo" role="img"></canvas></div></article>
      </section>
      <section class="panel"><div class="panel-heading"><div><h2>Seus aparelhos</h2><p>Dados informados por você</p></div><a class="btn btn-outline-secondary btn-sm" href="#aparelhos">Ver todos <i class="bi bi-arrow-right" aria-hidden="true"></i></a></div>${deviceTable(data.aparelhos.slice(0, 5))}</section>`}
  `;
  if (!noDevices) drawCharts(data.aparelhos);
}

function drawCharts(appliances) {
  if (!window.Chart) {
    document.querySelectorAll(".chart-area").forEach((el) => { el.innerHTML = '<p class="text-muted-small">Os gráficos dependem da conexão para carregar a biblioteca Chart.js.</p>'; });
    return;
  }
  const colors = ["#176b43", "#38895a", "#559d6d", "#73ae83", "#8dbd99", "#31734c", "#679b73", "#a0c9a8"];
  const labels = appliances.map((item) => item.nome);
  const values = appliances.map((item) => Number(item.consumoMensalKwh));
  const sharedOptions = {
    responsive: true,
    maintainAspectRatio: false,
    animation: { duration: 450 },
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: "#173d2b",
        padding: 11,
        cornerRadius: 8,
        titleFont: { family: "DM Sans", size: 12, weight: "600" },
        bodyFont: { family: "DM Sans", size: 11 },
        callbacks: { label: (context) => ` ${number(context.raw)} kWh/mês` }
      }
    }
  };
  const bars = new Chart(document.querySelector("#consumption-chart"), {
    type: "bar",
    data: { labels, datasets: [{ data: values, backgroundColor: labels.map((_, i) => colors[i % colors.length]), borderRadius: 5, maxBarThickness: 36 }] },
    options: {
      ...sharedOptions,
      layout: { padding: { top: 4, right: 8, bottom: 0, left: 0 } },
      scales: {
        x: {
          grid: { display: false },
          border: { display: false },
          ticks: {
            color: "#536258",
            font: { family: "DM Sans", size: 10 },
            maxRotation: 0,
            minRotation: 0,
            callback(value) {
              const label = this.getLabelForValue(value);
              return label.length > 13 ? `${label.slice(0, 12)}…` : label;
            }
          }
        },
        y: {
          beginAtZero: true,
          border: { display: false },
          grid: { color: "#e9eee9", drawTicks: false },
          ticks: { color: "#536258", padding: 8, font: { family: "DM Sans", size: 10 }, precision: 0 }
        }
      }
    }
  });
  const donut = new Chart(document.querySelector("#share-chart"), {
    type: "doughnut",
    data: { labels, datasets: [{ data: values, backgroundColor: labels.map((_, i) => colors[i % colors.length]), borderWidth: 3, borderColor: "#fff", hoverOffset: 4 }] },
    options: {
      ...sharedOptions,
      cutout: "70%",
      layout: { padding: { top: 2, right: 6, bottom: 0, left: 6 } },
      plugins: {
        ...sharedOptions.plugins,
        legend: {
          position: "bottom",
          labels: {
            color: "#3e4d43",
            usePointStyle: true,
            pointStyle: "circle",
            boxWidth: 8,
            boxHeight: 8,
            padding: 14,
            font: { family: "DM Sans", size: 10 }
          }
        }
      }
    }
  });
  charts.push(bars, donut);
}

function deviceTable(items) {
  if (!items.length) return emptyState("Nenhum aparelho encontrado", "Cadastre um aparelho para acompanhar seu consumo.", "Cadastrar aparelho", "novo");
  return `<div class="table-responsive"><table class="table"><thead><tr><th scope="col">APARELHO</th><th scope="col">POTÊNCIA</th><th scope="col">USO</th><th scope="col">CONSUMO</th><th scope="col">CUSTO EST.</th><th scope="col"><span class="visually-hidden">Ações</span></th></tr></thead><tbody>${items.map((item) => `
    <tr><td><span class="device-name"><span class="device-icon"><i class="bi ${iconFor(item.nome)}" aria-hidden="true"></i></span>${escapeHtml(item.nome)}</span></td>
    <td>${number(item.potenciaWatts)} W</td><td>${number(item.horasPorDia)} h/dia</td><td><strong>${number(item.consumoMensalKwh)} kWh</strong></td><td>${money(item.custoMensalEstimado)}</td>
    <td><span class="table-actions"><button class="table-action" type="button" data-action="edit" data-id="${item.id}" aria-label="Editar ${escapeHtml(item.nome)}"><i class="bi bi-pencil" aria-hidden="true"></i></button><button class="table-action" type="button" data-action="delete" data-id="${item.id}" data-name="${escapeHtml(item.nome)}" aria-label="Excluir ${escapeHtml(item.nome)}"><i class="bi bi-trash3" aria-hidden="true"></i></button></span></td></tr>`).join("")}</tbody></table></div>`;
}

async function renderAppliances() {
  const [items, config] = await Promise.all([api.appliances(), api.configuration()]);
  applianceCache = items;
  root.innerHTML = `${heading("SEU INVENTÁRIO", "Meus aparelhos", "Gerencie os equipamentos que você quer acompanhar.",
    '<a class="btn btn-primary" href="#novo"><i class="bi bi-plus-lg" aria-hidden="true"></i>Cadastrar aparelho</a>')}
    ${config.tarifaKwh == null ? '<div class="notice mb-3"><i class="bi bi-info-circle" aria-hidden="true"></i><span>Configure a tarifa por kWh para exibir estimativas de custo. <button class="btn btn-link p-0 align-baseline text-decoration-none" type="button" data-action="tarifa">Configurar tarifa</button></span></div>' : ""}
    <section class="panel">${items.length ? deviceTable(items) : emptyState("Sua lista ainda está vazia", "Adicione os aparelhos que deseja acompanhar. Os dados serão usados nos cálculos do seu perfil.", "Cadastrar primeiro aparelho", "novo")}</section>
    <p class="text-muted-small mt-3">O consumo é estimado a partir da potência e dos hábitos de uso informados.</p>`;
}

async function renderApplianceForm(id = null) {
  const item = id ? await api.appliance(id) : null;
  const edit = Boolean(item);
  const action = `<a class="btn btn-outline-secondary" href="#aparelhos"><i class="bi bi-arrow-left" aria-hidden="true"></i>Voltar</a>`;
  root.innerHTML = `${heading(edit ? "ATUALIZE SEUS DADOS" : "NOVO CADASTRO", edit ? "Editar aparelho" : "Cadastrar aparelho", "Informe os dados de uso para calcular uma estimativa mensal.", action)}
    <section class="content-card"><form id="appliance-form" data-id="${edit ? item.id : ""}" novalidate>
      <div class="form-grid">
        <div class="span-2"><label class="form-label" for="device-name">Nome do aparelho</label><input class="form-control" id="device-name" name="nome" maxlength="80" placeholder="Ex.: Geladeira" value="${escapeHtml(item?.nome || "")}" required><div class="invalid-feedback">Informe o nome do aparelho.</div></div>
        <div><label class="form-label" for="device-power">Potência (watts)</label><div class="input-group"><input class="form-control" id="device-power" name="potenciaWatts" type="number" min="0.01" max="20000" step="0.01" placeholder="Ex.: 150" value="${item?.potenciaWatts ?? ""}" required><span class="input-group-text">W</span><div class="invalid-feedback">Informe potência entre 0,01 e 20.000 W.</div></div><div class="form-text">Consulte a etiqueta do aparelho ou o manual.</div></div>
        <div><label class="form-label" for="device-hours">Horas de uso por dia</label><div class="input-group"><input class="form-control" id="device-hours" name="horasPorDia" type="number" min="0" max="24" step="0.01" placeholder="Ex.: 5" value="${item?.horasPorDia ?? ""}" required><span class="input-group-text">h/dia</span><div class="invalid-feedback">Informe um valor entre 0 e 24 horas.</div></div></div>
        <div><label class="form-label" for="device-days">Dias de uso por mês</label><div class="input-group"><input class="form-control" id="device-days" name="diasPorMes" type="number" min="0" max="31" step="1" placeholder="Ex.: 30" value="${item?.diasPorMes ?? ""}" required><span class="input-group-text">dias</span><div class="invalid-feedback">Informe de 0 a 31 dias.</div></div></div>
        <div class="span-2"><div class="notice"><i class="bi bi-info-circle" aria-hidden="true"></i><span>O consumo é calculado no servidor com os dados enviados. Informe seus hábitos reais; não usamos valores de exemplo.</span></div></div>
      </div>
      <div class="form-actions"><a class="btn btn-outline-secondary" href="#aparelhos">Cancelar</a><button class="btn btn-primary" type="submit"><i class="bi bi-check2" aria-hidden="true"></i>${edit ? "Salvar alterações" : "Cadastrar aparelho"}</button></div>
    </form></section>`;
}

function renderCalculator() {
  root.innerHTML = `${heading("ESTIMATIVA DE CONSUMO", "Calculadora", "Simule o consumo de um aparelho sem adicioná-lo à sua lista.")}<div class="row g-3">
    <div class="col-lg-7"><section class="content-card"><form id="calculator-form" novalidate><div class="form-grid">
      <div class="span-2"><label class="form-label" for="calc-name">Nome (opcional)</label><input class="form-control" id="calc-name" maxlength="80" placeholder="Ex.: Ventilador"></div>
      <div><label class="form-label" for="calc-power">Potência</label><div class="input-group"><input class="form-control" id="calc-power" name="potenciaWatts" type="number" min="0.01" max="20000" step="0.01" required><span class="input-group-text">W</span></div></div>
      <div><label class="form-label" for="calc-hours">Horas por dia</label><div class="input-group"><input class="form-control" id="calc-hours" name="horasPorDia" type="number" min="0" max="24" step="0.01" required><span class="input-group-text">h/dia</span></div></div>
      <div><label class="form-label" for="calc-days">Dias por mês</label><div class="input-group"><input class="form-control" id="calc-days" name="diasPorMes" type="number" min="0" max="31" step="1" required><span class="input-group-text">dias</span></div></div>
    </div><div class="form-actions"><button class="btn btn-primary" type="submit"><i class="bi bi-calculator" aria-hidden="true"></i>Calcular</button></div></form><div id="calculator-result"></div></section></div>
    <div class="col-lg-5"><section class="content-card"><h2>Como funciona</h2><p class="text-muted-small mt-2">O cálculo é feito pelo backend com os dados informados:</p><div class="notice mt-3"><i class="bi bi-lightning-charge" aria-hidden="true"></i><span><strong>Consumo mensal</strong><br>(potência em W ÷ 1.000) × horas por dia × dias de uso</span></div><div id="calculator-tariff" class="mt-3 text-muted-small"></div></section></div>
    </div>`;
  api.configuration().then((config) => {
    const text = config.tarifaKwh == null ? "A tarifa ainda não foi configurada; o resultado exibirá apenas o consumo." : `Tarifa informada: ${money(config.tarifaKwh)}/kWh.`;
    const node = document.querySelector("#calculator-tariff");
    if (node) node.textContent = text;
  }).catch(showError);
}

async function renderSimulator() {
  const [items, config] = await Promise.all([api.appliances(), api.configuration()]);
  applianceCache = items;
  const options = items.map((item) => `<option value="${item.id}">${escapeHtml(item.nome)} — ${number(item.horasPorDia)} h/dia</option>`).join("");
  root.innerHTML = `${heading("PLANEJE MUDANÇAS", "Simulador de economia", "Compare uma redução de uso sem alterar o cadastro original.")}${items.length ? `<div class="row g-3"><div class="col-lg-7"><section class="content-card"><form id="simulator-form" novalidate><div class="form-grid">
      <div class="span-2"><label class="form-label" for="sim-device">Aparelho cadastrado</label><select class="form-select" id="sim-device" required><option value="">Selecione um aparelho</option>${options}</select></div>
      <div><label class="form-label" for="sim-hours">Novo tempo diário</label><div class="input-group"><input class="form-control" id="sim-hours" type="number" min="0" max="24" step="0.01" required><span class="input-group-text">h/dia</span></div><div class="form-text" id="sim-limit">Escolha um aparelho para ver o limite de uso atual.</div></div>
    </div><div class="form-actions"><button class="btn btn-primary" type="submit"><i class="bi bi-sliders" aria-hidden="true"></i>Simular redução</button></div></form><div id="simulator-result"></div></section></div>
    <div class="col-lg-5"><section class="content-card"><h2>Simulação sem compromisso</h2><p class="text-muted-small mt-2">O cálculo considera a potência e os dias mensais já cadastrados, alterando somente as horas usadas nesta simulação.</p>${config.tarifaKwh == null ? '<div class="notice mt-3"><i class="bi bi-info-circle" aria-hidden="true"></i><span>Configure a tarifa para estimar economia financeira. <button type="button" class="btn btn-link p-0 align-baseline text-decoration-none" data-action="tarifa">Configurar tarifa</button></span></div>' : `<div class="notice mt-3"><i class="bi bi-info-circle" aria-hidden="true"></i><span>Tarifa utilizada: ${money(config.tarifaKwh)}/kWh. A economia é estimada.</span></div>`}</section></div></div>` : `<section class="panel">${emptyState("Cadastre um aparelho para simular", "A simulação compara o tempo de uso de um aparelho que já esteja no seu perfil.", "Cadastrar aparelho", "novo")}</section>`}`;
}

function renderSolarSimulator() {
  root.innerHTML = `${heading("CENÁRIO FOTOVOLTAICO", "Simulador Solar", "Compare sua conta atual com uma estimativa baseada nos dados e nas premissas que você informar.")}<div class="solar-layout">
    <section class="content-card solar-form-card"><form id="solar-form" novalidate>
      <div class="solar-form-section"><span class="solar-section-icon"><i class="bi bi-receipt" aria-hidden="true"></i></span><div><h2>Sua conta e localização</h2><p>Use informações recentes da sua conta de energia.</p></div></div>
      <div class="form-grid">
        <div><label class="form-label" for="solar-bill">Valor médio mensal da conta</label><div class="input-group"><span class="input-group-text">R$</span><input class="form-control" id="solar-bill" name="valorContaMensal" type="number" min="0.01" max="10000000" step="0.01" required><div class="invalid-feedback">Informe um valor acima de zero.</div></div></div>
        <div><label class="form-label" for="solar-consumption">Consumo mensal</label><div class="input-group"><input class="form-control" id="solar-consumption" name="consumoMensalKwh" type="number" min="0" max="1000000" step="0.01" placeholder="Opcional"><span class="input-group-text">kWh</span></div><div class="form-text">Prefira o consumo em kWh indicado na fatura.</div></div>
        <div><label class="form-label" for="solar-city">Cidade</label><input class="form-control" id="solar-city" name="cidade" maxlength="80" autocomplete="address-level2" required><div class="invalid-feedback">Informe sua cidade.</div></div>
        <div><label class="form-label" for="solar-state">Estado</label><input class="form-control text-uppercase" id="solar-state" name="estado" maxlength="2" pattern="[A-Za-z]{2}" autocomplete="address-level1" placeholder="Ex.: SP" required><div class="invalid-feedback">Informe a sigla de duas letras.</div></div>
        <div><label class="form-label" for="solar-tariff">Tarifa estimada por kWh</label><div class="input-group"><span class="input-group-text">R$</span><input class="form-control" id="solar-tariff" name="tarifaKwh" type="number" min="0.0001" max="100" step="0.0001" required><span class="input-group-text">/kWh</span><div class="invalid-feedback">Informe uma tarifa acima de zero.</div></div><div class="form-text">Consulte o valor na sua fatura.</div></div>
        <div><label class="form-label" for="solar-noncompensable">Custos mensais não compensáveis</label><div class="input-group"><span class="input-group-text">R$</span><input class="form-control" id="solar-noncompensable" name="custoMensalNaoCompensavel" type="number" min="0" max="10000000" step="0.01" required><div class="invalid-feedback">Informe um valor igual ou menor que a conta.</div></div><div class="form-text">Inclua disponibilidade/rede e outros itens que seu orçamento considerar. Use zero somente se estimar que não há cobrança.</div></div>
        <div class="span-2"><label class="form-label" for="solar-compensable-percent">Parcela da tarifa considerada compensável</label><div class="input-group"><input class="form-control" id="solar-compensable-percent" name="percentualTarifaCompensavel" type="number" min="0" max="100" step="0.01" required><span class="input-group-text">%</span><div class="invalid-feedback">Informe um percentual entre 0 e 100.</div></div><div class="form-text">Confirme a parcela aplicável ao seu sistema com a distribuidora ou responsável técnico. Não há percentual predefinido.</div></div>
      </div>
      <div class="solar-form-section solar-form-section-spaced"><span class="solar-section-icon"><i class="bi bi-sun-fill" aria-hidden="true"></i></span><div><h2>Dados do sistema solar</h2><p>Use uma estimativa recebida em orçamento ou estudo técnico.</p></div></div>
      <div class="form-grid">
        <div><label class="form-label" for="solar-generation">Geração mensal estimada</label><div class="input-group"><input class="form-control" id="solar-generation" name="geracaoMensalKwh" type="number" min="0" max="1000000" step="0.01" placeholder="Opcional"><span class="input-group-text">kWh</span></div><div class="form-text">Sem este dado, não estimamos economia ou retorno.</div></div>
        <div><label class="form-label" for="solar-investment">Investimento inicial estimado</label><div class="input-group"><span class="input-group-text">R$</span><input class="form-control" id="solar-investment" name="investimentoInicial" type="number" min="0" max="100000000" step="0.01" placeholder="Opcional"></div><div class="form-text">Necessário para calcular o retorno simples.</div></div>
      </div>
      <div class="solar-law-notice mt-4"><i class="bi bi-exclamation-circle-fill" aria-hidden="true"></i><div><strong>Regras de compensação variam</strong><p>Podem existir custo de disponibilidade, uso da rede e componentes não compensáveis. Em 2026, a transição do art. 27 da Lei nº 14.300/2022 pode se aplicar a unidades abrangidas por esse artigo; o enquadramento depende do sistema e da conexão. Este simulador não aplica uma regra única nem determina seu enquadramento. Confirme as condições vigentes com a distribuidora e o responsável técnico.</p></div></div>
      <div class="form-actions"><button class="btn btn-primary solar-submit" type="submit"><i class="bi bi-sun" aria-hidden="true"></i>Simular cenário solar</button></div>
    </form></section>
    <aside class="solar-side">
      <section class="content-card solar-explainer"><span class="solar-side-icon"><i class="bi bi-sun-fill" aria-hidden="true"></i></span><span class="eyebrow">ODS 7 · ENERGIA RENOVÁVEL</span><h2>Vale a pena investir em energia solar?</h2><p>A geração fotovoltaica transforma a luz do sol em eletricidade. A viabilidade depende do local, do consumo, do sistema, do custo de instalação e das regras de compensação da unidade.</p>
        <div class="solar-explainer-item"><i class="bi bi-arrow-left-right" aria-hidden="true"></i><p><strong>Geração não é o mesmo que compensação</strong><br>Produzir energia não significa abater cada kWh da fatura integralmente. Créditos, consumo instantâneo e parcelas tarifárias seguem condições aplicáveis ao sistema.</p></div>
        <div class="solar-explainer-item"><i class="bi bi-leaf" aria-hidden="true"></i><p><strong>Benefício ambiental potencial</strong><br>A geração solar é renovável e pode contribuir para diversificar a matriz energética. O resultado ambiental depende do sistema e do ciclo de vida dos equipamentos.</p></div>
        <div class="solar-explainer-item"><i class="bi bi-clipboard-check" aria-hidden="true"></i><p><strong>Antes de contratar</strong><br>Compare propostas, geração estimada, garantias, homologação, custos de rede e regras da distribuidora. Não interprete esta simulação como proposta ou garantia de economia.</p></div>
      </section>
      <section class="solar-ods-card"><span class="ods-sun" aria-hidden="true"><i class="bi bi-sun"></i></span><div><strong>Energia acessível e sustentável</strong><p>A energia solar pode apoiar a transição para fontes renováveis, um dos objetivos da ODS 7 da ONU.</p></div></section>
    </aside>
  </div>
  <div id="solar-result" class="solar-result-region" aria-live="polite"></div>`;
}

function renderSolarResult(data) {
  const result = document.querySelector("#solar-result");
  if (!result) return;
  const generationUnavailable = !data.geracaoInformada;
  const returnLabel = data.retornoEstimadoMeses == null
    ? "Não calculado"
    : `${number(data.retornoEstimadoMeses)} meses`;
  result.innerHTML = `<section class="solar-result-section">
    <div class="solar-result-heading"><div><span class="eyebrow">RESULTADO DA SIMULAÇÃO</span><h2>Comparativo estimado</h2><p>${escapeHtml(data.cidade)}, ${escapeHtml(data.estado)} · valores mensais</p></div><span class="solar-result-badge"><i class="bi bi-info-circle" aria-hidden="true"></i>Estimativa, não proposta</span></div>
    ${data.consumoEstimadoPeloValorDaConta ? '<div class="notice solar-assumption-notice"><i class="bi bi-exclamation-triangle" aria-hidden="true"></i><span><strong>Consumo aproximado:</strong> como o consumo em kWh não foi informado, o sistema estimou (conta − custo não compensável) ÷ tarifa. A conta pode incluir tributos e outros itens; este resultado é apenas ilustrativo e pode divergir da fatura.</span></div>' : ""}
    ${generationUnavailable ? '<div class="notice solar-assumption-notice"><i class="bi bi-sun" aria-hidden="true"></i><span>Geração mensal não informada. Não existem dados de irradiação para esta localização nesta simulação; por isso, geração, compensação, economia, conta solar e retorno não foram inventados. Informe uma estimativa técnica para comparar os custos.</span></div>' : ""}
    ${!generationUnavailable ? `<div class="solar-stat-grid">
      <article class="solar-stat-card"><span>Conta atual</span><strong>${money(data.custoMensalSemSolar)}</strong><small>Valor mensal informado</small></article>
      <article class="solar-stat-card solar-stat-highlight"><span>Conta estimada com solar</span><strong>${money(data.custoMensalComSolar)}</strong><small>Inclui o custo não compensável informado</small></article>
      <article class="solar-stat-card"><span>Economia mensal</span><strong>${money(data.economiaMensal)}</strong><small>${number(data.percentualEconomia)}% da conta informada</small></article>
      <article class="solar-stat-card"><span>Economia anual</span><strong>${money(data.economiaAnual)}</strong><small>Projeção simples de 12 meses</small></article>
      <article class="solar-stat-card"><span>Investimento inicial</span><strong>${money(data.investimentoInicial)}</strong><small>${data.investimentoInicial == null ? "Não informado" : "Valor informado pelo usuário"}</small></article>
      <article class="solar-stat-card"><span>Retorno simples estimado</span><strong class="${data.retornoEstimadoMeses == null ? "solar-no-return" : ""}">${returnLabel}</strong><small>${data.economiaMensal === 0 ? "Sem economia positiva, não há retorno calculável" : data.investimentoInicial == null ? "Informe o investimento para estimar" : "Investimento ÷ economia mensal"}</small></article>
    </div>
    <div class="solar-chart-grid">
      <article class="panel solar-chart-panel"><div class="panel-heading"><div><h3>Comparativo mensal</h3><p>Conta informada e estimativa com solar</p></div></div><div class="chart-area"><canvas id="solar-cost-chart" role="img" aria-label="Gráfico comparando conta atual e estimativa com energia solar"></canvas></div></article>
      <article class="panel solar-chart-panel"><div class="panel-heading"><div><h3>Energia mensal</h3><p>Consumo e geração informados/estimados</p></div></div><div class="chart-area"><canvas id="solar-energy-chart" role="img" aria-label="Gráfico comparando consumo mensal com geração solar estimada"></canvas></div></article>
    </div>` : `<div class="solar-stat-grid solar-stat-grid-incomplete">
      <article class="solar-stat-card"><span>Consumo mensal atual</span><strong>${number(data.consumoMensalAtualKwh)} kWh</strong><small>${data.consumoEstimadoPeloValorDaConta ? "Estimativa simplificada" : "Consumo informado"}</small></article>
      <article class="solar-stat-card"><span>Conta mensal atual</span><strong>${money(data.custoMensalSemSolar)}</strong><small>Valor informado</small></article>
      <article class="solar-stat-card"><span>Geração solar</span><strong>—</strong><small>Aguardando estimativa técnica</small></article>
    </div>`}
    <details class="solar-assumptions"><summary>Ver premissas desta simulação</summary><div class="solar-assumptions-grid">
      <span>Tarifa informada<strong>${money(data.tarifaKwh)}/kWh</strong></span>
      <span>Parcela da tarifa considerada compensável<strong>${number(data.percentualTarifaCompensavel)}%</strong></span>
      <span>Custo mensal não compensável informado<strong>${money(data.custoMensalNaoCompensavel)}</strong></span>
      <span>Geração mensal do sistema<strong>${data.geracaoInformada ? `${number(data.geracaoSolarMensalKwh)} kWh` : "Não informada"}</strong></span>
      <span>Energia considerada para compensação<strong>${data.energiaCompensavelKwh == null ? "Não calculada" : `${number(data.energiaCompensavelKwh)} kWh`}</strong></span>
      <span>Base de consumo<strong>${data.consumoEstimadoPeloValorDaConta ? "Conta menos custo informado ÷ tarifa; aproximação" : "kWh informado pelo usuário"}</strong></span>
    </div><p>Estimativa simplificada: o crédito é limitado ao menor valor entre consumo e geração e usa a parcela compensável e o custo não compensável informados. Não modela automaticamente modalidade de conexão, consumo simultâneo, créditos acumulados, tributos, bandeiras, reajustes, degradação, manutenção ou financiamento. Confirme regras e valores atuais com a distribuidora e o responsável técnico, inclusive eventual transição do art. 27 da Lei nº 14.300/2022 em 2026.</p></details>
  </section>`;
  if (!generationUnavailable) drawSolarCharts(data);
  result.scrollIntoView({ behavior: "smooth", block: "start" });
}

function drawSolarCharts(data) {
  if (!window.Chart) return;
  const moneyTick = (value) => brCurrency.format(value);
  const colors = { bill: "#176b43", solar: "#e5a331", consumed: "#176b43", generated: "#80b889" };
  const shared = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: "#173d2b",
        padding: 11,
        cornerRadius: 8,
        titleFont: { family: "DM Sans", size: 12, weight: "600" },
        bodyFont: { family: "DM Sans", size: 11 }
      }
    }
  };
  const costChart = new Chart(document.querySelector("#solar-cost-chart"), {
    type: "bar",
    data: {
      labels: ["Conta atual", "Com solar"],
      datasets: [{
        data: [Number(data.custoMensalSemSolar), Number(data.custoMensalComSolar)],
        backgroundColor: [colors.bill, colors.solar],
        borderRadius: 7,
        maxBarThickness: 64
      }]
    },
    options: {
      ...shared,
      scales: {
        x: { grid: { display: false }, border: { display: false }, ticks: { color: "#3e4d43", font: { family: "DM Sans", size: 11 } } },
        y: { beginAtZero: true, border: { display: false }, grid: { color: "#e9eee9", drawTicks: false }, ticks: { color: "#536258", padding: 8, font: { family: "DM Sans", size: 10 }, callback: moneyTick } }
      },
      plugins: { ...shared.plugins, tooltip: { ...shared.plugins.tooltip, callbacks: { label: (context) => ` ${money(context.raw)}` } } }
    }
  });
  const remaining = Math.max(Number(data.consumoMensalAtualKwh) - Number(data.energiaCompensavelKwh), 0);
  const energyChart = new Chart(document.querySelector("#solar-energy-chart"), {
    type: "doughnut",
    data: {
      labels: ["Energia compensável estimada", "Consumo não coberto pela geração"],
      datasets: [{ data: [Number(data.energiaCompensavelKwh), remaining], backgroundColor: [colors.generated, colors.consumed], borderColor: "#fff", borderWidth: 3, hoverOffset: 4 }]
    },
    options: {
      ...shared,
      cutout: "66%",
      plugins: {
        ...shared.plugins,
        legend: {
          position: "bottom",
          labels: { color: "#3e4d43", usePointStyle: true, pointStyle: "circle", boxWidth: 8, boxHeight: 8, padding: 14, font: { family: "DM Sans", size: 10 } }
        },
        tooltip: { ...shared.plugins.tooltip, callbacks: { label: (context) => ` ${number(context.raw)} kWh` } }
      }
    }
  });
  charts.push(costChart, energyChart);
}

function renderTips() {
  const tips = [
    ["bi-sun", "Aproveite a luz natural", "Abra cortinas e persianas durante o dia. Ao sair de um ambiente, apague as luzes que não estiverem sendo usadas.", "Iluminação"],
    ["bi-lightbulb", "Prefira lâmpadas LED", "Ao substituir uma lâmpada, confira se o modelo LED atende ao ambiente e à luminária. Compare a potência indicada na embalagem.", "Iluminação"],
    ["bi-snow", "Cuide da vedação da geladeira", "Verifique se a porta fecha corretamente e evite deixá-la aberta por muito tempo. Siga as orientações do fabricante para instalação e temperatura.", "Geladeira"],
    ["bi-droplet", "Reduza o tempo de banho", "Banhos mais curtos diminuem o tempo de funcionamento do chuveiro elétrico. Use a posição adequada à temperatura ambiente.", "Chuveiro elétrico"],
    ["bi-wind", "Use o ar-condicionado com atenção", "Mantenha portas e janelas fechadas enquanto o aparelho estiver ligado e limpe os filtros conforme o manual.", "Ar-condicionado"],
    ["bi-tv", "Desligue eletrônicos sem uso", "Desligue aparelhos quando não estiverem em uso. Evite deixar carregadores conectados sem necessidade.", "Eletrônicos"],
    ["bi-water", "Planeje a lavagem de roupas", "Quando adequado à sua rotina e às instruções da máquina, reúna peças para usar o equipamento com a carga recomendada.", "Máquina de lavar"],
    ["bi-plug", "Conheça seus aparelhos", "Consulte a potência na etiqueta ou no manual e observe seu tempo de uso. Esses dados ajudam a entender a estimativa de consumo.", "Consumo consciente"],
    ["bi-book", "Compare antes de substituir", "Ao comprar um aparelho, compare potência, eficiência energética, tamanho e suas necessidades. Confira informações oficiais do produto.", "Escolhas de compra"]
  ];
  root.innerHTML = `${heading("INFORMAÇÃO PARA O DIA A DIA", "Dicas de economia", "Atitudes simples para observar e repensar o uso de energia.")}<div class="notice mb-3"><i class="bi bi-info-circle" aria-hidden="true"></i><span>As dicas são educativas. Os resultados variam conforme o aparelho, a rotina e as condições de uso; não há economia garantida.</span></div>
    <section class="tip-grid">${tips.map(([icon, title, description, category]) => `<article class="tip-card"><span class="tip-icon"><i class="bi ${icon}" aria-hidden="true"></i></span><span class="tip-category">${category}</span><h2>${title}</h2><p>${description}</p></article>`).join("")}</section>`;
}

function renderAbout() {
  root.innerHTML = `<section class="about-hero"><span class="eyebrow">NEXTURING · ODS 7 · OBJETIVOS DE DESENVOLVIMENTO SUSTENTÁVEL</span><h1>Energia limpa e acessível começa com informação.</h1><p>Energia Consciente é o projeto Nexturing de extensão universitária, criado para ajudar pessoas a visualizar estimativas de consumo de aparelhos e refletir sobre seus hábitos de uso.</p></section>
    <section class="about-grid">
      <article class="content-card"><div class="ods-number"><strong>7</strong><h2>O que é a ODS 7?</h2></div><p>A ODS 7, da Agenda 2030 da ONU, busca assegurar acesso confiável, sustentável, moderno e a preço acessível à energia para todas as pessoas.</p></article>
      <article class="content-card"><span class="tip-icon"><i class="bi bi-search" aria-hidden="true"></i></span><h2>O problema que abordamos</h2><p>Nem sempre é fácil relacionar potência e tempo de uso ao consumo mensal. A plataforma organiza essas informações em estimativas compreensíveis, sem substituir a conta de energia.</p></article>
      <article class="content-card"><span class="tip-icon"><i class="bi bi-calculator" aria-hidden="true"></i></span><h2>Como funciona</h2><p>Você informa os aparelhos, potência e hábitos de uso. O sistema calcula o consumo estimado, permite informar uma tarifa e simular mudanças sem alterar seus dados originais.</p></article>
      <article class="content-card"><span class="tip-icon"><i class="bi bi-people" aria-hidden="true"></i></span><h2>Contribuição comunitária</h2><p>Ao oferecer uma ferramenta educativa e gratuita, o projeto apoia estudantes, famílias e pequenos consumidores na compreensão do consumo energético e na conversa sobre uso responsável.</p></article>
      <article class="content-card"><span class="tip-icon"><i class="bi bi-shield-check" aria-hidden="true"></i></span><h2>Transparência</h2><p>Os resultados são estimativas baseadas somente nos dados informados. Tarifas e encargos da fatura podem variar; não prometemos economia nem consultamos concessionárias.</p></article>
      <article class="content-card team-card"><span class="tip-icon"><i class="bi bi-mortarboard" aria-hidden="true"></i></span><h2>Projeto acadêmico Nexturing</h2>
        <div class="project-details"><span><strong>Componente:</strong> Itinerário Extensionista 2</span><span><strong>Semestre:</strong> 4º semestre</span><span><strong>Professor:</strong> Valter De Sales Santana</span></div>
        <h3 class="team-heading">Integrantes</h3>
        <ul class="team-list">
          <li><strong>Tiago Alves de Souza</strong><span>UCS · Ciência da Computação (Bacharelado) · 4N1 · 2026/2</span></li>
          <li><strong>Ronald Nathan Nascimento Alves</strong><span>UCS · Ciência da Computação (Bacharelado) · 4N1 · 2026/2</span></li>
          <li><strong>Wendel de Sousa Nobrega</strong><span>UCS · Ciência da Computação (Bacharelado) · 4N1 · 2026/2</span></li>
          <li><strong>Matheus dos Santos Godoy</strong><span>UCS · Ciência da Computação (Bacharelado) · 3N1 · 2026/2</span></li>
        </ul>
      </article>
    </section>`;
}

async function renderQuiz() {
  if (!quizRound) {
    quizRound = await api.startQuiz();
    quizAnswers = Array(quizRound.totalPerguntas).fill(null);
    quizSelections = Array(quizRound.totalPerguntas).fill(null);
    quizIndex = 0;
    quizResult = null;
  }
  if (currentView !== "quiz") return;
  if (quizResult) {
    renderQuizResult(quizResult);
    return;
  }

  const question = quizRound.perguntas[quizIndex];
  const answer = quizAnswers[quizIndex];
  const selectedId = quizSelections[quizIndex];
  const answeredCount = quizAnswers.filter(Boolean).length;
  const progress = (answeredCount / quizRound.totalPerguntas) * 100;
  root.innerHTML = `${heading("APRENDA JOGANDO", "Quiz Energético", "Dez perguntas sobre hábitos, aparelhos, energia renovável e sustentabilidade. Cada acerto vale 10 pontos.")}
    <section class="quiz-shell">
      <div class="quiz-progress-card">
        <div class="quiz-progress-heading"><span>Pergunta ${quizIndex + 1} de ${quizRound.totalPerguntas}</span><strong>${answeredCount} respondidas</strong></div>
        <div class="progress quiz-progress" role="progressbar" aria-label="Progresso do quiz" aria-valuenow="${answeredCount}" aria-valuemin="0" aria-valuemax="${quizRound.totalPerguntas}">
          <div class="progress-bar" style="width:${progress}%"></div>
        </div>
        <div class="quiz-categories" aria-label="Categorias do quiz"><span>Economia doméstica</span><span>Eletrodomésticos</span><span>Energia solar</span><span>Energia renovável</span><span>Sustentabilidade</span></div>
      </div>
      <section class="content-card quiz-question-card" aria-labelledby="quiz-question-title">
        <span class="quiz-category">${escapeHtml(question.categoria)}</span>
        <h2 id="quiz-question-title">${escapeHtml(question.enunciado)}</h2>
        <div class="quiz-options" role="group" aria-label="Alternativas">
          ${question.alternativas.map((alternative, index) => {
            const answered = Boolean(answer);
            const isSelected = answered
              ? alternative.id === answer.alternativaSelecionadaId
              : alternative.id === selectedId;
            const isCorrect = answered && alternative.id === answer.alternativaCorretaId;
            const isIncorrect = answered && isSelected && !answer.correta;
            const stateClass = isCorrect ? "is-correct" : isIncorrect ? "is-incorrect" : isSelected ? "is-selected" : "";
            const stateIcon = isCorrect ? '<i class="bi bi-check-circle-fill" aria-hidden="true"></i>'
              : isIncorrect ? '<i class="bi bi-x-circle-fill" aria-hidden="true"></i>'
                : `<span class="quiz-option-letter">${String.fromCharCode(65 + index)}</span>`;
            return `<button class="quiz-option ${stateClass}" type="button" data-action="quiz-select" data-id="${alternative.id}" aria-pressed="${isSelected}" ${answered || quizSubmitting ? "disabled" : ""}>
              ${stateIcon}<span>${escapeHtml(alternative.texto)}</span></button>`;
          }).join("")}
        </div>
        ${answer ? `<section class="quiz-feedback ${answer.correta ? "feedback-correct" : "feedback-incorrect"}" aria-live="polite">
          <strong><i class="bi ${answer.correta ? "bi-check-circle-fill" : "bi-info-circle-fill"}" aria-hidden="true"></i>${answer.correta ? "Resposta correta! +10 pontos" : "Não foi dessa vez. Confira a resposta correta acima."}</strong>
          <p>${escapeHtml(answer.explicacao)}</p>
        </section>` : ""}
        <div class="quiz-controls">
          <button class="btn btn-outline-secondary" type="button" data-action="quiz-previous" ${quizIndex === 0 ? "disabled" : ""}><i class="bi bi-arrow-left" aria-hidden="true"></i>Anterior</button>
          ${answer
            ? `<button class="btn btn-primary" type="button" data-action="quiz-next">Próxima pergunta<i class="bi bi-arrow-right" aria-hidden="true"></i></button>`
            : `<button class="btn btn-primary" type="button" data-action="quiz-answer" ${selectedId == null || quizSubmitting ? "disabled" : ""}>${quizSubmitting ? "Registrando..." : "Confirmar resposta"}<i class="bi bi-check2" aria-hidden="true"></i></button>`}
        </div>
      </section>
    </section>`;
}

function renderQuizResult(result) {
  root.innerHTML = `${heading("RODADA CONCLUÍDA", "Mandou bem!", "Conhecimento também é uma forma de transformar a relação com a energia.")}
    <section class="quiz-result content-card">
      <div class="quiz-result-icon"><i class="bi bi-stars" aria-hidden="true"></i></div>
      <span class="eyebrow">SUA CLASSIFICAÇÃO</span>
      <h2>${escapeHtml(result.classificacao)}</h2>
      <p class="quiz-result-intro">Obrigado por participar. Continue aprendendo e compartilhe boas ideias sobre energia limpa e acessível.</p>
      <div class="quiz-score-grid">
        <article><span>Acertos</span><strong>${result.acertos}</strong></article>
        <article><span>Erros</span><strong>${result.erros}</strong></article>
        <article><span>Pontuação</span><strong>${result.pontuacao}<small>/100</small></strong></article>
        <article><span>Aproveitamento</span><strong>${result.percentual}%</strong></article>
      </div>
      <div class="quiz-result-actions">
        <button class="btn btn-primary" type="button" data-action="quiz-restart"><i class="bi bi-arrow-repeat" aria-hidden="true"></i>Jogar novamente</button>
        <a class="btn btn-outline-secondary" href="#dicas"><i class="bi bi-lightbulb" aria-hidden="true"></i>Conhecer dicas de economia</a>
        <a class="btn btn-outline-secondary" href="#solar"><i class="bi bi-sun" aria-hidden="true"></i>Simulador de energia solar</a>
      </div>
    </section>
    <div class="quiz-result-note"><i class="bi bi-leaf" aria-hidden="true"></i><span>Pequenas escolhas e informação confiável ajudam a construir um futuro mais sustentável.</span></div>`;
}

async function openTariffModal() {
  try {
    const config = await api.configuration();
    document.querySelector("#tarifaKwh").value = config.tarifaKwh ?? "";
    bootstrapModal.show();
    setTimeout(() => document.querySelector("#tarifaKwh").focus(), 150);
  } catch (error) {
    showError(error);
  }
}

document.addEventListener("click", async (event) => {
  const actionButton = event.target.closest("[data-action]");
  if (!actionButton) return;
  const { action, id, name } = actionButton.dataset;
  if (action === "tarifa") await openTariffModal();
  else if (action === "retry") await navigate();
  else if (action === "quiz-select") {
    if (!quizAnswers[quizIndex] && !quizSubmitting) {
      quizSelections[quizIndex] = Number(id);
      renderQuiz();
    }
  }
  else if (action === "quiz-previous") {
    if (quizIndex > 0) {
      quizIndex--;
      renderQuiz();
    }
  }
  else if (action === "quiz-next") {
    if (quizAnswers[quizIndex] && quizIndex < quizRound.totalPerguntas - 1) {
      quizIndex++;
      renderQuiz();
    }
  }
  else if (action === "quiz-answer") {
    if (quizSelections[quizIndex] == null || quizSubmitting || quizAnswers[quizIndex]) return;
    quizSubmitting = true;
    renderQuiz();
    try {
      const question = quizRound.perguntas[quizIndex];
      const response = await api.answerQuiz(quizRound.id, question.id, quizSelections[quizIndex]);
      quizAnswers[quizIndex] = response;
      quizSubmitting = false;
      if (response.finalizada) {
        quizResult = response.resultado;
        quizRound.finalizada = true;
      }
      if (currentView === "quiz") renderQuiz();
    } catch (error) {
      quizSubmitting = false;
      showError(error);
      if (currentView === "quiz") renderQuiz();
    }
  }
  else if (action === "quiz-restart") {
    quizRound = null;
    quizAnswers = [];
    quizSelections = [];
    quizIndex = 0;
    quizResult = null;
    quizSubmitting = false;
    await renderQuiz();
  }
  else if (action === "edit") location.hash = `editar?id=${encodeURIComponent(id)}`;
  else if (action === "delete") {
    if (!window.confirm(`Deseja excluir "${name}"? Essa ação não pode ser desfeita.`)) return;
    try {
      await api.deleteAppliance(id);
      showToast("Aparelho excluído. O dashboard foi atualizado.");
      if (currentView === "dashboard") await renderDashboard();
      else await renderAppliances();
    } catch (error) {
      showError(error);
    }
  }
});

mobileMenu.addEventListener("click", () => {
  setMobileNavigation(!sidebar.classList.contains("open"));
});

navBackdrop.addEventListener("click", () => setMobileNavigation(false, true));

document.addEventListener("keydown", (event) => {
  if (event.key === "Escape" && sidebar.classList.contains("open")) {
    setMobileNavigation(false, true);
  }
});

window.addEventListener("resize", () => {
  if (!window.matchMedia("(max-width: 760px)").matches) setMobileNavigation(false);
});

document.addEventListener("change", (event) => {
  if (event.target.id === "sim-device") {
    const selected = applianceCache.find((item) => String(item.id) === event.target.value);
    const input = document.querySelector("#sim-hours");
    const limit = document.querySelector("#sim-limit");
    input.max = selected?.horasPorDia ?? 24;
    input.value = "";
    limit.textContent = selected ? `O uso atual é ${number(selected.horasPorDia)} h/dia; a simulação deve ser igual ou menor.` : "Escolha um aparelho para ver o limite de uso atual.";
  }
});

document.addEventListener("submit", async (event) => {
  const form = event.target;
  if (!form.matches("#appliance-form, #calculator-form, #simulator-form, #solar-form, #tariff-form")) return;
  event.preventDefault();
  if (!form.reportValidity()) {
    form.classList.add("was-validated");
    return;
  }
  try {
    if (form.id === "appliance-form") {
      const data = {
        nome: form.elements.nome.value.trim(),
        potenciaWatts: Number(form.elements.potenciaWatts.value),
        horasPorDia: Number(form.elements.horasPorDia.value),
        diasPorMes: Number(form.elements.diasPorMes.value)
      };
      const id = form.dataset.id;
      if (id) await api.updateAppliance(id, data);
      else await api.createAppliance(data);
      showToast(id ? "Aparelho atualizado com sucesso." : "Aparelho cadastrado com sucesso.");
      location.hash = "aparelhos";
    } else if (form.id === "calculator-form") {
      const data = await api.calculate({
        potenciaWatts: Number(form.elements.potenciaWatts.value),
        horasPorDia: Number(form.elements.horasPorDia.value),
        diasPorMes: Number(form.elements.diasPorMes.value)
      });
      const name = document.querySelector("#calc-name").value.trim();
      document.querySelector("#calculator-result").innerHTML = `<section class="result-card" aria-live="polite"><h3>Resultado${name ? ` · ${escapeHtml(name)}` : ""}</h3><div class="result-grid"><div class="result-item"><span>Consumo mensal estimado</span><strong>${number(data.consumoMensalKwh)} kWh</strong></div><div class="result-item"><span>Custo estimado</span><strong>${money(data.custoMensalEstimado)}</strong></div><div class="result-item"><span>Base</span><strong>${data.tarifaConfigurada ? "Tarifa informada" : "Sem tarifa"}</strong></div></div><p class="text-muted-small mt-3 mb-0">Estimativa informativa; pode não corresponder ao valor final da conta.</p></section>`;
    } else if (form.id === "simulator-form") {
      const data = await api.simulate({
        aparelhoId: Number(document.querySelector("#sim-device").value),
        horasPorDiaSimuladas: Number(document.querySelector("#sim-hours").value)
      });
      document.querySelector("#simulator-result").innerHTML = `<section class="result-card" aria-live="polite"><h3>Resultado · ${escapeHtml(data.nomeAparelho)}</h3><div class="result-grid"><div class="result-item"><span>Consumo original</span><strong>${number(data.consumoOriginalKwh)} kWh</strong></div><div class="result-item"><span>Consumo simulado</span><strong>${number(data.consumoSimuladoKwh)} kWh</strong></div><div class="result-item"><span>Redução estimada</span><strong>${number(data.reducaoConsumoKwh)} kWh</strong></div><div class="result-item"><span>Economia estimada</span><strong>${money(data.economiaEstimada)}</strong></div><div class="result-item"><span>Redução percentual</span><strong>${number(data.percentualReducao)}%</strong></div></div><p class="text-muted-small mt-3 mb-0">Esta simulação não modificou o cadastro original. Valores financeiros são estimativas.</p></section>`;
    } else if (form.id === "solar-form") {
      const data = await api.simulateSolar({
        valorContaMensal: Number(form.elements.valorContaMensal.value),
        consumoMensalKwh: form.elements.consumoMensalKwh.value === "" ? null : Number(form.elements.consumoMensalKwh.value),
        cidade: form.elements.cidade.value.trim(),
        estado: form.elements.estado.value.trim().toUpperCase(),
        tarifaKwh: Number(form.elements.tarifaKwh.value),
        investimentoInicial: form.elements.investimentoInicial.value === "" ? null : Number(form.elements.investimentoInicial.value),
        geracaoMensalKwh: form.elements.geracaoMensalKwh.value === "" ? null : Number(form.elements.geracaoMensalKwh.value),
        custoMensalNaoCompensavel: Number(form.elements.custoMensalNaoCompensavel.value),
        percentualTarifaCompensavel: Number(form.elements.percentualTarifaCompensavel.value)
      });
      renderSolarResult(data);
    } else {
      await api.updateTariff({ tarifaKwh: Number(form.elements.tarifaKwh.value) });
      bootstrapModal.hide();
      showToast("Tarifa atualizada. Os custos estimados foram recalculados.");
      if (currentView === "dashboard") await renderDashboard();
      else if (currentView === "aparelhos") await renderAppliances();
      else if (currentView === "simulador") await renderSimulator();
    }
  } catch (error) {
    showError(error);
  }
});

window.addEventListener("hashchange", navigate);
bootstrapModal = new bootstrap.Modal(document.querySelector("#tariff-modal"));
setMobileNavigation(false);
navigate();
