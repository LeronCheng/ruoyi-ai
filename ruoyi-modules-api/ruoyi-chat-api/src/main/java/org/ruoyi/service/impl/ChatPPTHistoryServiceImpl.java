package org.ruoyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.ruoyi.common.core.utils.MapstructUtils;
import org.ruoyi.common.core.utils.ObjectUtils;
import org.ruoyi.common.core.utils.StringUtils;
import org.ruoyi.core.page.PageQuery;
import org.ruoyi.core.page.TableDataInfo;
import org.ruoyi.domain.ChatGpts;
import org.ruoyi.domain.ChatPPTHistory;
import org.ruoyi.domain.bo.ChatGptsBo;
import org.ruoyi.domain.vo.ChatGptsVo;
import org.ruoyi.domain.vo.ChatPPTHistoryVo;
import org.ruoyi.mapper.ChatPPTHistoryMapper;
import org.ruoyi.service.ChatPPTHistoryService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class ChatPPTHistoryServiceImpl implements ChatPPTHistoryService {

    private final ChatPPTHistoryMapper baseMapper;


    /**
     * 查询应用管理
     */
    @Override
    public ChatPPTHistoryVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询应用管理列表
     */
    @Override
    public TableDataInfo<ChatPPTHistoryVo> queryPageList(ChatPPTHistory bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ChatPPTHistory> lqw = buildQueryWrapper(bo);
        Page<ChatPPTHistoryVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询应用管理列表
     */
    @Override
    public List<ChatPPTHistoryVo> queryList(ChatPPTHistory bo) {
        LambdaQueryWrapper<ChatPPTHistory> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ChatPPTHistory> buildQueryWrapper(ChatPPTHistory bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ChatPPTHistory> lqw = Wrappers.lambdaQuery();
        lqw.eq(ObjectUtils.isNotEmpty(bo.getUserId()), ChatPPTHistory::getUserId, bo.getUserId());
        lqw.like(StringUtils.isNotBlank(bo.getPrompt()), ChatPPTHistory::getPrompt, bo.getPrompt());
        lqw.eq(StringUtils.isNotBlank(bo.getPptValue()), ChatPPTHistory::getPptValue, bo.getPptValue());
        lqw.eq(ObjectUtils.isNotEmpty(bo.getCreateTime()), ChatPPTHistory::getCreateTime, bo.getCreateTime());
        lqw.eq(ObjectUtils.isNotEmpty(bo.getState()), ChatPPTHistory::getState, bo.getState());
        return lqw;
    }

    /**
     * 新增应用管理
     */
    @Override
    public Boolean insertByBo(ChatPPTHistory bo) {
//        ChatPPTHistory add = MapstructUtils.convert(bo, ChatPPTHistory.class);
        validEntityBeforeSave(bo);
        boolean flag = baseMapper.insert(bo) > 0;
        if (flag) {
            bo.setId(bo.getId());
        }
        return flag;
    }

    /**
     * 修改应用管理
     */
    @Override
    public Boolean updateByBo(ChatPPTHistory bo) {
        ChatPPTHistory update = MapstructUtils.convert(bo, ChatPPTHistory.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ChatPPTHistory entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除应用管理
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }
}
