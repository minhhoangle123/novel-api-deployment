package com.crawldata.back_end.utils;


import com.crawldata.back_end.response.DataResponse;

public class DataResponseUtils {
    private static String errorStatus = "error";
    public static DataResponse getErrorDataResponse(String message)
    {
        DataResponse dataResponse = new DataResponse();
        dataResponse.setStatus(errorStatus);
        dataResponse.setMessage(message);
        return dataResponse;
    }
}
