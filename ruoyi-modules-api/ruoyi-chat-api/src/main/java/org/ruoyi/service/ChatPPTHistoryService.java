package org.ruoyi.service;

import org.ruoyi.core.page.PageQuery;
import org.ruoyi.core.page.TableDataInfo;
import org.ruoyi.domain.ChatPPTHistory;
import org.ruoyi.domain.vo.ChatPPTHistoryVo;


import java.util.Collection;
import java.util.List;

public interface ChatPPTHistoryService {
    /**
     * 查询应用管理
     */
    ChatPPTHistoryVo queryById(Long id);

    /**
     * 查询应用管理列表
     */
    TableDataInfo<ChatPPTHistoryVo> queryPageList(ChatPPTHistory bo, PageQuery pageQuery);

    /**
     * 查询应用管理列表
     */
    List<ChatPPTHistoryVo> queryList(ChatPPTHistory bo);

    /**
     * 新增应用管理
     */
    Boolean insertByBo(ChatPPTHistory bo);

    /**
     * 修改应用管理
     */
    Boolean updateByBo(ChatPPTHistory bo);

    /**
     * 校验并批量删除应用管理信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
