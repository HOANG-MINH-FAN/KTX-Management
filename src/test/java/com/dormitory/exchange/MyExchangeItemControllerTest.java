package com.dormitory.exchange;

import com.dormitory.entity.Student;
import com.dormitory.exchange.controller.MyExchangeItemController;
import com.dormitory.exchange.dto.ExchangeItemForm;
import com.dormitory.exchange.entity.ExchangeItem;
import com.dormitory.exchange.service.ExchangeItemService;
import com.dormitory.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MyExchangeItemControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ExchangeItemService service;

    @Mock
    private UserRepository userRepository;

    private Student mockStudent;
    private Authentication mockAuth;

    @BeforeEach
    void setUp() {
        MyExchangeItemController controller = new MyExchangeItemController(service, userRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockStudent = new Student("sv001", "password", "SV2024001", "Nguyen Van A");
        // set ID via reflection or Mockito since User.id is private
        try {
            java.lang.reflect.Field idField = com.dormitory.entity.User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(mockStudent, 10L);

            java.lang.reflect.Field roleField = com.dormitory.entity.User.class.getDeclaredField("role");
            roleField.setAccessible(true);
            roleField.set(mockStudent, com.dormitory.entity.enums.Role.STUDENT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        mockAuth = new UsernamePasswordAuthenticationToken(
                "sv001",
                "password",
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT"))
        );
    }

    @Test
    void testGetMyItemsPageReturnsView() throws Exception {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(mockStudent));
        when(service.getByOwnerId(10L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/exchange/my-items").principal(mockAuth))
                .andExpect(status().isOk())
                .andExpect(view().name("exchange/my-items"))
                .andExpect(model().attributeExists("itemViews"))
                .andExpect(model().attribute("totalCount", 0));
    }

    @Test
    void testPostCreateValidSellItemRedirects() throws Exception {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(mockStudent));

        mockMvc.perform(post("/exchange/my-items/new")
                        .principal(mockAuth)
                        .param("title", "Bàn học gập chân")
                        .param("category", "Đồ gia dụng - Phòng ở")
                        .param("exchangeType", "SELL")
                        .param("itemCondition", "NEW")
                        .param("price", "120000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/exchange/my-items"))
                .andExpect(flash().attributeExists("success"));

        verify(service, times(1)).createItem(any(ExchangeItemForm.class), eq(10L), any());
    }

    @Test
    void testPostCreateSellItemWithoutPriceFailsValidation() throws Exception {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(mockStudent));

        mockMvc.perform(post("/exchange/my-items/new")
                        .principal(mockAuth)
                        .param("title", "Bàn học gập chân")
                        .param("category", "Đồ gia dụng - Phòng ở")
                        .param("exchangeType", "SELL")
                        .param("itemCondition", "NEW")
                        // price null
                )
                .andExpect(status().isOk())
                .andExpect(view().name("exchange/form"))
                .andExpect(model().attributeHasFieldErrors("item", "price"));

        verify(service, never()).createItem(any(), any(), any());
    }

    @Test
    void testPostCreateSellItemWithZeroPriceFailsValidation() throws Exception {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(mockStudent));

        mockMvc.perform(post("/exchange/my-items/new")
                        .principal(mockAuth)
                        .param("title", "Bàn học gập chân")
                        .param("category", "Đồ gia dụng - Phòng ở")
                        .param("exchangeType", "SELL")
                        .param("itemCondition", "NEW")
                        .param("price", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("exchange/form"))
                .andExpect(model().attributeHasFieldErrors("item", "price"));

        verify(service, never()).createItem(any(), any(), any());
    }

    @Test
    void testPostCreateGiveawayItemWithPriceFailsValidation() throws Exception {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(mockStudent));

        mockMvc.perform(post("/exchange/my-items/new")
                        .principal(mockAuth)
                        .param("title", "Sách giáo trình cũ")
                        .param("category", "Đồ dùng học tập - Sách")
                        .param("exchangeType", "GIVEAWAY")
                        .param("itemCondition", "USED")
                        .param("price", "50000"))
                .andExpect(status().isOk())
                .andExpect(view().name("exchange/form"))
                .andExpect(model().attributeHasFieldErrors("item", "price"));

        verify(service, never()).createItem(any(), any(), any());
    }

    @Test
    void testPostCreateExchangeItemWithoutDesiredItemFailsValidation() throws Exception {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(mockStudent));

        mockMvc.perform(post("/exchange/my-items/new")
                        .principal(mockAuth)
                        .param("title", "Ấm siêu tốc")
                        .param("category", "Đồ gia dụng - Phòng ở")
                        .param("exchangeType", "EXCHANGE")
                        .param("itemCondition", "USED")
                        // desiredItem is missing
                )
                .andExpect(status().isOk())
                .andExpect(view().name("exchange/form"))
                .andExpect(model().attributeHasFieldErrors("item", "desiredItem"));

        verify(service, never()).createItem(any(), any(), any());
    }

    @Test
    void testPostToggleHideCallsServiceAndRedirects() throws Exception {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(mockStudent));
        when(service.toggleHide(5L, 10L)).thenReturn("CLOSED");

        mockMvc.perform(post("/exchange/my-items/5/toggle-hide").principal(mockAuth))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/exchange/my-items"))
                .andExpect(flash().attributeExists("success"));

        verify(service, times(1)).toggleHide(5L, 10L);
    }

    @Test
    void testPostDeleteCallsServiceAndRedirects() throws Exception {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(mockStudent));
        doNothing().when(service).deleteItem(5L, 10L);

        mockMvc.perform(post("/exchange/my-items/5/delete").principal(mockAuth))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/exchange/my-items"))
                .andExpect(flash().attributeExists("success"));

        verify(service, times(1)).deleteItem(5L, 10L);
    }
}
