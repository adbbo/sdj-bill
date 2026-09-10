package com.shengdijia.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shengdijia.support.domain.Category;
import com.shengdijia.support.domain.Priority;
import com.shengdijia.support.web.dto.LoginRequest;
import com.shengdijia.support.web.dto.RemarkRequest;
import com.shengdijia.support.web.dto.TicketCreateRequest;
import com.shengdijia.support.web.dto.TicketUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SupportFlowTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void partnerSeesOnlyOwnCompanyAndInternalCanHandle() throws Exception {
        MockHttpSession partnerSession = login("xinghui", "Sdj@Partner2026");
        MockHttpSession otherSession = login("yuntu", "Sdj@Partner2026");
        MockHttpSession adminSession = login("admin", "Sdj@Admin2026");

        mockMvc.perform(get("/api/dashboard/summary").session(partnerSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(greaterThanOrEqualTo(1)));

        MvcResult created = mockMvc.perform(post("/api/tickets")
                        .session(partnerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TicketCreateRequest(
                                "沙箱支付结果查询偶发空单",
                                "query/order 在沙箱对部分商户订单返回空对象，请协助核对。",
                                Priority.HIGH,
                                Category.TRANSACTION,
                                "13912340001",
                                "xiaochen.wang@xinghui.example",
                                "MCH8001001"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketNo").exists())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        long id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/tickets/" + id).session(otherSession))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/tickets/" + id).session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("沙箱支付结果查询偶发空单"));

        mockMvc.perform(patch("/api/tickets/" + id)
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TicketUpdateRequest(
                                null, 1L, "已接手，正在核对沙箱网关日志。"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.handler.displayName").exists())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(post("/api/tickets/" + id + "/remarks")
                        .session(partnerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RemarkRequest("补充：发生时间集中在整点前后。"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timeline.length()").value(greaterThanOrEqualTo(3)));
    }

    @Test
    void loginRejectsBadPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin", "wrong"))))
                .andExpect(status().isUnauthorized());
    }

    private MockHttpSession login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(username, password))))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession();
    }
}
