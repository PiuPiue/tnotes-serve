package com.hao.tnotes.common.mq;

import com.hao.tnotes.cloud.dao.DirectoryDao;
import com.hao.tnotes.cloud.dao.UserDirectoryDao;
import com.hao.tnotes.common.bean.domain.Directory;
import com.hao.tnotes.common.bean.domain.Essay;
import com.hao.tnotes.common.bean.domain.Notebook;
import com.hao.tnotes.common.bean.domain.UserDirectory;
import com.hao.tnotes.common.bean.mq.CodeMessage;
import com.hao.tnotes.common.util.common.EmailUtil;
import com.hao.tnotes.note.dao.EssayDao;
import com.hao.tnotes.note.dao.NoteBookDao;
import com.hao.tnotes.note.dao.NoteDao;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MqListener {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private EmailUtil emailUtil;
    @Autowired
    private NoteBookDao noteBookDao;
    @Autowired
    private EssayDao essayDao;
    @Autowired
    private DirectoryDao directoryDao;
    @Autowired
    private UserDirectoryDao userDirectoryDao;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQRecord.AUTH_CODE_QUEUE),
            exchange = @Exchange(name = MQRecord.EXCHANGE_NAME, type = ExchangeTypes.DIRECT)
            ,key = MQRecord.AUTH_CODE_ROUTING_KEY
    ))
    public void sendCode(CodeMessage codeMessage){
        emailUtil.sendEmail(codeMessage.getEmail(), "验证码", codeMessage.getCode());
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQRecord.INIT_USER_NOTE_QUEUE),
            exchange = @Exchange(name = MQRecord.EXCHANGE_NAME, type = ExchangeTypes.DIRECT)
            ,key = MQRecord.INIT_USER_NOTE_ROUTING_KEY
    ))
    public void initUserNoteBook(Long id){
        //首先给用户创建一个默认笔记本
        Notebook notebook = new Notebook();
        notebook.setUser(id);
        notebook.setDescription("默认笔记本");
        notebook.setTitle("我的笔记本");
        notebook.setCover("https://haowallpaper.com/link/common/file/getCroppingImg/de48bd7b7e12ed6567bc84c24b193677");
        notebook.setStatus(0);
        notebook.setVisible(0);
        noteBookDao.insert(notebook);
        //再给用户插入一条随笔
        Essay essay = new Essay();
        essay.setUserId(id);
        essay.setTitle("我的随笔");
        essay.setContent("我的随笔");
        essay.setVisible(0);
        essayDao.insert(essay);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQRecord.INIT_USER_CLOUD_QUEUE),
            exchange = @Exchange(name = MQRecord.EXCHANGE_NAME, type = ExchangeTypes.DIRECT)
            ,key = MQRecord.INIT_USER_CLOUD_ROUTING_KEY
    ))
    public void initUserCloud(Long id){
        Directory directory = new Directory();
        directory.setName("/");
        directory.setType(1);
        directory.setUserId(id);
        directory.setSize(0L);
        directoryDao.insert(directory);
        UserDirectory userDirectory = new UserDirectory();
        userDirectory.setUserId(id);
        userDirectory.setDirectoryId(directory.getId());
        //10个G
        userDirectory.setAllSize(1024*1024*1024*10L);
        userDirectoryDao.insert(userDirectory);
    }

}
