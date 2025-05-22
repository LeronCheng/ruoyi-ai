package org.ruoyi.chat.controller.chat;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.ruoyi.chat.service.chat.IChatCostService;
import org.ruoyi.chat.util.SvgToppt;
import org.ruoyi.common.core.domain.R;
import org.ruoyi.common.core.validate.AddGroup;
import org.ruoyi.common.core.validate.EditGroup;
import org.ruoyi.common.excel.utils.ExcelUtil;
import org.ruoyi.common.idempotent.annotation.RepeatSubmit;
import org.ruoyi.common.log.annotation.Log;
import org.ruoyi.common.log.enums.BusinessType;
import org.ruoyi.common.web.core.BaseController;
import org.ruoyi.core.page.PageQuery;
import org.ruoyi.core.page.TableDataInfo;
import org.ruoyi.domain.ChatPPTHistory;
import org.ruoyi.domain.bo.ChatGptsBo;
import org.ruoyi.domain.vo.ChatGptsVo;
import org.ruoyi.domain.vo.ChatPPTHistoryVo;
import org.ruoyi.service.ChatPPTHistoryService;
import org.ruoyi.service.IChatGptsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 应用管理
 *
 * @author ageerle
 * @date 2025-04-08
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/pptHistory")
public class ChatPPTHistoryController extends BaseController {

    private final ChatPPTHistoryService chatPPTHistoryService;

    private final IChatCostService chatCostService;

    /**
     * 查询应用管理列表
     */
    @GetMapping("/list")
    public TableDataInfo<ChatPPTHistoryVo> list(ChatPPTHistory bo, PageQuery pageQuery) {
        bo.setUserId(chatCostService.getUserId());
        //设置只允许查询20条
        pageQuery.setPageNum(1);
        pageQuery.setPageSize(20);
        return chatPPTHistoryService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出应用管理列表
     */
//    @SaCheckPermission("system:pptHistory:export")
    @Log(title = "应用管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ChatPPTHistory bo, HttpServletResponse response) {
        List<ChatPPTHistoryVo> list = chatPPTHistoryService.queryList(bo);
        ExcelUtil.exportExcel(list, "应用管理", ChatPPTHistoryVo.class, response);
    }

    /**
     * 获取应用管理详细信息
     *
     * @param id 主键
     */
//    @SaCheckPermission("system:pptHistory:query")
    @GetMapping("/{id}")
    public R<ChatPPTHistoryVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(chatPPTHistoryService.queryById(id));
    }

    /**
     * 新增应用管理
     */
//    @SaCheckPermission("system:pptHistory:add")
    @Log(title = "应用管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ChatPPTHistory bo) {
        return toAjax(chatPPTHistoryService.insertByBo(bo));
    }

    /**
     * 修改应用管理
     */
//    @SaCheckPermission("system:pptHistory:edit")
    @Log(title = "应用管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ChatPPTHistory bo) {
        return toAjax(chatPPTHistoryService.updateByBo(bo));
    }

    /**
     * 删除应用管理
     *
     * @param ids 主键串
     */
//    @SaCheckPermission("system:pptHistory:remove")
    @Log(title = "应用管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(chatPPTHistoryService.deleteWithValidByIds(List.of(ids), true));
    }

    /**
     * 将SVG转换为PPT
     */
    @PostMapping("/convertSvgToPpt")
    public void convertSvgToPpt(@RequestBody String svgContent, HttpServletResponse response) {
        try {
            XMLSlideShow ppt = SvgToppt.convertToPptBySvgString(svgContent);
            
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
            response.setHeader("Content-Disposition", "attachment; filename=presentation.pptx");
            
            // 将PPT写入响应流
            ppt.write(response.getOutputStream());
        } catch (Exception e) {
            throw new RuntimeException("转换PPT失败", e);
        }
    }
}
