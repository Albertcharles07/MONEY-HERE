package com.moneyhere;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes dos endpoints de metas e orçamento do ApiController.
 * Os repositórios e o AuthService são simulados com Mockito, então os testes
 * rodam sem banco de dados e sem precisar subir a aplicação inteira.
 */
class ApiControllerTest {

    private AuthService auth;
    private Goals goals;
    private Entries entries;
    private MockMvc mvc;
    private User usuario;

    @BeforeEach
    void setUp() {
        auth = mock(AuthService.class);
        goals = mock(Goals.class);
        entries = mock(Entries.class);
        ApiController controller = new ApiController(auth, goals, entries, mock(Favorites.class));

        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiErrors())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        // Simula um usuário logado: o token JWT fica no contexto de segurança
        // e o AuthService devolve o usuário correspondente.
        usuario = new User();
        usuario.id = 1L;
        usuario.email = "lucas@teste.com";
        Jwt jwt = Jwt.withTokenValue("token-teste").header("alg", "HS256").subject(usuario.email).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        when(auth.me(any(Jwt.class))).thenReturn(usuario);
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    // ---------- Metas ----------

    @Test
    void buscaMetaDoProprioUsuarioPorId() throws Exception {
        Goal meta = meta(5L, "Reserva de emergência", "10000.00", "2500.00");
        when(goals.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(meta));

        mvc.perform(get("/api/goals/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.title").value("Reserva de emergência"));
    }

    @Test
    void metaDeOutroUsuarioRetorna404() throws Exception {
        // A consulta filtra pelo id do usuário logado, então a meta de outra pessoa não é encontrada
        when(goals.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/goals/99")).andExpect(status().isNotFound());
        mvc.perform(delete("/api/goals/99")).andExpect(status().isNotFound());

        verify(goals, never()).delete(any(Goal.class));
    }

    @Test
    void criaMetaValida() throws Exception {
        when(goals.save(any(Goal.class))).thenAnswer(chamada -> {
            Goal salva = chamada.getArgument(0);
            salva.id = 10L;
            return salva;
        });

        mvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"  Viagem  \",\"targetAmount\":5000,\"currentAmount\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Viagem"));
    }

    @Test
    void rejeitaMetaComValorAlvoZerado() throws Exception {
        mvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Viagem\",\"targetAmount\":0,\"currentAmount\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.targetAmount").exists());

        verify(goals, never()).save(any(Goal.class));
    }

    // ---------- Orçamento ----------

    @Test
    void buscaLancamentoDoProprioUsuarioPorId() throws Exception {
        Entry lancamento = lancamento(7L, "INCOME", "Salario", "3000.00", LocalDate.of(2026, 11, 5));
        when(entries.findByIdAndUserId(7L, 1L)).thenReturn(Optional.of(lancamento));

        mvc.perform(get("/api/budget/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kind").value("INCOME"))
                .andExpect(jsonPath("$.amount").value(3000.0));
    }

    @Test
    void editarLancamentoDeOutroUsuarioRetorna404() throws Exception {
        when(entries.findByIdAndUserId(50L, 1L)).thenReturn(Optional.empty());

        mvc.perform(put("/api/budget/50")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Mercado\",\"category\":\"Mercado\",\"kind\":\"EXPENSE\",\"amount\":100,\"entryDate\":\"2026-11-10\"}"))
                .andExpect(status().isNotFound());

        verify(entries, never()).save(any(Entry.class));
    }

    // ---------- Resumo do orçamento ----------

    @Test
    void resumoSomaReceitasDespesasECategorias() throws Exception {
        when(entries.findByUserIdOrderByEntryDateDescIdDesc(1L)).thenReturn(List.of(
                lancamento(1L, "INCOME", "Salario", "3000.00", LocalDate.of(2026, 11, 5)),
                lancamento(2L, "EXPENSE", "Mercado", "800.50", LocalDate.of(2026, 11, 8)),
                lancamento(3L, "EXPENSE", "Transporte", "450.00", LocalDate.of(2026, 11, 9))));

        mvc.perform(get("/api/budget/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").doesNotExist())
                .andExpect(jsonPath("$.income").value(3000.0))
                .andExpect(jsonPath("$.expense").value(1250.5))
                .andExpect(jsonPath("$.balance").value(1749.5))
                .andExpect(jsonPath("$.entries").value(3))
                .andExpect(jsonPath("$.expensesByCategory.Mercado").value(800.5))
                .andExpect(jsonPath("$.expensesByCategory.Transporte").value(450.0));
    }

    @Test
    void resumoComMesBuscaDoPrimeiroAoUltimoDia() throws Exception {
        when(entries.findByUserIdAndEntryDateBetween(1L, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30)))
                .thenReturn(List.of(lancamento(1L, "EXPENSE", "Lazer", "120.00", LocalDate.of(2026, 11, 20))));

        mvc.perform(get("/api/budget/summary").param("month", "2026-11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value("2026-11"))
                .andExpect(jsonPath("$.income").value(0))
                .andExpect(jsonPath("$.balance").value(-120.0));

        verify(entries).findByUserIdAndEntryDateBetween(1L, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));
    }

    @Test
    void resumoComMesInvalidoRetorna400() throws Exception {
        mvc.perform(get("/api/budget/summary").param("month", "novembro"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("AAAA-MM")));
    }

    // ---------- Auxiliares ----------

    private Goal meta(Long id, String titulo, String alvo, String atual) {
        Goal g = new Goal();
        g.id = id;
        g.user = usuario;
        g.title = titulo;
        g.targetAmount = new BigDecimal(alvo);
        g.currentAmount = new BigDecimal(atual);
        return g;
    }

    private Entry lancamento(Long id, String tipo, String categoria, String valor, LocalDate data) {
        Entry e = new Entry();
        e.id = id;
        e.user = usuario;
        e.title = categoria;
        e.category = categoria;
        e.kind = tipo;
        e.amount = new BigDecimal(valor);
        e.entryDate = data;
        return e;
    }
}
