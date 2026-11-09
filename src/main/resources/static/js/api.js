const API_BASE = "/api";

async function request(path, options = {}) {
  let response;
  try {
    response = await fetch(`${API_BASE}${path}`, {
      ...options,
      headers: {
        Accept: "application/json",
        ...(options.body ? { "Content-Type": "application/json" } : {}),
        ...options.headers
      }
    });
  } catch {
    throw new Error("Não foi possível conectar ao servidor. Verifique se a aplicação está em execução.");
  }

  if (response.status === 204) return null;
  const contentType = response.headers.get("content-type") || "";
  const payload = contentType.includes("application/json") ? await response.json() : null;
  if (!response.ok) {
    const details = payload?.detalhes ? Object.values(payload.detalhes).join(" ") : "";
    throw new Error([payload?.mensagem, details].filter(Boolean).join(" ") || `Não foi possível concluir a operação (${response.status}).`);
  }
  return payload;
}

export const api = {
  dashboard: () => request("/dashboard"),
  appliances: () => request("/aparelhos"),
  appliance: (id) => request(`/aparelhos/${encodeURIComponent(id)}`),
  createAppliance: (data) => request("/aparelhos", { method: "POST", body: JSON.stringify(data) }),
  updateAppliance: (id, data) => request(`/aparelhos/${encodeURIComponent(id)}`, { method: "PUT", body: JSON.stringify(data) }),
  deleteAppliance: (id) => request(`/aparelhos/${encodeURIComponent(id)}`, { method: "DELETE" }),
  calculate: (data) => request("/consumo", { method: "POST", body: JSON.stringify(data) }),
  simulate: (data) => request("/simulacoes", { method: "POST", body: JSON.stringify(data) }),
  simulateSolar: (data) => request("/simulacoes/solar", { method: "POST", body: JSON.stringify(data) }),
  startQuiz: () => request("/quiz/tentativas", { method: "POST" }),
  answerQuiz: (attemptId, questionId, alternativeId) => request(
    `/quiz/tentativas/${encodeURIComponent(attemptId)}/perguntas/${encodeURIComponent(questionId)}/respostas`,
    { method: "POST", body: JSON.stringify({ alternativaId: alternativeId }) }
  ),
  quizHistory: () => request("/quiz/historico"),
  configuration: () => request("/configuracoes"),
  updateTariff: (data) => request("/configuracoes", { method: "PUT", body: JSON.stringify(data) })
};
