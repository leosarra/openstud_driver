package lithium.openstud.driver.core;

import lithium.openstud.driver.exceptions.OpenstudConnectionException;
import lithium.openstud.driver.exceptions.OpenstudInvalidResponseException;

import java.util.List;

public interface NewsHandler
{
    public List<News> getNews(String locale, boolean withDescription, Integer limit, Integer page, Integer maxPage,
                              String query) throws OpenstudInvalidResponseException, OpenstudConnectionException;

    public List<Event> getNewsletterEvents() throws OpenstudInvalidResponseException, OpenstudConnectionException;
}

