package lithium.openstud.driver.core.internals;

import lithium.openstud.driver.core.models.Event;
import lithium.openstud.driver.core.models.News;
import lithium.openstud.driver.exceptions.OpenstudConnectionException;
import lithium.openstud.driver.exceptions.OpenstudInvalidResponseException;

import java.util.List;

public interface NewsHandler
{
    List<News> getNews(String locale, boolean withDescription, Integer limit, Integer page, Integer maxPage,
                       String query) throws OpenstudInvalidResponseException, OpenstudConnectionException;

    List<Event> getNewsletterEvents() throws OpenstudInvalidResponseException, OpenstudConnectionException;
}

