package com.shengdijia.support.config;

import com.shengdijia.support.domain.Category;
import com.shengdijia.support.domain.EventType;
import com.shengdijia.support.domain.Priority;
import com.shengdijia.support.domain.Role;
import com.shengdijia.support.domain.Ticket;
import com.shengdijia.support.domain.TicketEvent;
import com.shengdijia.support.domain.TicketStatus;
import com.shengdijia.support.domain.User;
import com.shengdijia.support.repo.TicketEventRepository;
import com.shengdijia.support.repo.TicketRepository;
import com.shengdijia.support.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@ConditionalOnProperty(name = "app.seed", havingValue = "true", matchIfMissing = true)
public class SeedDataRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDataRunner.class);

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final TicketEventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;

    public SeedDataRunner(
            UserRepository userRepository,
            TicketRepository ticketRepository,
            TicketEventRepository eventRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }

        User admin = user(
                "admin", "Sdj@Admin2026", "张明远", "盛迪嘉支付", Role.INTERNAL,
                "13800001001", "mingyuan.zhang@shengdijia.com", null
        );
        User handler = user(
                "handler", "Sdj@Handler2026", "李承泽", "盛迪嘉支付", Role.INTERNAL,
                "13800001002", "chengze.li@shengdijia.com", null
        );
        User alpha = user(
                "xinghui", "Sdj@Partner2026", "王晓晨", "星辉科技有限公司", Role.PARTNER,
                "13912340001", "xiaochen.wang@xinghui.example", "MCH8001001"
        );
        User beta = user(
                "yuntu", "Sdj@Partner2026", "陈思远", "云途电子商务有限公司", Role.PARTNER,
                "13912340002", "siyuan.chen@yuntu.example", "MCH8001002"
        );

        LocalDateTime now = LocalDateTime.now();

        Ticket t1 = ticket(
                "SDJ-" + now.toLocalDate().toString().replace("-", "") + "-0001",
                "生产环境异步通知签名校验失败",
                """
                我司已按《盛迪嘉支付开放平台接入文档》完成 RSA2 验签，但生产环境 notify_url 回调偶发验签失败。
                失败报文中 sign 字段可解出，charset 为 UTF-8。请协助核对商户公钥是否与控制台备案一致，并确认是否存在双重 URLEncode。
                影响：约 3% 的支付成功通知无法入账，需人工补单。
                """,
                alpha, handler, TicketStatus.PENDING, Priority.URGENT, Category.INTEGRATION,
                "13912340001", "xiaochen.wang@xinghui.example", "MCH8001001",
                now.minusHours(6), now.minusHours(6), null
        );
        event(t1, alpha, EventType.CREATED, "发起人 王晓晨（星辉科技有限公司）提交工单。", now.minusHours(6));
        event(t1, alpha, EventType.REMARK, "附件：失败 notify 原文已通过加密通道发送至对接群，工单侧仅保留摘要。", now.minusHours(5));

        Ticket t2 = ticket(
                "SDJ-" + now.toLocalDate().toString().replace("-", "") + "-0002",
                "代付接口超时率升高",
                """
                今日 10:12 起，/v1/payout/transfer 接口 P99 从 420ms 升至 3.8s，部分请求直接 504。
                商户单号前缀 YT20260907。请协助排查渠道侧拥堵或我方出口 IP 是否被限流。
                """,
                beta, handler, TicketStatus.IN_PROGRESS, Priority.HIGH, Category.TRANSACTION,
                "13912340002", "siyuan.chen@yuntu.example", "MCH8001002",
                now.minusHours(4), now.minusHours(1), null
        );
        event(t2, beta, EventType.CREATED, "发起人 陈思远（云途电子商务有限公司）提交工单。", now.minusHours(4));
        event(t2, admin, EventType.HANDLER_CHANGE, "处理人由「未指派」变更为「李承泽」。", now.minusHours(3).minusMinutes(40));
        event(t2, admin, EventType.STATUS_CHANGE, "指派处理人后，状态由「待处理」变更为「处理中」。", now.minusHours(3).minusMinutes(40));
        event(t2, handler, EventType.REMARK, "已联系渠道值班：代付通道 02 正在扩容，预计 40 分钟内恢复。建议临时切通道 01。", now.minusHours(1));

        Ticket t3 = ticket(
                "SDJ-" + now.minusDays(2).toLocalDate().toString().replace("-", "") + "-0001",
                "结算周期与合同不符",
                """
                合同约定 T+1 结算至对公户，近两周实际为 T+2 到账。请核对本商户结算产品配置及节假日顺延规则。
                """,
                alpha, admin, TicketStatus.RESOLVED, Priority.MEDIUM, Category.SETTLEMENT,
                "13912340001", "xiaochen.wang@xinghui.example", "MCH8001001",
                now.minusDays(2).minusHours(3), now.minusHours(20), now.minusHours(20)
        );
        event(t3, alpha, EventType.CREATED, "发起人 王晓晨（星辉科技有限公司）提交工单。", now.minusDays(2).minusHours(3));
        event(t3, admin, EventType.STATUS_CHANGE, "状态由「待处理」变更为「处理中」。", now.minusDays(2));
        event(t3, admin, EventType.REMARK, "已核对：国庆调休导致结算日历偏移，已将商户结算日历改回工作日 T+1，补结算批次今晚 22:00 执行。", now.minusHours(21));
        event(t3, admin, EventType.STATUS_CHANGE, "状态由「处理中」变更为「已解决」。", now.minusHours(20));

        Ticket t4 = ticket(
                "SDJ-" + now.minusDays(5).toLocalDate().toString().replace("-", "") + "-0003",
                "测试商户号无法开通分账",
                """
                沙箱商户 MCH8001002 在控制台开通分账时提示「产品未开通」。销售确认合同已含分账条款。
                """,
                beta, handler, TicketStatus.CLOSED, Priority.LOW, Category.INTEGRATION,
                "13912340002", "siyuan.chen@yuntu.example", "MCH8001002",
                now.minusDays(5), now.minusDays(4), now.minusDays(4).plusHours(2)
        );
        event(t4, beta, EventType.CREATED, "发起人 陈思远（云途电子商务有限公司）提交工单。", now.minusDays(5));
        event(t4, handler, EventType.REMARK, "沙箱环境需单独申请分账白名单，已开通并完成验证交易。", now.minusDays(4).plusHours(1));
        event(t4, handler, EventType.STATUS_CHANGE, "状态由「处理中」变更为「已解决」。", now.minusDays(4).plusHours(1));
        event(t4, beta, EventType.STATUS_CHANGE, "状态由「已解决」变更为「已关闭」。", now.minusDays(4).plusHours(2));

        Ticket t5 = ticket(
                "SDJ-" + now.toLocalDate().toString().replace("-", "") + "-0003",
                "退款回调重复推送",
                """
                同一退款单号在 8 分钟内收到 6 次 refund.notify，我方已按幂等处理，但希望确认是否为渠道重试策略变更。
                """,
                alpha, handler, TicketStatus.IN_PROGRESS, Priority.HIGH, Category.TRANSACTION,
                "13912340001", "xiaochen.wang@xinghui.example", "MCH8001001",
                now.minusHours(2), now.minusMinutes(35), null
        );
        event(t5, alpha, EventType.CREATED, "发起人 王晓晨（星辉科技有限公司）提交工单。", now.minusHours(2));
        event(t5, handler, EventType.HANDLER_CHANGE, "处理人由「未指派」变更为「李承泽」。", now.minusHours(1).minusMinutes(20));
        event(t5, handler, EventType.REMARK, "初步判断为商户 notify 接口 5xx 导致的标准重试，正在核对响应码。", now.minusMinutes(35));

        Ticket t6 = ticket(
                "SDJ-" + now.minusDays(1).toLocalDate().toString().replace("-", "") + "-0002",
                "对账单缺少退款明细",
                """
                昨日 download/bill 文件中 trade 正常，refund 区段为空，但商户后台可见 17 笔退款成功。请协助核对账单生成任务。
                """,
                beta, null, TicketStatus.PENDING, Priority.MEDIUM, Category.SETTLEMENT,
                "13912340002", "siyuan.chen@yuntu.example", "MCH8001002",
                now.minusDays(1).plusHours(8), now.minusDays(1).plusHours(8), null
        );
        event(t6, beta, EventType.CREATED, "发起人 陈思远（云途电子商务有限公司）提交工单。", now.minusDays(1).plusHours(8));

        log.info("已写入演示账号与工单。管理员 admin / Sdj@Admin2026");
    }

    private User user(
            String username,
            String rawPassword,
            String displayName,
            String company,
            Role role,
            String phone,
            String email,
            String merchantId
    ) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setDisplayName(displayName);
        user.setCompany(company);
        user.setRole(role);
        user.setPhone(phone);
        user.setEmail(email);
        user.setMerchantId(merchantId);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private Ticket ticket(
            String ticketNo,
            String title,
            String description,
            User initiator,
            User handler,
            TicketStatus status,
            Priority priority,
            Category category,
            String phone,
            String email,
            String merchantId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime resolvedAt
    ) {
        Ticket ticket = new Ticket();
        ticket.setTicketNo(ticketNo);
        ticket.setTitle(title);
        ticket.setDescription(description.trim());
        ticket.setInitiator(initiator);
        ticket.setHandler(handler);
        ticket.setStatus(status);
        ticket.setPriority(priority);
        ticket.setCategory(category);
        ticket.setContactPhone(phone);
        ticket.setContactEmail(email);
        ticket.setMerchantId(merchantId);
        ticket.setCreatedAt(createdAt);
        ticket.setUpdatedAt(updatedAt);
        ticket.setResolvedAt(resolvedAt);
        return ticketRepository.save(ticket);
    }

    private void event(Ticket ticket, User author, EventType type, String content, LocalDateTime at) {
        TicketEvent event = new TicketEvent();
        event.setTicket(ticket);
        event.setAuthor(author);
        event.setEventType(type);
        event.setContent(content);
        event.setCreatedAt(at);
        eventRepository.save(event);
    }
}
