package com.crawldata.back_end.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a generic data response returned by the backend.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataResponse {
    /**
     * The status of the response.
     */
    String status;

    /**
     * The total number of pages.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer totalPage;

    /**
     * The current page number.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer currentPage;

    /**
     * The number of items per page.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer perPage;

    /**
     * The value used for search, if any.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String searchValue;

    /**
     * The data payload of the response.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Object data;

    /**
     * Message for error
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String message;

    /**
     * Sets the current page number, ensuring it does not exceed the total number of pages.
     *
     * @param currentPage The current page number to set.
     */
    public void setCurrentPage(Integer currentPage) {
        if (currentPage > totalPage) {
            this.currentPage = totalPage;
        } else {
            this.currentPage = currentPage;
        }
    }

    public DataResponse status(String status) {
        setStatus(status);
        return this;
    }

    public DataResponse totalPage(Integer totalPage) {
        setTotalPage(totalPage);
        return this;
    }
    public DataResponse currentPage(Integer currentPage) {
        setCurrentPage(currentPage);
        return this;
    }

    public DataResponse perPage(Integer perPage) {
        setPerPage(perPage);
        return this;
    }

    public DataResponse searchValue(String searchValue) {
        setSearchValue(searchValue);
        return this;
    }
    public DataResponse data(Object data) {
        setData(data);
        return this;
    }
    public DataResponse message(String message) {
        setMessage(message);
        return this;
    }
}
