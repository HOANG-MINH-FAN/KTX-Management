package com.dormitory.exchange;

import com.dormitory.entity.Admin;
import com.dormitory.entity.Student;
import com.dormitory.exchange.controller.ExchangeRequestController;
import com.dormitory.exchange.entity.ExchangeRequest;
import com.dormitory.exchange.service.ExchangeItemService;
import com.dormitory.exchange.service.ExchangeRequestService;
import com.dormitory.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRequestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ExchangeRequestService requestService;

    @Mock
    private ExchangeItemService itemService;

    @Mock
    private UserRepository userRepository;

    private Student mockStudent;
    private Authentication studentAuth;
    private Admin mockAdmin;
    private Authentication adminAuth;

    @BeforeEach
    void setUp() {
        ExchangeRequestController controller = new ExchangeRequestController(
                requestService, itemService, userRepository
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockStudent = new Student("sv001", "password", "SV2024001", "Nguyen Van A");
        mockAdmin = new Admin("admin", "password");
        try {
            Field idField = com.dormitory.entity.User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(mockStudent, 10L);
            idField.set(mockAdmin, 1L);

            Field roleField = com.dormitory.entity.User.class.getDeclaredField("role");
            roleField.setAccessible(true);
            roleField.set(mockStudent, com.dormitory.entity.enums.Role.STUDENT);
            roleField.set(mockAdmin, com.dormitory.entity.enums.Role.ADMIN);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        studentAuth = new UsernamePasswordAuthenticationToken(
                "sv001", "password", List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
        adminAuth = new UsernamePasswordAuthenticationToken(
                "admin", "password", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    @Test
    void testGetSentRequestsPageReturnsView() throws Exception {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(mockStudent));
        when(requestService.getSentRequestViews(10L, null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/exchange/requests/sent").principal(studentAuth))
                .andExpect(status().isOk())
                .andExpect(view().name("exchange/requests-sent"))
                .andExpect(model().attributeExists("requests"))
                .andExpect(model().attribute("totalCount", 0));
    }

    @Test
    void testGetReceivedRequestsPageReturnsView() throws Exception {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(mockStudent));
        when(requestService.getReceivedRequestViews(10L, null, null)).thenReturn(Collections.emptyList());
        when(itemService.getByOwnerId(10L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/exchange/requests/received").principal(studentAuth))
                .andExpect(status().isOk())
                .andExpect(view().name("exchange/requests-received"))
                .andExpect(model().attributeExists("requests"))
                .andExpect(model().attribute("totalCount", 0));
    }

    @Test
    void testGetHistoryPageReturnsView() throws Exception {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(mockStudent));
        when(requestService.getHistoryViews(eq(10L), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/exchange/history").principal(studentAuth))
                .andExpect(status().isOk())
                .andExpect(view().name("exchange/history"))
                .andExpect(model().attributeExists("historyList"))
                .andExpect(model().attribute("totalCount", 0));
    }

    @Test
    void testAdminCannotAccessStudentExchangeRequestActions() throws Exception {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(mockAdmin));

        mockMvc.perform(get("/exchange/requests/sent").principal(adminAuth))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/exchange"))
                .andExpect(flash().attributeExists("error"));

        mockMvc.perform(post("/exchange/5/request").principal(adminAuth))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/exchange/5"))
                .andExpect(flash().attributeExists("error"));

        verify(requestService, never()).createRequest(any(), any(), any(), any(), any());
    }

    @Test
    void testPostSendRequestSuccessRedirectsToSentRequests() throws Exception {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(mockStudent));
        when(requestService.createRequest(5L, 10L, "SELL", null, "Tôi muốn mua"))
                .thenReturn(new ExchangeRequest());

        mockMvc.perform(post("/exchange/5/request")
                        .principal(studentAuth)
                        .param("transactionType", "SELL")
                        .param("message", "Tôi muốn mua"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/exchange/requests/sent"))
                .andExpect(flash().attributeExists("success"));

        verify(requestService, times(1)).createRequest(5L, 10L, "SELL", null, "Tôi muốn mua");
    }

    @Test
    void testPostAcceptRequestCallsServiceAndRedirects() throws Exception {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(mockStudent));
        when(requestService.acceptRequest(99L, 10L)).thenReturn(new ExchangeRequest());

        mockMvc.perform(post("/exchange/requests/99/accept").principal(studentAuth))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/exchange/requests/received"))
                .andExpect(flash().attributeExists("success"));

        verify(requestService, times(1)).acceptRequest(99L, 10L);
    }

    @Test
    void testPostCancelRequestCallsServiceAndRedirects() throws Exception {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(mockStudent));
        when(requestService.cancelRequest(99L, 10L)).thenReturn(new ExchangeRequest());

        mockMvc.perform(post("/exchange/requests/99/cancel").principal(studentAuth))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/exchange/requests/sent"))
                .andExpect(flash().attributeExists("success"));

        verify(requestService, times(1)).cancelRequest(99L, 10L);
    }

    @Test
    void testPostConfirmCompleteCallsServiceAndRedirects() throws Exception {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(mockStudent));
        ExchangeRequest completed = new ExchangeRequest();
        completed.setStatus("COMPLETED");
        when(requestService.confirmComplete(99L, 10L)).thenReturn(completed);

        mockMvc.perform(post("/exchange/requests/99/complete")
                        .principal(studentAuth)
                        .param("returnUrl", "/exchange/history"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/exchange/history"))
                .andExpect(flash().attributeExists("success"));

        verify(requestService, times(1)).confirmComplete(99L, 10L);
    }
}

