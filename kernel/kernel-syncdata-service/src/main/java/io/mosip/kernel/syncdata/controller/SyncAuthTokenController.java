package io.mosip.kernel.syncdata.controller;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;

import io.mosip.kernel.syncdata.service.impl.SyncAuthTokenServiceImpl;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


/**
 * @since 1.1.3
 */
@RestController
public class SyncAuthTokenController {

    @Autowired
    private SyncAuthTokenServiceImpl syncAuthTokenService;

    @ApiOperation(value = "API to get auth token details encrypted based on machine key")
    @PostMapping(value = "/authenticate/useridpwd")
    public ResponseWrapper<String> getTokenWithUserIdPwd(@RequestBody @Valid RequestWrapper<String> requestWrapper) {
        ResponseWrapper<String> responseWrapper = new ResponseWrapper<String>();
        responseWrapper.setResponse(syncAuthTokenService.getAuthToken(requestWrapper.getRequest()));
        return responseWrapper;
    }

}
