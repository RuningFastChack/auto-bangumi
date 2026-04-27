package auto.bangumi.rss.controller.v1;

import auto.bangumi.common.enums.CommonResponseEnum;
import auto.bangumi.common.enums.RssTypeEnum;
import auto.bangumi.common.model.dto.ApiResult;
import auto.bangumi.rss.factory.handler.RssHandler;
import auto.bangumi.rss.model.AnalysisResult;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping("v1/analysis")
public class AnalysisController {

    /**
     * 解析mikan的链接
     *
     * @param rss     mikan的链接
     */
    @GetMapping("mikan")
    public ApiResult<AnalysisResult> analysisMikan(@NotBlank(message = "不能为空") String rss) {
        AnalysisResult mikan = RssHandler.analysisRss(RssTypeEnum.Mikan, rss);
        if (StringUtils.isNotBlank(mikan.getSubGroupId())) {
            return ApiResult.success(mikan);
        }
        ApiResult<AnalysisResult> error = ApiResult.error(CommonResponseEnum.VALID_ERROR);
        error.setMessage("解析失败");
        return error;
    }
}
