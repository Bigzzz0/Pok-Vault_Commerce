package com.pokevault.modules.catalog;

import com.pokevault.common.exception.ResourceNotFoundException;
import com.pokevault.common.response.PageResponse;
import com.pokevault.modules.catalog.controller.CardApiController;
import com.pokevault.modules.catalog.dto.*;
import com.pokevault.modules.catalog.service.CardService;
import com.pokevault.modules.trade.advice.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** MVC contract tests; security filters are intentionally outside this fixture. */
@ExtendWith(MockitoExtension.class)
class CardApiControllerTest {
    @Mock CardService service;
    MockMvc mvc;
    private static final String VALID = """
        {"expansionId":1,"cardNumber":"001/226","name":"Pikachu",
         "cardType":"POKEMON","rarity":"COMMON","hp":60,"retreatCost":1}
        """;

    @BeforeEach void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new CardApiController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver()).build();
    }

    @Test void listBindsFiltersAndReturnsData() throws Exception {
        when(service.getAllCards(any())).thenReturn(List.of(CardResponse.builder().id(1L).name("Pikachu").build()));
        mvc.perform(get("/api/v1/cards").param("name", "Pika").param("expansionCode", "A1").param("rarity", "COMMON"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].name").value("Pikachu"));
        var capture = ArgumentCaptor.forClass(CardFilterRequest.class);
        verify(service).getAllCards(capture.capture());
        assertThat(capture.getValue().getName()).isEqualTo("Pika");
        assertThat(capture.getValue().getExpansionCode()).isEqualTo("A1");
        assertThat(capture.getValue().getRarity()).isEqualTo("COMMON");
    }

    @Test void getCardReturns200() throws Exception {
        when(service.getCardById(1L)).thenReturn(CardResponse.builder().id(1L).name("Pikachu").build());
        mvc.perform(get("/api/v1/cards/1")).andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(1));
    }

    @Test void missingCardReturns404() throws Exception {
        when(service.getCardById(99L)).thenThrow(new ResourceNotFoundException("Card", "id", 99L));
        mvc.perform(get("/api/v1/cards/99")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }

    @Test void searchUsesLiteralRouteRatherThanIdRoute() throws Exception {
        when(service.searchCards("Pika")).thenReturn(List.of());
        mvc.perform(get("/api/v1/cards/search").param("q", "Pika"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());
        verify(service).searchCards("Pika");
    }

    @Test void paginationBindsPageSizeAndDescendingSort() throws Exception {
        when(service.getCardsPaged(any(), any())).thenReturn(PageResponse.<CardResponse>builder()
                .content(List.of()).pageNumber(2).pageSize(5).totalElements(0).build());
        mvc.perform(get("/api/v1/cards/paged").param("page", "2").param("size", "5").param("sort", "name,desc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.pageNumber").value(2));
        var capture = ArgumentCaptor.forClass(Pageable.class);
        verify(service).getCardsPaged(any(), capture.capture());
        assertThat(capture.getValue().getPageNumber()).isEqualTo(2);
        assertThat(capture.getValue().getPageSize()).isEqualTo(5);
        assertThat(capture.getValue().getSort()).isEqualTo(Sort.by("name").descending());
    }

    @Test void defaultPaginationUsesCardNumberAscending() throws Exception {
        when(service.getCardsPaged(any(), any())).thenReturn(PageResponse.<CardResponse>builder().content(List.of()).build());
        mvc.perform(get("/api/v1/cards/paged")).andExpect(status().isOk());
        var capture = ArgumentCaptor.forClass(Pageable.class);
        verify(service).getCardsPaged(any(), capture.capture());
        assertThat(capture.getValue().getPageSize()).isEqualTo(10);
        assertThat(capture.getValue().getPageNumber()).isZero();
        assertThat(capture.getValue().getSort()).isEqualTo(Sort.by("cardNumber").ascending());
    }

    @Test void createReturns201AndBindsRequest() throws Exception {
        when(service.createCard(any())).thenReturn(CardResponse.builder().id(7L).name("Pikachu").build());
        mvc.perform(post("/api/v1/cards").contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.data.id").value(7));
        verify(service).createCard(argThat(r -> r.getExpansionId().equals(1L) && r.getName().equals("Pikachu") && r.getHp() == 60));
    }

    static Stream<String> invalidRequests() {
        return Stream.of(VALID.replace("\"expansionId\":1", "\"expansionId\":null"),
                VALID.replace("\"name\":\"Pikachu\"", "\"name\":\" \""),
                VALID.replace("\"cardNumber\":\"001/226\"", "\"cardNumber\":\"\""),
                VALID.replace("\"cardType\":\"POKEMON\"", "\"cardType\":null"),
                VALID.replace("\"rarity\":\"COMMON\"", "\"rarity\":null"),
                VALID.replace("\"hp\":60", "\"hp\":-1"),
                VALID.replace("\"retreatCost\":1", "\"retreatCost\":-1"),
                VALID.replace("Pikachu", "x".repeat(101)),
                VALID.replace("001/226", "x".repeat(21)));
    }

    @ParameterizedTest @MethodSource("invalidRequests")
    void invalidCreateReturns400WithoutCallingService(String json) throws Exception {
        mvc.perform(post("/api/v1/cards").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details").isNotEmpty());
        verifyNoInteractions(service);
    }

    @Test void updateReturns200() throws Exception {
        when(service.updateCard(eq(1L), any())).thenReturn(CardResponse.builder().id(1L).name("Pikachu").build());
        mvc.perform(put("/api/v1/cards/1").contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(1));
    }

    @Test void deleteReturns204WithEmptyBody() throws Exception {
        mvc.perform(delete("/api/v1/cards/1")).andExpect(status().isNoContent()).andExpect(content().string(""));
        verify(service).deleteCard(1L);
    }

    @Test void expansionRoutesDelegateToService() throws Exception {
        when(service.getAllExpansions()).thenReturn(List.of());
        when(service.getExpansionByCode("A1")).thenReturn(CardExpansionResponse.builder().code("A1").build());
        when(service.getCardsByExpansionCode("A1")).thenReturn(List.of());
        mvc.perform(get("/api/v1/cards/expansions")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/cards/expansions/A1")).andExpect(status().isOk()).andExpect(jsonPath("$.data.code").value("A1"));
        mvc.perform(get("/api/v1/cards/expansions/A1/cards")).andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());
        verify(service).getAllExpansions();
        verify(service).getExpansionByCode("A1");
        verify(service).getCardsByExpansionCode("A1");
    }
}
