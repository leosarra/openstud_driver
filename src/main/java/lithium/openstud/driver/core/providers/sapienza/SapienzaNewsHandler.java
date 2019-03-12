package lithium.openstud.driver.core.providers.sapienza;

import lithium.openstud.driver.core.Openstud;
import lithium.openstud.driver.core.OpenstudHelper;
import lithium.openstud.driver.core.internals.NewsHandler;
import lithium.openstud.driver.core.models.Event;
import lithium.openstud.driver.core.models.EventType;
import lithium.openstud.driver.core.models.News;
import lithium.openstud.driver.exceptions.OpenstudConnectionException;
import lithium.openstud.driver.exceptions.OpenstudInvalidResponseException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;
import org.threeten.bp.format.DateTimeParseException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public class SapienzaNewsHandler implements NewsHandler {
    private Openstud os;

    public SapienzaNewsHandler(Openstud os) {
        this.os = os;
    }

    @Override
    public List<News> getNews(String locale, boolean withDescription, Integer limit, Integer page, Integer maxPage, String query) throws OpenstudInvalidResponseException, OpenstudConnectionException {
        if (limit == null && page == null && maxPage == null)
            throw new IllegalStateException("limit, page and maxpage can't be all null");
        return _getNews(locale, withDescription, limit, page, maxPage, query);
    }

    private List<News> _getNews(String locale, boolean withDescription, Integer limit, Integer page, Integer maxPage, String query) throws OpenstudConnectionException, OpenstudInvalidResponseException {
        if (locale == null)
            locale = "en";
        Locale localeFormatter;
        if (locale.toLowerCase().equals("it")) localeFormatter = Locale.ITALIAN;
        else localeFormatter = Locale.ENGLISH;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy").withLocale(localeFormatter);
        try {
            List<News> ret = new LinkedList<>();
            int startPage = 0;
            int endPage = maxPage == null ? 1 : maxPage;
            if (page != null) {
                startPage = page;
                endPage = startPage + 1;
            }
            String website_url = "https://www.uniroma1.it";
            String page_key = "page";
            String query_key = "search_api_views_fulltext";
            boolean shouldStop = false;
            int iterations = 0;
            int miss = 0;
            for (int i = startPage; i < endPage && !shouldStop; i++) {
                Connection connection = Jsoup.connect(String.format("%s/%s/tutte-le-notizie", website_url, locale))
                        .data(page_key, i + "");
                if (query != null)
                    connection = connection.data(query_key, query);
                Document doc = connection.get();
                Elements boxes = doc.getElementsByClass("box-news");
                for (Element box : boxes) {
                    News news = new News();
                    news.setTitle(box.getElementsByTag("img").attr("title"));
                    // handle empty news
                    if (news.getTitle().isEmpty())
                        continue;
                    news.setLocale(locale);
                    news.setUrl(website_url + box.getElementsByTag("a").attr("href").trim());
                    news.setSmallImageUrl(box.getElementsByTag("img").attr("src"));
                    ret.add(news);
                    if (limit != null && ret.size() >= limit) {
                        shouldStop = true;
                        break;
                    }
                }
                if (boxes.isEmpty()) miss++;
                iterations++;
            }
            if (iterations == miss) {
                OpenstudInvalidResponseException invalidResponse = new OpenstudInvalidResponseException("invalid HTML").setHTMLType();
                os.log(Level.SEVERE, invalidResponse);
                throw invalidResponse;
            }
            LinkedList<News> ignored = new LinkedList<>();
            for (News news : ret) {
                if (!OpenstudHelper.isValidUrl(news.getUrl())) ignored.add(news);
                Document doc = Jsoup.connect(news.getUrl()).get();
                if (withDescription) {
                    Element start = doc.getElementsByAttributeValueEnding("class", "testosommario").first();
                    if (start != null)
                        news.setDescription(start.getElementsByClass("field-item even").first().text());
                }
                Element date = doc.getElementsByClass("date-display-single").first();
                if (date != null) {
                    try {
                        news.setDate(LocalDate.parse(date.text().substring(date.text().indexOf(",") + 1).trim(), formatter));
                    } catch (DateTimeParseException e) {
                        e.printStackTrace();
                    }
                }
                news.setImageUrl(doc.getElementsByClass("img-responsive").attr("src"));
            }
            ret.removeAll(ignored);
            return ret;

        } catch (IOException e) {
            OpenstudConnectionException connectionException = new OpenstudConnectionException(e);
            os.log(Level.SEVERE, connectionException);
            throw connectionException;
        }
    }

    @Override
    public List<Event> getNewsletterEvents() throws OpenstudInvalidResponseException, OpenstudConnectionException {
        return _getNewsletterEvents();
    }

    private List<Event> _getNewsletterEvents() throws OpenstudInvalidResponseException, OpenstudConnectionException {
        try {
            List<Event> ret = new LinkedList<>();

            String website_url = "https://www.uniroma1.it/it/newsletter";
            Document doc = Jsoup.connect(website_url).get();
            Elements events = doc.getElementsByClass("event");
            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                    .appendOptional(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))
                    .appendOptional(DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm"))
                    .appendOptional(DateTimeFormatter.ofPattern("d MMM yyyy HH:mm"))
                    .appendOptional(DateTimeFormatter.ofPattern("d MMMM yyyy HH:mm"))
                    .toFormatter(Locale.ENGLISH);
            int failed = 0;
            for (Element event : events) {
                Elements views = event.getElementsByClass("views-field");
                if (views.size() != 5) {
                    failed++;
                    continue;
                }
                Event ev = new Event(EventType.THEATRE);
                String date = views.remove(0).getElementsByTag("a").text().replace(",", "");
                String time = views.remove(0).getElementsByTag("a").text();
                try {
                    ev.setStart(LocalDateTime.parse(date + " " + time, formatter).atOffset(ZoneOffset.of("+1")).atZoneSameInstant(ZoneId.of("Europe/Rome")));
                } catch (DateTimeParseException e) {
                    failed++;
                    continue;
                }
                Elements title = views.remove(0).getElementsByTag("a");
                ev.setTitle(title.text());
                ev.setUrl(title.attr("href"));
                ev.setWhere(views.remove(0).getElementsByTag("a").text());
                ev.setRoom(views.remove(0).getElementsByTag("a").text());
                doc = Jsoup.connect(ev.getUrl()).get();
                Element image = doc.getElementsByClass("field-type-image").first();
                if (image != null) {
                    ev.setImageUrl(image.getElementsByTag("img").first().attr("src"));
                }
                Element description = doc.getElementsByClass("article-body").first();
                if (description != null) ev.setDescription(description.text());
                ret.add(ev);
            }
            if (failed == events.size()) {
                OpenstudInvalidResponseException invalidResponse = new OpenstudInvalidResponseException("invalid HTML").setHTMLType();
                os.log(Level.SEVERE, invalidResponse);
                throw invalidResponse;
            }
            return ret;

        } catch (IOException e) {
            OpenstudConnectionException connectionException = new OpenstudConnectionException(e);
            os.log(Level.SEVERE, connectionException);
            throw connectionException;
        }
    }

}
