package com.crawldata.back_end.response;

import org.springframework.hateoas.RepresentationModel;

public class RootResponse extends RepresentationModel<RootResponse> {
    private String status;
    public RootResponse(String status)
    {
        this.status = status;
    }
    public String getStatus()
    {
        return this.status;
    }
}
