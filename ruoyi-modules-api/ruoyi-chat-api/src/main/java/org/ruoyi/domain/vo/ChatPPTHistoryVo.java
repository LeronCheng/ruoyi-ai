package org.ruoyi.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableId;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.ruoyi.domain.ChatGpts;
import org.ruoyi.domain.ChatPPTHistory;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = ChatPPTHistory.class)
public class ChatPPTHistoryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ExcelProperty(value = "id")
    private Long id;

    /**
     * 用户id
     */
    @ExcelProperty(value = "用户id")
    private Long userId;

    /**
     * 问题
     */
    @ExcelProperty(value = "问题")
    private String prompt;

    /**
     * 答案
     */
    @ExcelProperty(value = "答案")
    private String pptValue;


    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date  createTime;
    /**
     * 状态
     */
    @ExcelProperty(value = "状态")
    private Integer state;
}
