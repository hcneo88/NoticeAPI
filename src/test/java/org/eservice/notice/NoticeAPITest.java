package org.eservice.notice;

import org.eservice.notice.component.NoticeAPI;
import org.eservice.notice.constants.NoticeNumEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class NoticeAPITest {

    @Autowired
    private NoticeAPI noticeAPI;

    @Test
    public void createNoticeTest() throws Exception {
        noticeAPI.createNotice(NoticeNumEnum.UC0101N001, null);
    }

}

